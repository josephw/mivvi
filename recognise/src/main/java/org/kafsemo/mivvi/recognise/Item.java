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

package org.kafsemo.mivvi.recognise;

public final class Item<T>
{
    public final String label;
    public final T resource;

    public Item(String l, T r)
    {
        this.label = l;
        this.resource = r;
    }

    public String toString()
    {
        return resource + " (" + label + ")";
    }

    @Override
    public int hashCode()
    {
        return 31 * label.hashCode() + resource.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Item<?>))
            return false;
        Item<?> other = (Item<?>) obj;
        return (label.equals(other.label)
        		&& resource.equals(other.resource));
    }
}
