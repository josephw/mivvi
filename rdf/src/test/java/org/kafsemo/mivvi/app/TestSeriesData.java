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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Test;
import org.kafsemo.mivvi.rdf.RdfUtil;

public class TestSeriesData
{
    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    public static SeriesData fromGraph(Graph g) throws RDFParseException, RepositoryException, IOException, RDFHandlerException
    {
        MemoryStore ms = new MemoryStore();
        SailRepository sr = new SailRepository(ms);
        sr.initialize();
        RepositoryConnection cn = sr.getConnection();

        for (Statement st : g) {
            cn.add(st);
        }

        SeriesData sd = new SeriesData();
        sd.initMviRepository(sr);

        return sd;
    }

    @Test
    public void getIconForResource() throws Exception
    {
        Model g = new LinkedHashModel();

        /* A directly defined icon */
        g.add(VF.createIRI("http://www.example.com/#"),
                RdfMiscVocabulary.smIcon,
                VF.createIRI("file:///example-icon.png"));

        SeriesData sd = fromGraph(g);

        List<? extends IRI> expected = Arrays.asList(
                VF.createIRI("file:///example-icon.png"));

        List<IRI> icons = sd.getResourceIcons(VF.createIRI("http://www.example.com/#"));

        assertEquals(expected, icons);
    }

    @Test
    public void getIconByTypeForResource() throws Exception
    {
        Model g = new LinkedHashModel();

        /* It has a type... */
        g.add(VF.createIRI("http://www.example.com/#"),
                RdfUtil.Rdf.type,
                RdfUtil.Mvi.Series);

        /* ...and that type has an icon */
        g.add(RdfUtil.Mvi.Series,
                RdfMiscVocabulary.smIcon,
                new URIImpl("file:///generic-series-icon.png"));

        SeriesData sd = fromGraph(g);

        List<? extends IRI> expected = Arrays.asList(
                VF.createIRI("file:///generic-series-icon.png"));

        List<IRI> icons = sd.getResourceIcons(VF.createIRI("http://www.example.com/#"));

        assertEquals(expected, icons);
    }

    @Test
    public void getIconReturnsNullIfNoIcon() throws Exception
    {
        Model g = new LinkedHashModel();

        /* It has a type... */
        g.add(VF.createIRI("http://www.example.com/#"),
                RdfUtil.Rdf.type,
                RdfUtil.Mvi.Series);

        /* ...but no icon for that type */

        SeriesData sd = fromGraph(g);

        List<IRI> icons = sd.getResourceIcons(VF.createIRI("http://www.example.com/#"));

        assertEquals(Collections.emptyList(), icons);
    }

    @Test
    public void canImportFromRdfXml() throws Exception
    {
        URL data = getClass().getResource("../rdf/example-show.rdf");
        assertNotNull(data);

        Model g = new LinkedHashModel();
        SeriesData sd = fromGraph(g);

        sd.importMivvi(data.toString());

        assertEquals(Collections.singletonList(VF.createIRI("http://www.example.com/#")),
                Arrays.asList(sd.getAllSeries()));
    }

    @Test
    public void canImportFromNTriples() throws Exception
    {
        URL data = getClass().getResource("../rdf/example-show.nt");
        assertNotNull(data);

        Model g = new LinkedHashModel();
        SeriesData sd = fromGraph(g);

        sd.importMivvi(data.toString());

        assertEquals(Collections.singletonList(VF.createIRI("http://www.example.com/#")),
                Arrays.asList(sd.getAllSeries()));
    }

    @Test
    public void canImportFromTurtle() throws Exception
    {
        URL data = getClass().getResource("../rdf/example-show.ttl");
        assertNotNull(data);

        Model g = new LinkedHashModel();
        SeriesData sd = fromGraph(g);

        sd.importMivvi(data.toString());

        assertEquals(Collections.singletonList(VF.createIRI("http://www.example.com/#")),
                Arrays.asList(sd.getAllSeries()));
    }
}
