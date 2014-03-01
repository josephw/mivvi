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

package org.kafsemo.mivvi.recognise;



/**
 * @author joe
 * @param <T> the underlying identifier type
 */
public class Matching<T>
{
//    final String origString;
    public final int start, end;
    public final String realString;
    public final T matchedResource;

    Matching(String orig, int s, int n, String realString, T matchedResource)
    {
//        this.origString = orig;
        this.start = s;
        this.end = n;
        this.realString = realString;
        this.matchedResource = matchedResource;
    }

    public int matchLength()
    {
        return end - start;
    }

    public T getResource()
    {
        return matchedResource;
    }
}
