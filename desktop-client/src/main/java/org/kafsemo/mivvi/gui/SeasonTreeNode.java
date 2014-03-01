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

import org.kafsemo.mivvi.desktop.AppState;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryException;

public class SeasonTreeNode extends EpisodeTreeNode implements Comparable<SeasonTreeNode>
{
    private final String seasonNumber;
    
    SeasonTreeNode(AppState state, SeriesTreeNode p, Resource s)
    {
        super(state, p, s);
        String num;
        try {
            num = state.getSeriesData().getSeasonNumber(s);
        } catch (RepositoryException re) {
            num = null;
        }
        
        if (num == null)
            num = "";
        this.seasonNumber = num;
    }
    
    String getTitle()
    {
        try {
            String t = state.getSeriesData().getTitle(this.resource);
            if (t != null) {
                return t;
            }
        } catch (RepositoryException re) {
            // Fall through
        }
        
        if (!seasonNumber.equals("")) {
            return "Season " + seasonNumber;
        } else {
            return "Untitled";
        }
    }
    
    Checkable[] createTreeNodes() throws RepositoryException
    {
        return createTreeNodes(state, this, resource);
    }
    
    static Checkable[] createTreeNodes(AppState state, EpisodeTreeNode parent, Resource resource) throws RepositoryException
    {
        Resource[] ra = state.getSeriesData().getEpisodes(resource);
        Checkable[] tna = new Checkable[ra.length];
        for (int i = 0 ; i < ra.length ; i++) {
            String epTitle = state.getSeriesData().getTitle(ra[i]);
            if (epTitle == null) {
                epTitle = "(untitled)";
            }
            tna[i] = new Leaf(state, ra[i], parent, (i + 1) + ". " + epTitle);
        }
        return tna;
    }
    
    private static int attemptNumeric(String s)
    {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    /**
     * Sort by season number. Use numeric order between numbers, and
     * place everything else beforehand in lexical order.
     */
    public int compareTo(SeasonTreeNode o)
    {
        String a = seasonNumber,
            b = o.seasonNumber;

        int an = attemptNumeric(a),
            bn = attemptNumeric(b);
        
        if (an > bn) {
            return 1;
        } else if (an < bn) {
            return -1;
        } else {
            return a.compareTo(b);
        }
    }
}
