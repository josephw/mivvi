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

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author joe
 * @param <T> the underlying identifier type
 */
public class FilenameMatch<T>
{
    public final T episode;
    public final boolean isExact; // Is this recognition an exact match?
    public final Collection<Matching<T>> ignored;

    FilenameMatch(T episode, boolean isExact)
    {
        this.episode = episode;
        this.isExact = isExact;
        this.ignored = new ArrayList<Matching<T>>();
    }
}
