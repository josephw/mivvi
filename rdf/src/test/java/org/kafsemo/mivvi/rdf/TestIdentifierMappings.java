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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import junit.framework.TestCase;

/**
 * Test {@link IdentifierMappings}, for discovery of mappings from
 * URIs to a more current form.
 *
 * @author joe
 */
public class TestIdentifierMappings extends TestCase
{
    public void testDeriveMappings() throws FileNotFoundException, IOException, RDFParseException, RepositoryException
    {
        ValueFactory vf = SimpleValueFactory.getInstance();

        Repository rep = new SailRepository(new MemoryStore());
        rep.initialize();
        RepositoryConnection cn = rep.getConnection();

        InputStream in =
            getClass().getResourceAsStream("test-identifiers.rdf");

        cn.add(in, "file:///", RDFFormat.RDFXML);

        IdentifierMappings im = new IdentifierMappings();
        im.deriveFrom(cn);

        /* Make sure the regular series mappings have been found */
        assertEquals(vf.createIRI("http://www.example.com/new-series-uri"), im.getUriFor(vf.createIRI("http://www.example.com/old-series-uri")));
        assertEquals(vf.createIRI("http://www.example.com/new-series-uri"), im.getUriFor(vf.createIRI("http://www.example.com/another-old-series-uri")));

        /* And that there's nothing in the other direction */
        assertNull(im.getUriFor(vf.createIRI("http://www.example.com/new-series-uri")));

        /* The unspecified source case should be picked up */
        assertEquals(vf.createIRI("http://www.example.com/new-series-2-uri"), im.getUriFor(vf.createIRI("http://www.example.com/old-series-2-uri")));

        /* Make sure the insufficient cases aren't picked up */
        assertNull(im.getUriFor(vf.createIRI("http://www.example.com/old-series-3-uri")));

        /* Make sure seasons and episodes are covered */
        assertEquals(vf.createIRI("http://www.example.com/new-season-uri"), im.getUriFor(vf.createIRI("http://www.example.com/old-season-uri")));
        assertEquals(vf.createIRI("http://www.example.com/new-episode-uri"), im.getUriFor(vf.createIRI("http://www.example.com/old-episode-uri")));

        /* A mapping with non-matching types should be ignored */
        assertNull(im.getUriFor(vf.createIRI("http://www.example.com/old-episode-2-uri")));

        /* As should one with non-Mivvi types */
        assertNull(im.getUriFor(vf.createIRI("http://www.example.com/old-adhoc-uri")));
    }
}
