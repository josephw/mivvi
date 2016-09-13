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

package org.kafsemo.mivvi.gui;

import java.io.InputStream;

import junit.framework.TestCase;

import org.kafsemo.mivvi.app.LocalFiles;
import org.kafsemo.mivvi.app.SeriesData;
import org.kafsemo.mivvi.gui.EpisodeDetailsFrame;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParserFactory;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class TestEpisodeDetailsFrame extends TestCase
{
    public void testFormatDate()
    {
        // XXX Assumes an English locale
        assertEquals("A simple 8601 date should be formatted as words",
                "Saturday, 1 January 2000",
                EpisodeDetailsFrame.formatDate("2000-01-01"));

        assertEquals("A full 8601 time should parse without failing",
                "Saturday, 1 January 2000",
                EpisodeDetailsFrame.formatDate("2000-01-01T00:00:00.000Z"));
    }

    /**
     * Make sure that a valid, empty RDF document is created when
     * info is dragged and dropped.
     *
     * @throws Exception
     */
    public void testDetailsRdfWidgetCreateNoStatementInputStream()
        throws Exception
    {
        Repository rep = new SailRepository(new MemoryStore());
        rep.initialize();

        LocalFiles lf = new LocalFiles();
        lf.initLocalFiles(rep.getConnection());

        SeriesData sd = new SeriesData();
        sd.initMviRepository(rep);

        Resource res = SimpleValueFactory.getInstance().createIRI("http://www.example.com/#");

        InputStream in = EpisodeDetailsFrame.createInputStream(lf, sd, res);
        assertNotNull(in);

        RDFParser parser = new RDFXMLParserFactory().getParser();
        StatementCollector sc = new StatementCollector();
        parser.setRDFHandler(sc);

        parser.parse(in, "file:///");

        assertEquals("There are no actual statements in the document",
                0, sc.getStatements().size());
    }
}
