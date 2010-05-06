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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.kafsemo.mivvi.app.Doap;
import org.kafsemo.mivvi.app.Version;
import org.kafsemo.mivvi.app.Versioning;
import org.kafsemo.mivvi.gui.LinkLabel;
import org.openrdf.repository.RepositoryException;

public class MainWindow extends ManagedJFrame
{
    public static final int PAD = 10;
    
    private final GuiState gs;

    private final Versioning currentVersion;
    
    MDIGlue[] mdig;

    MainWindow(GuiState gs, Doap doap, Versioning current)
        throws RepositoryException, IOException
    {
        super("main", "Mivvi");
        this.gs = gs;
        this.currentVersion = current;

        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e)
            {
                exit();
            }
        });

        JMenuBar jmb = new JMenuBar();
        JMenu jm;
        JMenuItem jmi;

        jm = new JMenu("File");
        jmi = new JMenuItem("Exit");
        jmi.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                exit();
            }
        });
        jm.add(jmi);
        jmb.add(jm);
        
        jm = new JMenu("Settings");
        final JCheckBoxMenuItem checkWebsite = new JCheckBoxMenuItem("Check website for fresh data on startup",
                Boolean.TRUE.equals(gs.appState.getDecisions().getDownloadDataPermission()));

        checkWebsite.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                MainWindow.this.gs.appState.getDecisions().setDownloadDataPermission(checkWebsite.isSelected());
            }
        });
        jmi = checkWebsite;
        jm.add(jmi);
        jmb.add(jm);

        jm = new JMenu("Help");
        jmi = new JMenuItem("Copyright...");
        jmi.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                About.showCopyright(MainWindow.this, MainWindow.this.gs.getDesktop());
            }
        });
        jm.add(jmi);
        jmi = new JMenuItem("About...");
        jmi.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                About.showAbout(MainWindow.this, MainWindow.this.gs.getDesktop(), currentVersion);
            }
        });
        jm.add(jmi);

        jmb.add(jm);
        setJMenuBar(jmb);

        mdig  = new MDIGlue[]{ 
                new MDIGlue(gs.getSeriesTreeFrame(), "All Series...", gs.seriesTreeFrameShown),
                new MDIGlue(gs.getUpcomingBroadcastFrame(), "Upcoming Broadcasts...", gs.ubgShown),
                new MDIGlue(gs.getAvailableDownloadFrame(), "Available Downloads...", gs.adgShown)
        };

        Box b = Box.createHorizontalBox();

        b.add(Box.createHorizontalStrut(PAD));
        
        b.add(Box.createHorizontalGlue());
        for (int i = 0 ; i < mdig.length ; i++) {
            if (i > 0)
                b.add(Box.createHorizontalStrut(10));
            
            MDIGlue glue = mdig[i];

            glue.openButton.addActionListener(glue.buttonListener);
            glue.frame.addWindowListener(glue.windowListener);

            glue.frame.mdiGlue = glue;

            b.add(glue.openButton);
            b.add(Box.createHorizontalStrut(PAD));
        }
        
        b.add(Box.createHorizontalGlue());
        
        b.setBorder(BorderFactory.createEmptyBorder(PAD, 0, PAD, 0));
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(BorderLayout.CENTER, b);
        
        setLocation(50, 100);

        Version latestVersion = doap.getLatestAvailableVersion();
        
        /* If there is a latest version, and it's greater than the current one... */
        if (latestVersion != null && latestVersion.compareTo(current.getVersion()) > 0) {
            Box latestBox = Box.createHorizontalBox();

            latestBox.add(Box.createHorizontalGlue());
            
            String preamble;

            if (current.getVersion().compareTo(Version.ZERO) == 0) {
                preamble = "The most recent version of Mivvi is " + latestVersion + ".";
            } else {
                preamble = "A more recent version of Mivvi (" + latestVersion + ") is available.";
            }
            
            latestBox.add(new JLabel(preamble));
            
            latestBox.add(new JLabel(" You can download it from "));

            String label = "the Mivvi website";

            if (doap.getDownloadPage() != null) {
                latestBox.add(LinkLabel.create(MainWindow.this.gs.getDesktop(), doap.getDownloadPage(), label));
            } else {
                latestBox.add(new JLabel(label));
            }
            
            latestBox.add(new JLabel("."));

            latestBox.add(Box.createHorizontalGlue());
        
            getContentPane().add(BorderLayout.AFTER_LAST_LINE, latestBox);
        }

        pack();

        int origHeight = getHeight();
        
        gs.loadBounds(this, true);
        
        if (getHeight() < origHeight) {
            setSize(getWidth(), origHeight);
        }
    }

    void showAll()
    {
        for (int i = 0 ; i < mdig.length ; i++) {
            mdig[i].openButton.setEnabled(!mdig[i].initiallyShown);
        }
        setVisible(true);
        for (int i = 0 ; i < mdig.length ; i++) {
            mdig[i].frame.setVisible(mdig[i].initiallyShown);
        }
    }
    
    void exit()
    {
        for (int i = 0 ; i < mdig.length ; i++) {
            mdig[i].initiallyShown = mdig[i].frame.isVisible();
            mdig[i].frame.setVisible(false);
        }
        
        gs.save(this, mdig);
        setVisible(false);
        gs.appState.setGUIRunning(false);
    }
    
    public void disposeAll()
    {
        dispose();
    }

    public static class MDIGlue
    {
        ManagedJFrame frame;
        JButton openButton;
        boolean initiallyShown;
        
        ActionListener buttonListener;
        WindowListener windowListener;

        MDIGlue(ManagedJFrame jf, String buttonTitle, boolean initiallyShown)
        {
            this.frame = jf;
            this.openButton = new JButton(buttonTitle);
            this.initiallyShown = initiallyShown;

            this.buttonListener =new ActionListener(){
                public void actionPerformed(ActionEvent e)
                {
                    openButton.setEnabled(false);
                    frame.setVisible(true);
                }
            };

            this.windowListener = new WindowAdapter(){
                public void windowClosing(WindowEvent e) {
                    close();
                }
            };
        }
        
        public void close()
        {
            frame.setVisible(false);
            openButton.setEnabled(true);
        }
    }
}
