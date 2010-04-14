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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import org.kafsemo.mivvi.app.TokenFile;
import org.kafsemo.mivvi.app.TokenSetFile;
import org.kafsemo.mivvi.cal.ICalFile;
import org.kafsemo.mivvi.desktop.Const;
import org.kafsemo.mivvi.desktop.UserState;
import org.kafsemo.mivvi.rss.DownloadTableModel;

public class BroadcastTableModel extends AbstractTableModel
{
    static final String[] COLUMNS = {
            "Channel",
            "Start",
            "Series",
            "Episode",
            "Title",
            "Seen?"
    };

    public int getColumnCount()
    {
        return 6;
    }

    public String getColumnName(int column)
    {
        return COLUMNS[column];
    }

    public Class<? extends Comparable<?>> getColumnClass(int columnIndex)
    {
        switch (columnIndex) {
            case 1:
                return Date.class;
            case 5:
                return Boolean.class;
            default:
                return String.class;
        }
    }
    
    public boolean isCellEditable(int row, int column)
    {
        return (column == 5);
    }

    final UserState userState;
    final ArrayList<Broadcast> displayBroadcasts;
    final Collection<Broadcast> allBroadcasts;

    public BroadcastTableModel(UserState ustate)
    {
        this.userState = ustate;
        this.displayBroadcasts = new ArrayList<Broadcast>();
        this.allBroadcasts = new ArrayList<Broadcast>();
    }

    public int getRowCount()
    {
        return displayBroadcasts.size();
    }
    
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Broadcast b = displayBroadcasts.get(rowIndex);
        
        switch (columnIndex) {
            case 0:
                return b.channel;
            
            case 1:
                return b.start;
            
            case 2:
                return b.getDetails().seriesTitle;
            
            case 3:
                return b.epnumString();
            
            case 4:
                return b.getDetails().title;
            
            case 5:
                return Boolean.valueOf(userState.getWatched().contains(b.getEpisode()));
            
            default:
                throw new IndexOutOfBoundsException(columnIndex + " > 5");
        }
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        Broadcast b = displayBroadcasts.get(rowIndex);
        if (columnIndex == 5) {
            if (((Boolean)aValue).booleanValue()) {
                userState.getWatched().add(b.getEpisode());
            } else {
                userState.getWatched().remove(b.getEpisode());
            }
        }
    }
    
    private static Comparator<Broadcast> IN_DATE_ORDER = new Comparator<Broadcast>(){
        public int compare(Broadcast a, Broadcast b) {
//            Broadcast a = (Broadcast)arg0,
//                b = (Broadcast)arg1;
            
            return a.start.compareTo(b.start);
        }
    };

    public void addBroadcast(Broadcast b)
    {
        if (b.start == null)
            System.err.println("Null start for " + b);

        allBroadcasts.add(b);

        if (!userState.isInteresting(b))
            return;

        int pos = Collections.binarySearch(displayBroadcasts, b, IN_DATE_ORDER);
        
        if (pos < 0)
            pos = -pos - 1;

        displayBroadcasts.add(pos, b);
        fireTableRowsInserted(pos, pos);
    }

    /**
     * Method to remove all reference to an existing channel's programming.
     * 
     * @param c
     */
    public void removeAllForChannel(String c)
    {
        Iterator<Broadcast> i;
        
        i = allBroadcasts.iterator();
        while (i.hasNext()) {
            if ((i.next()).channel.equals(c))
                i.remove();
        }

        boolean removed = false;

        i = displayBroadcasts.iterator();
        while (i.hasNext()) {
            Broadcast b = i.next();
            
            if (b.channel.equals(c)) {
                i.remove();
                removed = true;
            }
        }
        
        if (removed)
            fireTableDataChanged();
    }

    public void removeMissingChannels(TokenSetFile channels)
    {
        Iterator<Broadcast> i;
        
        i = allBroadcasts.iterator();
        while (i.hasNext()) {
            if (!channels.contains((i.next()).channel))
                i.remove();
        }

        boolean removed = false;

        i = displayBroadcasts.iterator();
        while (i.hasNext()) {
            Broadcast b = i.next();
            
            if (!channels.contains(b.channel)) {
                i.remove();
                removed = true;
            }
        }
        
        if (removed)
            fireTableDataChanged();
    }

    public Broadcast get(int row)
    {
        return displayBroadcasts.get(row);
    }
    
    ArrayList<Broadcast> nl = new ArrayList<Broadcast>();
    
    void updatedSubscriptions()
    {
        nl.clear();

        boolean[] highlightedA = DownloadTableModel.getHighlighted(displayBroadcasts);
        
        Iterator<Broadcast> i = allBroadcasts.iterator();
        while (i.hasNext()) {
            Broadcast b = i.next();
            
            if (userState.isInteresting(b))
                nl.add(b);
        }
        
        Collections.sort(nl, IN_DATE_ORDER);
        
        boolean[] highlightedB = DownloadTableModel.getHighlighted(displayBroadcasts);

        if (!nl.equals(displayBroadcasts) || !Arrays.equals(highlightedA, highlightedB)) {
            displayBroadcasts.clear();
            displayBroadcasts.addAll(nl);
            fireTableDataChanged();
            writeICalSchedule();
        }
    }
    
    void writeICalSchedule()
    {
        /* Write the schedule out to a file */
        try {
            File f = new File(Const.BASE, "tv-schedule.ics");
            
            File tf = TokenFile.mktmp(f);

            ICalFile icf = new ICalFile(tf);
            
            
            Iterator<Broadcast> it = displayBroadcasts.iterator();
            while (it.hasNext()) {
                Broadcast b = it.next();

                icf.writeEvent(b.start, b.end, b.getDetails().seriesTitle,
                        "TV: " + b.channel, b.getDetails().seasonNumber + "x" + b.getTwoDigitEpisodeNumber() + " - " + b.getDetails().title,
                        b.getEpisode().toString());
            }
            
            icf.close();
            
            TokenFile.installAs(f, tf);
        } catch (Throwable t) {
            System.err.println("Unable to write iCal for broadcast schedule: " + t);
            t.printStackTrace();
        }
    }
}
