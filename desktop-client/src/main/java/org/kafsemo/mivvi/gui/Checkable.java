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

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.kafsemo.mivvi.desktop.AppState;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryException;

public abstract class Checkable implements TreeNode
{
    final AppState state;
    final Resource resource;

    /* Indicate that some sub-items are checked, but not all */
    boolean implicit = false;
    
    /* This item has explicitly been checked, or all its
     *     sub-items have been.
     */
    boolean checked = false;

    /* Is there a local copy of this resource? */    
    boolean isLocal = false;
    boolean isPartiallyLocal = false;

    Checkable(AppState state, Resource resource)
    {
        this.state = state;
        this.resource = resource;
        
        this.checked = state.getUserState().getSubscription().contains(resource);
    }
    
    String getTitle()
    {
        try {
            String t = state.getSeriesData().getTitle(this.resource);
            if (t != null)
                return t;
        } catch (RepositoryException re) {
            // Fall through
        }
        
        return "<" + this.resource + ">";
    }

    public String toString()
    {
        return getTitle();
    }


    public boolean isSelected()
    {
        return checked;
    }
    
    /* The user clicked on the widget */
    public final void toggle(DefaultTreeModel tm)
    {
        checked = !checked;
        implicit = false;
        if (checked) {
            selectChildren(tm);
        } else {
            deselectChildren(tm);
        }
        
        checkParentNodes(tm);

        tm.nodeChanged(this);
        
        state.updatedSubscriptions();
    }
    
    abstract void checkParentNodes(DefaultTreeModel tm);
    
    abstract boolean allSubItemsChecked();
    abstract boolean anySubItemsChecked();
    
    void subItemToggled(DefaultTreeModel tm)
    {
        boolean i, c;
        
        if (allSubItemsChecked()) {
            i = false;
            c = true;
        } else if (anySubItemsChecked()) {
            i = true;
            c = false;
        } else {
            i = false;
            c = false;
        }

        if ((implicit != i) || (checked != c)) {
            implicit = i;
            checked = c;
            
            tm.nodeChanged(this);
            checkParentNodes(tm);
        }
    }

    void selectChildren(DefaultTreeModel tm)
    {
        checked = true;
        implicit = false;
        state.getUserState().getSubscription().add(resource);
        tm.nodeChanged(this);
    }

    void deselectChildren(DefaultTreeModel tm)
    {
        checked = false;
        implicit = false;
        state.getUserState().getSubscription().remove(resource);
        tm.nodeChanged(this);
    }
    
    /* On loading configuration */        
    abstract boolean loadedCheckImplications(DefaultTreeModel dtm) throws RepositoryException;
}
