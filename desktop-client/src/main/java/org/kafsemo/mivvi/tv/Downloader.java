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

package org.kafsemo.mivvi.tv;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.kafsemo.mivvi.app.TokenFile;
import org.kafsemo.mivvi.app.Versioning;
import org.kafsemo.mivvi.desktop.ProgressInputStream;
import org.kafsemo.mivvi.desktop.ProgressStatus;
import org.kafsemo.mivvi.rss.HttpCookieUtil;

/**
 * A <code>Downloader</code> enforces a download policy, supplying fresh local copies
 * of remote files.
 * 
 * @author Joseph Walton
 */
public class Downloader
{
    public static final long HOUR_MILLIS = 1000L * 60 * 60;

    private final File cacheDir;
    private final File indexFile;

    public Downloader()
    {
        this.cacheDir = null;
        this.indexFile = null;
    }

    public Downloader(File cacheDir) throws IOException
    {
        if (!cacheDir.isDirectory()) {
            if (!cacheDir.mkdirs())
                throw new IOException("Unable to create web cache directory " + cacheDir);
        }
        this.cacheDir = cacheDir;
        
        indexFile = new File(this.cacheDir, "index.xml");
        
        if (indexFile.isFile()) {
            InputStream in = new FileInputStream(indexFile);
            loadState(in);
            in.close();
        }
    }
    
    public synchronized void save() throws IOException
    {
        File tf = new File(indexFile.getParentFile(), indexFile.getName() + ".tmp");
        OutputStream out = new FileOutputStream(tf);
        saveState(out);
        out.close();
        
        if (indexFile.exists())
            indexFile.delete();
        
        if (!tf.renameTo(indexFile))
            throw new IOException("Unable to rename temporary file '" + tf + "' into place");
    }

    public File getCacheDirectory()
    {
        return cacheDir;
    }

    private Map<String, InstanceDetails> details = new HashMap<String, InstanceDetails>();

    private String[][] getStringArray()
    {
        String[][] sa = new String[details.size()][4];
        int c = 0;
        Iterator<Entry<String, InstanceDetails>> i = details.entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, InstanceDetails> e = i.next();
            
            sa[c][0] = e.getKey(); //((URL)e.getKey()).toString();
            sa[c][1] = e.getValue().lastModified;
            sa[c][2] = e.getValue().eTag;
            sa[c][3] = Long.toString(e.getValue().stamp);
            
            c++;
        }
        
        return sa;
    }

    private InstanceDetails getInstanceDetails(String url, File df)
    {
        InstanceDetails d = details.get(url);
        if (!df.exists()) {
            d = null;
            details.remove(url);
        }
        
        return d;
    }

    public synchronized boolean isFresh(String url, File df, long maxAge)
    {
        InstanceDetails d = getInstanceDetails(url, df);
        
        return ((d != null) && (System.currentTimeMillis() - maxAge < d.stamp));
    }

    public synchronized boolean downloadToFile(URL url, File df, ProgressStatus ps) throws IOException
    {
        return downloadToFile(url, df, null, ps);
    }

    private static final String USER_AGENT = "Mivvi/" + Versioning.from(Downloader.class);

    public synchronized boolean downloadToFile(URL url, File df, List<String> cookies, ProgressStatus ps) throws IOException
    {
        if (ps != null) {
            synchronized (ps) {
                ps.indeterminate = true;
            }
        }

        InstanceDetails d = getInstanceDetails(url.toString(), df);

        URLConnection uc = url.openConnection();
        
        uc.addRequestProperty("User-Agent", USER_AGENT);
        
        uc.addRequestProperty("Accept-Encoding", "gzip");

        if (cookies != null) {
            HttpCookieUtil.addCookieHeaders(uc, cookies);
        }

        if (d != null) {
            if (d.lastModified != null)
                uc.addRequestProperty("If-Modified-Since", d.lastModified);
            
            if (d.eTag != null)
                uc.addRequestProperty("If-None-Match", d.eTag);
        }
        
        if (uc instanceof HttpURLConnection) {
            HttpURLConnection huc = (HttpURLConnection)uc;

            if (huc.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                // XXX This duplicates code below
                d = new InstanceDetails(System.currentTimeMillis());
                
                d.lastModified = uc.getHeaderField("Last-Modified");
                d.eTag = uc.getHeaderField("ETag");
                
                details.put(url.toString(), d);

                return false;
            }
            
            if (huc.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                details.remove(url.toString());
                df.delete();
                return true;
            }
        }

        InputStream in;

        /* Behave like a 404 for a FileNotFoundException */
        try {
            in = uc.getInputStream();
        } catch (FileNotFoundException fnfe) {
            details.remove(url.toString());
            df.delete();
            return true;
        }
        
        if (ps != null) {
            String cl = uc.getHeaderField("Content-Length");
            if (cl != null) {
                try {
                    in = new ProgressInputStream(uc.getInputStream(), Long.parseLong(cl), ps);
                } catch (NumberFormatException nfe) {
                    // Leave indeterminate
                }
            }
        }

        String enc = uc.getHeaderField("Content-Encoding");
        if ("gzip".equalsIgnoreCase(enc)) {
            in = new GZIPInputStream(in);
        }
        
        File tf = TokenFile.mktmp(df);

        OutputStream out = new FileOutputStream(tf);
        
        copy(in, out);
        out.close();
        in.close();
        
        d = new InstanceDetails(System.currentTimeMillis());
        
        d.lastModified = uc.getHeaderField("Last-Modified");
        d.eTag = uc.getHeaderField("ETag");

        TokenFile.installAs(df, tf);

        details.put(url.toString(), d);
        
        return true;
    }

    /**
     * Make sure a local file is an up-to-date copy of a web resource.
     * 
     * @param url
     * @param f
     * @param maxAge
     * @return whether or not the server was contacted
     * @throws IOException
     */
    public synchronized boolean refresh(String url, File f, long maxAge, List<String> cookies) throws IOException
    {
        if (isFresh(url, f, maxAge)) {
            return false;
        }else {
            if (downloadToFile(new URL(url), f, cookies, null)) {
                if (f.exists()) {
                    System.err.println("Downloaded " + url);
                } else {
                    System.err.println("(" + url + " not found)");
                }
            } else {
//                System.err.println("(302'd " + url + ")");
            }
            return true;
        }
    }
    
    private final byte[] ba = new byte[65536];
    private long copy(InputStream in, OutputStream out) throws IOException
    {
        long count = 0;

        int l;
        while ((l = in.read(ba)) > 0) {
            out.write(ba, 0, l);
            count += l;
        }
        
        return count;
    }
    
    private static class InstanceDetails
    {
        String lastModified;
        String eTag;
        final long stamp;
        
        InstanceDetails(long stamp)
        {
            this.stamp = stamp;
        }
    }

    public synchronized void saveState(OutputStream os)
    {
        XMLEncoder xe = new XMLEncoder(os);
        
        xe.writeObject(getStringArray());
        xe.close();
    }
    
    public synchronized void loadState(InputStream in) throws MalformedURLException
    {
        details.clear();

        XMLDecoder xd = new XMLDecoder(in);
        
        Object o = xd.readObject();
        if (o instanceof String[][]) {
            String[][] sa = (String[][])o;
            for (int i = 0 ; i < sa.length ; i++) {
                InstanceDetails d = new InstanceDetails(Long.parseLong(sa[i][3]));
                d.lastModified = sa[i][1];
                d.eTag = sa[i][2];
                
                details.put(sa[i][0], d);
            }
        }
    }

    public boolean refresh(String url, File localFile, long hour_millis2) throws IOException
    {
        return refresh(url, localFile, hour_millis2, null);
    }

    public boolean downloadToFile(URL url, File file) throws IOException
    {
        return downloadToFile(url, file, null, null);
    }
}
