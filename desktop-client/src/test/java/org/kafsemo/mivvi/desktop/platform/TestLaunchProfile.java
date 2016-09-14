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
 * License along with this library.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.desktop.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestLaunchProfile
{
    @Test
    public void defaultLaunchProfileForUnknownOs() throws Exception
    {
        LaunchProfile lp = LaunchProfile.forOsName("unknown os");
        assertNotNull(lp);
        assertEquals(LaunchProfile.class, lp.getClass());
        assertEquals("Default", lp.getName());
    }
    
    @Test
    public void defaultLaunchProfileForNullName() throws Exception
    {
        LaunchProfile lp = LaunchProfile.forOsName("unknown os");
        assertNotNull(lp);
        assertEquals(LaunchProfile.class, lp.getClass());
        assertEquals("Default", lp.getName());
    }
    
    @Test
    public void linuxRecognisedByOsName() throws Exception
    {
        LaunchProfile lp = LaunchProfile.forOsName("Linux");
        assertEquals("Linux", lp.getName());
    }
    
    @Test
    public void linuxUsesXdgApplicationDirectories() throws Exception
    {
        LaunchProfile lp = LaunchProfile.forLinux();
        assertEquals(
                XdgApplicationDirectories.class,
                lp.getAppPaths().getClass());
    }
    
    @Test
    public void windowsRecognisedByOsName() throws Exception
    {
        assertEquals("Windows", LaunchProfile.forOsName("Windows").getName());
    }
    
    @Test
    public void windowsUsesWindowsApplicationDirectories() throws Exception
    {
        LaunchProfile lp = LaunchProfile.forWindows();
        assertEquals(
                WindowsApplicationDirectories.class,
                lp.getAppPaths().getClass());
    }
    
    @Test
    public void macOsXRecognisedByOsName() throws Exception
    {
        assertEquals("Mac OS X", LaunchProfile.forOsName("Mac OS X").getName());
    }
}
