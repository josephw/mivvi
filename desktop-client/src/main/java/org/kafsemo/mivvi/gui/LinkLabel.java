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

package org.kafsemo.mivvi.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JLabel;

import org.kafsemo.mivvi.gui.dw.DesktopWrapper;

/**
 * A label that acts, and appears, like an HTML link.
 */
public class LinkLabel extends JLabel
{
    private final URL href; //, text;

    public LinkLabel(URL href)
    {
        this(href, href.toString());
    }

    public LinkLabel(URL href, String text)
    {
        super(text);

        this.href = href;
//        this.text = text;
        
        addMouseListener(new ClickListener());
        
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(Color.BLUE);
    }
    
    public static JLabel create(String url, String text)
    {
        try {
            return new LinkLabel(new URL(url), text);
        } catch (MalformedURLException mfue) {
            return new JLabel(text + " <" + url + ">");
        }
    }

    public static JLabel create(String url)
    {
        try {
            return new LinkLabel(new URL(url));
        } catch (MalformedURLException mfue) {
            return new JLabel(url);
        }
    }

    class ClickListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            DesktopWrapper.browse(href, LinkLabel.this);
        }
    }
}
