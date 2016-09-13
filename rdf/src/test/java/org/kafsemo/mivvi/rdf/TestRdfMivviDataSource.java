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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kafsemo.mivvi.recognise.EpisodeTitleDetails;
import org.kafsemo.mivvi.recognise.Item;
import org.kafsemo.mivvi.recognise.SeriesDataException;
import org.kafsemo.mivvi.recognise.SeriesDetails;

public class TestRdfMivviDataSource
{
    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    private RepositoryConnection cn;

    @Before
    public void establishRepository() throws RepositoryException, SailException
    {
        MemoryStore ms = new MemoryStore();
        SailRepository sr = new SailRepository(ms);
        sr.initialize();

        cn = sr.getConnection();
    }

    @After
    public void shutdownRepository() throws RepositoryException
    {
        cn.close();
        // Assume repository and store will be cleaned up on collection
    }

    @Test
    public void extractEmptyKeywords() throws Exception
    {
        RdfMivviDataSource ds = new RdfMivviDataSource(cn);

        Collection<String> allKeywords = new ArrayList<String>();

        for(String kw : ds.getKeywords()) {
            allKeywords.add(kw);
        }

        assertEquals(0, allKeywords.size());
    }

    @Test
    public void extractDefinedKeywords() throws Exception
    {
        IRI res = VF.createIRI("http://www.example.com/");
        IRI kw = VF.createIRI("tag:kafsemo.org,2004:mivvi#keyword");

        cn.add(res, kw, VF.createLiteral("hr.hdtv"));
        cn.add(res, kw, VF.createLiteral("ac3.5.1"));


        RdfMivviDataSource ds = new RdfMivviDataSource(cn);

        Collection<String> allKeywords = new HashSet<String>();

        for(String k : ds.getKeywords()) {
            allKeywords.add(k);
        }

        Set<String> expected = new HashSet<String>(Arrays.asList("hr.hdtv", "ac3.5.1"));

        assertEquals(expected, allKeywords);
    }

    @Test
    public void emptySeriesTitles() throws Exception
    {
        RdfMivviDataSource ds = new RdfMivviDataSource(cn);

        assertEquals(0, ds.getSeriesTitles().size());
        assertEquals(0, ds.getSeriesDescriptions().size());
    }

    @Test
    public void seriesTitlesAndDescriptions() throws Exception
    {
        IRI uri = VF.createIRI("http://www.example.com/#");

        cn.add(uri, RdfUtil.Rdf.type, RdfUtil.Mvi.Series);
        cn.add(uri, RdfUtil.Dc.title, VF.createLiteral("Sample Series"));
        cn.add(uri, RdfUtil.Dc.description, VF.createLiteral("Sample Series: Alternate Name"));

        RdfMivviDataSource ds = new RdfMivviDataSource(cn);

        Set<Item<IRI>> expectedTitles = Collections.singleton(
                new Item<IRI>("Sample Series", uri));
        Set<Item<IRI>> expectedDescriptions = Collections.singleton(
                new Item<IRI>("Sample Series: Alternate Name", uri));

        assertEquals(expectedTitles, new HashSet<Item<Resource>>(ds.getSeriesTitles()));
        assertEquals(expectedDescriptions, new HashSet<Item<Resource>>(ds.getSeriesDescriptions()));
    }

    @Test(expected = SeriesDataException.class)
    public void askingForUnknownSeriesDetailsCausesException() throws Exception
    {
        IRI uri = VF.createIRI("http://www.example.com/#");
        RdfMivviDataSource ds = new RdfMivviDataSource(cn);

        ds.getSeriesDetails(uri);
    }

    @Test
    public void episodesArePresentInSeriesDetailsFromRdf() throws Exception
    {
        InputStream in = getClass().getResourceAsStream("TestRdfMivviDataSource-episodesArePresentInSeriesDetailsFromRdf.rdf");
        assertNotNull(in);

        cn.add(in, "http://www.example.com/", RDFFormat.RDFXML);

        IRI uri = VF.createIRI("http://www.example.com/#");
        RdfMivviDataSource ds = new RdfMivviDataSource(cn);

        SeriesDetails<Resource> d = ds.getSeriesDetails(uri);

        Map<String, IRI> expectedEpisodesByNumber = new HashMap<String, IRI>();
        expectedEpisodesByNumber.put("1", VF.createIRI("http://www.example.com/1x1#"));
        expectedEpisodesByNumber.put("2", VF.createIRI("http://www.example.com/1x2#"));
        expectedEpisodesByNumber.put("3", VF.createIRI("http://www.example.com/2x1#"));
        expectedEpisodesByNumber.put("1x1", VF.createIRI("http://www.example.com/1x1#"));
        expectedEpisodesByNumber.put("1x2", VF.createIRI("http://www.example.com/1x2#"));
        expectedEpisodesByNumber.put("2x1", VF.createIRI("http://www.example.com/2x1#"));

        assertEquals(expectedEpisodesByNumber, d.episodesByNumber);

        List<EpisodeTitleDetails<Resource>> expectedEpisodeTitleDetails =
            new ArrayList<EpisodeTitleDetails<Resource>>();
        expectedEpisodeTitleDetails.add(new EpisodeTitleDetails<Resource>(VF.createIRI("http://www.example.com/1x1#"), "First Episode", true));
        expectedEpisodeTitleDetails.add(new EpisodeTitleDetails<Resource>(VF.createIRI("http://www.example.com/1x2#"), "Second Episode", true));
        expectedEpisodeTitleDetails.add(new EpisodeTitleDetails<Resource>(VF.createIRI("http://www.example.com/2x1#"), "Third Episode", true));

        assertEquals(expectedEpisodeTitleDetails, d.episodeTitlesAndDescriptions);
    }
}
