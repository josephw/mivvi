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

package org.kafsemo.mivvi.app;

import org.kafsemo.mivvi.rdf.RdfUtil;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.impl.URIImpl;

/**
 * An <code>EpisodeResource</code> of a physical, buyable item.
 * 
 * @author Joseph Walton
 */
public class SkuEpisodeResource extends EpisodeResource
{
    Resource isbn, asin;

    public Resource getIsbn()
    {
        return isbn;
    }
    
    public Resource getAsin()
    {
        return asin;
    }

    String title;

    public String getTitle()
    {
        return title;
    }

    public static boolean isIsbn(Resource r)
    {
        String s = RdfUtil.resourceUri(r);
        return s.toLowerCase().startsWith("urn:isbn:");
    }

    public static boolean isAsin(Resource r)
    {
        String s = RdfUtil.resourceUri(r);
        return s.toLowerCase().startsWith("urn:x-asin:");
    }

    public String getLabel(SeriesData sd)
    {
        if (title != null) {
            return title;
        } else if (isbn != null) {
            return isbn.toString();
        } else if (asin != null) {
            return asin.toString();
        } else {
            return null;
        }
    }

    public URI getActionUri(SeriesData sd)
    {
        if (asin != null) {
            String s = asin.toString().substring(11);
            
            return new URIImpl("http://www.amazon.co.uk/exec/obidos/ASIN/" + s);
        } else if (isbn != null) {
            String s = isbn.toString().substring(9);
            return new URIImpl("http://en.wikipedia.org/wiki/Special:Booksources/" + s);
        } else {
            return null;
        }
    }
    
    public String getDescription(SeriesData sd)
    {
        if (asin != null) {
            return "Available from Amazon.co.uk";
        } else if (isbn != null) {
            return "Available by ISBN";
        } else {
            return null;
        }
    }
}
