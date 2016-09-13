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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.kafsemo.mivvi.rdf.RdfUtil;

import junit.framework.TestCase;

public class TestWebEpisodeResource extends TestCase
{
    private static ValueFactory VF = SimpleValueFactory.getInstance();

    static final Resource EPISODE = VF.createIRI("http://www.example.com/episode");

    private Repository rep;
    private RepositoryConnection cn;

    SeriesData sd;

    public void setUp() throws IOException, RepositoryException
    {
        rep = new SailRepository(new MemoryStore());
        rep.initialize();
        cn = rep.getConnection();

        sd = new SeriesData();
        sd.initMviRepository(rep);
    }

    public void tearDown() throws RepositoryException
    {
        sd.closeMviRepository();
        cn.close();
//        XXX shutdown seems to hang in MemoryStore; ignore for now
//        rep.shutDown();
    }

    public void testNonWebResourcesIgnored() throws RepositoryException
    {
        String[] nonWebResources = {
                "urn:isbn:not-a-real-isbn",
                "file:///some-file.type",
                "urn:sha1:3I42H3S6NNFQ2MSVX7XZKYAYSCX5QBYJ",
                "http:/resource-without-authority",
                "http:///resource-with-empty-authority",
                "file://even-with-a-hostname/file-is-not-the-web"
        };

        for (int i = 0 ; i < nonWebResources.length ; i++) {
            cn.add(VF.createIRI(nonWebResources[i]), RdfUtil.Mvi.episode, EPISODE);
        }

        Collection<EpisodeResource> c = new ArrayList<EpisodeResource>();

        EpisodeResource.extractWebEpisodeResources(EPISODE, c, sd);

        assertEquals(0, c.size());
    }

    public void testPageDiscovered() throws RepositoryException
    {
        cn.add(VF.createIRI("http://www.example.com/"), RdfUtil.Mvi.episode, EPISODE);

        List<WebEpisodeResource> c = new ArrayList<WebEpisodeResource>();

        EpisodeResource.extractWebEpisodeResources(EPISODE, c, sd);

        assertEquals(1, c.size());

        WebEpisodeResource wr = c.get(0);
        assertEquals(VF.createIRI("http://www.example.com/"), wr.getActionUri(sd));
        assertNull(wr.getDescription(sd));
        assertNull(wr.getLabel(sd));
    }

    public void testPageWithTitleDiscovered() throws RepositoryException
    {
        cn.add(VF.createIRI("http://www.example.com/"), RdfUtil.Mvi.episode, EPISODE);
        cn.add(VF.createIRI("http://www.example.com/"), RdfUtil.Dc.title, VF.createLiteral("Title of page"));
        cn.add(VF.createIRI("http://www.example.com/"), RdfUtil.Dc.description, VF.createLiteral("Description of page"));

        List<WebEpisodeResource> c = new ArrayList<WebEpisodeResource>();

        EpisodeResource.extractWebEpisodeResources(EPISODE, c, sd);

        assertEquals(1, c.size());

        WebEpisodeResource wr = c.get(0);
        assertEquals(VF.createIRI("http://www.example.com/"), wr.getActionUri(sd));
        assertEquals("Title of page", wr.getLabel(sd));
        assertEquals("Description of page", wr.getDescription(sd));
    }

    public void testDiscoverMultipleWebEpisodeResources() throws RepositoryException
    {
        cn.add(VF.createIRI("http://www.example.com/1"), RdfUtil.Mvi.episode, EPISODE);
        cn.add(VF.createIRI("http://www.example.com/2"), RdfUtil.Mvi.episode, EPISODE);
        cn.add(VF.createIRI("http://www.example.com/3"), RdfUtil.Mvi.episode, EPISODE);

        List<WebEpisodeResource> c = new ArrayList<WebEpisodeResource>();

        EpisodeResource.extractWebEpisodeResources(EPISODE, c, sd);

        assertEquals(3, c.size());
    }
}
