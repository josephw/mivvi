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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.kafsemo.mivvi.desktop.AppPaths;

/**
 * Tests for {@link WindowsApplicationDirectories}, to ensure
 * it asserts the environment variables it needs and returns
 * the expected results when they are present.
 *
 * @author joe
 */
public class TestWindowsApplicationDirectories
{
    @Test(expected = IOException.class)
    public void failsWithoutEnvironmentVariables()
        throws Exception
    {
        AppPaths ap = new WindowsApplicationDirectories("test"){
            @Override
            String getenv(String n)
            {
                return null;
            }
        };
        ap.getDataDirectory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsWithNullAppName()
        throws Exception
    {
        new WindowsApplicationDirectories(null);
    }

    @Test
    public void expectedDirectoriesBasedOnVariables()
        throws Exception
    {
        final Map<String, String> m = new HashMap<String, String>();
        m.put("APPDATA", "app-data");
        m.put("LOCALAPPDATA", "local-app-data");

        AppPaths ap = new WindowsApplicationDirectories("test"){
            @Override
            String getenv(String n)
            {
                return m.get(n.toUpperCase());
            }
        };

        assertEquals(new File("local-app-data/test"), ap.getCacheDirectory());
        assertEquals(new File("app-data/test"), ap.getConfigDirectory());
        assertEquals(new File("app-data/test"), ap.getDataDirectory());
    }

    /**
     * Windows XP lacks the <code>%LocalAppData%</code> variable. Ensure that
     * it works with only %AppData%.
     *
     * @throws Exception
     */
    @Test
    public void setsDirectoriesWithOnlyAppDataOnXp()
        throws Exception
    {
        final Map<String, String> m = new HashMap<String, String>();
        m.put("APPDATA", "app-data");

        AppPaths ap = new WindowsApplicationDirectories("test"){
            @Override
            String getenv(String n)
            {
                return m.get(n.toUpperCase());
            }
        };

        assertEquals(new File("app-data/test"), ap.getCacheDirectory());
        assertEquals(new File("app-data/test"), ap.getConfigDirectory());
        assertEquals(new File("app-data/test"), ap.getDataDirectory());
    }
}
