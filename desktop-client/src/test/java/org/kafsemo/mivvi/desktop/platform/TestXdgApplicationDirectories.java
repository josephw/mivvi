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

package org.kafsemo.mivvi.desktop.platform;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.kafsemo.mivvi.desktop.AppPaths;

/**
 * Tests for {@link XdgApplicationDirectories} to verify defaults
 * and results from environment variables.
 * 
 * @author joe
 */
public class TestXdgApplicationDirectories
{
    @Test
    public void defaultsToCorrectPaths() throws Exception
    {
        AppPaths xag = stubbedXdgAppDirs("test", "home", Collections.<String,String>emptyMap());
        
        assertEquals(new File("home/.cache/test"), xag.getCacheDirectory());
        assertEquals(new File("home/.config/test"), xag.getConfigDirectory());
        assertEquals(new File("home/.local/share/test"), xag.getDataDirectory());
    }
    
    @Test
    public void acceptsDefinedEnvironmentVariables() throws Exception
    {
        final Map<String, String> m = new HashMap<String, String>();

        m.put("XDG_CACHE_HOME", "test-cache-directory");
        m.put("XDG_CONFIG_HOME", "test-config-directory");
        m.put("XDG_DATA_HOME", "test-data-directory");

        AppPaths xag = stubbedXdgAppDirs("test", "home", m);
        
        assertEquals(new File("test-cache-directory/test"), xag.getCacheDirectory());
        assertEquals(new File("test-config-directory/test"), xag.getConfigDirectory());
        assertEquals(new File("test-data-directory/test"), xag.getDataDirectory());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void failsWithNullAppName()
    {
        new XdgApplicationDirectories(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void failsWithNullUserHome()
    {
        new XdgApplicationDirectories("app", null);
    }
    
    private static AppPaths stubbedXdgAppDirs(String app, String userHome, final Map<String, String> env)
    {
        return new XdgApplicationDirectories(app, userHome) {
            @Override
            String getenv(String n)
            {
                return env.get(n);
            }
        };
    }
}
