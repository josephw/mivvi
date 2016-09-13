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

package org.kafsemo.mivvi.gui;

import static org.junit.Assert.assertEquals;

import javax.swing.JLabel;

import org.junit.Test;

/**
 * {@link LinkLabel} should degrade , into a plain label, when no Desktop is
 * available or when a bad URL is given for the link.
 * 
 * @author joe
 * 
 */
public class TestLinkLabel
{
    @Test
    public void withoutDesktopFallsBackToPlainJLabel()
    {
        JLabel jl = LinkLabel.create(null, "http://www.example.com/#");
        assertEquals(JLabel.class, jl.getClass());
        assertEquals("http://www.example.com/#", jl.getText());
    }
    
    @Test
    public void withoutDesktopFallsBackToUrlAnnotatedJLabel()
    {
        JLabel jl = LinkLabel.create(null, "http://www.example.com/#", "Label");
        assertEquals(JLabel.class, jl.getClass());
        assertEquals("Label <http://www.example.com/#>", jl.getText());
    }

    @Test
    public void badUrlFallsBackToPlainJLabel()
    {
        JLabel jl = LinkLabel.create(null, "A bad URL");
        assertEquals(JLabel.class, jl.getClass());
        assertEquals("A bad URL", jl.getText());
    }

    @Test
    public void badUrlFallsBackToUrlAnnotatedJLabel()
    {
        JLabel jl = LinkLabel.create(null, "A bad URL", "Label");
        assertEquals(JLabel.class, jl.getClass());
        assertEquals("Label <A bad URL>", jl.getText());
    }
}
