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

package org.kafsemo.mivvi.recognise;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import org.junit.Test;

public class TestTitleMatching
{
    @Test
    public void testMatchingComparatorPrefersExpectedTitleMatching() throws URISyntaxException
    {
        URI r1 = new URI("http://www.example.com/#1"),
            r2 = new URI("http://www.example.com/#2");
        
        TitleMatching<URI> tm1 = new TitleMatching<URI>("", 0, 0, "", r1, false, false, 0);
        TitleMatching<URI> tm2 = new TitleMatching<URI>("", 0, 0, "", r2, false, false, 0);
        
        Comparator<TitleMatching<?>> cmp = new TitleMatching.MatchPriorityComparator();
        
        assertEquals("Equivalent matchings are equal when neither is expected",
                0, cmp.compare(tm1, tm2));
        
        cmp = new TitleMatching.MatchPriorityComparator(Collections.singleton(r1));
        
        assertEquals("An expected matching is placed before an unexpected one",
                -1, cmp.compare(tm1, tm2));

        cmp = new TitleMatching.MatchPriorityComparator(Collections.singleton(r2));
        
        assertEquals("An unexpected matching is placed after an expected one",
                1, cmp.compare(tm1, tm2));
        
        cmp = new TitleMatching.MatchPriorityComparator(new HashSet<URI>(Arrays.asList(r1, r2)));
        
        assertEquals("Equivalent matchings are equal when both are expected",
                0, cmp.compare(tm1, tm2));
    }
    
    @Test
    public void exactMatchPreferred() throws URISyntaxException
    {
        URI r = new URI("http://www.example.com/#1");
        
        TitleMatching<URI> exactMatch = new TitleMatching<URI>("", 0, 0, "", r, true, false, 0);
        TitleMatching<URI> match2 = new TitleMatching<URI>("", 0, 0, "", r, false, false, 0);

        assertEquals("An exact match is before an inexact one",
                -1, TitleMatching.MATCHING_COMPARATOR.compare(exactMatch, match2));
        assertEquals("An inexact match is after an exact one",
                1, TitleMatching.MATCHING_COMPARATOR.compare(match2, exactMatch));
    }
    
    @Test
    public void titlesPreferredToDescriptions() throws URISyntaxException
    {
        URI r = new URI("http://www.example.com/#1");
        
        TitleMatching<URI> titleMatch = new TitleMatching<URI>("", 0, 0, "", r, true, true, 0);
        TitleMatching<URI> descriptionMatch = new TitleMatching<URI>("", 0, 0, "", r, true, false, 0);

        assertEquals("A title match is before a description one",
                -1, TitleMatching.MATCHING_COMPARATOR.compare(titleMatch, descriptionMatch));
        assertEquals("A description match is after a title one",
                1, TitleMatching.MATCHING_COMPARATOR.compare(descriptionMatch, titleMatch));
    }
    
    @Test
    public void longerMatchesArePreferred() throws URISyntaxException
    {
        // Matching 'Pilot 2' against
        URI r1 = new URI("http://www.example.com/1#"), // 'Pilot'
            r2 = new URI("http://www.example.com/2#"); // 'Pilot 2'
        
        TitleMatching<URI> m1 = new TitleMatching<URI>("Pilot", 0, 5, "Pilot", r1, true, true, 0);
        TitleMatching<URI> m2 = new TitleMatching<URI>("Pilot 2", 0, 7, "Pilot 2", r2, true, true, 0);

        assertEquals("A longer match is before a shorter one",
                -1, TitleMatching.MATCHING_COMPARATOR.compare(m2, m1));
        assertEquals("A longer match is after a shorter one",
                1, TitleMatching.MATCHING_COMPARATOR.compare(m1, m2));
    }
}
