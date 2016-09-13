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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * The graphical representation of a tristate checkbox widget.
 */
public class TristateUI
{
    private static final TristateUI INSTANCE = new TristateUI();
    
    public static TristateUI getInstance()
    {
        return INSTANCE;
    }

    public void paint(Graphics g, Component c, boolean selected, boolean implicit, boolean armed)
    {
        Border b;
        
        if (armed || implicit) {
            b = b2;
        } else if (selected) {
            b = b2;
        } else {
            b = b1;
        }

        Rectangle br = new Rectangle(c.getSize());

        Rectangle r = new Rectangle(br);

//        b.paintBorder(c, g, r.x, r.y, r.width, r.height);

        Insets ins = b.getBorderInsets(c);

        r.x += ins.left;
        r.y += ins.top;
        r.width -= ins.left + ins.right;
        r.height -= ins.top + ins.bottom;
        
        if (armed) {
//            g.setColor(Color.DARK_GRAY);
            g.setColor(Color.GRAY);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        if (implicit) {
            if (!armed) {
                g.setColor(Color.LIGHT_GRAY);
//                g.setColor(Color.GRAY);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
        } else if (selected) {
            g.setColor(Color.BLACK);
//            g.setColor(Color.GRAY);

            g.fillRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
//            g.fillRect(r.x, r.y, r.width, r.height);
        }

        b.paintBorder(c, g, br.x, br.y, br.width, br.height);
    }
    
    private static final Dimension ps = new Dimension(14, 14);

    public Dimension getPreferredSize()
    {
        return ps;
    }
    
    public Dimension getMaximumSize()
    {
        return ps;
    }
    
    Border b1 = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
        b2 = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
}
