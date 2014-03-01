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
import java.io.IOException;

import org.kafsemo.mivvi.desktop.AppPaths;

/**
 * Application state locations for Windows. Uses the %AppData%
 * and %LocalAppData% variables with an app-specific subdirectory.
 * If, on Windows XP, %LocalAppData% is unavailable, fall back to
 * %AppData%.
 * 
 * @author joe
 */
public class WindowsApplicationDirectories implements AppPaths
{
    private static final String APP_DATA = "AppData";
    private static final String LOCAL_APP_DATA = "LocalAppData";
    
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

    private File getAppData() throws IOException
    {
        String d = getenv(APP_DATA);
        if (d == null) {
            throw new IOException("Missing environment variable: " + APP_DATA);
        }
        return new File(d);
    }
    
    private File getLocalAppDataIfAvailable() throws IOException
    {
        String d = getenv(LOCAL_APP_DATA);
        if (d != null) {
            return new File(d);
        } else {
            return getAppData();
        }
    }
    
    private File basedDir(File base, String appName) throws IOException
    {
        return new File(base, appName);
    }

    @Override
    public File getCacheDirectory() throws IOException
    {
        if (cacheDirectory == null) {
            cacheDirectory = basedDir(getLocalAppDataIfAvailable(), app);
        }
        return cacheDirectory;
    }

    @Override
    public File getConfigDirectory() throws IOException
    {
        if (configDirectory == null) {
            configDirectory = basedDir(getAppData(), app);
        }
        return configDirectory;
    }

    @Override
    public File getDataDirectory() throws IOException
    {
        if (dataDirectory == null) {
            dataDirectory = basedDir(getAppData(), app);
        }
        return dataDirectory;
    }
}
