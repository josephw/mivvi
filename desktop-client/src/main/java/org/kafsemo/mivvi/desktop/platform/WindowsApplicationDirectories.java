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
import java.io.IOException;

import org.kafsemo.mivvi.desktop.AppPaths;

/**
 * Application state locations for Windows. Uses the %AppData%
 * and %LocalAppData% variables with an app-specific subdirectory.
 * 
 * @author joe
 */
public class WindowsApplicationDirectories implements AppPaths
{
    private final String app;
    private File cacheDirectory;
    private File configDirectory;
    private File dataDirectory;
    
    public WindowsApplicationDirectories(String appName) throws IOException
    {
        if (appName == null) {
            throw new IllegalArgumentException("Application name may not be null");
        }
    
        this.app = appName;
    }

    String getenv(String n)
    {
        return System.getenv(n);
    }
    
    private File envBasedDir(String var, String appName) throws IOException
    {
        String d = getenv(var);
        if (d == null) {
            throw new IOException("Missing environment variable: " + var);
        }
        
        return new File(d, appName);
    }

    @Override
    public File getCacheDirectory() throws IOException
    {
        if (cacheDirectory == null) {
            cacheDirectory = envBasedDir("LocalAppData", app);
        }
        return cacheDirectory;
    }

    @Override
    public File getConfigDirectory() throws IOException
    {
        if (configDirectory == null) {
            configDirectory = envBasedDir("AppData", app);
        }
        return configDirectory;
    }

    @Override
    public File getDataDirectory() throws IOException
    {
        if (dataDirectory == null) {
            dataDirectory = envBasedDir("AppData", app);
        }
        return dataDirectory;
    }
}
