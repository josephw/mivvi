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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kafsemo.mivvi.recognise.EpisodeTitleDetails;
import org.kafsemo.mivvi.recognise.Item;
import org.kafsemo.mivvi.recognise.SeriesDataException;
import org.kafsemo.mivvi.recognise.SeriesDetails;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

public class TestRdfMivviDataSource
{
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
        URI res = new URIImpl("http://www.example.com/");
        URI kw = new URIImpl("tag:kafsemo.org,2004:mivvi#keyword");
        
        cn.add(res, kw, new LiteralImpl("hr.hdtv"));
        cn.add(res, kw, new LiteralImpl("ac3.5.1"));
      
        
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
        URI uri = new URIImpl("http://www.example.com/#");
        
        cn.add(uri, RdfUtil.Rdf.type, RdfUtil.Mvi.Series);
        cn.add(uri, RdfUtil.Dc.title, new LiteralImpl("Sample Series"));
        cn.add(uri, RdfUtil.Dc.description, new LiteralImpl("Sample Series: Alternate Name"));

        RdfMivviDataSource ds = new RdfMivviDataSource(cn);
        
        Set<Item<URI>> expectedTitles = Collections.singleton(
                new Item<URI>("Sample Series", uri));
        Set<Item<URI>> expectedDescriptions = Collections.singleton(
                new Item<URI>("Sample Series: Alternate Name", uri));
        
        assertEquals(expectedTitles, new HashSet<Item<Resource>>(ds.getSeriesTitles()));
        assertEquals(expectedDescriptions, new HashSet<Item<Resource>>(ds.getSeriesDescriptions()));
    }
    
    @Test(expected = SeriesDataException.class)
    public void askingForUnknownSeriesDetailsCausesException() throws Exception
    {
        URI uri = new URIImpl("http://www.example.com/#");
        RdfMivviDataSource ds = new RdfMivviDataSource(cn);
        
        ds.getSeriesDetails(uri);
    }
    
    @Test
    public void episodesArePresentInSeriesDetailsFromRdf() throws Exception
    {
        InputStream in = getClass().getResourceAsStream("TestRdfMivviDataSource-episodesArePresentInSeriesDetailsFromRdf.rdf");
        assertNotNull(in);
        
        cn.add(in, "http://www.example.com/", RDFFormat.RDFXML);
        
        URI uri = new URIImpl("http://www.example.com/#");
        RdfMivviDataSource ds = new RdfMivviDataSource(cn);
        
        SeriesDetails<Resource> d = ds.getSeriesDetails(uri);

        Map<String, URI> expectedEpisodesByNumber = new HashMap<String, URI>();
        expectedEpisodesByNumber.put("1", new URIImpl("http://www.example.com/1x1#"));
        expectedEpisodesByNumber.put("2", new URIImpl("http://www.example.com/1x2#"));
        expectedEpisodesByNumber.put("3", new URIImpl("http://www.example.com/2x1#"));
        expectedEpisodesByNumber.put("1x1", new URIImpl("http://www.example.com/1x1#"));
        expectedEpisodesByNumber.put("1x2", new URIImpl("http://www.example.com/1x2#"));
        expectedEpisodesByNumber.put("2x1", new URIImpl("http://www.example.com/2x1#"));

        assertEquals(expectedEpisodesByNumber, d.episodesByNumber);

        List<EpisodeTitleDetails<Resource>> expectedEpisodeTitleDetails =
            new ArrayList<EpisodeTitleDetails<Resource>>();
        expectedEpisodeTitleDetails.add(new EpisodeTitleDetails<Resource>(new URIImpl("http://www.example.com/1x1#"), "First Episode", true));
        expectedEpisodeTitleDetails.add(new EpisodeTitleDetails<Resource>(new URIImpl("http://www.example.com/1x2#"), "Second Episode", true));
        expectedEpisodeTitleDetails.add(new EpisodeTitleDetails<Resource>(new URIImpl("http://www.example.com/2x1#"), "Third Episode", true));

        assertEquals(expectedEpisodeTitleDetails, d.episodeTitlesAndDescriptions);
    }
}
