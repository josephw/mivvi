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

package org.kafsemo.mivvi.algo;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for consistency of a {@link BkTree}.
 * 
 * @author joe
 */
public class TestBkTree
{
    BkTree<Integer> newFinder(Metric<Integer> metric,
            Collection<Integer> contents)
    {
        return new BkTree<Integer>(metric, contents);
    }
    
    @Test
    public void nothingInEmptyFinder()
    {
        BkTree<Integer> f = newFinder(new IntMetric(), Collections.<Integer>emptySet());
        
        Set<Integer> results = new HashSet<Integer>();
        f.find(Integer.valueOf(0), 0, results);
        assertEquals(Collections.emptySet(), results);
    }
    
    @Test
    public void findsSelfWithZeroDistance()
    {
        BkTree<Integer> f = newFinder(new IntMetric(), Collections.singleton(Integer.valueOf(0)));
        
        Set<Integer> results = new HashSet<Integer>();
        f.find(Integer.valueOf(0), 0, results);
        assertEquals(Collections.singleton(Integer.valueOf(0)), results);
    }
    
    @Test
    public void findsOtherValuesWithLargerDistance()
    {
        Set<Integer> set = new HashSet<Integer>(Arrays.asList(0, 1, 2, 3, 4));
        
        BkTree<Integer> f = newFinder(new IntMetric(), set);
        
        Set<Integer> results = new HashSet<Integer>();
        f.find(Integer.valueOf(0), 4, results);
        assertEquals(set, results);
    }
}
