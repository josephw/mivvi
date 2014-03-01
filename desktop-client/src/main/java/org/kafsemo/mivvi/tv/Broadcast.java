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

package org.kafsemo.mivvi.tv;

import java.util.Date;

import org.kafsemo.mivvi.gui.EpisodeItem;
import org.kafsemo.mivvi.rdf.Presentation;
import org.openrdf.model.Resource;

/**
 * @author Joseph Walton
 */
public class Broadcast extends EpisodeItem
{
    Broadcast(Resource episode, Presentation.Details details)
    {
        super(episode, details);
    }

    String channel;
    Date start;
    Date end;
    public String displayName;
    
    public String toString()
    {
        return Presentation.filenameFor(getDetails()) + " (" + channel + ", " + start + ", " + getEpisode().toString() + ")";
    }
    
    private String epnum;

    String epnumString()
    {
        if (epnum == null) {
            String s = Integer.toString(getDetails().episodeNumber);
            if (s.length() == 1) {
                s = "0" + s;
            }
            
            epnum = getDetails().seasonNumber + "x" + s;
        }
        return epnum;
    }
}
