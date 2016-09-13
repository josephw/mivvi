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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

/**
 * A checkbox with three states - set, unset, and implicitly set.
 * 
 * @author joe
 */
public class TristateCheckbox extends JComponent
    implements MouseListener
{
    private static final TristateUI ui = TristateUI.getInstance();
    
    TristateCheckbox()
    {
//        setBorder(BorderFactory.createEtchedBorder());
        addMouseListener(this);
    }

    public Dimension getPreferredSize()
    {
        return ui.getPreferredSize();
    }
    
    public Dimension getMaximumSize()
    {
        return ui.getMaximumSize();
    }

    boolean implicit = false;    
    boolean active = false;
    
//    private Rectangle r;
//    private Insets ins;

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        ui.paint(g, this, active, implicit, pressed && mouseInside);
    }
    
    private boolean mouseInside = false;

    public void mouseEntered(MouseEvent e)
    {
        mouseInside = true;
        repaint();
    }

    public void mouseExited(MouseEvent e)
    {
        mouseInside = false;
        repaint();
    }
    
    boolean pressed = false;

    public void mousePressed(MouseEvent e)
    {
        pressed = true;
        repaint();
    }
    
    public void mouseReleased(MouseEvent e)
    {
        pressed = false;
        
        if (new Rectangle(getSize()).contains(e.getPoint()))
            clicked();
        
        repaint();
    }
    
    public void mouseClicked(MouseEvent e)
    {
        // mouseReleased takes care of this
    }
    
    private void clicked()
    {
        if (implicit) {
            active = true;
            implicit = false;
        } else if (active) {
            active = false;
        } else {
            active = true;
        }
        repaint();
    }
    
    void setImplicit(boolean isImplicit)
    {
        if (!active) {
            this.implicit = isImplicit;
            repaint();
        }
    }
}
