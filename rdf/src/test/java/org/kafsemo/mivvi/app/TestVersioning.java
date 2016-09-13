/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright © 2004-2016 Joseph Walton
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.ParseException;

import org.junit.Test;

public class TestVersioning
{
    @Test
    public void nullVersionsProvideDefaultString()
    {
        Versioning v = new Versioning(null);
        assertNull(v.getVersionString());
        assertEquals(Version.ZERO, v.getVersion());
        assertEquals("0.0", v.toString());
    }
    
    @Test
    public void unparseableVersionsReturnedAsStrings()
    {
        Versioning v = new Versioning("SNAPSHOT");
        assertEquals("SNAPSHOT", v.getVersionString());
        assertEquals(Version.ZERO, v.getVersion());
        assertEquals("SNAPSHOT", v.toString());
    }
    
    @Test
    public void realVersionsPassedBackAsNumbers() throws ParseException
    {
        Versioning v = new Versioning("0.9.09");
        assertEquals("0.9.09", v.getVersionString());
        assertEquals(Version.parse("0.9.9"), v.getVersion());
        assertEquals("0.9.9", v.toString());
    }
    
    @Test
    public void constructableFromClass()
    {
        Versioning v = Versioning.from(getClass());
        assertNotNull(v.getVersion());
        assertNotNull(v.toString());
    }
}
