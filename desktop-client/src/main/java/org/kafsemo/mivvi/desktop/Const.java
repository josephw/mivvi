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

package org.kafsemo.mivvi.desktop;

import java.io.File;

public class Const
{
    public static final String BASE;
    
    static {
        File m = new File(System.getProperty("user.home"), ".mivvi");
        if (!m.exists())
            m.mkdirs();
        BASE = m.getPath();
    }
    
    private static String getPath(String name)
    {
        return new File(BASE, name).getPath();
    }

    // The repository for local resource identification
    public static final String LOCAL_RESOURCE_FILE = getPath("local-resources.rdf");
    
    // Local subscription list
    public static final String SUBSCRIPTION_FILE = getPath("subscription.uris");
    
    // MVI repository
    public static final String MVI_REPOSITORY = getPath("mvi");
}
