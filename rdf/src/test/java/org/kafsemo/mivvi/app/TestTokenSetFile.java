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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import junit.framework.TestCase;

public class TestTokenSetFile extends TestCase
{
    File tf;

    public void setUp() throws IOException
    {
        tf = File.createTempFile(getClass().getName(), "tokens");
        tf.deleteOnExit();
    }

    public void tearDown()
    {
        tf = null;
    }

    public void testLoadTokensEmpty() throws IOException
    {
        TokenSetFile tsf = new TokenSetFile(tf);

        assertTrue(tsf.load());

        assertTrue(tsf.isEmpty());
    }

    public void testLoadTokensDuplicate() throws IOException
    {
        FileWriter fw = new FileWriter(tf);
        fw.write("Test\nTest2\nTest\n");
        fw.close();

        TokenSetFile tsf = new TokenSetFile(tf);

        assertTrue(tsf.load());

        assertEquals(2, tsf.getTokens().size());
    }

    public void testSaveTokens() throws IOException
    {
        TokenSetFile tsf = new TokenSetFile(tf);

        tsf.add("TestB");
        tsf.add("TestC");
        tsf.add("TestA");

        tsf.save();

        BufferedReader br = new BufferedReader(new FileReader(tf));

        assertEquals("TestA", br.readLine());
        assertEquals("TestB", br.readLine());
        assertEquals("TestC", br.readLine());
        assertNull(br.readLine());

        br.close();
    }

    public void testSaveReloadUriSetWithBlankNodes() throws IOException
    {
        UriSetFile usf = new UriSetFile(tf);

        ValueFactory vf = SimpleValueFactory.getInstance();

        Resource a = vf.createIRI("http://www.example.com/"),
            b = vf.createBNode("blank-identifier"),
            c = vf.createIRI("uri:null");

        usf.add(a);
        usf.add(b);
        usf.add(c);

        usf.save();

        assertTrue(usf.load());

        assertTrue(usf.remove(a));
        assertFalse(usf.contains(b));
        assertTrue(usf.remove(c));

        assertTrue(usf.isEmpty());
    }
}
