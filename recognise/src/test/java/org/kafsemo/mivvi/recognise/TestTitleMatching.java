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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import junit.framework.TestCase;

public class TestTitleMatching extends TestCase
{
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
}
