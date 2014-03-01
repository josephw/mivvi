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

package org.kafsemo.mivvi.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TokenSetFile extends TokenFile
{
    private final Set<String> tokens;
    
    public TokenSetFile(File f)
    {
        super(f);
        tokens = new HashSet<String>();
    }

    synchronized void clear()
    {
        tokens.clear();
    }
    
    synchronized void loadedToken(String t)
    {
        tokens.add(t);
    }
    
    public synchronized Collection<String> getTokens()
    {
        return new ArrayList<String>(tokens);
    }
    
    public synchronized boolean isEmpty()
    {
        return tokens.isEmpty();
    }
    
    public synchronized boolean add(String s)
    {
        return tokens.add(s);
    }

    public synchronized boolean contains(String channel)
    {
        return tokens.contains(channel);
    }

    public synchronized boolean remove(String channel)
    {
        return tokens.remove(channel);
    }

    public synchronized void replace(Set<String> set)
    {
        tokens.clear();
        tokens.addAll(set);
    }
}
