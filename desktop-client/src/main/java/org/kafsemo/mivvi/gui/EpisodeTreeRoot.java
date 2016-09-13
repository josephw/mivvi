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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultTreeModel;

import org.kafsemo.mivvi.desktop.AppState;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryException;

public class EpisodeTreeRoot extends EpisodeTreeNode.AddableEpisodeTreeNode
{
    EpisodeTreeRoot(AppState state, Resource res)
    {
        super(state, null, res);
    }
    
    public String toString()
    {
        return "Television";
    }
    
    private final Map<List<String>, EpisodeTreeNode.AddableEpisodeTreeNode> categoryNodes = new HashMap<List<String>, AddableEpisodeTreeNode>();

    void initCategoryNodes() throws RepositoryException
    {
        Resource[] sa = state.getSeriesData().getAllSeries();
        
        for (int i = 0 ; i < sa.length ; i++) {
            Resource r = sa[i];

            List<String> cl = state.getMetaData().getCategory(r);

            EpisodeTreeNode.AddableEpisodeTreeNode p = getCategoryTreeNode(cl);
            p.addChild(new SeriesTreeNode(state, p, r));
        }
    }

    private EpisodeTreeNode.AddableEpisodeTreeNode getCategoryTreeNode(List<String> cl) throws RepositoryException
    {
        EpisodeTreeNode.AddableEpisodeTreeNode ctn = categoryNodes.get(cl);
        if (ctn == null) {
            if (cl.isEmpty()) {
                ctn = this;
            } else {
                EpisodeTreeNode.AddableEpisodeTreeNode p = getCategoryTreeNode(cl.subList(0, cl.size() - 1));
                ctn = new CategoryTreeNode(cl.get(cl.size() - 1), state, p);
                p.addChild(ctn);
            }
            categoryNodes.put(cl, ctn);
        }
        return ctn;
    }

    void checkParentNodes(DefaultTreeModel tm)
    {
        // No parent nodes to check
    }
}
