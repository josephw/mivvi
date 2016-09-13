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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.kafsemo.mivvi.app.EpisodeResource;
import org.kafsemo.mivvi.app.FileEpisodeResource;
import org.kafsemo.mivvi.app.LocalFiles;
import org.kafsemo.mivvi.app.SeriesData;
import org.kafsemo.mivvi.rdf.RdfUtil;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LiteralImpl;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

/**
 * Test the extraction of information about file episode resources from the
 * statement graphs. That is, that hashes are associated with files and identifiers
 * and, where possible, unified.
 * 
 * @author Joseph Walton
 */
public class TestFileEpisodeResource extends TestCase
{
    static final Resource EPISODE = new URIImpl("http://www.example.com/episode"),
        FILE = new URIImpl("file:///some-file.type"),
        FILE2 = new URIImpl("file:///some-other-file.type"),
        HASH = new URIImpl("urn:sha1:3I42H3S6NNFQ2MSVX7XZKYAYSCX5QBYJ"),
        HASH2 = new URIImpl("urn:md5:2QOYZWMPACZAJ2MABGMOZ6CCPY"),
        SOURCE = new URIImpl("http://www.example.org/archive");

    private RepositoryConnection repcn;
    private SeriesData sd;
    private LocalFiles lf;
    private RepositoryConnection srepcn;

    public void setUp() throws RepositoryException, IOException
    {
        SailRepository sdrep = new SailRepository(new MemoryStore());
        sdrep.initialize();
        sd = new SeriesData();
        sd.initMviRepository(sdrep);
        srepcn = sdrep.getConnection();

        SailRepository lrep = new SailRepository(new MemoryStore());
        lrep.initialize();
        lf = new LocalFiles();
        lf.initLocalFiles(repcn = lrep.getConnection());
    }
    
    public void tearDown() throws RepositoryException
    {
        lf.close();
        sd.closeMviRepository();
    }

    private void clear() throws RepositoryException
    {
        repcn.clear();
    }

    public void testWithoutIdentifiedResources() throws Exception
    {
        Collection<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();

        // No statements at all
        resources.clear();
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(0, resources.size());

        // A single file with an unrelated statement
        clear();
        repcn.add(FILE, RdfUtil.Dc.title, new LiteralImpl("File Title"));
        
        resources.clear();
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(0, resources.size());
        
        // A single hash with an unrelated statement
        clear();
        repcn.add(HASH, RdfUtil.Dc.title, new LiteralImpl("The Empty File"));
        
        resources.clear();
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(0, resources.size());
    }
    
    public void testSingleIdentifiedFile() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();
        
        repcn.add(FILE, RdfUtil.Mvi.episode, EPISODE);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(1, resources.size());
        
        FileEpisodeResource fer = resources.get(0);
        assertEquals(FILE, fer.getLocation());
        assertNull(fer.getSource());
        assertEquals(0, fer.getHashUris().size());
    }
    
    public void testSingleIdentifiedHash() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();
        
        srepcn.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(1, resources.size());
        
        FileEpisodeResource fer = resources.get(0);
        assertNull(fer.getLocation());
        assertNull(fer.getSource());
        assertEquals(1, fer.getHashUris().size());
        assertTrue(fer.getHashUris().contains(HASH));
    }

    /**
     * If pairs of hashes are associated, make sure to link them to
     * the same file resource.
     */
    public void testLinkedHashes() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();

        srepcn.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        
        srepcn.add(HASH2, RdfUtil.Owl.sameAs, HASH);

        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(1, resources.size());
        
        FileEpisodeResource fer = resources.get(0);
        assertNull(fer.getLocation());
        assertNull(fer.getSource());
        assertEquals(2, fer.getHashUris().size());
        assertTrue(fer.getHashUris().contains(HASH));
        assertTrue(fer.getHashUris().contains(HASH2));
    }

    /**
     * If pairs of hashes are associated, make sure to link them to
     * the same file resource.
     */
    public void testLinkedHashes2() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();

        srepcn.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        
        srepcn.add(HASH, RdfUtil.Owl.sameAs, HASH2);

        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(1, resources.size());
        
        FileEpisodeResource fer = resources.get(0);
        assertNull(fer.getLocation());
        assertNull(fer.getSource());
        assertEquals(2, fer.getHashUris().size());
        assertTrue(fer.getHashUris().contains(HASH));
        assertTrue(fer.getHashUris().contains(HASH2));
    }


/*
    public void testSingleIdentifiedHashInBothGraphs() throws Exception
    {
        List resources = new ArrayList();
        
        lrepGraph.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        sdGraph.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        
        EpisodeResource.extractResources(EPISODE, resources, sd, lf);
        assertEquals(1, resources.size());
        
        FileEpisodeResource fer = (FileEpisodeResource)resources.get(0);
        assertNull(fer.getLocation());
        assertNull(fer.getSource());
        assertEquals(1, fer.getHashUris().size());
        assertTrue(fer.getHashUris().contains(HASH));
    }
*/
    public void testSeparateIdentifiedHashAndFile() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();
        
        repcn.add(FILE, RdfUtil.Mvi.episode, EPISODE);
        srepcn.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(2, resources.size());
        
        boolean hadFile = false, hadHash = false;
        
        Iterator<FileEpisodeResource> i = resources.iterator();
        while (i.hasNext()) {
            FileEpisodeResource fer = i.next();
            
            assertNull(fer.getSource());
            
            if (fer.getLocation() != null) {
                assertEquals(FILE, fer.getLocation());
                assertEquals(0, fer.getHashUris().size());
                hadFile = true;
            } else {
                assertNull(fer.getLocation());
                assertEquals(1, fer.getHashUris().size());
                assertTrue(fer.getHashUris().contains(HASH));
                hadHash = true;
            }
        }
        
        assertTrue(hadFile);
        assertTrue(hadHash);
    }
    
    public void testIdentifiedFileAndAssociatedHash() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();
        
        repcn.add(FILE, RdfUtil.Mvi.episode, EPISODE);
        repcn.add(FILE, RdfUtil.Dc.identifier, HASH);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(1, resources.size());
        
        FileEpisodeResource fer = resources.get(0);
        assertEquals(FILE, fer.getLocation());
        assertNull(fer.getSource());
        assertEquals(1, fer.getHashUris().size());
        assertTrue(fer.getHashUris().contains(HASH));
    }
    
    public void testIdentifiedFileAndAssociatedIdentifiedHash() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();
        
        repcn.add(FILE, RdfUtil.Mvi.episode, EPISODE);
        repcn.add(FILE, RdfUtil.Dc.identifier, HASH);
        srepcn.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(1, resources.size());
        
        FileEpisodeResource fer = resources.get(0);
        assertEquals(FILE, fer.getLocation());
        assertNull(fer.getSource());
        assertEquals(1, fer.getHashUris().size());
        assertTrue(fer.getHashUris().contains(HASH));
    }
    
    public void testFileAndAssociatedIdentifiedHash() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();
        
        srepcn.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        repcn.add(FILE, RdfUtil.Dc.identifier, HASH);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(1, resources.size());
        
        FileEpisodeResource fer = resources.get(0);
        assertEquals(FILE, fer.getLocation());
        assertNull(fer.getSource());
        assertEquals(1, fer.getHashUris().size());
        assertTrue(fer.getHashUris().contains(HASH));
    }
    
    public void testFileWithTwoHashesOnlyOneIdentified() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();
        
        repcn.add(FILE, RdfUtil.Dc.identifier, HASH);
        repcn.add(FILE, RdfUtil.Dc.identifier, HASH2);
        srepcn.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(1, resources.size());
        
        FileEpisodeResource fer = resources.get(0);
        assertEquals(FILE, fer.getLocation());
        assertNull(fer.getSource());
        assertEquals(2, fer.getHashUris().size());
        assertTrue(fer.getHashUris().contains(HASH));
        assertTrue(fer.getHashUris().contains(HASH2));
    }
    
    public void testIdentifiedHashWithSource() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();
        
        srepcn.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        srepcn.add(HASH, RdfUtil.Dc.source, SOURCE);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(1, resources.size());
        
        FileEpisodeResource fer = resources.get(0);
        assertNull(fer.getLocation());
        assertEquals(SOURCE, fer.getSource());
        assertEquals(1, fer.getHashUris().size());
        assertTrue(fer.getHashUris().contains(HASH));
    }

    public void testIdentifiedFileWithAssociatedHashWithSource() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();
        
        repcn.add(FILE, RdfUtil.Mvi.episode, EPISODE);
        repcn.add(FILE, RdfUtil.Dc.identifier, HASH);
        srepcn.add(HASH, RdfUtil.Dc.source, SOURCE);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(1, resources.size());
        
        FileEpisodeResource fer = resources.get(0);
        assertEquals(FILE, fer.getLocation());
        assertEquals(SOURCE, fer.getSource());
        assertEquals(1, fer.getHashUris().size());
        assertTrue(fer.getHashUris().contains(HASH));
    }
    
    public void testFileWithTwoIdentifiedAndAssociatedHashes() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();
        
        repcn.add(FILE, RdfUtil.Dc.identifier, HASH);
        repcn.add(FILE, RdfUtil.Dc.identifier, HASH2);
        srepcn.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        srepcn.add(HASH2, RdfUtil.Mvi.episode, EPISODE);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(1, resources.size());
        
        FileEpisodeResource fer = resources.get(0);
        assertEquals(FILE, fer.getLocation());
        assertNull(fer.getSource());
        assertEquals(2, fer.getHashUris().size());
        assertTrue(fer.getHashUris().contains(HASH));
        assertTrue(fer.getHashUris().contains(HASH2));
    }
    
    public void testIdentifiedFileWithTwoIdentifiedUnassociatedHashes() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();
        
        repcn.add(FILE, RdfUtil.Mvi.episode, EPISODE);
        srepcn.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        srepcn.add(HASH2, RdfUtil.Mvi.episode, EPISODE);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        
        assertEquals(3, resources.size());
        
        boolean hadFile = false, hadHash = false, hadHash2 = false;
        
        Iterator<FileEpisodeResource> i = resources.iterator();
        while (i.hasNext()) {
            FileEpisodeResource fer = i.next();
            
            assertNull(fer.getSource());
            
            if (fer.getLocation() != null) {
                assertEquals(FILE, fer.getLocation());
                assertEquals(0, fer.getHashUris().size());
                hadFile = true;
            } else {
                assertNull(fer.getLocation());
                assertEquals(1, fer.getHashUris().size());
                if (fer.getHashUris().contains(HASH)) {
                    hadHash = true;
                } else if (fer.getHashUris().contains(HASH2)) {
                    hadHash2 = true;
                } else {
                    fail();
                }
            }
        }
        
        assertTrue(hadFile);
        assertTrue(hadHash);
        assertTrue(hadHash2);
    }

    /**
     * Two completely separate files, both identified.
     * @throws RepositoryException 
     */
    public void testTwoIdentifiedFiles() throws RepositoryException
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();
        
        repcn.add(FILE, RdfUtil.Mvi.episode, EPISODE);
        repcn.add(FILE2, RdfUtil.Mvi.episode, EPISODE);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(2, resources.size());
        
        boolean hadFile = false, hadFile2 = false;
        
        Iterator<FileEpisodeResource> i = resources.iterator();
        while (i.hasNext()) {
            FileEpisodeResource fer = i.next();
            
            assertNull(fer.getSource());

            assertNotNull(fer.getLocation());
            
            if (fer.getLocation().equals(FILE)) {
                hadFile = true;
            } else if (fer.getLocation().equals(FILE2)) {
                hadFile2 = true;
            } else {
                fail();
            }
            assertEquals(0, fer.getHashUris().size());
        }
        
        assertTrue(hadFile);
        assertTrue(hadFile2);
    }

    /**
     * If we have two local files apparently containing the exact same data, we
     * should still consider them as separate files and provide the same additional
     * data for each of them.
     * @throws RepositoryException 
     */
    public void testTwoIdentifiedFilesSameHash() throws RepositoryException
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();
        
        repcn.add(FILE, RdfUtil.Mvi.episode, EPISODE);
        repcn.add(FILE, RdfUtil.Dc.identifier, HASH);
        repcn.add(FILE2, RdfUtil.Mvi.episode, EPISODE);
        repcn.add(FILE2, RdfUtil.Dc.identifier, HASH);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        assertEquals(2, resources.size());
        
        boolean hadFile = false, hadFile2 = false;
        
        Iterator<FileEpisodeResource> i = resources.iterator();
        while (i.hasNext()) {
            FileEpisodeResource fer = i.next();
            
            assertNull(fer.getSource());

            assertNotNull(fer.getLocation());
            
            if (fer.getLocation().equals(FILE)) {
                hadFile = true;
            } else if (fer.getLocation().equals(FILE2)) {
                hadFile2 = true;
            } else {
                fail();
            }
            assertEquals(1, fer.getHashUris().size());
            assertTrue(fer.getHashUris().contains(HASH));
        }
        
        assertTrue(hadFile);
        assertTrue(hadFile2);
    }

    /**
     * If we have two local files apparently containing the exact same data, we
     * should still consider them as separate files and provide the same additional
     * data for each of them.
     */
/*    public void testTwoFilesSameAssociatedHash()
    {
        List resources = new ArrayList();
        
        lrepGraph.add(FILE, RdfUtil.Dc.identifier, HASH);
        lrepGraph.add(FILE2, RdfUtil.Dc.identifier, HASH);
        sdGraph.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        
        EpisodeResource.extractResources(EPISODE, resources, sd, lf);
        System.err.println(resources);
        assertEquals(2, resources.size());
        
        boolean hadFile = false, hadFile2 = false;
        
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            FileEpisodeResource fer = (FileEpisodeResource)i.next();
            
            assertNull(fer.getSource());

            assertNotNull(fer.getLocation());
            
            if (fer.getLocation().equals(FILE)) {
                hadFile = true;
            } else if (fer.getLocation().equals(FILE2)) {
                hadFile2 = true;
            } else {
                fail();
            }
            assertEquals(1, fer.getHashUris().size());
            assertTrue(fer.getHashUris().contains(HASH));
        }
        
        assertTrue(hadFile);
        assertTrue(hadFile2);
    }
*/

    // TODO:
    //  Verify that only URLs are permitted as locations?
    
    /**
     * A <code>FileEpisodeResource</code> with hashes, but no known location,
     * should give a magnet URI to try and discover it.
     * @throws AccessDeniedException 
     * @throws IOException 
     */
    public void testHashBasedUri() throws Exception
    {
        List<FileEpisodeResource> resources = new ArrayList<FileEpisodeResource>();

        /* A single hash */
        srepcn.add(HASH, RdfUtil.Mvi.episode, EPISODE);
        
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        
        assertEquals(1, resources.size());
        
        FileEpisodeResource fer = resources.get(0);
        
        assertNull(fer.getLocation());
        
        assertEquals("magnet:?xt=" + HASH, fer.getActionUri(sd).toString());

        /* Two hashes for the same file */
        resources.clear();
        
        repcn.add(FILE, RdfUtil.Mvi.episode, EPISODE);
        repcn.add(FILE, RdfUtil.Dc.identifier, HASH);
        repcn.add(FILE, RdfUtil.Dc.identifier, HASH2);
        EpisodeResource.extractFileResources(EPISODE, resources, sd, lf);
        
        assertEquals(1, resources.size());
        fer = resources.get(0);
        
        assertEquals("magnet:?xt.1=" + HASH2 + "&xt.2=" + HASH, fer.getMagnetUri(sd).toString());
    }
    
    // XXX Adding hash statements to ldgraph causes hashes to
    //  be treated as files
}
