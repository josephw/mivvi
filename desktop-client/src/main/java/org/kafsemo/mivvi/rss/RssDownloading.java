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

package org.kafsemo.mivvi.rss;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.kafsemo.mivvi.app.FileUtil;
import org.kafsemo.mivvi.app.TokenFile;
import org.kafsemo.mivvi.desktop.AppState;
import org.kafsemo.mivvi.desktop.BackgroundRefreshable;
import org.kafsemo.mivvi.desktop.ProgressStatus;
import org.kafsemo.mivvi.recognise.SeriesDataException;
import org.kafsemo.mivvi.tv.Downloader;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RssDownloading extends BackgroundRefreshable
{
    private final AppState state;
    private final DocumentBuilder db;
    
    public RssDownloading(AppState state) throws ParserConfigurationException
    {
        super(Downloader.HOUR_MILLIS / 4);
//        super(5000);
        this.state = state;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        this.db = dbf.newDocumentBuilder();
        
        db.setEntityResolver(new EntityResolver(){
            InputStream in = new ByteArrayInputStream(new byte[]{});
            InputSource s = new InputSource(in);
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException
            {
                return s;
            }
        });
    }
    
    ProgressStatus ps = new ProgressStatus();
    
    Runnable updateStatus = new Runnable() {
        public void run()
        {
            adg.updateProgress(ps);
        }
    };

    private void updateStatus()
    {
        SwingUtilities.invokeLater(updateStatus);
    }

    Object feedListLock = new Object();
    long lastReadStamp = Long.MIN_VALUE;

    void loadFeeds() throws SAXException, IOException
    {
        synchronized (ps) {
            ps.indeterminate = true;
            ps.label = "Loading feed list";
        }
        updateStatus();

        synchronized (feedListLock) {
            File f = state.getConfig().getFeedListFile();
            if (f.exists()) {
                long s = f.lastModified();
                if (s > lastReadStamp) {
                    List<Feed> l = RssUtil.loadFeedList(db.parse(f));
                    List<Feed> fl = state.getUserState().getFeeds();
                    synchronized (fl) {
                        fl.clear();
                        fl.addAll(l);
                    }
                    
                    lastReadStamp = s;
                }
            }
        }

        synchronized (ps) {
            ps.indeterminate = false;
            ps.maximum = 1;
            ps.value = 1;
            ps.label = "Feed list loaded";
        }
        updateStatus();
    }
    
    public void saveFeeds(List<Feed> feeds) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError, IOException
    {
        File f = state.getConfig().getFeedListFile();

        FileUtil.ensureDirectory(f.getParentFile(), f.getName());
        
        File tf = TokenFile.mktmp(f);

        OutputStream out = new FileOutputStream(tf);

        synchronized (feeds) {
            TransformerFactory.newInstance().newTransformer().transform(
                    new DOMSource(RssUtil.saveFeedList(db, feeds)),
                    new StreamResult(out));
        }
        
        out.close();

        TokenFile.installAs(f, tf);
    }
    
    AvailableDownloadGui adg;

    public void refresh()
    {
        final List<Feed> l;
        
        List<Feed> ol = state.getUserState().getFeeds();
        
        synchronized (ol) {
            l = Feed.deepCopy(ol);
        }
        
        SwingUtilities.invokeLater(new Runnable(){
            public void run()
            {
                adg.removeMissingFeeds(l);
            }
        });

        synchronized (ps) {
            ps.indeterminate = false;
            ps.maximum = l.size();
            ps.value = 0;
            ps.label = "Downloading feeds";
        }
        updateStatus();

        int count = 0;

        Collection<String> failedFeeds = new ArrayList<String>();

        Iterator<Feed> i = l.iterator();
        while (i.hasNext()) {
            if (!running())
                return;

            final Feed f = i.next();
            
            synchronized (ps) {
                ps.value = count++;
                ps.label = "Downloading " + f.url;
            }
            updateStatus();

            try {
                Document d = downloadFeed(f, ps);

                synchronized (ps) {
                    ps.label = "Processing " + f.url;
                }
                updateStatus();

                if (d != null) {
                    final List<Download> downloads = RssUtil.findDownloads(d, state.getSeriesData(), f);
                    
                    SwingUtilities.invokeLater(new Runnable(){
                        public void run()
                        {
                            adg.updateFeed(f, downloads);
                        }
                    });
                } else {
                    failedFeeds.add("Unable to parse " + f.url);
                }
            } catch (RepositoryException re) {
                System.err.println(re);
                failedFeeds.add(f.url + ": " + re);
            } catch (SeriesDataException sde) {
                System.err.println(sde);
                failedFeeds.add(f.url + ": " + sde);
            } catch (IOException ioe) {
                // Report network error
                System.err.println(ioe);
                failedFeeds.add(f.url + ": " + ioe);
            } catch (URISyntaxException use) {
                // XXX Report configuration error
                System.err.println(use);
                failedFeeds.add(f.url + ": " + use);
            }
        }
        
        synchronized (ps) {
            ps.value = l.size();
            if (failedFeeds.isEmpty()) {
                ps.label = "Done.";
            } else {
                if (failedFeeds.size () < 3)
                    ps.label = "Failed to download: " + failedFeeds;
                else
                    ps.label = "Failed to download: " + failedFeeds.size() + " feeds";
            }
        }
        updateStatus();
    }

//    private static void copy(InputStream in, OutputStream out) throws IOException
//    {
//        byte[] ba = new byte[16384];
//        int l;
//        while ((l = in.read(ba)) >= 0) {
//            out.write(ba, 0, l);
//        }
//    }

    private Map<String, CookieDetails> cachedCookies = new HashMap<String, CookieDetails>();

    private static class CookieDetails
    {
        final String username, password;
        final List<String> cookies;
        
        final long stamp = System.currentTimeMillis();
        
        CookieDetails(String username, String password, List<String> cookies)
        {
            this.username = username;
            this.password = password;
            this.cookies = cookies;
        }
        
        boolean sameDetails(String username, String password)
        {
            return (this.username.equals(username) && this.password.equals(password));
        }
        
        boolean fresh(String username, String password)
        {
            return sameDetails(username, password) && (stamp + 3 * 60 * 60 * 1000L < System.currentTimeMillis());
        }
    }

    public static String safeFilename(URL url)
    {
        String s = url.toString();
        return s.replaceAll("[^-A-Za-z0-9]", "_");
    }

    private Document downloadFeed(final Feed f, ProgressStatus p) throws IOException, URISyntaxException
    {
        List<String> cookies = Collections.emptyList();
        
        if (f.loginPage != null) {
            synchronized (p) {
                p.label = "Logging in to " + f.loginPage;
            }
            synchronized (cachedCookies) {
                CookieDetails cd = cachedCookies.get(f.loginPage);
                if (cd == null || !cd.fresh(f.username, f.password)) {
                    cd = new CookieDetails(f.username, f.password,
                            HttpUtil.loginAndGetCookies(new URI(f.loginPage),
                            f.username, f.password));
                    cachedCookies.put(f.loginPage, cd);
                }
                cookies = cd.cookies;
            }
            synchronized (p) {
                p.label =  "Retrieved cookies (" + cookies.size() + ")";
            }
        }

        URL url = new URL(f.url);
        final String hostname = url.getHost();

        Authenticator.setDefault(new Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication()
            {
                if (hostname.equals(getRequestingHost()))
                    return new PasswordAuthentication(f.username, f.password.toCharArray());
                else
                    return null;
            }
        });

        Downloader dl = state.getDownloader();
        File localFile = new File(dl.getCacheDirectory(), safeFilename(url));

        dl.refresh(f.url, localFile, Downloader.HOUR_MILLIS, cookies);

        Document d = null;

        /* Try for RSS */
        try {
            synchronized (db) {
                d = db.parse(localFile);
            }
            if (!d.getDocumentElement().getTagName().equals("rss"))
                d = null;
        } catch (SAXException se) {
            // Wasn't an XML format
        }

        // Try scraping HTML
        if (d == null) {
            InputStream in = new FileInputStream(localFile);
            synchronized (db) {
                d = HtmlUtil.scrape(db, url.toString(), in);
            }
            in.close();
        }
        
        return d;
    }

}
