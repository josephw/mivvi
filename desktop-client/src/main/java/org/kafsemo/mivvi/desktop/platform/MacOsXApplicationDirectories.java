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

import java.io.File;

import org.kafsemo.mivvi.desktop.AppPaths;

/**
 * Paths to store data on Mac OS X, as described by
 * <a href='http://developer.apple.com/mac/library/documentation/MacOSX/Conceptual/BPFileSystem/Articles/LibraryDirectory.html'>this document</a>.
 * 
 * @author joe
 */
public class MacOsXApplicationDirectories implements AppPaths
{
    private final File cacheDirectory;
    private final File configDirectory;
    private final File dataDirectory;

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
        
        cacheDirectory = new File(new File(library, "Caches"), bundle);
        configDirectory = new File(new File(library, "Preferences"), bundle);
        dataDirectory = new File(new File(library, "Application Support"), bundle);
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

    @Override
    public File getConfigDirectory()
    {
        return configDirectory;
    }

    /**
     * Uses ~/Library/Application Support. Apple documentation says
     * "This directory should never contain any kind of user data," but
     * at least Firefox and Chrome use it. Do what they do, not what
     * Apple says.
     */
    @Override
    public File getDataDirectory()
    {
        return dataDirectory;
    }
}
