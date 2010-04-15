/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright (C) 2004, 2005, 2006, 2010  Joseph Walton
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.desktop;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.kafsemo.mivvi.app.TokenSetFile;
import org.kafsemo.mivvi.gui.SeriesTreeFrame;
import org.kafsemo.mivvi.rss.AvailableDownloadGui;
import org.kafsemo.mivvi.tv.UpcomingBroadcastGui;
import org.openrdf.repository.RepositoryException;

public class GuiState
{
    final AppState appState;
    
    GuiState(AppState s)
    {
        this.appState = s;
    }

    SeriesTreeFrame seriesTreeFrame;
    boolean seriesTreeFrameShown;

    SeriesTreeFrame getSeriesTreeFrame() throws RepositoryException
    {
        if (seriesTreeFrame == null) {
            seriesTreeFrame = new SeriesTreeFrame(appState);
            seriesTreeFrameShown = loadBounds(seriesTreeFrame, false);
            
            TokenSetFile tsf = appState.getConfig().getTokenSetFile("gui-expanded-nodes");
            try {
                if (tsf.load())
                    seriesTreeFrame.expandUris(tsf.getTokens());
    
                int verticalScroll = windowNode(((ManagedJFrame)seriesTreeFrame).id).getInt("vertical-scroll", 0);
                seriesTreeFrame.setScrollportPosition(verticalScroll);
            } catch (IOException ioe) {
                // Ephemeral GUI state - losing it isn't a serious problem
                ioe.printStackTrace();
            }
        }
        return seriesTreeFrame;
    }
    
    UpcomingBroadcastGui ubg;
    boolean ubgShown;

    UpcomingBroadcastGui getUpcomingBroadcastFrame()
    {
        if (ubg == null) {
            ubg = new UpcomingBroadcastGui(appState);
            ubgShown = loadBounds(ubg, true);
        }
        return ubg;
    }
    
    AvailableDownloadGui adg;
    boolean adgShown;
    
    AvailableDownloadGui getAvailableDownloadFrame()
    {
        if (adg == null) {
            adg = new AvailableDownloadGui(appState);
            adgShown = loadBounds(adg, false);
        }
        return adg;
    }
    
    void dispose() throws InterruptedException, InvocationTargetException
    {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run()
            {
                if (adg != null) {
                    adg.disposeAll();
                    adg = null;
                }
                
                if (ubg != null) {
                    ubg.disposeAll();
                    ubg = null;
                }
                
                if (seriesTreeFrame != null) {
                    seriesTreeFrame.disposeAll();
                    seriesTreeFrame = null;
                }
            }
        });
    }
    
    void save(MainWindow mw, MainWindow.MDIGlue[] mdig)
    {
        for (int i = 0 ; i < mdig.length ; i++) {
            saveBounds(mdig[i].frame, mdig[i].initiallyShown);
        }
        
        saveBounds(mw, true);
        
        if (seriesTreeFrame != null) {
            Set<String> enu = seriesTreeFrame.getExpandedNodeUris();
            
            TokenSetFile tsf = appState.getConfig().getTokenSetFile("gui-expanded-nodes");
            tsf.replace(enu);
            try {
                tsf.save();
                
                Point scrollPoint = seriesTreeFrame.getScrollportPosition();
                
                // XXX Why is this cast necessary?
                windowNode(((ManagedJFrame)seriesTreeFrame).id).putInt("vertical-scroll", scrollPoint.y);
            } catch (IOException ioe) {
                // Ephemeral GUI state - losing it isn't a serious problem
                ioe.printStackTrace();
            }
        }
    }

    /**
     * If a window is wholly offscreen, move it to the top left corner of the screen.
     * 
     * @param jf
     */
    static void ensureOnScreen(JFrame jf)
    {
        Rectangle onscreen = jf.getGraphicsConfiguration().getBounds().intersection(jf.getBounds());
        if (onscreen.isEmpty())
        {
            jf.setLocation(100, 100);
        }
    }
    
    Preferences prefs;

    private Preferences getPrefs()
    {
        if (prefs == null)
            prefs = Preferences.userNodeForPackage(getClass());
        return prefs;
    }

    private Preferences windowNode(String id)
    {
        return getPrefs().node("win-" + id);
    }

    boolean loadBounds(ManagedJFrame jf, boolean visible)
    {
        Preferences p = windowNode(jf.id);
        
        Rectangle c = jf.getBounds();
        
        Rectangle bounds = new Rectangle(p.getInt("x", c.x), p.getInt("y", c.y),
                p.getInt("width", c.width), p.getInt("height", c.height));
        
        jf.setBounds(bounds);
        
        ensureOnScreen(jf);
        
        if (p.getBoolean("maximised", false))
            jf.setExtendedState(Frame.MAXIMIZED_BOTH);
        
        return p.getBoolean("visible", visible);
    }
    
    void saveBounds(ManagedJFrame jf, boolean visible)
    {
        Preferences p = windowNode(jf.id);
        
        Rectangle r = jf.getBounds();

        p.putInt("x", r.x);
        p.putInt("y", r.y);
        p.putInt("width", r.width);
        p.putInt("height", r.height);
        p.putBoolean("maximised", jf.getExtendedState() == Frame.MAXIMIZED_BOTH);
        if (!jf.id.equals("main"))
            p.putBoolean("visible", visible);
    }

    Desktop getDesktop()
    {
        return appState.getDesktop();
    }
}
