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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.kafsemo.mivvi.desktop.MetaData;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;

/**
 * An RDF icon that, when dragged, provides representative RDF. Abstract,
 * so subclasses can be created for specific contexts.
 * 
 * @author Joseph Walton
 */
public abstract class RdfDragWidget extends JLabel implements DragGestureListener, DragSourceListener
{
    public static final URI RDF_URI = new URIImpl("http://www.w3.org/RDF/");

    static final Border PADDING = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    static final Border REGULAR = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), PADDING),
        ACTIVE = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), PADDING);

    private final DragSource ds;

    public RdfDragWidget(MetaData md)
    {
        Icon icon;
        
        try {
            icon = md.getIcon(RDF_URI);
        } catch (RepositoryException re) {
            // XXX Log
            icon = null;
        }
        
        if (icon != null) {
            setIcon(icon);
        } else {
            setText("[RDF]");
        }
        
        setToolTipText("Drag this episode onto another application as RDF");
        setBorder(REGULAR);
        
        ds = new DragSource();
        ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
    }
    
    /**
     * Create an input stream for the real content to be read from.
     * @return
     */
    abstract InputStream createInputStream() throws IOException;

    DataFlavor[] flavors = {
            new DataFlavor("application/rdf+xml", "RDF Document"),
            new DataFlavor("application/xml", "XML Document"),
            new DataFlavor("text/xml", "XML Document")
    };
    
    /**
     * Under Windows, data typing is fairly coarse. Claim text/xml and
     * text editors appear to accept it. We also accept the more specific
     * XML and RDF flavors.
     */
    public void dragGestureRecognized(DragGestureEvent dge)
    {
        Transferable t = new Transferable(){
            public Object getTransferData(DataFlavor flavor)
                    throws UnsupportedFlavorException, IOException
            {
                if (isDataFlavorSupported(flavor)) {
                    return createInputStream();
                } else {
                    throw new UnsupportedFlavorException(flavor);
                }
            }
            
            public DataFlavor[] getTransferDataFlavors()
            {
                return flavors;
            }
            
            public boolean isDataFlavorSupported(DataFlavor flavor)
            {
                for (int i = 0 ; i < flavors.length ; i++) {
                    if (flavors[i].equals(flavor)) {
                        return true;
                    }
                }
                
                return false;
            }
        };
        
        setBorder(ACTIVE);
        try {
            ds.startDrag(dge, null, t, this);
        } catch (InvalidDnDOperationException idoe) {
            if (idoe.getMessage().equals("Drag and drop in progress")) {
                JOptionPane.showMessageDialog(this,
                        "There was a problem with drag and drop, possibly due to a bug in Java.\n" +
                        "It is reported that, under Linux, upgrading to Sun's 1.5 JDK fixes this bug.\n" +
                        "\n" +
                        "(Dragging and dropping from Java to Mozilla is broken, and will only work once in a session,\n" +
                        "possibly related to Sun bug ID 4638443.)",
                        "InvalidDnDOperationException: Drag and drop in progress", JOptionPane.ERROR_MESSAGE);
            } else {
                throw idoe;
            }
        }
    }
    
    public void dragEnter(DragSourceDragEvent dsde)
    {
    }
    
    public void dragOver(DragSourceDragEvent dsde)
    {
    }
    
    public void dragExit(DragSourceEvent dse)
    {
    }

    public void dragDropEnd(DragSourceDropEvent dsde)
    {
        setBorder(REGULAR);
    }
    
    public void dropActionChanged(DragSourceDragEvent dsde)
    {
    }
}
