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
 * License along with this library.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.gui;

import org.kafsemo.mivvi.rdf.Presentation;
import org.eclipse.rdf4j.model.Resource;

/**
 * An item that concerns a single program, with common details included.
 */
public class EpisodeItem
{
    private final Resource episode;
    private final Presentation.Details details;
    
    protected EpisodeItem(Resource episode, Presentation.Details details)
    {
        this.episode = episode;
        this.details = details;
    }

    public Resource getEpisode()
    {
        return episode;
    }

    public Presentation.Details getDetails()
    {
        return details;
    }
    
    public String getTwoDigitEpisodeNumber()
    {
        if (details != null) {
            String s = Integer.toString(details.episodeNumber);
            if (s.length() == 1)
                s = "0" + s;
            return s;
        } else {
            return null;
        }
    }

    private boolean highlighted;

    public void setHighlighted(boolean h)
    {
        this.highlighted = h;
    }

    public boolean isHighlighted()
    {
        return highlighted;
    }
    
    public int hashCode()
    {
        return details.hashCode() ^ (highlighted ? 1 : 0);
    }
    
    public boolean equals(Object o)
    {
        if (o instanceof EpisodeItem) {
            EpisodeItem ei = (EpisodeItem)o;
            return details.equals(ei.details) && highlighted == ei.highlighted;
        } else {
            return false;
        }
    }
}
