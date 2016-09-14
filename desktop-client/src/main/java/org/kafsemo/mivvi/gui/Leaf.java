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

import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.kafsemo.mivvi.desktop.AppState;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryException;

public class Leaf extends Checkable
{
    final EpisodeTreeNode season;
    final String name;

    Leaf(AppState state, Resource resource, EpisodeTreeNode season, String name)
    {
        super(state, resource);
        this.season = season;
        this.name = name;
    }
    
    public String toString()
    {
        return name;
    }
    
    public Enumeration<TreeNode> children()
    {
        return null;
    }
    
    public boolean getAllowsChildren()
    {
        return false;
    }
    
    public TreeNode getChildAt(int childIndex)
    {
        return null;
    }
    
    public int getChildCount()
    {
        return 0;
    }

    public int getIndex(TreeNode node)
    {
        return -1;
    }
    
    public TreeNode getParent()
    {
        return season;
    }
    
    public boolean isLeaf()
    {
        return true;
    }
    
    public boolean anySubItemsChecked()
    {
        return checked;
    }
    
    public boolean allSubItemsChecked()
    {
        return checked;
    }
    
    void checkParentNodes(DefaultTreeModel tm)
    {
        season.subItemToggled(tm);
    }
    
    boolean loadedCheckImplications(DefaultTreeModel dtm) throws RepositoryException
    {
        boolean l = (state.getLocalFiles().localFileFor(resource) != null);
        
        boolean changed = (this.isLocal != l || this.isPartiallyLocal != l);
        
        this.isLocal = l;
        this.isPartiallyLocal = l;
        
        if (changed)
            dtm.nodeChanged(this);
        
        return checked;
    }
}
