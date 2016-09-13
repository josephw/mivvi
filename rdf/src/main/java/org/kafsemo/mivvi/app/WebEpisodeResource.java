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

package org.kafsemo.mivvi.app;

import org.kafsemo.mivvi.rdf.RdfUtil;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

public class WebEpisodeResource extends EpisodeResource
{
    private final URI resource;
    private final String title;
    private final String description;

    public WebEpisodeResource(URI uri, RepositoryConnection g)
        throws RepositoryException
    {
        this.resource = uri;
        
        this.title = RdfUtil.getStringProperty(g, uri, RdfUtil.Dc.title);
        this.description = RdfUtil.getStringProperty(g, uri, RdfUtil.Dc.description);
    }

    public WebEpisodeResource fromRepository(URI uri, RepositoryConnection rep)
        throws RepositoryException
    {
        return new WebEpisodeResource(uri, rep);
    }
    
    public URI getUri()
    {
        return resource;
    }
    
    public URI getActionUri(SeriesData sd)
    {
        return resource;
    }

    public String getDescription(SeriesData sd)
    {
        return description;
    }
    
    public String getLabel(SeriesData sd)
    {
        return title;
    }

    /**
     * Is this a non-opaque, non-file: URI with a non-empty network path?
     * 
     * @param uri
     * @return
     */
    public static boolean isWebResource(URI uri)
    {
        String s = uri.toString();

        /* file: isn't the web */
        if (s.toLowerCase().startsWith("file:")) {
            return false;
        }

        /* Is there a network path? */
        int i = s.indexOf("://");
        if (i < 0 || i == 0) {
            return false;
        }

        /* Is it non-empty? */
        int j = s.indexOf('/', i + 3);
        if (j <= i + 4) {
            return false;
        } else {
            return true;
        }
    }
}
