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

package org.kafsemo.mivvi.util;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A comparator for natural ordering of strings including decimal numbers.
 * Specifically, '10' will sort after '9'.
 * 
 * @author Joseph Walton
 */
public class NumericStringComparator implements Comparator<String>
{
    private static final Pattern p = Pattern.compile("\\d+");
    
    /**
     * Compare a pair of Strings by segments, comparing sequences of digits
     * numerically and and other sequences lexically.
     */
    public int compare(String a, String b)
    {
        Matcher ma = p.matcher(a),
            mb = p.matcher(b);

        try {
            while (ma.find() && mb.find()) {
                String beforeA = a.substring(0, ma.start()),
                    beforeB = b.substring(0, mb.start());
                
                int beforeRes = beforeA.compareTo(beforeB);
                
                if (beforeRes != 0) {
                    return beforeRes;
                }
                
                int ia = Integer.parseInt(ma.group()),
                    ib = Integer.parseInt(mb.group());
                
                if (ia < ib) {
                    return -1;
                } else if (ia > ib) {
                    return 1;
                }
                
                a = a.substring(ma.end());
                ma.reset(a);
                b = b.substring(mb.end());
                mb.reset(b);
            }
        } catch (NumberFormatException nfe) {
            // Silent fallthrough
        }

        return a.compareTo(b);
    }
}
