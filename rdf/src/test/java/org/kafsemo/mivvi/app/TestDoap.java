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
import java.io.InputStream;
import java.text.ParseException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.kafsemo.mivvi.rdf.RdfUtil;

import junit.framework.TestCase;

public class TestDoap extends TestCase
{
    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    private static final IRI MIVVI_DESKTOP_CLIENT = VF.createIRI("http://mivvi.net/code/#desktop-client");

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
                VF.createIRI("http://www.example.com/"));

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
            Resource v = VF.createBNode("blank" + i);

            cn.add(v, RdfUtil.Doap.revision, VF.createLiteral(vsa[i]));

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
