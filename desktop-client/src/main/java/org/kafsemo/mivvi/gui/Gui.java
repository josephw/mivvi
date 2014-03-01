/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright © 2004-2014 Joseph Walton
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

package org.kafsemo.mivvi.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.kafsemo.mivvi.app.Progress;
import org.kafsemo.mivvi.app.SeriesData;
import org.kafsemo.mivvi.app.Startup;
import org.kafsemo.mivvi.desktop.AppState;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

/**
 * @author joe
 */
public class Gui extends JFrame
{
    /*
    static boolean loadMivviData(AppState state)
        throws InterruptedException, InvocationTargetException, IOException
    {
        return loadMivviData(state.getMivviRepository());
    }
    */

    public static void synchronousAlert(final String msg) throws InterruptedException, InvocationTargetException
    {
        SwingUtilities.invokeAndWait(new Runnable(){
            public void run()
            {
                JOptionPane.showMessageDialog(null, msg, "Mivvi Startup", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static boolean loadMivviData(SeriesData seriesData, List<String> mivviUrls)
        throws InterruptedException,
            InvocationTargetException, IOException, RDFParseException,
            RepositoryException
    {
        final ProgressState pr = new ProgressState("Reading metadata");
        pr.setNote("Finding data...");

        pr.createTimer().start();
        
        if (mivviUrls == null)
            mivviUrls = Startup.gatherDataURLs(pr);

        if (mivviUrls.isEmpty()) {
            pr.setComplete();
            synchronousAlert("No data available.\nPlease make sure mivvi-data.zip, or a mivvi-data directory, is in the current directory.");
            return false;
        }

        pr.setMaximum(mivviUrls.size());

        try {
            int p = 0;
            Iterator<String> ui = mivviUrls.iterator();
            while (ui.hasNext()) {
                String s = ui.next();
                pr.setNote("Reading " + s + "...");
//                URL u = new URL(s);
                seriesData.importMivvi(s);
//                rep.addData(u, s, RDFFormat.RDFXML, true, RdfUtil.getErrorAdminListener(u.toString()));
                p++;
                pr.setProgress(p);
                if (pr.isCanceled()) {
                    return false;
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
        
        pr.setComplete();
        
        return true;
    }

    public static boolean loadMivviData(SeriesData seriesData)
            throws InterruptedException, InvocationTargetException,
            IOException, RDFParseException, RepositoryException
    {
        return loadMivviData(seriesData, null);
    }
    
    public static class ProgressState implements Progress, ActionListener
    {
        private final Component comp;
        private final String title;
        
        public ProgressState(String t)
        {
            this(null, t);
        }
        
        public ProgressState(Component c, String t)
        {
            this.comp = c;
            this.title = t;
        }

        private Timer timer;
        private long t0;

        public synchronized Timer createTimer()
        {
            timer = new Timer(250, this);
            timer.setInitialDelay(0);
            t0 = System.currentTimeMillis();
            return timer;
        }
        
        private boolean canceled = false;

        private ProgressDialog pm;

        private void end()
        {
            if (pm != null)
                pm.close();
            timer.stop();
        }

        public synchronized void actionPerformed(ActionEvent e)
        {
            if (complete) {
                end();
                return;
            }

            if (pm == null) {
                if (System.currentTimeMillis() < t0 + 2000
                        || (maximum >= 0 && progress >= maximum / 2))
                {
                    return;
                }
                pm = new ProgressDialog(comp, title);
                pm.setNote(note);
                pm.show();
            }

            if (this.canceled = pm.isCanceled()) {
                end();
                return;
            }

            if (this.maximum >= 0) {
                pm.setMaximum(this.maximum);
                pm.setProgress(this.progress);
            }
            pm.setNote(this.note);
            pm.packLarger(comp);
        }

        private int maximum = -1, progress;
        private String note;

        public synchronized void setMaximum(int m)
        {
            this.maximum = m;
        }

        public synchronized void setProgress(int p)
        {
            this.progress = p;
        }

        public synchronized void setNote(String s)
        {
            this.note = s;
        }
        
        public synchronized boolean isCanceled()
        {
            return canceled;
        }
        
        private boolean complete = false;
        
        public synchronized void setComplete()
        {
            this.complete = true;
        }
    }

    static void launchApp(final AppState state, final App app)
    {
        app.setGUIRunning(true);

        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                /* Warn about subscribed, but unknown, items? */
                try {
                    constructAndShow();
                } catch (RepositoryException re) {
                    System.err.println(re);
                }
            }
            
            private void constructAndShow() throws RepositoryException
            {
                final SeriesTreeFrame jf = new SeriesTreeFrame(state);

                JMenuBar jmb = jf.getJMenuBar();
                
                JMenu jm = jmb.getMenu(0);
                jm.addSeparator();
                
                JMenuItem jmi = new JMenuItem("Exit");
                jmi.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        jf.exit(app);
                    }
                });
                jm.add(jmi);

                jm = new JMenu("Help");
                jmi = new JMenuItem("About...");
                jmi.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane.showMessageDialog(jf, "Metadata for Video Initiative\nhttp://mivvi.net/", "Mivvi", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                jm.add(jmi);

                jmb.add(jm);
                
                jf.addWindowListener(new WindowAdapter(){
                    public void windowClosing(WindowEvent e)
                    {
                        jf.exit(app);
                    }
                });

                jf.setVisible(true);
            }
        });
    }
}
