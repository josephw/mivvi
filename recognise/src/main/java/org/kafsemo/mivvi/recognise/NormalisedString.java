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

package org.kafsemo.mivvi.recognise;

/**
 * @author joe
 */
public class NormalisedString
{
    private final String s;
    private final int[] origPos;

    private final String original;

    public NormalisedString(String s)
    {
        this.original = s;
//        this.s = process(s); //s.replaceAll("\\W+", " ").toLowerCase().trim();

        char[] ca = s.toCharArray();
        StringBuffer sb = new StringBuffer(ca.length);
        int[] origPos = new int[ca.length * 3 + 1];

        int lastRealChar = -1;

        boolean needsSpace = false;

        int i = 0;
        while (i < ca.length) {
            char c = ca[i];

            if (Character.isLetterOrDigit(c) || (c == '&')) {
                if (needsSpace) {
                    if (sb.length() > 0) {
//                        origPos[sb.length()] = i - 1;
                        sb.append(' ');
                    }
                    needsSpace = false;
                }
                if (c == '&') {
                    for (int x = 0; x < 3; x++)
                        origPos[sb.length() + x] = i;
                    sb.append("and");
                } else {
                    origPos[sb.length()] = i;
                    sb.append(Character.toLowerCase(c));
                }
                lastRealChar = i;
            } else if (!needsSpace) {
                origPos[sb.length()] = i;
                needsSpace = true;
            }
            i++;
        }

        origPos[sb.length()] = lastRealChar + 1;

        this.s = sb.toString();
        this.origPos = origPos;
    }

    public int getOriginalPosition(int i)
    {
        return origPos[i];
    }

    public String toString()
    {
        return s;
    }

    public int hashCode()
    {
        return s.hashCode();
    }

    public boolean equals(Object o)
    {
        return ((o instanceof NormalisedString)
                && s.equals(((NormalisedString) o).s));
    }

    public String getOriginal()
    {
        return original;
    }
}
