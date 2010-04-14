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

package org.kafsemo.mivvi.gui;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.kafsemo.mivvi.app.FileUtil;
import org.kafsemo.mivvi.app.SeriesData;
import org.kafsemo.mivvi.desktop.AppState;
import org.kafsemo.mivvi.desktop.ManagedJFrame;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryException;

public class SeriesTreeFrame extends ManagedJFrame
{
    final AppState appState;
    final JLabel statusLabel;
    DefaultTreeModel tm;
    EpisodeTreeRoot treeRoot;
    final JScrollPane scrollPane;
    
    final EpisodeTreeExpandedState expandedState;

    public static String HELP_BRIEF = "Use the 'All series' window to manage which series and episodes you're interested in.";
    
    private static final String HELP_TEXT = HELP_BRIEF + "\n"
            + "Click on the boxes to the left of the titles to subscribe and unsubscribe.\n"
            + "Drag local media (files or folders) onto this window to have Mivvi attempt to recognise them."
            + " The display will then show which episodes are available.\n"
            + "Double-click on an episode to show more details. (In the episode details window,\n drag files onto the resource list to identify them with that episode; right-click to remove, and double-click to launch.)";

    public SeriesTreeFrame(AppState state) throws RepositoryException
    {
        super("series", "Mivvi - All series");

        this.appState = state;
        statusLabel = new JLabel("Mivvi");
        treeRoot = new EpisodeTreeRoot(state, SeriesData.ROOT_IDENTIFIER);
        treeRoot.initCategoryNodes();
        tm = new DefaultTreeModel(treeRoot);
        
        JPanel jp = new JPanel(new BorderLayout());
        
        JPanel footerPanel = new JPanel(new BorderLayout());
        
        final JCheckBox showOnlySubscribedCheckbox = new JCheckBox("Show only subscribed programs in listings",
                appState.getUserState().getShowOnlySubscribed());
        showOnlySubscribedCheckbox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                appState.setShowOnlySubscribed(showOnlySubscribedCheckbox.isSelected());
            }
        });
        footerPanel.add(BorderLayout.PAGE_START, showOnlySubscribedCheckbox);
        footerPanel.add(statusLabel);
        footerPanel.add(BorderLayout.LINE_END, new HelpButton(HELP_TEXT));
        jp.add(BorderLayout.PAGE_END, footerPanel);

        treeRoot.loadedCheckImplications(tm);

        final Commands cmds = new Commands(state, this);

        setJMenuBar(cmds.createMenuBar());
        
        JTree jt = new JTree(tm);
        
        jt.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        jt.addMouseListener(new SelectionListener(state, this, jt));
        
        jt.setCellRenderer(new EpisodeTreeCellRenderer(state));

        DropTarget dt = new DropTarget(jt, DnDConstants.ACTION_LINK, new FileDropTarget(){
            public void dropped(Collection<URI> uris)
            {
                Collection<File> localFiles = new ArrayList<File>(uris.size());

                Iterator<URI> i = uris.iterator();
                while (i.hasNext()) {
                    URI u = i.next();
                    
                    File f = FileUtil.fileFrom(u);
                    if (f != null)
                        localFiles.add(f);
                }
                
                cmds.importWithProgress(localFiles);
            }
        });
        
        scrollPane = new JScrollPane(jt);

        JPanel jp2 = new JPanel(new BorderLayout());
        
        jp2.setBorder(BorderFactory.createEtchedBorder());

        jp2.add(scrollPane);

        jp.add(BorderLayout.CENTER, jp2);
        
        jp.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        getContentPane().add(jp);
        
        this.expandedState = new EpisodeTreeExpandedState(jt, treeRoot);

        pack();
        
        setSize(720, 486);
        setLocation(72, 49);
    }
    
    void exit(App app)
    {
        dispose();
        app.setGUIRunning(false);
    }
    
    public void disposeAll()
    {
        Iterator<? extends JFrame> i = episodeDetailsFrames.values().iterator();
        while (i.hasNext()) {
            JFrame jf = i.next();
            jf.dispose();
            i.remove();
        }
        dispose();
    }

    private Map<Resource, EpisodeDetailsFrame> episodeDetailsFrames = new HashMap<Resource, EpisodeDetailsFrame>();

    JFrame getDetailsFrame(Resource resource, EpisodeTreeRoot root, DefaultTreeModel model) throws RepositoryException
    {
        EpisodeDetailsFrame jf = episodeDetailsFrames.get(resource);
        if (jf == null || !jf.isDisplayable()) {
            jf = new EpisodeDetailsFrame(appState, resource, root, model);
            jf.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e)
                {
                    e.getWindow().setVisible(false);
                    e.getWindow().dispose();
                }
            });
            episodeDetailsFrames.put(resource, jf);
        }
        
        return jf;
    }

    public void refreshEpisodeResources() throws RepositoryException
    {
        for (EpisodeDetailsFrame edf : episodeDetailsFrames.values()) {
            edf.refreshEpisodeResources();
        }
    }
    
    public Set<String> getExpandedNodeUris()
    {
        return expandedState.getExpandedNodeUris();
    }

    /**
     * Return the position that the treeview is currently scrolled to.
     */
    public Point getScrollportPosition()
    {
        return scrollPane.getViewport().getViewPosition();
    }

    public void expandUris(Collection<String> tokens)
    {
        expandedState.expandUris(tokens);
    }

    public void setScrollportPosition(int verticalScroll)
    {
        scrollPane.getViewport().setViewPosition(new Point(0, verticalScroll));
    }
}
