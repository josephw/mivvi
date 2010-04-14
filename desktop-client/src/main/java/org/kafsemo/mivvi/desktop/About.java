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

package org.kafsemo.mivvi.desktop;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JOptionPane;

import org.kafsemo.mivvi.app.Versioning;
import org.kafsemo.mivvi.gui.LinkLabel;

public class About
{
    public static void showAbout(Component parent)
    {
        Object[] message = {
                "Metadata for Video Initiative",
                LinkLabel.create("http://mivvi.net/"),
                "Mivvi " + Versioning.getInstance().getVersion()
        };

        JOptionPane.showMessageDialog(parent, message, "Mivvi", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showCopyright(Component parent)
    {
        JOptionPane.showMessageDialog(parent,
                new Object[] {
                LinkLabel.create("http://mivvi.net/", "Mivvi"),
                    "Copyright 2004-2006 Joseph Walton. All rights reserved.",
                    Box.createVerticalStrut(10),
                    "Includes code licensed under the GNU Lesser General Public License:",
                    LinkLabel.create("http://www.openrdf.org/", "Sesame"),
                    "Copyright 2001-2007 Aduna",
                    LinkLabel.create("http://jdic.dev.java.net/", "JDesktop Integration Components"),
                    "Copyright 2004 Sun Microsystems, Inc.",
        }, "Mivvi Copyright Notice", JOptionPane.PLAIN_MESSAGE);
    }
}
