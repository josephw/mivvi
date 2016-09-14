/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright © 2004-2016 Joseph Walton
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
 * License along with this library.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.desktop;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.kafsemo.mivvi.app.Startup;
import org.kafsemo.mivvi.tv.Downloader;

public class MivviDataStartup
{
    MivviDataDownloadFrame mddf;

    List<File> refreshLocalData(Config cfg, Downloader dl) throws MalformedURLException, InvocationTargetException
    {
        String[] ua = cfg.getMivviDataUrls();

        List<File> files = new ArrayList<File>(ua.length);
        boolean dlApproved = false;

        final ProgressStatus psMain = new ProgressStatus();
        final ProgressStatus psIndividual = new ProgressStatus();
        
        synchronized (psMain) {
            psMain.indeterminate = false;
            psMain.maximum = ua.length;
            psMain.value = 0;
        }

        for (int i = 0 ; i < ua.length ; i++) {
            File f = new File(dl.getCacheDirectory(), "mivvi-data-" + i + ".zip");

            synchronized (psIndividual) {
                psIndividual.label = ua[i];
                psIndividual.indeterminate = true;
            }

            try {
                if (!dl.isFresh(ua[i], f, Downloader.HOUR_MILLIS * 72)) {
                    if (!dlApproved) {
                        if (new Decisions().ensureDownloadDataPermission()) {
                            dlApproved = true;
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    mddf = MivviDataDownloadFrame.showDownloadProgress(psMain, psIndividual);
                                }
                            });
                        } else
                            break;
                    }
    
                    dl.downloadToFile(new URL(ua[i]), f, psIndividual);
                }
                files.add(f);
            } catch (InterruptedIOException iioe) {
                break;
            } catch (IOException ioe) {
                // TODO Flag this error
                System.err.println(ioe);
            }

            synchronized (psIndividual) {
                psIndividual.indeterminate = false;
            }

            synchronized (psMain) {
                psMain.value = i + 1;
            }
        }

        /*
         * Bring down the GUI.
         */
        if (dlApproved) {
            try {
                SwingUtilities.invokeAndWait(new Runnable(){
                    public void run()
                    {
                        if (mddf != null) {
                            mddf.stop();
                            mddf.dispose();
                        }
                    }
                });
            } catch (InterruptedException ie) {
            }
        }

        if (files.isEmpty() && !dlApproved)
            return null;
        else
            return files;
    }
    
    public static List<String> gatherDataUrls(Iterable<File> jarFiles)
    {
        List<String> urls = new ArrayList<String>();
        
        Iterator<File> i = jarFiles.iterator();
        while (i.hasNext()) {
            File f = i.next();
            
            if (f.isFile()) {
                try {
                    urls.addAll(Startup.fetchJarContentsUrls(f));
                } catch (IOException ioe) {
                    System.err.println(ioe);
                    break;
                }
            } else {
                break;
            }
        }
        
        return urls;
    }
    
    /**
     * This is broken for a number of cases.
     * 
     * @param l
     * @return
     */
    public static List<URL> removeOverriddenFiles(List<URL> l)
    {
        List<URL> nl = new LinkedList<URL>();

        Set<String> seen = new HashSet<String>();

        ListIterator<URL> li = l.listIterator(l.size());
        while (li.hasPrevious()) {
            URL u = li.previous();
            
            String n = u.getPath();
            int i = n.lastIndexOf('/');
            if (i >= 0)
                n = n.substring(i + 1);
            
            if (!seen.contains(n)) {
                nl.add(0, u);
                seen.add(n);
            }
        }
        
        return nl;
    }
}
