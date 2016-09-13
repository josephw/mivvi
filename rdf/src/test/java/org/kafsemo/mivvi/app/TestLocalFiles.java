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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.kafsemo.mivvi.app.FileUtil;
import org.kafsemo.mivvi.app.LocalFiles;
import org.kafsemo.mivvi.rdf.IdentifierMappings;
import org.kafsemo.mivvi.rdf.RdfUtil;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.impl.BNodeImpl;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParser;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class TestLocalFiles extends TestCase
{
    public void testMapIdentifiers() throws Exception
    {
        URI orig = new URIImpl("http://www.example.com/orig-episode-uri"),
            newUri = new URIImpl("http://www.example.com/new-episode-uri"),
            other = new URIImpl("http://www.example.com/other-episode-uri");

        URI file1 = new URIImpl("file:///local-file-1"),
            file2 = new URIImpl("file:///local-file-2");

        Resource blankNode = new BNodeImpl("blank");

        IdentifierMappings im = new IdentifierMappings();
        im.put(orig, newUri);
    
        LocalFiles lf = new LocalFiles();
        Repository lrep = new SailRepository(new MemoryStore());
        lrep.initialize();

        RepositoryConnection cn = lrep.getConnection();
        cn.add(file1, RdfUtil.Mvi.episode, orig);
        cn.add(file2, RdfUtil.Mvi.episode, other);
        cn.add(blankNode, RdfUtil.Mvi.episode, orig);

        lf.initLocalFiles(cn);

        lf.update(im);

        Collection<Resource> c = new HashSet<Resource>();

        /* Check per-episode resources */
        lf.getResourcesFor(orig, c);
        assertTrue("The original URI should no longer be used", c.isEmpty());
        
        c.clear();
        lf.getResourcesFor(newUri, c);
        Collection<Resource> expected = new HashSet<Resource>();
        expected.add(file1);
        expected.add(blankNode);
        assertEquals("The new URI should be associated with the file", expected, c);
        
        c.clear();
        lf.getResourcesFor(other, c);
        assertEquals("Unmapped URIs should be unaffected", Collections.singleton(file2), c);
    }
    
    public void testProcessIndexWithMappings() throws Exception
    {
        URI orig = new URIImpl("http://www.example.com/orig-episode-uri"),
            newUri = new URIImpl("http://www.example.com/new-episode-uri"),
            other = new URIImpl("http://www.example.com/other-episode-uri"),
            another = new URIImpl("http://www.example.com/another-episode-uri");

        File f = new File("src/test/resources/dummy-file");
        assertTrue("This test requires that 'test-data/dummy-file' exists", f.isFile());
        
        URI dummyFileUri = FileUtil.createFileURI(f);
        
        IdentifierMappings im = new IdentifierMappings();
        im.put(orig, newUri);
    
        LocalFiles lf = new LocalFiles();
        Repository lrep = new SailRepository(new MemoryStore());
        lrep.initialize();

        RepositoryConnection cn = lrep.getConnection();
        lf.initLocalFiles(cn);

        lf.processIndex(new File("src/test/resources/test-index.rdf"), im);
        
        Collection<Resource> c = new HashSet<Resource>();
        
        lf.getResourcesFor(another, c);
        assertTrue("Statements about nonexistent files should be ignored", c.isEmpty());
        
        c.clear();
        lf.getResourcesFor(other, c);
        assertEquals("Statements about unmapped URIs should be unaffected",
                Collections.singleton(dummyFileUri), c);
        
        c.clear();
        lf.getResourcesFor(orig, c);
        assertTrue("Statements about mapped URIs should be mapped", c.isEmpty());
        
        c.clear();
        lf.getResourcesFor(newUri, c);
        assertEquals("Statements about mapped URIs should be mapped",
                Collections.singleton(dummyFileUri), c);
    }
    
    public void testFileStorage() throws Exception
    {
        File f = File.createTempFile(getClass().getName(), ".rdf");
        f.deleteOnExit();
        
        // Make sure that file is currently empty
        assertEquals(0, f.length());
        
        LocalFiles lf = new LocalFiles();

        lf.initLocalFiles();
        lf.save(f);
        lf.close();

        RDFXMLParser parser = new RDFXMLParser();
        StatementCollector sc = new StatementCollector();
        parser.setRDFHandler(sc);
        
        InputStream in = new FileInputStream(f);
        parser.parse(in, f.toURI().toString());

        /* No actual statements in an empty local file storage */
        assertEquals(0, sc.getStatements().size());
    }
    
    public void testSaveIndex() throws Exception
    {
        File f = File.createTempFile(getClass().getName(), ".rdf");
        f.deleteOnExit();
        
        // Make sure that file is currently empty
        assertEquals(0, f.length());
        
        LocalFiles lf = new LocalFiles();
        lf.initLocalFiles();
        
        File f1 = new File(f.getParentFile(), "first-file");
        File f2 = new File(new File(f.getParentFile(), "subdirectory"), "second-file");
        File f3 = new File(f.getParentFile(), "file-with-no-statements");
        File f4 = new File(f.getParentFile(), "file-with-statements-not-requested");

        /* Add statements about those files */

        lf.addFileEpisode(f1, new URIImpl("http://www.example.com/1#"));
        lf.addFileEpisode(f1, new URIImpl("http://another-identifier.example.com/1#"));
        lf.addFileEpisode(f2, new URIImpl("http://www.example.com/2#"));
        lf.addFileEpisode(f4, new URIImpl("http://www.example.com/4#"));
        
        Collection<File> files = Arrays.asList(f1, f2, f3);
        
        lf.saveIndex(f, files);
        
        RDFXMLParser parser = new RDFXMLParser();
        StatementCollector sc = new StatementCollector();
        parser.setRDFHandler(sc);
        
        InputStream in = new FileInputStream(f);
        parser.parse(in, "http://www.example.com/newroot/");

        /* Four statements recorded */
        assertEquals(3, sc.getStatements().size());
        
        /* Make sure they were relativised as expected */
        Set<URI> expected = new HashSet<URI>(Arrays.asList(
                new URIImpl("http://www.example.com/newroot/first-file"),
                new URIImpl("http://www.example.com/newroot/subdirectory/second-file")
        ));
        
        Set<URI> actual = new HashSet<URI>();
        
        for (Statement st : sc.getStatements()) {
            actual.add((URI) st.getSubject());
        }
        
        assertEquals("Subjects should have been relative", expected, actual);
    }
}
