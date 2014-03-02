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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;


/**
 * @author joe
 */
public class Startup
{
    public static List<String> gatherDataURLs() throws IOException
    {
        return gatherDataURLs(null);
    }

    /**
     * Examine bootstrap data locations and find all URLs referring to
     * RDF data.
     *
     * @param ps
     * @return
     * @throws IOException
     */
    public static List<String> gatherDataURLs(Progress ps) throws IOException
    {
        List<String> mivviUrls = new ArrayList<String>();

        /* Check for Mivvi data */
        File f = new File("mivvi-data");
        if (f.exists() && f.isDirectory()) {
            Collection<File> c = FileUtil.gatherFilenames(f, ps);
            Iterator<File> i = c.iterator();
            while (i.hasNext()) {
                File nf = i.next();
                if (isRdfFile(nf.getName()))
                    mivviUrls.add(nf.toURI().toString());
            }
        } else {
            f = new File("mivvi-data.zip");
            if (!f.exists())
                f = new File("../mivvi-data.zip");

            if (f.exists()) {
                mivviUrls.addAll(fetchJarContentsUrls(f));
            }
        }
        return mivviUrls;
    }

    public static List<String> fetchJarContentsUrls(File f)
        throws IOException
    {
        String base = "jar:" + f.toURI().toString() + "!/";

        List<String> l = new ArrayList<String>();

        JarFile jf = new JarFile(f);
        Enumeration<JarEntry> e = jf.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze = e.nextElement();

            if (ze.isDirectory())
                continue;

            String n = ze.getName();
            if (isRdfFile(n))
                l.add(base + n);
        }

        return l;
    }

    public static boolean isRdfFile(String name)
    {
        String ln = name.toLowerCase(Locale.ROOT);
        return ln.endsWith(".rdf") || ln.endsWith(".ttl") || ln.endsWith(".nt");
    }

    public static RDFFormat typeFor(String url)
    {
        return Rio.getParserFormatForFileName(url);
    }
}
