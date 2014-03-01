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
 */
public class StringUtil
{
    /**
     * <p>Calculate the edit distance between two strings.
     * This will be zero for identical strings, up to the length
     * of the longer string for completely distinct strings.</p>
     * <p>Additionally,
     * identify the largest suffix addition in an optimal transformation
     * from a to b, and the edit distance if this suffix were omitted
     * from the target string.</p>
     *
     * @param a
     * @param b
     */
    public static LevenshteinResult levenshteinDistance(String a, String b)
    {
        char[] ca = a.toCharArray(), cb = b.toCharArray();

        int[] lastRow = new int[cb.length + 1],
            thisRow = new int[cb.length + 1];

        for (int i = 0; i < lastRow.length; i++) {
            lastRow[i] = i;
        }

        int lastDecrease = 0;

        for (int r = 1; r < ca.length + 1; r++) {
            thisRow[0] = r;
            for (int c = 1; c < thisRow.length; c++) {
                int costInsertion = thisRow[c - 1] + 1;
                int costDeletion = lastRow[c] + 1;
                int costSubstitution = lastRow[c - 1] + ((ca[r - 1] == cb[c - 1]) ? 0 : 1);

                thisRow[c] = Math.min(costInsertion, Math.min(costDeletion, costSubstitution));

                /* Record last decrease in cost */
                if (thisRow[c] < thisRow[c - 1]) {
                    lastDecrease = c;
                }
            }

            int[] t = lastRow;
            lastRow = thisRow;
            thisRow = t;
        }

        return new LevenshteinResult(
        		lastRow[lastRow.length - 1],
        		lastDecrease,
        		lastRow[lastDecrease]);
    }

    public static class LevenshteinResult
    {
        /**
         * The edit distance between two strings.
         */
        public final int distance;

        /**
         * A suffix which may be omitted from the target string.
         */
        public final int lengthWithoutSuffix;

        /**
         * The edit distance between the first string and
         * the target string with the suffix omitted.
         */
        public final int distanceWithoutSuffix;
        
        LevenshteinResult(int dist, int lenWithout, int distWithout)
        {
        	this.distance = dist;
        	this.lengthWithoutSuffix = lenWithout;
        	this.distanceWithoutSuffix = distWithout;
		}
    }

    public static float maxDistance(String a)
    {
        return a.length() / 5.0f;
    }

    public static float suggestionFactor(float maxDistance)
    {
        return (maxDistance * 5.0f) / 2.0f;
    }

    public static float weight(float distance, int length)
    {
        return distance / ((float) length + 1);
    }
}
