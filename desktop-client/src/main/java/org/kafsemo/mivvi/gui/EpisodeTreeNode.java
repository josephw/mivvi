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

package org.kafsemo.mivvi.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.kafsemo.mivvi.desktop.AppState;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryException;

/**
 *  A non-terminal node in the episode tree.
 */
public abstract class EpisodeTreeNode extends Checkable
{
    final Icon icon;
    final EpisodeTreeNode parent;

    EpisodeTreeNode(AppState state, EpisodeTreeNode parent, Resource resource)
    {
        super(state, resource);
        this.parent = parent;
        
        Icon icn;
        
        try {
            icn = state.getMetaData().getIcon(resource);
        } catch (RepositoryException re) {
            // Logging
            icn = null;
        }
        
        this.icon = icn;
    }
    
    public final TreeNode getParent()
    {
        return parent;
    }

    abstract Checkable[] createTreeNodes() throws RepositoryException;

    private Checkable[] tna;

    private Checkable[] getTreeNodes()
    {
        if (tna == null) {
            try {
                tna = createTreeNodes();
            } catch (RepositoryException re) {
                System.err.println(re); // Logging
                tna = new Checkable[0];
            }
        }
        return tna;
    }

    public boolean getAllowsChildren()
    {
        return true;
    }
    
    public boolean isLeaf()
    {
        return false;
    }

    public Enumeration<Checkable> children()
    {
        return Collections.enumeration(Arrays.asList(getTreeNodes()));
    }
    
    public TreeNode getChildAt(int childIndex)
    {
        return getTreeNodes()[childIndex];
    }
    
    public int getChildCount()
    {
        return getTreeNodes().length;
    }
    
    public int getIndex(TreeNode node)
    {
        TreeNode[] tna = getTreeNodes();
        for (int i = 0 ; i < tna.length ; i++)
            if (node == tna[i])
                return i;
        return -1;
    }
    
    void checkParentNodes(DefaultTreeModel tm)
    {
        parent.subItemToggled(tm);
    }

    public boolean anySubItemsChecked()
    {
        Checkable[] etna = getTreeNodes();
        for (int i = 0 ; i < etna.length ; i++) {
            if (etna[i].checked)
                return true;
            if (etna[i].implicit)
                return true;
        }
        return false;
    }
    
    public boolean allSubItemsChecked()
    {
        Checkable[] etna = getTreeNodes();
        for (int i = 0 ; i < etna.length ; i++) {
            if (!etna[i].checked)
                return false;
        }
        return true;
    }
    
    void selectChildren(DefaultTreeModel tm)
    {
        Checkable[] etna = getTreeNodes();
        for (int i = 0 ; i < etna.length ; i++) {
            this.checked = true;
            this.implicit = false;
            etna[i].selectChildren(tm);
        }
        super.selectChildren(tm);
    }
    
    void deselectChildren(DefaultTreeModel tm)
    {
        Checkable[] etna = getTreeNodes();
        for (int i = 0 ; i < etna.length ; i++) {
            this.checked = false;
            this.implicit = false;        
            etna[i].deselectChildren(tm);
        }
        super.deselectChildren(tm);
    }
    
    boolean loadedCheckImplications(DefaultTreeModel dtm) throws RepositoryException
    {
        if (checked)
            selectChildren(dtm);

        boolean local = true, partial = false;

        Checkable[] ca = getTreeNodes();
        
        for (int i = 0 ; i < ca.length ; i++) {
            if (ca[i].loadedCheckImplications(dtm) && !checked) {
                implicit = true;
            }
            local &= ca[i].isLocal;
            partial |= ca[i].isPartiallyLocal;
        }

        boolean changed = (this.isLocal != local || this.isPartiallyLocal != partial);
        
        this.isLocal = local;
        this.isPartiallyLocal = partial;

        if (changed)
            dtm.nodeChanged(this);
        
        return checked || implicit;
    }
    
    static abstract class OrganisationalTreeNode extends EpisodeTreeNode
        implements Comparable<OrganisationalTreeNode>
    {
        OrganisationalTreeNode(AppState state, EpisodeTreeNode parent,
                Resource res)
        {
            super(state, parent, res);
        }

        public final int compareTo(OrganisationalTreeNode o)
        {
            OrganisationalTreeNode n = o;

            boolean a = this instanceof AddableEpisodeTreeNode,
                b = n instanceof AddableEpisodeTreeNode;
            
            if (a && !b)
                return -1;
            else if (!a && b)
                return 1;
            
            return toString().compareTo(n.toString());
        }
    }
    
    static abstract class AddableEpisodeTreeNode extends OrganisationalTreeNode
    {
        AddableEpisodeTreeNode(AppState state, EpisodeTreeNode parent,
                Resource res)
        {
            super(state, parent, res);
        }

        private List<OrganisationalTreeNode> l = new ArrayList<OrganisationalTreeNode>();

        private static Checkable[] CA = {};

        final Checkable[] createTreeNodes()
        {
            Collections.sort(l);
            return l.toArray(CA);
        }
        
        final void addChild(OrganisationalTreeNode c)
        {
            l.add(c);
        }
        
        public final boolean isLeaf()
        {
            return false;
        }
        
        public final boolean getAllowsChildren()
        {
            return true;
        }
    }
}
