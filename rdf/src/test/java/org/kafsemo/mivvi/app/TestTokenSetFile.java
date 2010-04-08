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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.kafsemo.mivvi.app.TokenSetFile;
import org.kafsemo.mivvi.app.UriSetFile;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.URIImpl;

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
    }
    
    public void testSaveReloadUriSetWithBlankNodes() throws IOException
    {
        UriSetFile usf = new UriSetFile(tf);

        Resource a = new URIImpl("http://www.example.com/"),
            b = new BNodeImpl("blank-identifier"),
            c = new URIImpl("uri:null");

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