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

package org.kafsemo.mivvi.desktop.platform;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.kafsemo.mivvi.desktop.AppPaths;

/**
 * Tests for {@link MacOsXApplicationDirectories}.
 * 
 * @author joe
 */
public class TestMacOsXApplicationDirectories
{
    @Test(expected = IllegalArgumentException.class)
    public void failsWithNullAppName()
        throws Exception
    {
        new MacOsXApplicationDirectories(null, "home");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void failsWithNullUserHome()
        throws Exception
    {
        new MacOsXApplicationDirectories("com.example.Test", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void failsWithUndottedName()
        throws Exception
    {
        new MacOsXApplicationDirectories("test", "home");
    }
    
    @Test
    public void directoriesAsExpected() throws IOException
    {
        AppPaths ap = new MacOsXApplicationDirectories("com.example.Test", "home");
        
        assertEquals(new File("home/Library/Caches/com.example.Test"), ap.getCacheDirectory());
        assertEquals(new File("home/Library/Application Support/com.example.Test"), ap.getDataDirectory());
        assertEquals(new File("home/Library/Preferences/com.example.Test"), ap.getConfigDirectory());
    }
}
