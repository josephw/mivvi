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

package org.kafsemo.mivvi.app;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.kafsemo.mivvi.app.EpisodeResource;
import org.kafsemo.mivvi.app.SeriesData;
import org.kafsemo.mivvi.app.SkuEpisodeResource;
import org.kafsemo.mivvi.rdf.RdfUtil;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Make sure that {@link SkuEpisodeResource}s are extracted from RDF.
 * 
 * @author Joseph Walton
 */
public class TestSkuEpisodeResource extends TestCase
{
    static final Resource EPISODE = new URIImpl("http://www.example.com/episode"),
        ISBN = new URIImpl("urn:isbn:not-a-real-isbn"),
        ASIN = new URIImpl("urn:x-asin:not-a-real-asin");

    EpisodeResourceTestUtils.TempRdfFile trf;

    private void initRxw() throws IOException
    {
        trf = new EpisodeResourceTestUtils.TempRdfFile();
    }

    private SeriesData endRxw() throws MalformedURLException, IOException, RDFParseException, RepositoryException, RDFHandlerException
    {
        Repository rep = new SailRepository(new MemoryStore());
        rep.initialize();
        
        SeriesData sd = new SeriesData();
        sd.initMviRepository(rep);
        sd.importMivvi(trf.endRxw());
        return sd;
    }

    public void testSkuEpisodeResourceJustIsbn() throws Exception
    {
        SeriesData sd;
        initRxw();
        trf.writeStatement(ISBN, RdfUtil.Mvi.episode, EPISODE);
        sd = endRxw();
        
        
        List<SkuEpisodeResource> c = new ArrayList<SkuEpisodeResource>();
        
        EpisodeResource.extractSkuEpisodeResources(EPISODE, c, sd);

        assertEquals(1, c.size());
        
        SkuEpisodeResource ser = c.get(0);
        assertEquals(ISBN, ser.getIsbn());
        assertNull(ser.getAsin());
    }

    public void testSkuEpisodeResourceJustAsin() throws Exception
    {
        SeriesData sd;
        initRxw();
        trf.writeStatement(ASIN, RdfUtil.Mvi.episode, EPISODE);
        sd = endRxw();
        
        
        List<SkuEpisodeResource> c = new ArrayList<SkuEpisodeResource>();
        
        EpisodeResource.extractSkuEpisodeResources(EPISODE, c, sd);

        assertEquals(1, c.size());
        
        SkuEpisodeResource ser = c.get(0);
        assertNull(ser.getIsbn());
        assertEquals(ASIN, ser.getAsin());
    }

    public void testSkuEpisodeResourceIsbnAndAsinNoLink() throws Exception
    {
        SeriesData sd;
        initRxw();
        trf.writeStatement(ISBN, RdfUtil.Mvi.episode, EPISODE);
        trf.writeStatement(ASIN, RdfUtil.Mvi.episode, EPISODE);
        sd = endRxw();
        
        
        List<SkuEpisodeResource> c = new ArrayList<SkuEpisodeResource>();
        
        EpisodeResource.extractSkuEpisodeResources(EPISODE, c, sd);

        assertEquals(2, c.size());
        
        SkuEpisodeResource a = c.get(0),
            b = c.get(1);
        
        if (a.getIsbn() == null) {
            assertNull(a.getIsbn());
            assertEquals(ASIN, a.getAsin());
            assertEquals(ISBN, b.getIsbn());
            assertNull(b.getAsin());
        } else {
            assertEquals(ISBN, a.getIsbn());
            assertNull(a.getAsin());
            assertEquals(ASIN, b.getAsin());
            assertNull(b.getIsbn());
        }
    }

    public void testSkuEpisodeResourceBothLinked() throws Exception
    {
        SeriesData sd;
        initRxw();
        trf.writeStatement(ASIN, RdfUtil.Mvi.episode, EPISODE);
        trf.writeStatement(ISBN, RdfUtil.Mvi.episode, EPISODE);
        trf.writeStatement(ASIN, RdfUtil.Owl.sameAs, ISBN);
        sd = endRxw();
        
        
        List<SkuEpisodeResource> c = new ArrayList<SkuEpisodeResource>();
        
        EpisodeResource.extractSkuEpisodeResources(EPISODE, c, sd);

        assertEquals(1, c.size());
        
        SkuEpisodeResource ser = c.get(0);
        assertEquals(ISBN, ser.getIsbn());
        assertEquals(ASIN, ser.getAsin());
    }

    public void testSkuEpisodeResourceBothWithAsinIdentified() throws Exception
    {
        SeriesData sd;
        initRxw();
        trf.writeStatement(ASIN, RdfUtil.Mvi.episode, EPISODE);
        trf.writeStatement(ASIN, RdfUtil.Owl.sameAs, ISBN);
        sd = endRxw();
        
        
        List<SkuEpisodeResource> c = new ArrayList<SkuEpisodeResource>();
        
        EpisodeResource.extractSkuEpisodeResources(EPISODE, c, sd);

        assertEquals(1, c.size());
        
        SkuEpisodeResource ser = c.get(0);
        assertEquals(ISBN, ser.getIsbn());
        assertEquals(ASIN, ser.getAsin());
    }

    public void testSkuEpisodeResourceBothWithIsbnIdentified() throws Exception
    {
        SeriesData sd;
        initRxw();
        trf.writeStatement(ISBN, RdfUtil.Mvi.episode, EPISODE);
        trf.writeStatement(ASIN, RdfUtil.Owl.sameAs, ISBN);
        sd = endRxw();
        
        
        List<SkuEpisodeResource> c = new ArrayList<SkuEpisodeResource>();
        
        EpisodeResource.extractSkuEpisodeResources(EPISODE, c, sd);

        assertEquals(1, c.size());
        
        SkuEpisodeResource ser = c.get(0);
        assertEquals(ISBN, ser.getIsbn());
        assertEquals(ASIN, ser.getAsin());
    }

    /**
     * As many statements as possible around the fact that we have both ISBN and ASIN
     * for a single resource.
     */
    public void testSkuEpisodeResourceMutuallyLinked() throws Exception
    {
        SeriesData sd;
        List<SkuEpisodeResource> c;
        c = new ArrayList<SkuEpisodeResource>();
        SkuEpisodeResource ser;

        initRxw();
        trf.writeStatement(ISBN, RdfUtil.Mvi.episode, EPISODE);
        trf.writeStatement(ASIN, RdfUtil.Mvi.episode, EPISODE);
        trf.writeStatement(ASIN, RdfUtil.Owl.sameAs, ISBN);
        trf.writeStatement(ISBN, RdfUtil.Owl.sameAs, ASIN);
        trf.writeStatement(ISBN, RdfUtil.Owl.sameAs, ISBN);
        trf.writeStatement(ASIN, RdfUtil.Owl.sameAs, ASIN);
        sd = endRxw();
        
        c.clear();
        EpisodeResource.extractSkuEpisodeResources(EPISODE, c, sd);
        assertEquals(1, c.size());
        
        ser = c.get(0);
        assertEquals(ISBN, ser.getIsbn());
        assertEquals(ASIN, ser.getAsin());
    }

    public void testSkuEpisodeResourceTitles() throws Exception
    {
        SeriesData sd;
        List<SkuEpisodeResource> c;
        c = new ArrayList<SkuEpisodeResource>();
        SkuEpisodeResource ser;

        // ISBN title only
        initRxw();
        trf.writeStatement(ISBN, RdfUtil.Mvi.episode, EPISODE);
        trf.writeStatement(ASIN, RdfUtil.Mvi.episode, EPISODE);
        trf.writeStatement(ASIN, RdfUtil.Owl.sameAs, ISBN);
        trf.writeStatement(ISBN, RdfUtil.Owl.sameAs, ASIN);
        trf.writeStatement(ISBN, RdfUtil.Owl.sameAs, ISBN);
        trf.writeStatement(ASIN, RdfUtil.Owl.sameAs, ASIN);
        
        trf.writeStatement(ISBN, RdfUtil.Dc.title, new LiteralImpl("ISBN Title"));
        sd = endRxw();
        
        c.clear();
        EpisodeResource.extractSkuEpisodeResources(EPISODE, c, sd);
        assertEquals(1, c.size());
        
        ser = c.get(0);
        assertEquals(ISBN, ser.getIsbn());
        assertEquals(ASIN, ser.getAsin());
        assertEquals("ISBN Title", ser.getTitle());
        
        // ASIN title only
        initRxw();
        trf.writeStatement(ISBN, RdfUtil.Mvi.episode, EPISODE);
        trf.writeStatement(ASIN, RdfUtil.Mvi.episode, EPISODE);
        trf.writeStatement(ASIN, RdfUtil.Owl.sameAs, ISBN);
        trf.writeStatement(ISBN, RdfUtil.Owl.sameAs, ASIN);
        trf.writeStatement(ISBN, RdfUtil.Owl.sameAs, ISBN);
        trf.writeStatement(ASIN, RdfUtil.Owl.sameAs, ASIN);
        
        trf.writeStatement(ASIN, RdfUtil.Dc.title, new LiteralImpl("ASIN Title"));
        sd = endRxw();
        
        c.clear();
        EpisodeResource.extractSkuEpisodeResources(EPISODE, c, sd);
        assertEquals(1, c.size());
        
        ser = c.get(0);
        assertEquals(ISBN, ser.getIsbn());
        assertEquals(ASIN, ser.getAsin());
        assertEquals("ASIN Title", ser.getTitle());
        
        // Both, with ISBN preferred
        initRxw();
        trf.writeStatement(ISBN, RdfUtil.Mvi.episode, EPISODE);
        trf.writeStatement(ASIN, RdfUtil.Mvi.episode, EPISODE);
        trf.writeStatement(ASIN, RdfUtil.Owl.sameAs, ISBN);
        trf.writeStatement(ISBN, RdfUtil.Owl.sameAs, ASIN);
        trf.writeStatement(ISBN, RdfUtil.Owl.sameAs, ISBN);
        trf.writeStatement(ASIN, RdfUtil.Owl.sameAs, ASIN);
        
        trf.writeStatement(ISBN, RdfUtil.Dc.title, new LiteralImpl("ISBN Title"));
        trf.writeStatement(ASIN, RdfUtil.Dc.title, new LiteralImpl("ASIN Title"));
        sd = endRxw();
        
        c.clear();
        EpisodeResource.extractSkuEpisodeResources(EPISODE, c, sd);
        assertEquals(1, c.size());
        
        ser = c.get(0);
        assertEquals(ISBN, ser.getIsbn());
        assertEquals(ASIN, ser.getAsin());
        assertEquals("ISBN Title", ser.getTitle());
    }
}
