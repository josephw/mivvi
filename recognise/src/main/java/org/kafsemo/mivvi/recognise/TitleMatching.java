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

import java.util.Comparator;
import java.util.Set;

/**
 * @author joe
 */
public class TitleMatching<T> extends Matching<T>
{
    public final boolean isExact; // Is the error weight within tolerances?
    public final boolean isTitle; // Is this a match against the title, or a description?
    public final float weight; // Weighted distance between this and the real string

    TitleMatching(String orig, int s, int n, String realString, T matchedResource,
                  boolean isExact, boolean isTitle, float weight)
    {
        super(orig, s, n, realString, matchedResource);

        this.isExact = isExact;
        this.isTitle = isTitle;
        this.weight = weight;
    }

    static Comparator<TitleMatching<?>> MATCHING_COMPARATOR = new MatchPriorityComparator();

    /**
     * A comparator to put matches in order, better ones first. If a match
     * is closer, or for a title rather than a description, it will
     * be placed earlier.
     * 
     * @author joe
     *
     */
    static class MatchPriorityComparator implements Comparator<TitleMatching<?>>
    {
        MatchPriorityComparator()
        {
            this(null);
        }

        private final Set<?> expectedUris;

        /**
         * A comparator that gives priority to any of the expected identifiers
         * provided.
         * 
         * @param expectedUris
         */
        MatchPriorityComparator(Set<?> expectedUris)
        {
            this.expectedUris = expectedUris;
        }

        public int compare(TitleMatching<?> m1, TitleMatching<?> m2)
        {
            /* Firstly, give priority to very close matches */
            if (m1.isExact != m2.isExact) {
                return (m1.isExact ? -1 : 1);
            }

            /* If there is an expected URI, give it priority */
            if (expectedUris != null) {
                boolean em1 = expectedUris.contains(m1.matchedResource);
                boolean em2 = expectedUris.contains(m2.matchedResource);

                if (em1 != em2) {
                    return (em1 ? -1 : 1);
                }
            }

            /* Secondly, matches on titles (rather than descriptions) */
            if (m1.isTitle != m2.isTitle) {
                return (m1.isTitle ? -1 : 1);
            }

            /* Now go by weighted edit distance */
            if (m1.weight < m2.weight) {
                return -1;
            } else if (m1.weight > m2.weight) {
                return 1;
            }

            /* Prefer longer matches to shorter */
            if (m1.matchLength() > m2.matchLength()) {
                return -1;
            } else if (m1.matchLength() < m2.matchLength()) {
                return 1;
            }

            /* Everything else is equivalent */
            return 0;
        }
    };

    public static Comparator<TitleMatching<?>> episodePriorityComparator(Set<?> matchUris)
    {
        return new MatchPriorityComparator(matchUris);
    }
}
