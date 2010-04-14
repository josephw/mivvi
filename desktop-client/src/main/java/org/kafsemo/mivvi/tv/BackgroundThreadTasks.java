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

package org.kafsemo.mivvi.tv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.kafsemo.mivvi.desktop.AppState;
import org.kafsemo.mivvi.desktop.BackgroundRefreshable;
import org.kafsemo.mivvi.desktop.ProgressStatus;
import org.kafsemo.mivvi.rdf.Presentation;
import org.kafsemo.mivvi.recognise.SeriesDataException;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryException;

public class BackgroundThreadTasks extends BackgroundRefreshable
{
    private final AppState appState;
    private final UpcomingBroadcastGui ubg;
    
    BackgroundThreadTasks(AppState state, UpcomingBroadcastGui ubg)
    {
        super(Downloader.HOUR_MILLIS);
        this.appState = state;
        this.ubg = ubg;
    }

    private RadioTimesService rts;

    ProgressStatus ps = new ProgressStatus();

    Runnable updateStatus = new Runnable() {
        public void run()
        {
            ubg.updateProgress(ps);
        }
    };

    private void updateStatus()
    {
        SwingUtilities.invokeLater(updateStatus);
    }

    void attemptProvideChannelList()
    {
        try {
            synchronized (ps) {
                ps.indeterminate = true;
                ps.maximum = 0;
                ps.value = 0;
                ps.label = "Downloading channel list...";
            }
            updateStatus();

            RadioTimesService s = new RadioTimesService(appState.getDownloader(),
                    appState.getConfig().getRadioTimesBaseUrl());
            s.init();
            this.rts = s;

            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    ubg.setChannelList(rts.getChannelList());
                }
            });
        } catch (final IOException ioe) {
            SwingUtilities.invokeLater(new Runnable(){
                public void run()
                {
                    JOptionPane.showMessageDialog(ubg, "Unable to fetch channel list:\n" + ioe, "Error fetching channel list", JOptionPane.ERROR_MESSAGE);
                    ubg.getMDIGlue().close();
                }
            });
        } finally {
            SwingUtilities.invokeLater(new Runnable(){
                public void run()
                {
                    ubg.completeFetching();
                }
            });
            synchronized (ps) {
                ps.indeterminate = false;
            }

            updateStatus();
        }
    }

    public void refresh()
    {
        if (rts == null)
            return;

        Collection<String> channels = appState.getUserState().getChannels().getTokens();
        
        synchronized (ps) {
            ps.indeterminate = false;
            ps.maximum = channels.size();
            ps.value = 0;
            ps.label = "";
        }
        
        updateStatus();

        Collection<String> failedChannels = new ArrayList<String>();

        int p = 0;

        Iterator<String> i = channels.iterator();
        while (i.hasNext()) {
            if (!running())
                return;

            final String c = i.next();

            synchronized (ps) {
                ps.label = "Fetching " + c;
                ps.value = p;
            }
            
            updateStatus();

            int id = rts.getChannelId(c);
            
            if (id >= 0) {
                try {
                    File f = rts.refreshChannel(id);
                    if (f != null) {
                        List<Programme> programs;
                        InputStream in = new FileInputStream(f);
                        programs = RadioTimesService.parseListing(in, c);
                        in.close();
                        
                        final Collection<Broadcast> broadcasts = detectBroadcasts(programs);
                        
//                        System.err.println("Detected broadcasts on " + c + ": " + broadcasts.size());
                        
                        SwingUtilities.invokeLater(new Runnable(){
                            public void run()
                            {
                                ubg.removeAllForChannel(c);
                                Iterator<Broadcast> i = broadcasts.iterator();
                                while (i.hasNext())
                                    ubg.addBroadcast(i.next());
                            }
                        });
                    } else {
                        failedChannels.add(c);
                    }
                    // XXX Log, or display, exception
                } catch (RepositoryException re) {
                    failedChannels.add(c);
                } catch (SeriesDataException sde) {
                    failedChannels.add(c);
                } catch (IOException ioe) {
                    failedChannels.add(c);
                }
            } else {
                failedChannels.add(c);
            }
            
            p++;
        }
        
        synchronized (ps) {
            ps.value = p;
            if (failedChannels.isEmpty()) {
                ps.label = "Done.";
            } else {
                if (failedChannels.size () < 3)
                    ps.label = "Failed to download: " + failedChannels;
                else
                    ps.label = "Failed to download: " + failedChannels.size() + " channels";
            }
        }
        updateStatus();
        SwingUtilities.invokeLater(new Runnable(){
            public void run()
            {
                ubg.writeICalSchedule();
            }
        });
    }

    Collection<Broadcast> detectBroadcasts(Collection<Programme> progs)
            throws RepositoryException, SeriesDataException
    {
        int programs = 0;
        int withKnownSeries = 0;
        int recognised = 0;

        Collection<Broadcast> broadcasts = new ArrayList<Broadcast>();

        Iterator<Programme> i = progs.iterator();
        while (i.hasNext()) {
            programs++;
            Programme p = i.next();
            
            String title = p.getTitle();

            if (title == null) {
                continue;
            }
            
            Resource series = appState.getSeriesData().getSeries(title);
            
            if (series != null) {
                withKnownSeries++;
                String st = p.getSubTitle();
                if (st != null && !st.equals("")) {
                    Resource ep = RadioTimesService.recognise(appState.getSeriesData(), series, st);

                    if (ep != null) {
                        Presentation.Details details = appState.getSeriesData().getDetailsFor(ep);
                        
                        if (details != null) {
                            Broadcast b = new Broadcast(ep, details);
                            b.channel = p.getChannel();
                            b.start = p.getStart();
                            b.end = p.getEnd();

                            b.displayName = Presentation.filenameFor(details);
                            
                            broadcasts.add(b);
                            recognised++;
                        } else {
                            System.err.println("Unable to get details for " + ep);
                        }
                    } else {
                        System.err.println("(Unable to recognise " + st + " of " + title + ")");
                    }
                } else {
                    System.err.println("(No sub-title for episode of " + title);
                }
            }
        }
        
        return broadcasts;
    }
}
