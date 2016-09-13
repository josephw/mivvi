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

package org.kafsemo.mivvi.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class TokenFile
{
    private final File f;
    
    TokenFile(File f)
    {
        this.f = f;
    }

    public final boolean load() throws IOException
    {
        if (!f.exists())
            return false;

        clear();

        BufferedReader br = new BufferedReader(new FileReader(f));
        String s;
        while ((s = br.readLine()) != null)
            loadedToken(s);
        br.close();
        
        return true;
    }

    public static File mktmp(File f)
    {
        return new File(f.getParentFile(), f.getName() + ".tmp");
    }
    
    public static void installAs(File f, File tf) throws IOException
    {
        if (f.exists())
            f.delete();

        if (!tf.renameTo(f)) {
            throw new IOException("Failed to move temporary file " + tf + " in to place");
        }
    }

    public final void save() throws IOException
    {
        List<String> l = new ArrayList<String>(getTokens());
        Collections.sort(l);

        FileUtil.ensureDirectory(f.getParentFile(), f.getName());
        
        File tf = mktmp(f);

        FileWriter fw = new FileWriter(tf);

        Iterator<String> i = l.iterator();
        while (i.hasNext()) {
            fw.write(i.next() + "\n");
        }
        fw.close();

        installAs(f, tf);
    }

    abstract void clear();
    abstract void loadedToken(String t);
    abstract Collection<String> getTokens();
}
