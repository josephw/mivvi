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

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.kafsemo.mivvi.desktop.AppState;
import org.eclipse.rdf4j.repository.RepositoryException;

public class SelectionListener extends MouseAdapter
{
    final AppState state;
    final SeriesTreeFrame seriesTreeFrame;
    JTree jt;
    
    SelectionListener(AppState state, SeriesTreeFrame stf, JTree t)
    {
        this.state = state;
        this.seriesTreeFrame = stf;
        this.jt = t;
    }

    public void mouseClicked(MouseEvent e)
    {
        int x = e.getX(), y = e.getY();

        TreePath tp = jt.getPathForLocation(x, y);
        if (tp != null) {
            TreeNode tn = (TreeNode)tp.getLastPathComponent();
            
            Rectangle r1 = jt.getPathBounds(tp);
            
            Rectangle rect = new Rectangle();
            EpisodeTreeCellRenderer.getCheckboxRect(r1.getSize(), rect);
            rect.setLocation(r1.getLocation());
            
            if (rect.contains(e.getPoint())) {
                DefaultTreeModel tm = (DefaultTreeModel)jt.getModel();
                ((Checkable)tn).toggle(tm);
            } else if (e.getClickCount() == 2) {
                if (tp.equals(jt.getSelectionPath())) {
                    if (tn instanceof Leaf) {
                        try {
                            JFrame jf = seriesTreeFrame.getDetailsFrame(((Leaf)tn).resource, (EpisodeTreeRoot)jt.getModel().getRoot(),
                                    (DefaultTreeModel)jt.getModel());
                            if (!jf.isVisible())
                                jf.setLocationRelativeTo(seriesTreeFrame);
                            jf.setVisible(true);
                        } catch (RepositoryException re) {
                            // Logging
                            System.err.println(re);
                        }
                    }
                }
            }
        }
        
        e.consume();
    }
}
