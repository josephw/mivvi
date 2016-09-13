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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class FeedListTableModel extends AbstractTableModel
{
    FeedListTableModel()
    {
        this.feeds = new ArrayList<Feed>();
    }
    
    final List<Feed> feeds;
    
    void setFeeds(List<Feed> f)
    {
        feeds.clear();
        feeds.addAll(f);
        fireTableDataChanged();
    }

    static final String[] COLUMNS = {
            "URL",
            "Username",
            "Password",
            "Login Page"
    };

    public int getColumnCount()
    {
        return COLUMNS.length;
    }
    
    public Class<String> getColumnClass(int columnIndex)
    {
        return String.class;
    }
    
    public int getRowCount()
    {
        return feeds.size() + 1;
    }
    
    public String getColumnName(int column)
    {
        return COLUMNS[column];
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (rowIndex >= feeds.size()) {
            return "";
        } else {
            Feed f = feeds.get(rowIndex);
            
            switch (columnIndex) {
                case 0:
                    return f.url;
                    
                case 1:
                    return f.username;
                
                case 2:
                    return f.password;
                
                case 3:
                    return f.loginPage;
                
                default:
                    throw new IndexOutOfBoundsException(columnIndex + "> 3");
            }
        }
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return true;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        String s = (String)aValue;

        Feed f;

        boolean newFeed;

        if (rowIndex == feeds.size()) {
            if (isEmpty(s))
                return;

            f = new Feed();
            feeds.add(f);
            newFeed = true;
        } else {
            f = feeds.get(rowIndex);
            newFeed = false;
        }
        
        switch (columnIndex) {
            case 0:
                f.url = s;
                break;
                
            case 1:
                f.username = s;
                break;
            
            case 2:
                f.password = s;
                break;
            
            case 3:
                f.loginPage = s;
                break;
            
            default:
                throw new IndexOutOfBoundsException(columnIndex + "> 3");
        }
        
        if (newFeed)
            fireTableRowsInserted(rowIndex, rowIndex);
        else {
            if (f.isEmpty()) {
                feeds.remove(rowIndex);
                fireTableRowsDeleted(rowIndex, rowIndex);
            } else {
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
    
    private static boolean isEmpty(String s)
    {
        return (s == null || s.equals(""));
    }

    public boolean isEmpty()
    {
        return feeds.isEmpty();
    }
}
