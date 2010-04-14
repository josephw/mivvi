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

package org.kafsemo.mivvi.desktop;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.kafsemo.mivvi.app.RdfMiscVocabulary;
import org.kafsemo.mivvi.app.SeriesData;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

public class MetaData
{
    private final SeriesData seriesData;
    
    public MetaData(SeriesData seriesData)
    {
        this.seriesData = seriesData;
    }

    public List<String> getCategory(Resource res) throws RepositoryException
    {
        String category = seriesData.getStringProperty(res, RdfMiscVocabulary.kafsemoMivviCategory);
        if (category == null)
            return Collections.emptyList();
        else
            return Arrays.asList(category.split("/"));
    }

    public Icon getIcon(Resource res) throws RepositoryException
    {
        List<URI> icons = seriesData.getResourceIcons(res);
        
        for (URI uri : icons) {
            String u = uri.toString();
            String ul = u.toLowerCase();
            
            if (ul.startsWith("file:") || ul.startsWith("jar:file:")) {
                try {
                    return new ImageIcon(new URL(u));
                } catch (MalformedURLException mue) {
                    // Do nothing
                }
            }
        }
        
        return null;
    }

    URI createCategoryURI(String category) throws RepositoryException
    {
        URI r;

        try {
            r = new URIImpl("tag:kafsemo.org,2004:mivvi#category/" + URLEncoder.encode(category, "utf-8"));
        } catch (UnsupportedEncodingException ue) {
            throw new RuntimeException("UTF-8 not supported", ue);
        }
        
//        seriesData.mviRepCn.add(r, RdfUtil.Rdf.type, RdfMiscVocabulary.kafsemoMivviCategory);

        return r;
    }

    private final Map<String, URI> categories = new HashMap<String, URI>();

    public URI getCategoryURI(String category) throws RepositoryException
    {
        URI r = categories.get(category);
        if (r == null) {
            r = createCategoryURI(category);
            categories.put(category, r);
        }
        return r;
    }
}
