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

import org.kafsemo.mivvi.recognise.NormalisedString;

/**
 * @author joe
 */
public class TestNormalisedString extends TestCase
{
    /* Test string normalisation */
    public void testNormalAlready()
    {
        NormalisedString ns = new NormalisedString("test");
        assertEquals("test", ns.toString());
    }

    public void testNormalRemoveDots()
    {
        NormalisedString ns = new NormalisedString("this..is.a.test.");
        assertEquals("this is a test", ns.toString());
    }

    public void testNumbersRemain()
    {
        NormalisedString ns = new NormalisedString("24");
        assertEquals("24", ns.toString());
    }

    public void testNewCharPosition()
    {
        NormalisedString ns = new NormalisedString(" test");
        assertEquals("test", ns.toString());
        assertEquals(1, ns.getOriginalPosition(0));
    }

    public void testOrigPosStringEndEmpty()
    {
        NormalisedString ns = new NormalisedString("");
        assertEquals(0, ns.getOriginalPosition(0));
    }

    public void testOrigPosStringEnd()
    {
        NormalisedString ns = new NormalisedString(" - 1 ");
        assertEquals(3, ns.getOriginalPosition(0));
        assertEquals(4, ns.getOriginalPosition(1));
    }

    public void testOrigPosBeforeSpace()
    {
        NormalisedString ns = new NormalisedString("ab c");
        assertEquals("ab c", ns.toString());
        assertEquals(0, ns.getOriginalPosition(0));
        assertEquals(2, ns.getOriginalPosition(2));
    }

    public void testOrigPosMultipleSpaces()
    {
        NormalisedString ns = new NormalisedString("a  b");
        assertEquals("a b", ns.toString());
        assertEquals(0, ns.getOriginalPosition(0));
        assertEquals(1, ns.getOriginalPosition(1));
        assertEquals(4, ns.getOriginalPosition(3));
    }

    public void testInitialMultipleSpaces()
    {
        NormalisedString ns = new NormalisedString("  a");
        assertEquals("a", ns.toString());
        assertEquals(2, ns.getOriginalPosition(0));
        assertEquals(3, ns.getOriginalPosition(1));
    }

    public void testEquals()
    {
        NormalisedString a = new NormalisedString(""),
            b = new NormalisedString("");
        assertEquals(a, b);
    }

    public void testEqualsDifferentCase()
    {
        NormalisedString s1 = new NormalisedString("a"),
            s2 = new NormalisedString("A");

        assertEquals(s1, s2);
    }

    public void testAmpersandToAnd()
    {
        NormalisedString s = new NormalisedString("&");
        assertEquals("and", s.toString());
        assertEquals(0, s.getOriginalPosition(0));
        assertEquals(0, s.getOriginalPosition(1));
        assertEquals(0, s.getOriginalPosition(2));
        assertEquals(1, s.getOriginalPosition(3));
    }
}
