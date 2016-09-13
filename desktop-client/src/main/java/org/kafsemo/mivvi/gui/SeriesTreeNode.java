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

import java.util.Arrays;

import org.kafsemo.mivvi.desktop.AppState;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.repository.RepositoryException;

public class SeriesTreeNode extends EpisodeTreeNode.OrganisationalTreeNode
{
    SeriesTreeNode(AppState state, EpisodeTreeNode p, Resource s)
    {
        super(state, p, s);
    }
    
    Checkable[] createTreeNodes() throws RepositoryException
    {
        Resource[] ra = state.getSeriesData().getSeasons(resource);

        /* Check for the degenerate case: a single season with no URI. */
        if (ra.length == 1 && !(ra[0] instanceof URI)) {
            return SeasonTreeNode.createTreeNodes(state, this, ra[0]);
        }

        SeasonTreeNode[] tna = new SeasonTreeNode[ra.length];
        for (int i = 0 ; i < ra.length ; i++) {
            tna[i] = new SeasonTreeNode(state, this,  ra[i]);
        }
        Arrays.sort(tna);
        
        return tna;
    }
}
