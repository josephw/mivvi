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
 * License along with this library.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileDropTarget extends DropTargetAdapter
{
    private static final Logger log = LoggerFactory.getLogger(FileDropTarget.class);

    public void drop(DropTargetDropEvent dtde)
    {
        Transferable t =  dtde.getTransferable();

        if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            dtde.acceptDrop(DnDConstants.ACTION_LINK);

            try {
                String s = (String)t.getTransferData(DataFlavor.stringFlavor);

                {
                    String s2 = s.trim();
                    if (!s2.equals(s)) {
                        System.err.println("Thanks for the extra whitespace!");
                    }
                    s = s2;
                }

//                System.err.println('\'' + s + '\'');

                java.net.URI uri = new java.net.URI(s);

                dropped(Collections.singleton(uri));

                dtde.dropComplete(true);
            } catch (URISyntaxException use) {
                System.err.println(use);
            } catch (IOException ioe) {
                System.err.println(ioe);
            } catch (UnsupportedFlavorException ufe) {
                System.err.println(ufe);
            }
        } else if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrop(DnDConstants.ACTION_LINK);

            try {
                @SuppressWarnings("unchecked")
                List<File> l = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                Collection<URI> uris = new ArrayList<URI>(l.size());
                Iterator<File> i = l.iterator();
                while (i.hasNext()) {
                    File f = i.next();

                    uris.add(f.toURI());
                }

                dropped(uris);

                dtde.dropComplete(true);
            } catch (IOException ioe) {
                System.err.println(ioe);
            } catch (UnsupportedFlavorException ufe) {
                System.err.println(ufe);
            }
        } else {
            for (DataFlavor df : t.getTransferDataFlavors()) {
                log.debug("Supported flavor: {}", df);
            }
            dtde.rejectDrop();
        }
    }

    public abstract void dropped(Collection<URI> uris);
}
