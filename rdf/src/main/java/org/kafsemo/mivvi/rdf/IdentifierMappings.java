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

package org.kafsemo.mivvi.rdf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * Recognise and report dc:identifier mappings between old and new URIs
 * for Mivvi resources.
 * 
 * @author joe
 */
public class IdentifierMappings
{
    private final Map<URI, URI> mappings = new HashMap<URI, URI>();

    private static URI[] KNOWN_TYPES = {
        RdfUtil.Mvi.Episode,
        RdfUtil.Mvi.Season,
        RdfUtil.Mvi.Series
    };

    /**
     * Learn about all the mappings contained in a repository. This information
     * will be added to that already present.
     * 
     * @param rep
     * @throws RepositoryException 
     * @throws IOException
     * @throws AccessDeniedException
     */
    public void deriveFrom(RepositoryConnection rep) throws RepositoryException
    {
        RepositoryResult<Statement> si = rep.getStatements(null, RdfUtil.Owl.sameAs, null, true);
        while (si.hasNext()) {
            Statement stmt = si.next();

            if (stmt.getSubject() instanceof URI && stmt.getObject() instanceof URI) {
                URI orig = (URI)stmt.getSubject(),
                    newId = (URI)stmt.getObject();

                boolean usesKnownTypes = false;

                for (int i = 0 ; i < KNOWN_TYPES.length && !usesKnownTypes; i++) {
                    /* Is the new type known? */
                    if (rep.hasStatement(newId, RdfUtil.Rdf.type, KNOWN_TYPES[i], true)) {
                        
                        /* Is the old type either the same or unspecified? */
                        if (rep.hasStatement(orig, RdfUtil.Rdf.type, null, true)) {
                            usesKnownTypes |= rep.hasStatement(orig, RdfUtil.Rdf.type, KNOWN_TYPES[i], true);
                        } else {
                            usesKnownTypes |= true;
                        }
                    }
                }
                
                if (usesKnownTypes) {
                    put(orig, newId);
                }
            }
        }
    }

    /**
     * Establish a mapping, so all references to <code>orig</code> will be
     * replaced with <code>newId</code>.
     * 
     * @param orig
     * @param newId
     */
    public void put(URI orig, URI newId)
    {
        mappings.put(orig, newId);
    }

    /**
     * Get the URI that should be used in preference to the one passed
     * in (if any).
     * 
     * @param orig
     * @return
     */
    public URI getUriFor(URI orig)
    {
        return mappings.get(orig);
    }
}
