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

package org.kafsemo.mivvi.recognise;

/**
 * Details of a possible episode title.
 */
public class EpisodeTitleDetails<T>
{
    final T res;
    final String title;
    final boolean isPrimary;

    public EpisodeTitleDetails(T res, String title, boolean isPrimary)
    {
        this.res = res;
        this.title = title;
        this.isPrimary = isPrimary;
    }

    public T getResource()
    {
        return res;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public boolean isPrimary()
    {
        return isPrimary;
    }
    
    public String toString()
    {
        return res + " (" + title + "," + isPrimary + ")";
    }

    public int hashCode()
    {
        return res.hashCode() ^ title.hashCode() ^ (isPrimary ? 1 : 0);
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof EpisodeTitleDetails<?>)) {
            return false;
        }

        EpisodeTitleDetails<?> other = (EpisodeTitleDetails<?>) o;

        return res.equals(other.res)
        	&& title.equals(other.title)
        	&& isPrimary == other.isPrimary;
    }
}
