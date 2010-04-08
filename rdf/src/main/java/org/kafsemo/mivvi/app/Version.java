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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A release version, that knows how to convert to and from strings and
 * to compare itself to other versions.
 * 
 * @author Joseph Walton
 */
public class Version implements Comparable<Version>
{
    private static final int[] ZERO_BYTES = {0, 0};

    public static final Version ZERO = new Version(ZERO_BYTES);

    private final int[] pieces;
    
    private Version(int[] ia)
    {
        this.pieces = ia;
    }

    public static Version parse(String s) throws ParseException
    {
        List<Integer> l = new ArrayList<Integer>();

        int o = 0;
        
        int i = 0;
        
        while ((i = s.indexOf('.', o)) >= 0) {
            String vs = s.substring(o, i);

            int n;
            try {
                n = Integer.parseInt(vs);
            } catch (NumberFormatException nfe) {
                throw new ParseException("Bad number in version '" + s + "'", o);
            }
            
            if (n < 0) {
                throw new ParseException("Negative numbers not allowed: '" + s + "'", o);
            }
            l.add(Integer.valueOf(n));
            
            o = i + 1;
        }
        
        if (o >= 0) {
            if (o >= s.length()) {
                throw new ParseException("Missing number in version '" + s + "'", o);
            }
            
            String vs = s.substring(o);
            int n;
            try {
                n = Integer.parseInt(vs);
            } catch (NumberFormatException nfe) {
                throw new ParseException("Bad number in version: '" + s + "'", o);
            }
            
            if (n < 0) {
                throw new ParseException("Negative numbers not allowed: '" + s + "'", o);
            }
            l.add(Integer.valueOf(n));
            
        }

        int[] ia = new int[l.size()];
        int j = 0;
        Iterator<Integer> it = l.iterator();
        while (it.hasNext()) {
            ia[j++] = it.next().intValue();
        }

        return new Version(ia);
    }
    
    public int[] toIntArray()
    {
        int[] iac = new int[pieces.length];
        System.arraycopy(pieces, 0, iac, 0, pieces.length);
        return iac;
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0 ; i < pieces.length ; i++) {
            if (i > 0) {
                sb.append('.');
            }
            sb.append(pieces[i]);
        }
        return sb.toString();
    }
    
    public boolean equals(Object o)
    {
        return (o instanceof Version &&
                Arrays.equals(pieces, ((Version)o).pieces));
    }
    
    public int hashCode()
    {
        return Arrays.hashCode(pieces);
    }

    public int compareTo(Version b)
    {
        for (int i = 0 ; i < Math.max(pieces.length, b.pieces.length) ; i++) {
            int na = (i < pieces.length) ? pieces[i] : 0,
                nb = (i < b.pieces.length) ? b.pieces[i] : 0;
                
            if (na > nb) {
                return 1;
            } else if (na < nb) {
                return -1;
            }
        }
        return 0;
    }
}
