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

package org.kafsemo.mivvi.tv;

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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import javax.swing.table.DefaultTableCellRenderer;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.kafsemo.mivvi.desktop.AppState;
import org.kafsemo.mivvi.desktop.ManagedJFrame;
import org.kafsemo.mivvi.desktop.ProgressStatus;
import org.kafsemo.mivvi.gui.HelpButton;
import org.kafsemo.mivvi.gui.SeriesTreeFrame;

/**
 * @author Joseph Walton
 */
public class UpcomingBroadcastGui extends ManagedJFrame
{
    final BroadcastTableModel model;
    final JTable table;

    private final AppState appState;

    private final BackgroundThreadTasks backgroundThreadTasks;

    private final JButton configureButton;
    private final JLabel progressStatus;
    private final JProgressBar progressBar;

    private final Thread backgroundThreadTasksThread;

    private final File icalFile;

    static final String HELP_TEXT = "Upcoming television broadcasts are shown.\n"
        + "Double-click on episode titles to show program details in a web browser; double-click"
        + " on series titles to show series details.\n"
        + "Use 'Select channels...' to choose the channels you want to see listings for.\n"
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

            if (c instanceof JComponent) {
                ((JComponent)c).setToolTipText((value == null) ? null : value.toString());
            }

            return c;
        }
    }

    public void browseResource(Resource res, Component parent)
    {
        if (res instanceof IRI) {
            Desktop d = appState.getDesktop();
            if (d != null) {
                try {
                    d.browse(new java.net.URI(res.stringValue()));
                } catch (URISyntaxException use) {
                    JOptionPane.showMessageDialog(parent, use.toString(), "Unable to browse; bad URL", JOptionPane.ERROR_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(parent, e.toString(), "Unable to browse", JOptionPane.ERROR_MESSAGE);
                } catch (UnsupportedOperationException e) {
                    JOptionPane.showMessageDialog(parent, e.toString(), "Unable to browse", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public UpcomingBroadcastGui(AppState state) throws IOException
    {
        super("broadcast", "Mivvi - Upcoming broadcasts");

        this.appState = state;

        backgroundThreadTasks = new BackgroundThreadTasks(state, this);

        this.icalFile = state.getConfig().getDataFile("tv-schedule.ics");
        model = new BroadcastTableModel(state.getUserState(),
                icalFile);

        table = new JTable(model);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2) {
                    Point p = e.getPoint();
                    int row = table.rowAtPoint(p);
                    int column = table.columnAtPoint(p); // This is the view column!
                    column = table.convertColumnIndexToModel(column);

                    if (row >=0 && column >= 0) {
                        Broadcast b = model.get(row);

                        switch (column) {
                            case 2:
                                // Series
                                browseResource(b.getDetails().series, UpcomingBroadcastGui.this);
                                break;

                            case 3:
                            case 4:
                                // Episode
                                browseResource(b.getEpisode(), UpcomingBroadcastGui.this);
                                break;
                        }
                    }
                }
            }
        });

//        table.setC
//        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
//        for (int i = 0 ; i < 4 ; i++)
//            table.getColumnModel().getColumn(i).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(40);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        table.getColumnModel().getColumn(5).setPreferredWidth(30);

        table.setDefaultRenderer(String.class, new BoldeningTableCellRenderer());
        table.setDefaultRenderer(Date.class, new BoldeningTableCellRenderer(){
            DateFormat formatter;

            public void setValue(Object value) {
                if (formatter == null) {
                    formatter = new SimpleDateFormat("E, d MMMM hh:mm a");
                }
                setText((value == null) ? "" : formatter.format(value));
            }

        });
        getContentPane().add(new JScrollPane(table));

        configureButton = new JButton("Select channels...");

        configureButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                showConfigurationFrame(getChannelConfigurationFrame());
            }
        });

        configureButton.setEnabled(false);

        progressStatus = new JLabel();
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

        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e)
            {
                dispose();
                synchronized (runlock) {
                    running = false;
                    runlock.notify();
                }
            }

        });

        pack();
        setSize(new Dimension(800, 400));
        setLocation(100, 200);

        backgroundThreadTasksThread = new Thread(backgroundThreadTasks);
        backgroundThreadTasksThread.start();
    }

    void addBroadcast(Broadcast b)
    {
//        if (isInteresting(b, appState.getUserState().getSubscription()))
            model.addBroadcast(b);
    }

    void removeAllForChannel(String c)
    {
        model.removeAllForChannel(c);
    }

    void removeMissingChannels()
    {
        model.removeMissingChannels(appState.getUserState().getChannels());
    }

    final Object runlock = new Object();
    boolean running = true;

    boolean isRunning()
    {
        synchronized (runlock) {
            return running;
        }
    }

    private List<String> channelList = null;
    private Thread fetchThread;

    private void checkChannelList()
    {
        if (channelList == null) {
            if (fetchThread != null)
                return;

            fetchThread = new Thread(){
                public void run()
                {
                    backgroundThreadTasks.attemptProvideChannelList();
                }
            };

//            progressStatus.setText("Downloading channel list");
//            progressBar.setIndeterminate(true);

            fetchThread.start();
        } else {
            initConfigurationFrame();
        }
    }

    void completeFetching()
    {
        fetchThread = null;
//        updateProgress();
    }

    void setChannelList(List<String> l)
    {
        this.channelList = l;
        progressStatus.setText("OK");
        initConfigurationFrame();

        backgroundThreadTasks.scheduleRefresh();
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

    ChannelConfigurationFrame ccf;

    private final ChannelConfigurationFrame getChannelConfigurationFrame()
    {
        if (ccf == null && channelList != null) {
            ccf = new ChannelConfigurationFrame(this, appState, channelList,
                    appState.getChannelAvailability());
        }

        ccf.csm.setSelected(appState.getUserState().getChannels().getTokens());

        return ccf;
    }

    public void disposeAll()
    {
        backgroundThreadTasks.end();

        if (ccf != null) {
            ccf.dispose();
            ccf = null;
        }

        try {
            backgroundThreadTasksThread.join();
        } catch (InterruptedException ie) {
            System.err.println(ie);
        }
        dispose();
    }

    public void setVisible(boolean v)
    {
        super.setVisible(v);

        if (v != true)
            return;

        if (!appState.getDecisions().ensureDownloadListingsPermission(this)) {
            getMDIGlue().close();
            return;
        }

        checkChannelList();
    }

    private void initConfigurationFrame()
    {
        ChannelConfigurationFrame ccf = getChannelConfigurationFrame();

        if (ccf.csm.isUnconfigured()) {
            if (ccf.showConfigurationDialog(this)) {
                showConfigurationFrame(ccf);
            } else {
                appState.getUserState().getChannels().replace(ccf.csm.selected);
            }
        }

        configureButton.setEnabled(true);
    }

    private void showConfigurationFrame(ChannelConfigurationFrame ccf)
    {
        ccf.setLocationRelativeTo(this);
        ccf.setVisible(true);
        if (ccf.wasOKd()) {
            appState.getUserState().getChannels().replace(ccf.csm.selected);
            removeMissingChannels();
            backgroundThreadTasks.scheduleRefresh();
        }
    }

    public void updatedSubscriptions()
    {
        model.updatedSubscriptions();
    }

    void writeICalSchedule()
    {
        model.writeICalSchedule(icalFile);
    }
}
