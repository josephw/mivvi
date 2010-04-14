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

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.kafsemo.mivvi.rdf.RdfUtil;

public class EpisodeTreeExpandedState
{
    private JTree jt;
    private TreeNode root;

    EpisodeTreeExpandedState(JTree jt, TreeNode root)
    {
        this.jt = jt;
        this.root = root;
    }

    public Set<String> getExpandedNodeUris()
    {
        final Set<String> s = new HashSet<String>();
        visitEpisodeTreeNodes(new EpisodeTreeNodeVisitor(){
           public void visit(TreePath tp, EpisodeTreeNode etn)
            {
               if (jt.isExpanded(tp)) {
                   String uri = RdfUtil.resourceUri(etn.resource);
                   if (uri != null)
                       s.add(uri);
               }
            } 
        });
        return s;
    }

    public void expandUris(final Collection<String> tokens)
    {
        visitEpisodeTreeNodes(new EpisodeTreeNodeVisitor(){
            public void visit(TreePath path, EpisodeTreeNode etn)
            {
                if (tokens.contains(RdfUtil.resourceUri(etn.resource))) {
                    jt.expandPath(path);
                }
            }
        });
    }
    
    interface EpisodeTreeNodeVisitor
    {
        void visit(TreePath path, EpisodeTreeNode node);
    }
    
    void visitEpisodeTreeNodes(EpisodeTreeNodeVisitor v)
    {
        visitEpisodeTreeNodes(v, new TreePath(root));
    }

    void visitEpisodeTreeNodes(EpisodeTreeNodeVisitor v, TreePath path)
    {
        TreeNode tn = (TreeNode)path.getLastPathComponent();
        
        if (tn instanceof EpisodeTreeNode) {
            v.visit(path, (EpisodeTreeNode)tn);
        }

        if (tn.getAllowsChildren()) {
            Enumeration en = tn.children();
            while (en.hasMoreElements()) {
                visitEpisodeTreeNodes(v, path.pathByAddingChild(en.nextElement()));
            }
        }
    }
}
