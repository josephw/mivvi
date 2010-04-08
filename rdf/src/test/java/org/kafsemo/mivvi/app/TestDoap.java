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

package org.kafsemo.mivvi.app;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import junit.framework.TestCase;

import org.kafsemo.mivvi.app.Doap;
import org.kafsemo.mivvi.app.Version;
import org.kafsemo.mivvi.rdf.RdfUtil;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

public class TestDoap extends TestCase
{
    private static final URIImpl MIVVI_DESKTOP_CLIENT = new URIImpl("http://mivvi.net/code/#desktop-client");

    public void testGetLatestNoVersions() throws RepositoryException
    {
        Repository rep = new SailRepository(new MemoryStore());
        rep.initialize();
        RepositoryConnection cn = rep.getConnection();
        
        assertNull(Doap.getLatestAvailableVersion(cn));
    }
    
    public void testGetDownloadPage() throws RepositoryException
    {
        Repository rep = new SailRepository(new MemoryStore());
        rep.initialize();
        RepositoryConnection cn = rep.getConnection();
        
        assertNull(Doap.getDownloadPage(cn));
        
        cn.add(MIVVI_DESKTOP_CLIENT,
                RdfUtil.Doap.downloadPage,
                new URIImpl("http://www.example.com/"));
        
        assertEquals("http://www.example.com/",
                Doap.getDownloadPage(cn));
    }
    
    public void testGetLatestVersion() throws ParseException, RepositoryException
    {
        Repository rep = new SailRepository(new MemoryStore());
        rep.initialize();
        RepositoryConnection cn = rep.getConnection();

        String[] vsa = {"0.2", "0.2.2", "0.2.1"};
        
        for (int i = 0 ; i < vsa.length ; i++) {
            Resource v = new BNodeImpl("blank" + i);
            
            cn.add(v, RdfUtil.Doap.revision, new LiteralImpl(vsa[i]));
            
            cn.add(MIVVI_DESKTOP_CLIENT, RdfUtil.Doap.release, v);
        }
        
        assertEquals(Version.parse("0.2.2"), Doap.getLatestAvailableVersion(cn));
    }
    
    public void testDoapFromFile() throws RepositoryException, RDFParseException, IOException, ParseException
    {
        Repository rep = new SailRepository(new MemoryStore());
        rep.initialize();
        RepositoryConnection cn = rep.getConnection();

        InputStream f = getClass().getResourceAsStream("mivvi-doap.rdf");
        cn.add(f, "file:///", RDFFormat.RDFXML);

        Doap d = Doap.check(cn);
        
        assertEquals(Version.parse("0.2.1"), d.getLatestAvailableVersion());
        assertEquals("http://mivvi.net/code/", d.getDownloadPage());
    }
}
