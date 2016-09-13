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

import org.kafsemo.mivvi.gui.EpisodeItem;
import org.kafsemo.mivvi.rdf.Presentation;
import org.eclipse.rdf4j.model.Resource;

/**
 * A Download is a resource, available over the web, that contains an instance
 * of a TV programme.
 */
public class Download extends EpisodeItem implements Comparable<Download>
{
    // Where the resource is hosted, or listed
    public String hostTitle, hostUrl;
    
    public String resourceUrl;
    public final Feed feed;
    
    Download(Feed f, Resource episode, Presentation.Details details)
    {
        super(episode, details);
        this.feed = f;
    }
    
    public int hashCode()
    {
        return super.hashCode() ^ hostTitle.hashCode() ^ hostUrl.hashCode() ^ resourceUrl.hashCode();
    }
    
    public boolean equals(Object o)
    {
        if (super.equals(o) && o instanceof Download) {
            Download d = (Download)o;
            return hostTitle.equals(d.hostTitle) && hostUrl.equals(d.hostUrl) && resourceUrl.equals(d.resourceUrl);
        } else {
            return false;
        }
    }
    
    public String toString()
    {
        return getEpisode() + "(" + hostTitle + "," + hostUrl + "," + resourceUrl + ")";
    }
    
    public int compareTo(Download d)
    {
        int c = getDetails().seriesTitle.compareTo(d.getDetails().seriesTitle);
        if (c != 0)
            return c;
        
        try {
            int a = Integer.parseInt(getDetails().seasonNumber),
                b = Integer.parseInt(d.getDetails().seasonNumber);
            
            if (a > b) {
                return -1;
            } else if (a < b) {
                return 1;
            }
        } catch (NumberFormatException nfe) {
            c = getDetails().seasonNumber.compareTo(d.getDetails().seasonNumber);
            if (c != 0)
                return c;
        }
        
        if (getDetails().episodeNumber > d.getDetails().episodeNumber) {
            return -1;
        } else if (getDetails().episodeNumber < d.getDetails().episodeNumber) {
            return 1;
        } else {
            return resourceUrl.compareTo(d.resourceUrl);
        }
    }
}
