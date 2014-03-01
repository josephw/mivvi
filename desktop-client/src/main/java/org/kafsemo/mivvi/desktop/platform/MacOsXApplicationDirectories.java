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

import java.io.File;

import org.kafsemo.mivvi.desktop.AppPaths;

/**
 * Paths to store data on Mac OS X, as described by
 * <a href='https://developer.apple.com/library/mac/documentation/FileManagement/Conceptual/FileSystemProgrammingGuide/MacOSXDirectories/MacOSXDirectories.html'>OS X Library Directory Details</a>.
 */
public class MacOsXApplicationDirectories implements AppPaths
{
    private final File cacheDirectory;
    private final File applicationSupportDirectory;

    public MacOsXApplicationDirectories(String bundle, String userHome)
    {
        if (bundle == null) {
            throw new IllegalArgumentException("Bundle name may not be null");
        }

        if (userHome == null) {
            throw new IllegalArgumentException("user.home may not be null");
        }

        if (!bundle.contains(".")) {
            throw new IllegalArgumentException("Bundle name must be dotted reverse domain name");
        }

        File library = new File(userHome, "Library");

        // "Contains cached data that can be regenerated as needed."
        cacheDirectory = new File(new File(library, "Caches"), bundle);

        // "Contains all app-specific data and support files. These are the files that your
        //  app creates and manages on behalf of the user and can include files that contain
        //  user data."
        applicationSupportDirectory = new File(new File(library, "Application Support"), bundle);
    }

    public MacOsXApplicationDirectories(String bundle)
    {
        this(bundle, System.getProperty("user.home"));
    }

    @Override
    public File getCacheDirectory()
    {
        return cacheDirectory;
    }

    /**
     * Uses <code>Application Support</code>. (<code>Preferences</code> says "You should never
     * create files in this directory yourself.")
     */
    @Override
    public File getConfigDirectory()
    {
        return applicationSupportDirectory;
    }

    /**
     * Uses <code>~/Library/Application Support</code>. Apple documentation now says
     * this "can include files that contain user data."
     */
    @Override
    public File getDataDirectory()
    {
        return applicationSupportDirectory;
    }
}
