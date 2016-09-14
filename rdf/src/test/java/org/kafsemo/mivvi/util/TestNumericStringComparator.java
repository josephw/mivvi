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

package org.kafsemo.mivvi.util;

import java.util.Comparator;

import org.kafsemo.mivvi.util.NumericStringComparator;

import junit.framework.TestCase;

/**
 * Consistency checks for {@link NumericStringComparator}.
 * 
 * @author Joseph Walton
 */
public class TestNumericStringComparator extends TestCase
{
    private Comparator<String> cmp;
    
    public void setUp()
    {
        cmp = new NumericStringComparator();
    }
    
    public void tearDown()
    {
        cmp = null;
    }
    
    /**
     * String, with no numeric component.
     */
    public void testPureStrings()
    {
        assertEquals(0, cmp.compare("", ""));
        assertEquals(-1, cmp.compare("", "a"));
        assertEquals(1, cmp.compare("a", ""));
        assertEquals(0, cmp.compare("a", "a"));
        
        assertEquals(-1, cmp.compare("Test", "TestA"));
        assertEquals(1, cmp.compare("TestA", "Test"));
    }
    
    /**
     * Strings consisting of a single number.
     */
    public void testPureNumbers()
    {
        assertEquals(0, cmp.compare("1", "1"));
        assertEquals(-1, cmp.compare("1", "2"));
        assertEquals(1, cmp.compare("2", "1"));
        
        assertEquals(-1, cmp.compare("9", "10"));
        assertEquals(1, cmp.compare("10", "9"));
    }

    /**
     * Strings consisting of a single number with leading zeroes,
     * which should be ignored.
     */
    public void testPureNumbersLeadingZeroes()
    {
        assertEquals(0, cmp.compare("01", "1"));
        assertEquals(0, cmp.compare("1", "01"));
        assertEquals(1, cmp.compare("02", "1"));
        assertEquals(1, cmp.compare("2", "01"));
        assertEquals(-1, cmp.compare("1", "02"));
        assertEquals(-1, cmp.compare("01", "2"));

        assertEquals(-1, cmp.compare("09", "10"));
        assertEquals(1, cmp.compare("010", "9"));
        assertEquals(-1, cmp.compare("9", "010"));
        assertEquals(1, cmp.compare("10", "09"));
    }
    
    /**
     * Strings followed by numbers. As the strings match the comparison should
     * use the suffixes' numeric value.
     */
    public void testStringsWithNumericSuffixes()
    {
        assertEquals(0, cmp.compare("Test1", "Test1"));
        
        assertEquals(-1, cmp.compare("Test1", "Test2"));
        assertEquals(1, cmp.compare("Test2", "Test1"));

        assertEquals(-1, cmp.compare("Test9", "Test10"));
        assertEquals(1, cmp.compare("Test10", "Test9"));
        assertEquals(-1, cmp.compare("Test 9", "Test 10"));
        assertEquals(1, cmp.compare("Test 10", "Test 9"));
        
        assertEquals(-1, cmp.compare("Test", "Test1"));
        assertEquals(1, cmp.compare("Test1", "Test"));
    }

    /**
     * Any dashes should be treated as strings, not as negating prefixes.
     */
    public void testNegativesAreIgnored()
    {
        assertEquals(0, cmp.compare("Test-1", "Test-1"));
        assertEquals(-1, cmp.compare("Test-1", "Test-2"));
        assertEquals(1, cmp.compare("Test-2", "Test-1"));
        assertEquals(-1, cmp.compare("Test-9", "Test-10"));
        assertEquals(1, cmp.compare("Test-10", "Test-9"));
    }

    /**
     * Numbers should be found and tested repeatedly.
     */
    public void testCompositeStrings()
    {
        assertEquals(0, cmp.compare("Test 1 2", "Test 001 0002"));
        assertEquals(-1, cmp.compare("Test 1 1", "Test 001 0002"));
        assertEquals(1, cmp.compare("Test 1 1 2", "Test 001 0001 1"));
    }

    /**
     * Numbers are parsed as integers. If they cannot be parsed the
     * comparison should fall back on a string comparison.
     */
    public void testOversizedNumbersFallback()
    {
        long a = (long) Integer.MAX_VALUE + 1;

        assertEquals(0, cmp.compare(Long.toString(a), Long.toString(a)));
        assertEquals(-1, cmp.compare("0" + a, "1" + a));
        assertEquals(1, cmp.compare("1" + a, "0" + a));
    }
    
    public void testNumericStringsFirstComparedByString()
    {
        assertEquals(1, cmp.compare("TestB-1", "TestA-1"));
        assertEquals(-1, cmp.compare("AnotherTest-1", "BasicTest-1"));
    }

    /**
     * Comparison of oversized numbers should still be
     * numerically consistent.
     */
    public void testOversizedNumbersAreConsistent()
    {
        String a = "3",
            b = "20",
            c = "2147483648"; // Integer.MAX_VALUE + 1
        
        assertEquals("a < b", -1, cmp.compare(a, b));
        assertEquals("b < c", -1, cmp.compare(b, c));
        assertEquals("a < c", -1, cmp.compare(a, c));
    }
}
