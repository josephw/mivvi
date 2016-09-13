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

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import junit.framework.TestCase;

/**
 * @author joe
 */
public class TestFileUtil extends TestCase
{
    private static String asNative(String s)
    {
        StringBuffer sb = new StringBuffer(s.length());

        char[] ca = s.toCharArray();
        for (int i = 0 ; i < ca.length ; i++) {
            if (ca[i] == '/')
                sb.append(File.separatorChar);
            else
                sb.append(ca[i]);
        }

        return sb.toString();
    }

    private static boolean contains(String a, String b)
    {
        return FileUtil.contains(new File(asNative(a)), new File(asNative(b)));
    }

    public void testContains()
    {
        assertFalse(contains("", ""));
        assertFalse(contains("a", "ab"));
        assertTrue(contains("", "x"));

        assertFalse(contains("a", ""));
        assertFalse(contains("a", "a"));
        assertFalse(contains("a", "a/"));
        assertFalse(contains("a", "x"));
        assertFalse(contains("a/b", "a/x"));
        assertFalse(contains("a/b", "a/"));

        assertTrue(contains("", "x"));
        assertFalse(contains("a", "x"));
        assertTrue(contains("a", "a/b"));
        assertTrue(contains("a", "a/b/c"));
        assertTrue(contains("a/", "a/b/c"));
        assertTrue(contains("a/b", "a/b/c"));
    }

    public void testFileFrom()
    {
        ValueFactory vf = SimpleValueFactory.getInstance();

        Value v = vf.createLiteral("Just a string");
        assertNull(FileUtil.fileFrom(v));

        v = vf.createIRI("http://www.example.com/");
        assertNull(FileUtil.fileFrom(v));

        File d = new File("").getAbsoluteFile();

        v = vf.createIRI(d.toURI().toString());
        assertEquals(d, FileUtil.fileFrom(v));
    }
}
