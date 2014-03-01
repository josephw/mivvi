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

package org.kafsemo.mivvi.rss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.kafsemo.mivvi.desktop.AppState;
import org.kafsemo.mivvi.desktop.UserState;
import org.kafsemo.mivvi.gui.EpisodeItem;

public class DownloadTableModel extends AbstractTableModel
{
    static final String[] COLUMNS = {
            "Site",
            "Series",
            "Episode",
            "Title",
            "URL"
    };

    public int getColumnCount()
    {
        return COLUMNS.length;
    }
    
    public String getColumnName(int column)
    {
        return COLUMNS[column];
    }

    final UserState userState;
//    final UriSetFile subscribed;
    
    DownloadTableModel(AppState appState)
    {
        this.userState = appState.getUserState();
//        this.subscribed = userState.getSubscription();
    }

    private final Collection<Download> allDownloads = new ArrayList<Download>();
    private final List<Download> displayDownloads = new ArrayList<Download>();

    public int getRowCount()
    {
        return displayDownloads.size();
    }

    private void add(Download d)
    {
        allDownloads.add(d);

        // XXX Sort into order
        if (userState.isInteresting(d)) {
            int p = Collections.binarySearch(displayDownloads, d);
            if (p < 0)
                p = -p - 1;
            
            displayDownloads.add(p, d);
            
//            Collections.sort(displayDownloads);

//            int p = displayDownloads.size() - 1;
//            fireTableRowsInserted(p, p);
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Download d = displayDownloads.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return d.hostTitle;
            
            case 1:
                return d.getDetails().seriesTitle;
            
            case 2:
                return d.getDetails().seasonNumber + "x" + d.getDetails().episodeNumber;
            
            case 3:
                return d.getDetails().title;
            
            case 4:
                return d.resourceUrl;
            
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    public Download get(int row)
    {
        return displayDownloads.get(row);
    }

    private void removeAllFromSite(Feed f)
    {
        Iterator<Download> i;
        
        i = allDownloads.iterator();
        while (i.hasNext()) {
            Download d = i.next();
            if (f.url.equals(d.feed.url)) {
                i.remove();
            }
        }

//        boolean removed = false;

        i = displayDownloads.iterator();
        while (i.hasNext()) {
            Download d = i.next();
            if (f.url.equals(d.feed.url)) {
                i.remove();
//                removed = true;
            }
        }
        
//        if (removed)
//            fireTableDataChanged();
    }

    public void removeMissingFeeds(List<Feed> l)
    {
        Set<String> s = new HashSet<String>();
        
        for (Feed f : l)
            s.add(f.url);
        
        Iterator<Download> i;
        
        i = allDownloads.iterator();
        while (i.hasNext()) {
            Download d = i.next();
            if (!s.contains(d.feed.url))
                i.remove();
        }

        boolean removed = false;

        i = displayDownloads.iterator();
        while (i.hasNext()) {
            Download d = i.next();
            if (!s.contains(d.feed.url)) {
                i.remove();
                removed = true;
            }
        }
        
        if (removed)
            fireTableDataChanged();
    }

    ArrayList<Download> nl = new ArrayList<Download>();

    void updatedSubscriptions()
    {
        boolean[] currentHighlighted = getHighlighted(displayDownloads);
        nl.clear();

        Iterator<Download> i = allDownloads.iterator();
        while (i.hasNext()) {
            Download d = i.next();
            
            if (userState.isInteresting(d))
                nl.add(d);
        }
        
//        Collections.sort(nl, IN_DATE_ORDER);
        
        boolean[] nowHighlighted = getHighlighted(nl);

        if (!nl.equals(displayDownloads) || !Arrays.equals(currentHighlighted, nowHighlighted)) {
            displayDownloads.clear();
            displayDownloads.addAll(nl);
            fireTableDataChanged();
        }
    }

    public void updateFeed(Feed f, List<Download> downloads)
    {
        List<Download> current = new ArrayList<Download>(displayDownloads);

        removeAllFromSite(f);
        Iterator<Download> j = downloads.iterator();
        while (j.hasNext()) {
            add(j.next());
        }

        if (!displayDownloads.equals(current)) {
            fireTableDataChanged();
//            System.err.println("(list has changed)");
//            System.err.println(current);
//            System.err.println(displayDownloads);
        } else {
        }
    }
    
    public static boolean[] getHighlighted(List<? extends EpisodeItem> dl)
    {
        boolean[] ba = new boolean[dl.size()];
        Iterator<? extends EpisodeItem> i = dl.iterator();
        int j = 0;
        while (i.hasNext())
            ba[j++] = i.next().isHighlighted();
        return ba;
    }
}
