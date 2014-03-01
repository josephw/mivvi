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

package org.kafsemo.mivvi.desktop;

import java.io.File;

/**
 * An implementation of {@link AppPaths} with old-style Unix
 * behaviour - a single dotted application directory in $HOME
 * for all configuration and data.
 *
 * @author joe
 */
public class SingleDotDirectoryAppPaths implements AppPaths
{
    private final File base;

    public SingleDotDirectoryAppPaths()
    {
        File m = new File(System.getProperty("user.home"), ".mivvi");
        base = m;
    }

    @Override
    public File getCacheDirectory()
    {
        return base;
    }

    @Override
    public File getConfigDirectory()
    {
        return base;
    }

    @Override
    public File getDataDirectory()
    {
        return base;
    }
}
