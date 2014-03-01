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

package org.kafsemo.mivvi.desktop;

import java.awt.Component;
import java.awt.Desktop;

import javax.swing.Box;
import javax.swing.JOptionPane;

import org.kafsemo.mivvi.app.Versioning;
import org.kafsemo.mivvi.gui.LinkLabel;

public class About
{
    public static void showAbout(Component parent, Desktop desktop, Versioning v)
    {
        Object[] message = {
                "Metadata for Video Initiative",
                LinkLabel.create(desktop, "http://mivvi.net/"),
                "Mivvi " + v
        };

        JOptionPane.showMessageDialog(parent, message, "Mivvi", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showCopyright(Component parent, Desktop desktop)
    {
        JOptionPane.showMessageDialog(parent,
                new Object[] {
                LinkLabel.create(desktop, "http://mivvi.net/", "Mivvi"),
                    "Copyright © 2004-2014 Joseph Walton.",
                    "",
                    Box.createVerticalStrut(10),
                    "This program is free software: you can redistribute it and/or modify",
                    "it under the terms of the GNU General Public License as published by",
                    "the Free Software Foundation, either version 3 of the License, or",
                    "(at your option) any later version.",
                    "",
                    Box.createVerticalStrut(10),
                    "This program is distributed in the hope that it will be useful,",
                    "but WITHOUT ANY WARRANTY; without even the implied warranty of",
                    "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the",
                    "GNU General Public License for more details.",
                    "",
                    Box.createVerticalStrut(10),
                    "You should have received a copy of the GNU General Public License",
                    "along with this program.  If not, see",
                    LinkLabel.create(desktop, "http://www.gnu.org/licenses/"),
                    Box.createVerticalStrut(10),
                    "Incorporates:",
                    LinkLabel.create(desktop, "http://www.openrdf.org/", "Sesame"),
                    "Copyright Aduna (http://www.aduna-software.com/) 2001-2013",
                    "All rights reserved."
        }, "Mivvi Copyright Notice", JOptionPane.PLAIN_MESSAGE);
    }
}
