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

import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.kafsemo.mivvi.desktop.AppPaths;
import org.kafsemo.mivvi.desktop.SingleDotDirectoryAppPaths;

public class LaunchProfile
{
    private static final String APP_NAME = "mivvi";
    private static final String APP_BUNDLE_NAME = "org.kafsemo.Mivvi";
    private final String name;
    private final AppPaths paths;
    
    private LaunchProfile(String profileName, AppPaths appPaths)
    {
        this.name = profileName;
        this.paths = appPaths;
    }
    
    public LaunchProfile()
    {
        this("Default", new SingleDotDirectoryAppPaths());
    }

    /**
     * @param osName the system property os.name
     * @return
     */
    public static LaunchProfile forOsName(String osName) throws IOException
    {
        if (osName.equals("Linux")) {
            return forLinux();
        } else if (osName.toLowerCase().startsWith("windows")) {
            return forWindows();
        } else if (osName.toLowerCase().startsWith("mac os x")) {
            return forMacOsX();
        } else {
            return new LaunchProfile();
        }
    }

    public String getName()
    {
        return name;
    }

    public AppPaths getAppPaths()
    {
        return paths;
    }

    public void initialiseLookAndFeel() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
    {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        
        /* Use Nimbus if available */
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException cnfe) {
            // Fall back to the default
        }
    }
    
    public static LaunchProfile forWindows() throws IOException
    {
        return new LaunchProfile("Windows", new WindowsApplicationDirectories(APP_NAME));
    }

    public static LaunchProfile forLinux()
    {
        /* The native L+F crashes for me; avoid it for now */
        
        return new LaunchProfile("Linux", new XdgApplicationDirectories(APP_NAME)){
            public void initialiseLookAndFeel() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
            {
                /* Use Nimbus if available */
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (ClassNotFoundException cnfe) {
                    // Fall back to the default
                }
            }
        };
    }

    public static LaunchProfile forMacOsX() throws IOException
    {
        return new LaunchProfile("Mac OS X", new MacOsXApplicationDirectories(APP_BUNDLE_NAME)) {
            public void initialiseLookAndFeel()
            {
                // Stick with defaults
            }
        };
    }
}
