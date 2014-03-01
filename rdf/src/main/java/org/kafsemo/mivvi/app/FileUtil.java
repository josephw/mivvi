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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

/**
 * @author joe
 */
public class FileUtil
{
    /**
     * Is <code>b</code> contained within the directory <code>a</code>,
     * at any depth?
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean contains(File a, File b)
    {
        String pa = a.getPath(), pb = b.getPath();

        if (pb.startsWith(pa)) {
            if (!pb.equals(pa)) {
                if ((pa.length() == 0) || (pb.charAt(pa.length()) == File.separatorChar)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static Collection<File> gatherFilenames(File b, Progress ps)
    {
        Collection<File> c = new ArrayList<File>();
        gatherFilenamesRecursive(b, c, ps);
        return c;
    }

    public static Collection<File> gatherFilenames(File b)
    {
        return gatherFilenames(b, null);
    }

    public static void gatherFilenamesRecursive(File base, Collection<File> c, Progress ps)
    {
        if (base.isFile()) {
            c.add(base);
            return;
        }

        File[] fa = base.listFiles();
        if (fa == null) {
            System.err.println("Unable to list files in " + base);
            return;
        }

        for (int i = 0 ; i < fa.length ; i++) {
            if ((ps != null) && ps.isCanceled())
                return;
            File f = fa[i];
            if (f.isDirectory())
                gatherFilenamesRecursive(f, c, ps);
            else
                c.add(f);
        }
    }

    /**
     * If a URI identifies a local file, return the appropriate object.
     *
     * @param v
     * @return
     */
    public static File fileFrom(Value v)
    {
        if (v instanceof URI) {
            String s = ((URI) v).toString();
            try {
                java.net.URI u = new java.net.URI(s);
                return fileFrom(u);
            } catch (URISyntaxException use) {
                // Fall through
            }
        }

        return null;
    }

    public static File fileFrom(java.net.URI u)
    {
        if ("file".equalsIgnoreCase(u.getScheme())) {
            if ((u.getAuthority() == null) || u.getAuthority().equals(""))  {
                try {
                    return new File(u);
                } catch (IllegalArgumentException ie) {
                    // Fall through; not a file URI for a local file
                }
            }
        }

        return null;
    }

    public static URI createFileURI(File f)
    {
        return new URIImpl(FileUtil.fixupUri(f.toURI().toString()));
    }

    /**
     * Java seems to use file:/path, rather than file:///path. This
     * method corrects this.
     *
     * @param uri
     * @return
     */
    public static String fixupUri(String uri)
    {
        String PREFIX = "file:/";

        if (uri.toLowerCase().startsWith(PREFIX) && !uri.substring(PREFIX.length()).startsWith("/")) {
            return PREFIX + "//" + uri.substring(PREFIX.length());
        } else {
            return uri;
        }
    }

    public static void ensureDirectory(File d, String name) throws IOException
    {
        if (!d.isDirectory() && !d.mkdirs()) {
            throw new IOException("Unable to create directory " + d + " to save "
                    + name);
        }
    }
}
