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

package org.kafsemo.mivvi.sesame.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.kafsemo.mivvi.sesame.JarRDFXMLParser;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

/**
 * Verify reading RDF from inside jars.
 *
 * @author Joseph
 */
public class TestJarRDFXMLParser
{
    /**
     * Ensure that relative paths in RDF from a jar: are resolved correctly.
     */
    @Test
    public void testLoading() throws Exception
    {
        URL zipfileUrl = TestJarRDFXMLParser.class.getResource("sample-data.zip");

        assertNotNull("The sample-data.zip file must be present for this test", zipfileUrl);

        String url = "jar:" + zipfileUrl + "!/index.rdf";

        RDFParser parser;
        parser = new JarRDFXMLParser();

        StatementCollector sc = new StatementCollector();
        parser.setRDFHandler(sc);

        InputStream in = new URL(url).openStream();

        parser.parse(in, url);

        Collection<Statement> stmts = sc.getStatements();

        assertEquals("There should be exactly one statement in index.rdf", 1, stmts.size());

        Statement stmt = stmts.iterator().next();

        Resource res = (Resource) stmt.getObject();

        String resourceUrl = res.stringValue();

        assertThat(resourceUrl, CoreMatchers.startsWith("jar:" + zipfileUrl + "!"));

        URL javaUrl = new URL(resourceUrl);
        assertEquals("jar", javaUrl.getProtocol());

        InputStream uc = javaUrl.openStream();
        assertEquals("The resource stream should be empty", -1, uc.read());
        uc.close();
    }
}
