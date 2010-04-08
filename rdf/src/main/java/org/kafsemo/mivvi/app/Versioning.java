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

package org.kafsemo.mivvi.app;

import java.text.ParseException;

public class Versioning
{
    private final Version version;

    private Versioning()
    {
        Package p = getClass().getPackage();
        if (p != null) {
            Version v = null;
            String s = p.getImplementationVersion();
            if (s != null) {
                try {
                    v = Version.parse(s);
                } catch (ParseException pe) {
                    System.err.println("Unable to parse version: " + pe);
                }
            }
            version = v;
        } else {
            version = null;
        }
    }
    
    public Version getVersion()
    {
        return (version != null) ? version : Version.ZERO;
    }
    
    private static final Versioning v = new Versioning();
    
    public static Versioning getInstance()
    {
        return v;
    }
}
