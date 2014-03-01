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

package org.kafsemo.mivvi.desktop;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.kafsemo.mivvi.desktop.MivviDataStartup;

import junit.framework.TestCase;

public class TestMivviDataStartup extends TestCase
{
    public void testRemoveOverridenFiles() throws Exception
    {
        List<URL> l = Arrays.asList(
                new URL("file:///a/test2.rdf"),
                new URL("file:///b/test.rdf"),
                new URL("file:///c/test2.rdf"),
                new URL("file:///d/test.rdf"),
                new URL("file:///e/test.rdf")
        );
        
        List<URL> l2 = MivviDataStartup.removeOverriddenFiles(l);

        List<URL> expected = Arrays.asList(
                new URL("file:///c/test2.rdf"),
                new URL("file:///e/test.rdf")
        );
        
        assertEquals(expected, l2);
    }
}
