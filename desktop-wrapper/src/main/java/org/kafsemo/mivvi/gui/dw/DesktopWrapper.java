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

package org.kafsemo.mivvi.gui.dw;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JOptionPane;

/**
 * A wrapper around browser launching and file opening, that
 * knows how to detect missing implementations and alert the user if
 * there's no way to browse or launch. The Java 6 Desktop is used,
 * falling back to JDIC on Java 5.
 * 
 * @author Joseph Walton
 */
public class DesktopWrapper
{
    private static final void error(Component parent, String msg, String title)
    {
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.ERROR_MESSAGE);
    }
    
    private static final void warn(Component parent, String msg, String title)
    {
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.WARNING_MESSAGE);
    }
    
    public static boolean attemptJdicBrowse(URL url, Component parent)
        throws NoClassDefFoundError, UnsatisfiedLinkError
    {
        try
        {
            org.jdesktop.jdic.desktop.Desktop.browse(url);
            return true;
        }
        catch (Exception de)
        {
            error(parent, de.toString(), "Unable to browse URL");
            return false;
        }
    }

    public static boolean attemptJdicOpen(File f, Component parent)
        throws NoClassDefFoundError, UnsatisfiedLinkError
    {
        try
        {
            org.jdesktop.jdic.desktop.Desktop.open(f);
            return true;
        }
        catch (Exception de)
        {
            error(parent, de.toString(), "Unable to open file");
            return false;
        }
    }
    
    public static void attemptJava6DesktopBrowse(URI uri) throws NoClassDefFoundError, IOException
    {
        Desktop.getDesktop().browse(uri);
    }

    public static void attemptJava6DesktopOpen(File file) throws NoClassDefFoundError, IOException
    {
        Desktop.getDesktop().open(file);
    }
    
    public static void browse(URI uri, Component parent)
    {
        try {
            attemptJava6DesktopBrowse(uri);
        } catch (IOException ioe) {
            error(parent, ioe.toString(), "Unable to browse the web");
        } catch (NoClassDefFoundError ncdfe) {
            /* No Java 6 - try JDIC */
            try {
                URL u = uri.toURL();

                attemptJdicBrowse(u, parent);
            } catch (MalformedURLException mfue) {
                error(parent, "Unable to browse URI with JDIC: " + uri, "Unable to browse the web");
            } catch (NoClassDefFoundError ncdfe2) {
                warn(parent, ncdfe2.toString() + "\nUnable to browse with Java 5 without JDIC installed.",
                    "Unable to browse the web");
            } catch (UnsatisfiedLinkError ule) {
                error(parent, ule.toString() + "\nUnable to browse the web without JDIC's native libraries installed.",
                    "Unable to browse the web");
            }
        }
    }
    
    public static void browse(URL url, Component parent)
    {
        try {
            browse(url.toURI(), parent);
        } catch (URISyntaxException use) {
            error(parent, use.toString(), "Unable to browse the web");
        }
    }
    
    public static void open(File file, Component parent)
    {
        try {
            attemptJava6DesktopOpen(file);
        } catch (IOException ioe) {
            error(parent, ioe.toString(), "Unable to open file");
        } catch (NoClassDefFoundError ncdfe) {
            /* No Java 6 - try JDIC */
            try {
                attemptJdicOpen(file, parent);
            } catch (NoClassDefFoundError ncdfe2) {
                warn(parent, ncdfe.toString() + "\nUnable to open files with Java 5 without JDIC installed.",
                        "Unable to open file");
            } catch (UnsatisfiedLinkError ule) {
                error(parent, ule.toString() + "\nUnable to open files without JDIC's native libraries installed.",
                        "Unable to open file");
            }
        }
    }
}
