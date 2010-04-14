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

package org.kafsemo.mivvi.rss;

import java.io.File;

import junit.framework.TestCase;

import org.kafsemo.mivvi.rss.Annotator;

public class TestAnnotator extends TestCase
{
    public void testAnnotatedFilename()
    {
        File f = new File("sample");
        assertEquals("An unsuffixed file should have a string appended",
                new File("sample-mivvi-annotated"),
                Annotator.annotatedName(f));

        f = new File("sample.xml");
        assertEquals("A file with a suffix should have a change before the suffix",
                new File("sample-mivvi-annotated.xml"),
                Annotator.annotatedName(f));
    }
}
