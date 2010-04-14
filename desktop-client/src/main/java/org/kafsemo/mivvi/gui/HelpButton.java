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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * A <code>HelpButton</code> looks like a question mark, captures F1 and displays
 * useful advice when activated.
 * 
 * @author Joseph Walton
 */
public class HelpButton extends JButton implements ActionListener
{
    private String help = "No help is available for this item.";

    public HelpButton()
    {
        super("?");
        
        addActionListener(this);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"),
            "help");

        getActionMap().put("help", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                HelpButton.this.actionPerformed(e);
            }
        });
    }
    
    public HelpButton(String help)
    {
        this();
        setHelp(help);
    }
    
    public void setHelp(String s)
    {
        help = s;
    }

    public void actionPerformed(ActionEvent e)
    {
        JOptionPane.showMessageDialog(getRootPane(), help, "Mivvi Help", JOptionPane.INFORMATION_MESSAGE, null);
    }
}
