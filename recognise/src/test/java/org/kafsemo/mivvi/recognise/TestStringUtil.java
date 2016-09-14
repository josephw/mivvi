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
 * License along with this library.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.recognise;

import junit.framework.TestCase;

import org.kafsemo.mivvi.recognise.StringUtil;
import org.kafsemo.mivvi.recognise.StringUtil.LevenshteinResult;

/**
 * @author joe
 */
public class TestStringUtil extends TestCase
{
    public void testLevenshteinDistanceIdentical()
    {
        assertEquals(0, StringUtil.levenshteinDistance("", "").distance);
        assertEquals(0, StringUtil.levenshteinDistance("Test", "Test").distance);
    }

    public void testLevenshteinDistanceDegenerate()
    {
        assertEquals(4, StringUtil.levenshteinDistance("Test", "").distance);
        assertEquals(4, StringUtil.levenshteinDistance("", "Test").distance);
    }

    public void testLevenshteinDistanceSimple()
    {
        assertEquals(3, StringUtil.levenshteinDistance("pzzel", "puzzle").distance);
        assertEquals(3, StringUtil.levenshteinDistance("puzzle", "pzzel").distance);
    }

    public void testLevenshteinDistanceDifferent()
    {
        assertEquals(4, StringUtil.levenshteinDistance("abcd", "efgh").distance);
    }

    public void testSuffixFindingArticleSample()
    {
        LevenshteinResult r = StringUtil.levenshteinDistance("pzzel", "puzzle");

        assertEquals(3, r.distance);
        assertEquals(5, r.lengthWithoutSuffix);
        assertEquals(2, r.distanceWithoutSuffix);
    }

    public void testSuffixFinding()
    {
        StringUtil.LevenshteinResult r;
        r = StringUtil.levenshteinDistance("test", "testing");

        assertEquals(3, r.distance);
        assertEquals(4, r.lengthWithoutSuffix);
        assertEquals(0, r.distanceWithoutSuffix);
    }

    public void testSuffixFindingEnsureOptimal()
    {
        StringUtil.LevenshteinResult r;

        r = StringUtil.levenshteinDistance("aaa", "aaaaaa");

        assertEquals(3, r.distance);
        assertEquals(3, r.lengthWithoutSuffix);
        assertEquals(0, r.distanceWithoutSuffix);
    }

    private static final boolean isNumeric(float f)
    {
        return !Float.isInfinite(f) && !Float.isNaN(f);
    }

    public void testDistanceWeighting()
    {
        assertTrue("Weight should always be numeric", isNumeric(StringUtil.weight(0, 0)));
        assertTrue("Weight should always be numeric", isNumeric(StringUtil.weight(1, 0)));

        float w1 = StringUtil.weight(3, 3);
        float w2 = StringUtil.weight(6, 6);

        assertTrue("Distance should be proportionally more significant for longer strings", w1 < w2);
    }
}
