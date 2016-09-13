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

package org.kafsemo.mivvi.rss;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.kafsemo.mivvi.desktop.AppState;
import org.kafsemo.mivvi.desktop.ManagedJFrame;
import org.kafsemo.mivvi.desktop.ProgressStatus;
import org.kafsemo.mivvi.gui.HelpButton;
import org.kafsemo.mivvi.gui.SeriesTreeFrame;

public class AvailableDownloadGui extends ManagedJFrame
{
    private final AppState state;
    private final DownloadTableModel model;
    private final JTable table;
    private final RssDownloading dt;
//    private final Thread dtThread;

    static final String HELP_TEXT = "This window show programs available for download.\n"
        + "Double-click on torrent URLs to download an episode. "
        + "Double-click on episode titles to show program details in a web browser; double-click"
        + " on series titles to show series details.\n"
        + "Use 'Configure feeds...' to add web pages and RSS feeds listing programs for download..\n"
        + SeriesTreeFrame.HELP_BRIEF + "\n";

    // XXX Factor out into common code
    class BoldeningTableCellRenderer extends DefaultTableCellRenderer
    {
        Font f, fb;

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus,
                int row, int column)
        {
            if (f == null)
                f = table.getFont();
            
            if (fb == null)
                fb = table.getFont().deriveFont(Font.BOLD);
                
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            c.setFont(model.get(row).isHighlighted() ? fb : f);
            
//            System.err.println(model.get(row).isHighlighted());

            if (c instanceof JComponent) {
                ((JComponent)c).setToolTipText((value == null) ? null : value.toString());
            }

            return c;
        }
    }

    public AvailableDownloadGui(AppState state)
    {
        super("download", "Mivvi - Available downloads");

        this.state = state;
        model = new DownloadTableModel(this.state);
        table = new JTable(model);
        dt = state.getRssDownloading();
        
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(100);
        tcm.getColumn(1).setPreferredWidth(150);
        tcm.getColumn(2).setPreferredWidth(50);
        tcm.getColumn(3).setPreferredWidth(200);
        tcm.getColumn(4).setPreferredWidth(300);
        
        table.setDefaultRenderer(Object.class, new BoldeningTableCellRenderer());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2) {
                    Point p = e.getPoint();
                    int row = table.rowAtPoint(p);
                    int column = table.columnAtPoint(p); // This is the view column!
                    column = table.convertColumnIndexToModel(column);

                    if (row >=0 && column >= 0) {
                        Download d = model.get(row);
                        
                        switch (column) {
                            case 0:
                                // Download site
                                browseResource(d.hostUrl);
                                break;
                            
                            case 1:
                                browseResource(d.getDetails().series.toString());
                                break;
                            
                            case 2:
                            case 3:
                                browseResource(d.getEpisode().toString());
                                break;
                            
                            case 4:
                                browseResource(d.resourceUrl);
                                break;
                        }
                    }
                }
            }
        });
        getContentPane().add(new JScrollPane(table));
        
        configureButton = new JButton("Configure feeds...");

        configureButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                try {
                    dt.loadFeeds();
                } catch (Exception x) {
                    JOptionPane.showMessageDialog(AvailableDownloadGui.this, "Unable to load feed configuration:\n" + x, "Error loading feeds.xml", JOptionPane.ERROR_MESSAGE);
                }
                FeedConfigurationDialog fcd = new FeedConfigurationDialog(AvailableDownloadGui.this);
                List<Feed> l = AvailableDownloadGui.this.state.getUserState().getFeeds();
                synchronized (l) {
                    fcd.fltm.setFeeds(Feed.deepCopy(l));
                }
                fcd.setSize(800, 200);
                fcd.setLocationRelativeTo(AvailableDownloadGui.this);
                fcd.setVisible(true);
                if (fcd.wasOk) {
                    synchronized (l) {
                        l.clear();
                        l.addAll(fcd.fltm.feeds);
                    }
                    dt.scheduleRefresh();
                }
                fcd.dispose();
//                showConfigurationFrame(getChannelConfigurationFrame());
            }
        });
        
        configureButton.setEnabled(false);
        
        progressStatus = new JLabel("Ready");
        progressBar = new JProgressBar();

        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalStrut(10));
        b.add(configureButton);
        b.add(Box.createHorizontalStrut(10));
        
        Container pc = new JPanel(new BorderLayout());
        pc.add(progressBar);
        pc.add(BorderLayout.PAGE_END, progressStatus);
        b.add(pc);

        b.add(Box.createHorizontalStrut(10));

        b.add(new HelpButton(HELP_TEXT));
        b.add(Box.createHorizontalStrut(10));

        getContentPane().add(BorderLayout.PAGE_END, b);
        pack();
        setSize(new Dimension(700, 300));
        setLocation(250, 250);
    }
    
    public void disposeAll()
    {
        dt.end();
        if (t != null) {
            try {
                t.join();
            } catch (InterruptedException ie) {
                System.err.println(ie);
            }
        }
        dispose();
    }

    private void browseResource(String url)
    {
        Desktop d = state.getDesktop();
        if (d != null) {
            try {
                d.browse(new java.net.URI(url));
            } catch (URISyntaxException use) {
                JOptionPane.showMessageDialog(this, use.toString(), "Unable to browse; bad URL", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.toString(), "Unable to browse", JOptionPane.ERROR_MESSAGE);
            } catch (UnsupportedOperationException e) {
                JOptionPane.showMessageDialog(this, e.toString(), "Unable to browse", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
/*
    public void addDownload(final Download dl)
    {
        assert(dl.details != null);

        SwingUtilities.invokeLater(new Runnable(){
            public void run()
            {
                model.add(dl);
            }
        });
    }
*/
    private Thread t = null;
    private JButton configureButton;
    private JLabel progressStatus;
    private JProgressBar progressBar;

    public void setVisible(boolean v)
    {
        super.setVisible(v);

        if (v == true && t == null) {
            progressStatus.setText("Loading feeds configuration...");
            t = new Thread() {
                public void run()
                {
                    try {
                        dt.adg = AvailableDownloadGui.this;
                        dt.loadFeeds();
                        
                    } catch (final Exception x) {
                        SwingUtilities.invokeLater(new Runnable(){
                            public void run()
                            {
                                JOptionPane.showMessageDialog(AvailableDownloadGui.this, "Unable to load feed configuration:\n" + x, "Error loading feeds.xml", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                    SwingUtilities.invokeLater(new Runnable(){
                        public void run()
                        {
                            progressStatus.setText("Done");
                            configureButton.setEnabled(true);
                            
                            List<Feed> l = state.getUserState().getFeeds();
                            synchronized (l) {
                                if (!l.isEmpty())
                                    return;
                            }

                            JOptionPane.showMessageDialog(AvailableDownloadGui.this, "No feeds configured. Add some manually, or run a feed finding utility first.");
                        }
                    });
                    dt.scheduleRefresh();
                    dt.run();
                }
            };
            t.start();
        }
    }

    public void removeMissingFeeds(List<Feed> l)
    {
        model.removeMissingFeeds(l);
    }

    void updateProgress(ProgressStatus ps)
    {
        synchronized (ps) {
            progressBar.setIndeterminate(ps.indeterminate);
            progressBar.setMaximum(ps.maximum);
            progressBar.setValue(ps.value);
            progressStatus.setText(ps.label);
        }
    }

    public void updatedSubscriptions()
    {
        model.updatedSubscriptions();
    }

    void updateFeed(Feed f, List<Download> downloads)
    {
        model.updateFeed(f, downloads);
    }
}
