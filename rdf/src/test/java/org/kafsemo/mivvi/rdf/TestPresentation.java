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

package org.kafsemo.mivvi.rdf;

import java.io.IOException;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.kafsemo.mivvi.rdf.Presentation.Details;

import junit.framework.TestCase;

/**
 * @author joe
 */
public class TestPresentation extends TestCase
{
    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    Presentation getPresentation(String... resources)
        throws IOException, RepositoryException, RDFParseException
    {
        Repository rep = new SailRepository(new MemoryStore());
        rep.initialize();
        RepositoryConnection cn = rep.getConnection();

        for (String res : resources) {
            rep.getConnection().add(
                    getClass().getResourceAsStream(res),
                    "file:///", RDFFormat.RDFXML);
        }

        Presentation p = new Presentation(cn);
        return p;
    }

    public void testGetFilenameFor() throws Exception
    {
        Presentation p = getPresentation("example-show.rdf");
        String f = p.getFilenameFor(VF.createIRI("http://www.example.com/1/1#"));

        assertEquals("Example Show - 1x01 - Named Episode", f);
    }

    /**
     * The returned details should indicate the position
     * of the episode within its season, rather than
     * overall in the series.
     *
     * @throws Exception
     */
    public void testEpisodeNumberInSeason() throws Exception
    {
        Presentation p = getPresentation("example-show.rdf");
        Details d = p.getDetailsFor(VF.createIRI("http://www.example.com/2/2#"));

        assertEquals("2", d.seasonNumber);
        assertEquals(2, d.episodeNumber);
    }

    public void testQueryForUnknownResource() throws Exception
    {
        Presentation p = getPresentation("example-show.rdf");
        Details details = p.getDetailsFor(VF.createIRI("http://www.example.com/no-such-resource#"));
        assertNull("No details for unknown resource", details);
    }

    public void testQueryAgainstEmptyPresentation() throws Exception
    {
        Presentation emptyPres = getPresentation();

        Details details = emptyPres.getDetailsFor(VF.createIRI("http://www.example.com/1/1#"));
        assertNull("Any query against an empty repository should give null", details);
    }
}
