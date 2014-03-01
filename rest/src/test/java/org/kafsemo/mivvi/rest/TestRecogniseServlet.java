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

package org.kafsemo.mivvi.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class TestRecogniseServlet
{
    @Test
    public void ensureSeeAlsoUrlIsCorrect() throws Exception
    {
        String subject = "http://www.example.com/#";
        
        String base = "http://localhost/about";
        
        assertEquals("The seeAlso URL should be escaped correctly",
                "http://localhost/about?subject=http%3A%2F%2Fwww.example.com%2F%23",
                RecogniseServlet.seeAlsoUrl(base, subject));

        subject = "http://www.example.com/?%23";
        
        assertEquals("Percent signs should be double-escaped",
                "http://localhost/about?subject=http%3A%2F%2Fwww.example.com%2F%3F%2523",
                RecogniseServlet.seeAlsoUrl(base, subject));
    }
}
