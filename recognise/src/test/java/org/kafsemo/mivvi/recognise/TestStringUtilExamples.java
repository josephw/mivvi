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

import junit.framework.TestCase;

import org.kafsemo.mivvi.recognise.StringUtil;

/**
 * @author joe
 */
public class TestStringUtilExamples extends TestCase
{
    /**
     * There are a couple of real typo examples that should be picked up.
     * This case keeps track of the typical distance needed to cover
     * a real example.
     */
    public void testMaximumDistance()
    {
        String input = "stay away from my mom";

        int dist = StringUtil.levenshteinDistance(input, "get away from my mom").distance;
        assertEquals(4, dist);

        float maxDistance = StringUtil.maxDistance(input);
        assertTrue(dist + " <= " + maxDistance, dist <= maxDistance);
    }

    /**
     * A practical example of a real-world mistake that should be
     * suggested, rather than flagged as an exact match.
     */
    public void testSuggestionFactor()
    {
        String input = "t shirt of the dead";

        int dist = StringUtil.levenshteinDistance(input, "t shirt of the living dead").distance;
        assertEquals(7, dist);

        float maxDistance = StringUtil.maxDistance(input);
        assertTrue(maxDistance < dist);

        float suggestionDistance = StringUtil.suggestionFactor(maxDistance);
        assertTrue(dist + " <= " + suggestionDistance, dist <= suggestionDistance);
    }

    /**
     * A real example of two confusable matches. The longer of the
     * two should be preferred.
     */
    public void testLengthInfluencesPreference()
    {
        String input = "stay away from my mom";

        String ta = "get away from my mom";
        String tb = "stow a way";

        StringUtil.LevenshteinResult lr;

        lr = StringUtil.levenshteinDistance(ta, input);

        assertEquals(input.length(), lr.lengthWithoutSuffix);
        int dwsA = lr.distanceWithoutSuffix;

        lr = StringUtil.levenshteinDistance(tb, input);
        assertEquals(9, lr.lengthWithoutSuffix);
        int dwsB = lr.distanceWithoutSuffix;

        float weightA = StringUtil.weight(dwsA, ta.length());
        float weightB = StringUtil.weight(dwsB, tb.length());

        assertTrue(weightA < weightB);
    }
}
