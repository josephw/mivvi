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

import org.kafsemo.mivvi.desktop.AppState;
import org.openrdf.repository.RepositoryException;


/**
 * @author joe
 */
public class CategoryTreeNode extends EpisodeTreeNode.AddableEpisodeTreeNode
{
    private final String category;

    CategoryTreeNode(String category, AppState state, EpisodeTreeNode.AddableEpisodeTreeNode parent) throws RepositoryException
    {
        super(state, parent, state.getMetaData().getCategoryURI(category));
        this.category = category;
    }
    
    public String toString()
    {
        return category;
    }
}
