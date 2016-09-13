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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.kafsemo.mivvi.app.FileUtil;
import org.kafsemo.mivvi.desktop.AppState;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFHandlerException;

/**
 * @author joe
 */
public class Commands
{
    private final AppState state;
    private final SeriesTreeFrame gui;

    Commands(final AppState state, final SeriesTreeFrame gui)
    {
        this.state = state;
        this.gui = gui;
    }

    JMenuBar createMenuBar()
    {
        JMenuBar jmb = new JMenuBar();
        
        JMenu jm = new JMenu("File");
        JMenuItem jmi;
        
        jmi = new JMenuItem("Import media...");
        jmi.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                importMedia();
            }
        });
        jmi.setToolTipText("Scan a directory for existing local media files");
        jm.add(jmi);
        
        jmi = new JMenuItem("Save directory index...");
        jmi.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                saveDirectoryIndex();
            }
        });
        jmi.setToolTipText("Save an RDF index for a media directory");
        jm.add(jmi);
        
        jmi = new JMenuItem("Prune missing files");
        jmi.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    pruneMissingFiles();
                } catch (RepositoryException re) {
                    System.err.println(re);
                }
            }
        });
        jmi.setToolTipText("Forget about files that are no longer present");
        jm.add(jmi);
        
        jmb.add(jm);
        
        return jmb;
    }

    JFileChooser jfc;

    void importMedia()
    {
        if (jfc == null) {
            jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            
            File f = state.getDecisions().getDefaultMediaDirectory();
            if (f != null) {
                jfc.setCurrentDirectory(f.getParentFile());
            }
            jfc.setSelectedFile(f);
        }
        
        if (jfc.showOpenDialog(gui) == JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            
            importWithProgress(f);
            
            state.getDecisions().setDefaultMediaDirectory(f);
        }
    }

    void importWithProgress(final File base)
    {
        importWithProgress(Collections.singleton(base));
    }
    
    void importWithProgress(final Collection<File> fileBases)
    {
        final Gui.ProgressState ps = new Gui.ProgressState(gui, "Importing media");
        
        ps.setNote("Gathering filenames...");
        
        Timer timer = ps.createTimer();
        timer.start();

        Thread t = new Thread() {
            public void run() {
                Collection<File> files = new ArrayList<File>();
                Iterator<File> i = fileBases.iterator();
                while (i.hasNext()) {
                    files.addAll(FileUtil.gatherFilenames(i.next(), ps));

                    if (ps.isCanceled()) {
                        return;
                    }
                }

                ps.setMaximum(files.size());
        
                int p = 0 ;
                int success = 0;
                ps.setProgress(p);

                /* Check for indexes */
                i = files.iterator();
                while (i.hasNext()) {
                    File f = i.next();
                    if (f.getName().equals("index.rdf")) {
                        ps.setNote("Reading " + f);
                        success += state.getLocalFiles().processIndex(f,
                                state.getIdentifierMappings());
                        i.remove();
                        ps.setProgress(++p);
                    } else if (f.getName().endsWith(".rdf")) {
                        // Import Mivvi data from this file?
                        // Need to do this before processing the indexes.
                    }
                }

                i = files.iterator();
                while (i.hasNext()) {
                    File f = i.next();
                    ps.setNote("Processing " + f);
                    try {
                        if (state.process(f))
                            success++;
                    } catch (Exception e) {
                        System.err.println("Internal error processing: " + f);
                        e.printStackTrace();
                    }
                    ps.setProgress(++p);
                    if (ps.isCanceled())
                        break;
                }
                
                ps.setComplete();
                
                refreshWithStatus("Imported: " + success + "/" + p);
            }
        };
        
        t.start();
    }
    
    void refreshWithStatus(final String s)
    {
        SwingUtilities.invokeLater(new Runnable(){
            public void run()
            {
                try {
                    gui.treeRoot.loadedCheckImplications(gui.tm);
                    gui.refreshEpisodeResources();
                    // This should flag the tree as changed
                    gui.statusLabel.setText(s);
                } catch (RepositoryException re) {
                    System.err.println(re);
                }
            }
        });
    }

    JFileChooser jfcSd;
    
    private void saveDirectoryIndex()
    {
        if (jfcSd == null) {
            jfcSd = new JFileChooser();
            jfcSd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        
        if (jfcSd.showSaveDialog(gui) == JFileChooser.APPROVE_OPTION) {
            File f = jfcSd.getSelectedFile();

            File fi = new File(f, "index.rdf");
            try {
                if (!fi.createNewFile()) {
                    if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(gui, "Overwrite existing file " + fi + "?", "Confirm", JOptionPane.OK_CANCEL_OPTION))
                        return;
                }
                
                if (!state.getLocalFiles().saveIndex(fi)) {
                    JOptionPane.showMessageDialog(gui, "No media was known in that directory");
                }
            } catch (RDFHandlerException rhe) {
                System.err.println(rhe);
            } catch (RepositoryException re) {
                System.err.println(re);
            } catch (URISyntaxException use) {
                System.err.println(use);
            } catch (IOException ioe) {
                System.err.println(ioe);
            }
        }
    }

    private void pruneMissingFiles() throws RepositoryException
    {
        int n = state.getLocalFiles().pruneMissingFiles();
        gui.treeRoot.loadedCheckImplications(gui.tm);
        gui.refreshEpisodeResources();
        gui.statusLabel.setText("Files pruned: " + n);
    }
}
