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
 * An implementation of {@link AppPaths} using the
 * <a href="http://standards.freedesktop.org/basedir-spec/latest/ar01s03.html">FreeDesktop XDG spec</a>.
 * 
 * @author joe
 */
public class XdgApplicationDirectories implements AppPaths
{
    private final File cacheDirectory;
    private final File configDirectory;
    private final File dataDirectory;
    
    public XdgApplicationDirectories(String app)
    {
        this(app, System.getProperty("user.home"));
    }
    
    public XdgApplicationDirectories(String app, String userHome)
    {
        if (app == null) {
            throw new IllegalArgumentException("Application name may not be null");
        }
        
        if (userHome == null) {
            throw new IllegalArgumentException("user.home may not be null");
        }
        
        cacheDirectory = envOrHomeDir("XDG_CACHE_HOME", userHome, ".cache", app);
        configDirectory = envOrHomeDir("XDG_CONFIG_HOME", userHome, ".config", app);
        dataDirectory = envOrHomeDir("XDG_DATA_HOME", userHome, ".local/share", app);
    }

    /**
     * Wraps {@link System#getenv(String)}. Overridable for testing.
     * 
     * @param n
     * @return
     */
    String getenv(String n)
    {
        return System.getenv(n);
    }

    private File envOrHomeDir(String varName, String home, String subdir, String app)
    {
        File base;
        String s = getenv(varName);
        if (s != null) {
            base = new File(s);
        } else {
            base = new File(home, subdir);
        }
        
        return new File(base, app);
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

    @Override
    public File getDataDirectory()
    {
        return dataDirectory;
    }
}
