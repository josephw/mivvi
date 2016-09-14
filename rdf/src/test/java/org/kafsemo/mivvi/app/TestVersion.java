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

package org.kafsemo.mivvi.app;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.kafsemo.mivvi.app.Version;

public class TestVersion extends TestCase
{
    /* Parsing */

    public void testParseEmptyString() throws ParseException
    {
        try {
            Version.parse("");
            fail("Shouldn't parse an empty version string");
        } catch (ParseException pe) {
            // Okay
            assertEquals(0, pe.getErrorOffset());
        }
    }
    
    public void testParseSimpleString() throws ParseException
    {
        Version v = Version.parse("0");
        assertTrue(Arrays.equals(new int[]{0}, v.toIntArray()));
        assertEquals("0", v.toString());
    }
    
    public void testMultiPartVersionString() throws ParseException
    {
        Version v = Version.parse("0.2.1.99");
        assertEquals("[0, 2, 1, 99]", Arrays.toString(v.toIntArray()));
        assertEquals(Arrays.toString(new int[]{0, 2, 1, 99}),
                Arrays.toString(v.toIntArray()));
        assertEquals("0.2.1.99", v.toString());
    }
    
    public void testLeadingZeroesIgnored() throws ParseException
    {
        Version v = Version.parse("00.02.01.099");
        assertEquals(Arrays.toString(new int[]{0, 2, 1, 99}),
                Arrays.toString(v.toIntArray()));
        assertEquals("0.2.1.99", v.toString());
    }

    public void testParseBadString()
    {
        try {
            Version.parse(" ");
            fail("Shouldn't parse a bad version string");
        } catch (ParseException pe) {
            // Okay
            assertEquals(0, pe.getErrorOffset());
        }

        try {
            Version.parse("1.");
            fail("Shouldn't parse a badly-terminated version string");
        } catch (ParseException pe) {
            // Okay
            assertEquals(2, pe.getErrorOffset());
        }
        
        try {
            Version.parse("1.a");
            fail("Shouldn't parse a non-numeric version string");
        } catch (ParseException pe) {
            // Okay
            assertEquals(2, pe.getErrorOffset());
        }
    }
    
    public void testParseNegativeNumbersNotAllowed()
    {
        try {
            Version.parse("1.-1");
            fail("Shouldn't parse a negative version string");
        } catch (ParseException pe) {
            // Okay
            assertEquals(2, pe.getErrorOffset());
        }
        
        try {
            Version.parse("-1.0");
            fail("Shouldn't parse a negative version string");
        } catch (ParseException pe) {
            // Okay
            assertEquals(0, pe.getErrorOffset());
        }
    }
    
    
    /* Comparison */
    private void assertComparison(String a, String b, int result) throws ParseException
    {
        Version va = Version.parse(a),
            vb = Version.parse(b);
        
        assertEquals(va, Version.parse(a));
        assertEquals(va.hashCode(), Version.parse(a).hashCode());
        assertEquals(vb, Version.parse(b));
        assertEquals(a + " <=> " + b + " should be " + result, result, va.compareTo(vb));
        
        if (result != 0) {
            assertFalse(va.equals(vb));
            assertFalse(vb.equals(va));
            /* The converse isn't necessarily true: 1.0 != 1 */
        }
    }
    
    public void testZeroIsZero() throws ParseException
    {
        assertComparison("0", "0", 0);
    }
    
    public void testZeroPointZeroIsZero() throws ParseException
    {
        assertComparison("0", "0.0", 0);
        assertComparison("0", "0.0.0.0.0", 0);
        assertComparison("0.0.0.0.0", "0.0", 0);
    }

    public void testOneIsMoreThanZero() throws ParseException
    {
        assertComparison("1", "0", 1);
        assertComparison("0", "1", -1);
    }
    
    public void testMisc() throws ParseException
    {
        assertComparison("1", "1", 0);
        assertComparison("1.1", "1", 1);
        assertComparison("1", "1.1", -1);
    }
    
    public void testTrailingZeroes() throws ParseException
    {
        assertComparison("1", "1.0", 0);
        assertComparison("1.0", "1", 0);
    }
    
    public void testSorting() throws ParseException
    {
        String[] sa = {"1.0", "0", "1.0.1", "2"};
        
        List<Version> l = new ArrayList<Version>();
        for (int i = 0 ; i < sa.length ; i++) {
            l.add(Version.parse(sa[i]));
        }
        
        Collections.sort(l);
        
        assertEquals("[0, 1.0, 1.0.1, 2]", l.toString());
    }
}
