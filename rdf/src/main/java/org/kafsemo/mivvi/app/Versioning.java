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
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.app;

import java.text.ParseException;

/**
 * A wrapper around a version string that treats valid release
 * versions specially.
 * 
 * @author joe
 */
public class Versioning
{
    private final String s;
    private final Version v;
    
    public Versioning(String s)
    {
        this.s = s;
        this.v = toVersion(s);
    }

    private static Version toVersion(String s)
    {
        if (s != null) {
            try {
                return Version.parse(s);
            } catch (ParseException pe) {
                return null;
            }
        } else {
            return null;
        }
    }
    
    public static Versioning from(Class<?> c)
    {
        String s;
        
        Package p = c.getPackage();
        if (p != null) {
            s = p.getImplementationVersion();
        } else {
            s = null;
        }
        
        return new Versioning(s);
    }

    public String getVersionString()
    {
        return s;
    }

    public Version getVersion()
    {
        if (v != null) {
            return v;
        } else {
            return Version.ZERO;
        }
    }
    
    public String toString()
    {
        if (v != null) {
            return v.toString();
        } else if (s != null) {
            return s;
        } else {
            return Version.ZERO.toString();
        }
    }
}
