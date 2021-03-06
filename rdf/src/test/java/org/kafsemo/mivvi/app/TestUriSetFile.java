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

package org.kafsemo.mivvi.app;

import java.io.File;
import java.io.IOException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.kafsemo.mivvi.rdf.IdentifierMappings;

import junit.framework.TestCase;

public class TestUriSetFile extends TestCase
{
    /**
     * Test updating a <code>UriSetFile</code> according to an <code>IdentifierMappings</code>.
     *
     * @throws IOException
     */
    public void testMapIdentifiers() throws IOException
    {
        ValueFactory vf = SimpleValueFactory.getInstance();

        IRI orig = vf.createIRI("http://www.example.com/orig-episode-uri"),
            newUri = vf.createIRI("http://www.example.com/new-episode-uri"),
            other = vf.createIRI("http://www.example.com/other-episode-uri");

        IdentifierMappings im = new IdentifierMappings();
        im.put(orig, newUri);

        File f = File.createTempFile(getClass().getName(), "uris");
        f.deleteOnExit();

        UriSetFile usf = new UriSetFile(f);

        usf.add(orig);
        usf.add(other);

        usf.update(im);

        assertFalse("The original URI should no longer be present", usf.contains(orig));
        assertTrue("The new URI should now be present", usf.contains(newUri));
        assertTrue("The other URI should be unaffected", usf.contains(other));
    }
}
