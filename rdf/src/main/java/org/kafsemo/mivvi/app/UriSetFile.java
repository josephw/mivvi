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

package org.kafsemo.mivvi.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.kafsemo.mivvi.rdf.IdentifierMappings;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class UriSetFile extends TokenFile
{
    private Set<Resource> identifiers;

    public UriSetFile(File f)
    {
        super(f);
        this.identifiers = new HashSet<Resource>();
    }

    void clear()
    {
        identifiers.clear();
    }

    void loadedToken(String t)
    {
        identifiers.add(new URIImpl(t));
    }

    Collection<String> getTokens()
    {
        ArrayList<String> al = new ArrayList<String>(identifiers.size());
        
        Iterator<Resource> i = identifiers.iterator();
        while (i.hasNext()) {
            Resource r = i.next();
            if (r instanceof URI) {
                al.add(((URI)r).toString());
            } else {
                // Blank node.
            }
        }
        
        return al;
    }

    /**
     * @param resource
     * @return
     */
    public synchronized boolean contains(Resource resource)
    {
        return identifiers.contains(resource);
    }

    public synchronized boolean add(Resource uri)
    {
        return identifiers.add(uri);
    }

    public synchronized boolean remove(Resource uri)
    {
        return identifiers.remove(uri);
    }
    
    public synchronized void addAll(Collection<Resource> resources)
    {
        Iterator<Resource> i = resources.iterator();
        while (i.hasNext()) {
            identifiers.add(i.next());
        }
    }

    public synchronized boolean isEmpty()
    {
        return identifiers.isEmpty();
    }

    /**
     * Update all URIs with a new mapping to use that newer mapping, leaving
     * all other identifiers unaffected.
     * 
     * @param im
     */
    public synchronized void update(IdentifierMappings im)
    {
        Collection<URI> allNewUris = new ArrayList<URI>();

        Iterator<Resource> i = identifiers.iterator();
        while (i.hasNext()) {
            Resource r = i.next();
            
            if (r instanceof URI) {
                URI newUri = im.getUriFor((URI)r);
                if (newUri != null) {
                    allNewUris.add(newUri);
                    i.remove();
                }
            }
        }
        
        identifiers.addAll(allNewUris);
    }
}
