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

package org.kafsemo.mivvi.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * A label that acts, and appears, like an HTML link.
 */
public class LinkLabel extends JLabel
{
    private final Desktop desktop;
    private final URI href;

    public LinkLabel(Desktop d, URI href)
    {
        this(d, href, href.toString());
    }

    public LinkLabel(Desktop d, URI href, String text)
    {
        super(text);

        this.desktop = d;
        this.href = href;
        
        addMouseListener(new ClickListener());
        
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(Color.BLUE);
    }
    
    public static JLabel create(Desktop d, String url, String text)
    {
        if (d != null) {
            try {
                return new LinkLabel(d, new URI(url), text);
            } catch (URISyntaxException mfue) {
                // Fall through
            }
        }
        return new JLabel(text + " <" + url + ">");
    }

    public static JLabel create(Desktop d, String url)
    {
        if (d != null) {
            try {
                return new LinkLabel(d, new URI(url));
            } catch (URISyntaxException mfue) {
                // Fall through
            }
        }
        return new JLabel(url);
    }

    class ClickListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            try {
                desktop.browse(href);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(LinkLabel.this, ioe.toString(), "Unable to browse", JOptionPane.ERROR_MESSAGE);
            } catch (UnsupportedOperationException uoe) {
                JOptionPane.showMessageDialog(LinkLabel.this, uoe.toString(), "Unable to browse", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
