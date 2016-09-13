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
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.kafsemo.mivvi.app.EpisodeResource;
import org.kafsemo.mivvi.app.FileUtil;
import org.kafsemo.mivvi.app.LocalFiles;
import org.kafsemo.mivvi.app.SeriesData;
import org.kafsemo.mivvi.desktop.AppState;
import org.kafsemo.mivvi.desktop.MetaData;
import org.kafsemo.mivvi.rdf.HashUris;
import org.kafsemo.mivvi.rdf.Mivvi;
import org.kafsemo.mivvi.rdf.RdfUtil;

/**
 * @author joe
 */
public class EpisodeDetailsFrame extends JFrame
{
    private final AppState state;
    private final Resource res;

    static Object[][] props = {
            {RdfUtil.Dc.title, "Title"},
            {RdfUtil.Dc.description, "Also known as"},
            {RdfUtil.Dc.date, "Date"},
            {RdfUtil.Mvi.productionCode, "Code"}
    };

    private Resource popupResource;

    private static final WindowListener disposingListener = new WindowAdapter(){
        public void windowClosing(WindowEvent e)
        {
            e.getWindow().dispose();
        }
    };

    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd"),
        df2 = new SimpleDateFormat("EEEE, d MMMM yyyy");

    public static synchronized String formatDate(String s)
    {
        try {
            return df2.format(df.parse(s));
        } catch (ParseException pe) {
            return s;
        }
    }

    private final DefaultListModel<ResourceItem> lm;
    private JButton updateHashesButton;

    private final Runnable refreshRunnable = new Runnable(){
        public void run()
        {
            try {
                refreshEpisodeResources();
            } catch (RepositoryException re) {
                // XXX Logging
                System.err.println(re);
            }
        }
    };

    public static final String BUG_REPORT_URL = "https://jdic.dev.java.net/issues/show_bug.cgi?id=33";

    EpisodeDetailsFrame(AppState state, Resource res, final EpisodeTreeRoot etr, final DefaultTreeModel treeModel)
        throws RepositoryException
    {
        super(state.getSeriesData().getFullEpisodeTitle(res));
        this.state = state;
        this.res = res;

        addWindowListener(disposingListener);

        getContentPane().setLayout(new BorderLayout(5, 5));

        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.ipadx = 5;
        gbc.ipady = 2;

        JComponent details = new JPanel(gridBag);

        for (int i = 0 ; i < props.length ; i++) {
            IRI pred = (IRI)props[i][0];

            if (RdfUtil.Dc.description.equals(pred)) {
                List<String> akal = state.getSeriesData().getStringList(res, pred);
                if (akal.isEmpty())
                    continue;

                JLabel l = new JLabel(props[i][1] + ":");
                details.add(l);
                gbc.gridx = GridBagConstraints.RELATIVE;
                gbc.gridwidth = 1;
                gbc.weightx = 0;
                gbc.anchor = GridBagConstraints.LINE_END;
                gridBag.setConstraints(l, gbc);

                gbc.gridx = 1;
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbc.weightx = 1;
                gbc.anchor = GridBagConstraints.LINE_START;

                for (String aka : akal) {
                    l = new JLabel(aka);
                    details.add(l);
                    gridBag.setConstraints(l, gbc);
                }
            } else {
                String s = state.getSeriesData().getStringProperty(res, pred);

                // An untitled episode. TODO Show in italics?
                if (s == null && pred.equals(RdfUtil.Dc.title))
                    s = "Untitled";

                if (RdfUtil.Dc.date.equals(pred) && s != null)
                    s = formatDate(s);

                JLabel l = new JLabel(props[i][1] + ":"),
                t = new JLabel(s);

                if (RdfUtil.Dc.title.equals(pred) && res instanceof IRI
                        && state.getDesktop() != null)
                {
                    try {
                        t = new LinkLabel(state.getDesktop(),
                                new java.net.URI(res.toString()), s);
                    } catch (URISyntaxException e) {
                        // Do nothing
                    }
                }

                details.add(l);
                details.add(t);

                gbc.gridx = GridBagConstraints.RELATIVE;
                gbc.gridwidth = 1;
                gbc.weightx = 0;
                gbc.anchor = GridBagConstraints.LINE_END;
                gridBag.setConstraints(l, gbc);
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbc.weightx = 1;
                gbc.anchor = GridBagConstraints.LINE_START;
                gridBag.setConstraints(t, gbc);
            }
        }

        List<SeriesData.NamedResource> cl = state.getSeriesData().getContributors(res);

        if (!cl.isEmpty()) {
            JLabel jl = new JLabel("Contributors:");
            details.add(jl);
            gbc.gridx = GridBagConstraints.RELATIVE;
            gbc.gridwidth = 1;
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.LINE_END;
            gridBag.setConstraints(jl, gbc);

            gbc.gridx = 1;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1;
            gbc.anchor = GridBagConstraints.LINE_START;

            for (SeriesData.NamedResource nr : cl) {
                JComponent comp = null;
                if (state.getDesktop() != null) {
                    try {
                        comp = new LinkLabel(state.getDesktop(),
                                new java.net.URI(nr.getResource().toString()),
                                nr.getName());
                    } catch (URISyntaxException e) {
                        // Fall through
                    }
                }
                if (comp == null) {
                    comp = new JLabel(nr.getName());
                }
                details.add(comp);
                gridBag.setConstraints(comp, gbc);
            }
        }
        details.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Details")));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(details, BorderLayout.CENTER);

        Box b = Box.createVerticalBox();
        b.add(Box.createVerticalGlue());
        b.add(new DetailsRdfWidget(state.getMetaData()));
        b.add(Box.createVerticalGlue());

        topPanel.add(b, BorderLayout.LINE_END);

        getContentPane().add(topPanel, BorderLayout.PAGE_START);

        this.lm = new DefaultListModel<ResourceItem>();

        refreshEpisodeResources();
        final JList<ResourceItem> jl = new JList<ResourceItem>(lm) {
            public String getToolTipText(MouseEvent evt)
            {
                int index = locationToIndex(evt.getPoint());

                if (index >= 0) {
                    if (!getCellBounds(index, index).contains(evt.getPoint()))
                        index = -1;
                }

                if (index >= 0) {
                    ResourceItem ri = getModel().getElementAt(index);
                    if (ri == null) {
                        return null;
                    } else {
                        return ri.tooltip;
                    }
                } else {
                    return null;
                }
            }
        };

        final JPopupMenu pop = new JPopupMenu();

        final JMenuItem dropItem = new JMenuItem("Drop");
        dropItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                Resource r = popupResource;
                if (r != null) {
                    try {
                        EpisodeDetailsFrame.this.state.getLocalFiles().dropEpisode(EpisodeDetailsFrame.this.res, r);
                        refreshEpisodeResources();
                        etr.loadedCheckImplications(treeModel);
                    } catch (RepositoryException re) {
                        // XXX Logging
                        System.err.println(re);
                    }
                }
            }
        });
        pop.add(dropItem);

        jl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jl.addMouseListener(new MouseAdapter(){
            private void checkPopup(MouseEvent e)
            {
                if (e.isPopupTrigger()) {
                    int idx = jl.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        jl.setSelectedIndex(idx);

                        ResourceItem ri = jl.getModel().getElementAt(idx);
                        popupResource = ri.droppableResource;
                        dropItem.setEnabled(popupResource != null);
                        pop.show(jl, e.getX(), e.getY());
                    }
                }
            }

            public void mousePressed(MouseEvent e)
            {
                checkPopup(e);
            }

            public void mouseReleased(MouseEvent e)
            {
                checkPopup(e);
            }

            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2) {
                    int idx = jl.locationToIndex(e.getPoint());
                    if (idx == jl.getSelectedIndex()) {
                        if (idx >= 0) {
                            Desktop d = EpisodeDetailsFrame.this.state.getDesktop();

                            if (d == null) {
                                JOptionPane.showMessageDialog(EpisodeDetailsFrame.this, "No Desktop access from this JVM.", "Unable to browse", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            ResourceItem ri = jl.getModel().getElementAt(idx);
                            try {
                                if (ri.item instanceof File) {
                                    d.open((File)ri.item);
                                } else if (ri.item instanceof IRI) {
                                    IRI u = (IRI)ri.item;
                                    d.browse(new java.net.URI(u.toString()));
                                } else {
                                    System.err.println("Unknown resource item type: " + ri.item.getClass().getName() + " " + ri.item);
                                }
                            } catch (URISyntaxException use) {
                                JOptionPane.showMessageDialog(EpisodeDetailsFrame.this, use.toString(), "Unable to browse; bad URL", JOptionPane.ERROR_MESSAGE);
                            } catch (IOException ioe) {
                                JOptionPane.showMessageDialog(EpisodeDetailsFrame.this, ioe.toString(), "Unable to browse", JOptionPane.ERROR_MESSAGE);
                            } catch (UnsupportedOperationException uoe) {
                                JOptionPane.showMessageDialog(EpisodeDetailsFrame.this, uoe.toString(), "Unable to browse", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        });

        jl.setCellRenderer(new DefaultListCellRenderer(){/*
           public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
           {
               Component c = super.getListCellRendererComponent(list, value, index, isSelected,
                    cellHasFocus);

               if (c instanceof JComponent) {
                   String tt = ((ResourceItem)value).getToolTip();
                   ((JComponent)c).setToolTipText(tt);
               }

               return c;
           } */
        });

        jl.setCellRenderer(new ResourceCellRenderer());

        new DropTarget(jl, DnDConstants.ACTION_LINK, new FileDropTarget(){
            public void dropped(Collection<java.net.URI> uris)
            {
                try {
                    for (java.net.URI u : uris) {
                        EpisodeDetailsFrame.this.state.getLocalFiles().addFileEpisode(u.toString(), EpisodeDetailsFrame.this.res);
                    }
                } catch (RepositoryException re) {
                    // XXX Logging
                    System.err.println(re);
                }

                try {
                    refreshEpisodeResources();
                } catch (RepositoryException re) {
                    // XXX Logging
                    System.err.println(re);
                }

                try {
                    etr.loadedCheckImplications(treeModel);
                } catch (RepositoryException re) {
                    // XXX Logging
                    System.err.println(re);
                }
            }
        });

        JComponent scrollpane = new JScrollPane(jl);

        scrollpane.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Available")));

        this.getContentPane().add(scrollpane);

        this.updateHashesButton = new JButton("Update hashes");

        updateHashesButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                updateHashesButton.setEnabled(false);

                final List<Resource> episodeResources = new ArrayList<Resource>();

                try {
                    EpisodeDetailsFrame.this.state.getLocalFiles().getResourcesFor(EpisodeDetailsFrame.this.res, episodeResources);
                } catch (RepositoryException re) {
                    JOptionPane.showMessageDialog(EpisodeDetailsFrame.this, "Error getting list of files for hashing: " + re, "Error updating hashes", JOptionPane.ERROR_MESSAGE);
                }

                /* XXX Need collection of all files */
                new Thread(){
                    public void run()
                    {
                        try {
                            for (Resource r : episodeResources) {

                                File f = FileUtil.fileFrom(r);
                                if (f != null) {
                                    Collection<IRI> newHashes;

                                    try {
                                        newHashes = HashUris.digest(f);
                                    } catch (FileNotFoundException fnfe) {
                                        newHashes = Collections.emptyList();
                                    }

                                    EpisodeDetailsFrame.this.state.getLocalFiles().replaceHashes(r, newHashes);
                                    queueRefreshEpisodeResources();
                                }

                            }
                        } catch (final RepositoryException re) {
                            SwingUtilities.invokeLater(new Runnable(){
                                public void run()
                                {
                                    JOptionPane.showMessageDialog(EpisodeDetailsFrame.this, "Error reading files for hashing: " + re, "Error updating hashes", JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        } catch (final IOException ioe) {
                            SwingUtilities.invokeLater(new Runnable(){
                                public void run()
                                {
                                    JOptionPane.showMessageDialog(EpisodeDetailsFrame.this, "Error reading files for hashing: " + ioe, "Error updating hashes", JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        } finally {
                            SwingUtilities.invokeLater(new Runnable(){
                               public void run()
                                {
                                   updateHashesButton.setEnabled(true);
                                }
                            });
                        }
                    }
                }.start();
            }
        });

        Box bottomBarBox = Box.createHorizontalBox();
        bottomBarBox.add(Box.createHorizontalGlue());
        bottomBarBox.add(Box.createHorizontalStrut(10));
        bottomBarBox.add(updateHashesButton);
        bottomBarBox.add(Box.createHorizontalStrut(10));
        bottomBarBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        getContentPane().add(BorderLayout.PAGE_END, bottomBarBox);

        this.pack();
    }

    private boolean isQueued = false;

    void queueRefreshEpisodeResources()
    {
        synchronized (this) {
            if (isQueued)
                return;
            isQueued = true;
        }

        SwingUtilities.invokeLater(refreshRunnable);
    }

    void refreshEpisodeResources() throws RepositoryException
    {
        synchronized (this) {
            isQueued = false;
        }

        lm.clear();

        List<EpisodeResource> episodeResources = new ArrayList<EpisodeResource>();

        EpisodeResource.extractResources(res, episodeResources, state.getSeriesData(), state.getLocalFiles());

        List<ResourceItem> l = new ArrayList<ResourceItem>();

        for (EpisodeResource er : episodeResources) {

            IRI actionUri = er.getActionUri(state.getSeriesData());
            String label = er.getLabel(state.getSeriesData());
            String tooltip = er.getDescription(state.getSeriesData());

            Object action;
            Resource droppableRes = null;

            if (actionUri != null) {
                File f = FileUtil.fileFrom(actionUri);
                if (f != null) {
                    action = f;
                    droppableRes = actionUri;
                } else {
                    action = actionUri;
                }
            } else {
                action = null;
            }

            ResourceItem re = new ResourceItem(action, label, droppableRes, tooltip);
            l.add(re);
        }

        Collections.sort(l);

        for (ResourceItem item : l) {
            lm.addElement(item);
        }
    }

    static class ResourceItem implements Comparable<ResourceItem>
    {
        final String label;
        final Object item;
        final Resource droppableResource;
/*
        ResourceItem(Object item, String label)
        {
            this(item, label, null);
        }
*/
        private final String tooltip;

        public String getToolTip()
        {
            return tooltip;
        }

        ResourceItem(Object item, String label, Resource droppableResource,
                String tooltip)
        {
            this.label = label;
            this.item = item;
            this.droppableResource = droppableResource;
            this.tooltip = tooltip;
        }

        public String toString()
        {
            return label;
        }

        private static int compareNullness(Object a, Object b)
        {
            return (a == null ? 1 : 0) - (b == null ? 1 : 0);
        }

        public int compareTo(ResourceItem a)
        {
            int c;

            c = compareNullness(droppableResource, a.droppableResource);
            if (c != 0)
                return c;

            c = compareNullness(label, a.label);
            if (c != 0)
                return c;

            if (label != null) {
                c = label.compareTo(a.label);
                if (c != 0)
                    return c;
            }

            int hc = hashCode(), ahc = hashCode();
            if (hc > ahc) {
                return 1;
            } else if (hc < ahc) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    class DetailsRdfWidget extends RdfDragWidget
    {
        DetailsRdfWidget(MetaData md)
        {
            super(md);
        }

        InputStream createInputStream() throws IOException
        {
            try {
                return EpisodeDetailsFrame.createInputStream(
                        state.getLocalFiles(),
                        state.getSeriesData(),
                        res);
            } catch (RDFHandlerException rhe) {
                throw new IOException("Unable to create RDF: " + rhe);
            } catch (RepositoryException re) {
                throw new IOException("Unable to create RDF: " + re);
            }
        }

    }

    public static InputStream createInputStream(
            LocalFiles localFiles, SeriesData seriesData, Resource res)
        throws RepositoryException, RDFHandlerException
    {
        Model g = new LinkedHashModel();

        localFiles.exportRelevantStatements(g, res);
        seriesData.exportRelevantStatements(g, res);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        RDFXMLWriter rxw = new RDFXMLWriter(baos);
        rxw.handleNamespace("mvi", Mivvi.URI);
        rxw.handleNamespace("dc", RdfUtil.DC_URI);

        rxw.startRDF();

        for (Statement s : g) {
            rxw.handleStatement(s);
        }

        rxw.endRDF();

        return new ByteArrayInputStream(baos.toByteArray());
    }

    static class ResourceCellRenderer extends DefaultListCellRenderer
    {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected,
                    cellHasFocus);

            Color col = Color.BLACK;

            if (((ResourceItem)value).item instanceof File) {
                // Okay
            } else {
                col = Color.GRAY;
            }


            c.setForeground(col);

            return c;
        }
    }
}
