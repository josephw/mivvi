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

package org.kafsemo.mivvi.rss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.kafsemo.mivvi.app.SeriesData;
import org.kafsemo.mivvi.rdf.Mivvi;
import org.kafsemo.mivvi.rdf.Presentation;
import org.kafsemo.mivvi.recognise.FilenameMatch;
import org.kafsemo.mivvi.recognise.SeriesDataException;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class RssUtil
{
    public static Element findChannel(Document d)
    {
        Element r = d.getDocumentElement();
        
        NodeList nl;
        nl = r.getElementsByTagName("channel");
        if (nl.getLength() < 1)
            return null;
        else
            return (Element)nl.item(0);
    }

    public static List<Element> findItems(Document d)
    {
        Element c = findChannel(d);
        if (c == null)
            return Collections.emptyList();

        NodeList nl;
    
        nl = c.getElementsByTagName("item");
        List<Element> l = new ArrayList<Element>(nl.getLength());
        for (int i = 0 ; i < nl.getLength() ; i++) {
            l.add((Element)nl.item(i));
        }
        
        return l;
    }
    
    public static String getItemTitle(Element e)
    {
        return getValue(e, "title");
    }
    
    private static String stringValue(Element e)
    {
        StringBuffer sb = new StringBuffer();

        NodeList nl = e.getChildNodes();
        for (int i = 0 ; i < nl.getLength() ; i++) {
            Node n = nl.item(0);
            if (n instanceof Text)
                sb.append(n.getNodeValue());
        }
        
        return sb.toString();
    }

    public static String getValue(Element e, String name)
    {
        NodeList nl = e.getChildNodes();
        for (int i = 0 ; i < nl.getLength() ; i++) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                Element ne = (Element)n;
                if (ne.getTagName().equals(name))
                    return stringValue(ne);
            }
        }
        
        return null;
    }

    public static List<String> mivviDetails(Element item)
    {
        List<String> l = new ArrayList<String>();

        NodeList nl = item.getChildNodes();
        for (int i = 0 ; i < nl.getLength() ; i++) {
            Node n = nl.item(i);
            if (n instanceof Element) {
                Element e = (Element)n;
                
                if (Mivvi.URI.equals(e.getNamespaceURI()))
                    l.add(stringValue(e));
            }
        }
        
        return l;
    }

    public static List<Download> findDownloads(Document doc, SeriesData sd, Feed fd)
        throws RepositoryException, SeriesDataException
    {
        Element channel = findChannel(doc);
        
        if (channel == null)
            return Collections.emptyList();

        String feedHostTitle = getValue(channel, "title");
        String feedHostUrl = getValue(channel, "link");

        List<Download> downloads = new ArrayList<Download>();

        Iterator<Element> i = findItems(doc).iterator();
        while (i.hasNext()) {
            Element e = i.next();
            
            // XXX Should check existing Mivvi details
            
            String title = getItemTitle(e);
            if (title == null)
                continue;
            
            FilenameMatch<Resource> fnm = sd.processName(title);
            if (fnm == null)
                continue;

            Presentation.Details details = sd.getDetailsFor(fnm.episode);
            if (details == null)
                continue;

            Download d = new Download(fd, fnm.episode, details);

            d.hostTitle = feedHostTitle;
            d.hostUrl = feedHostUrl;
            
            d.resourceUrl = getValue(e, "link");

            downloads.add(d);
        }
        
        return downloads;
    }
    
    public static Document saveFeedList(DocumentBuilder db, List<Feed> feeds)
    {
        Document d = db.newDocument();
        
        Element fs = d.createElement("feeds");
        
        Iterator<Feed> i = feeds.iterator();
        while (i.hasNext()) {
            Feed f = i.next();
            
            Element e = d.createElement("feed");
            e.setAttribute("url", f.url);

            if (f.loginPage != null) {
                e.setAttribute("loginPage", f.loginPage);
            }
            
            if (f.username != null) {
                e.setAttribute("username", f.username);
                e.setAttribute("password", f.password);
            }

            fs.appendChild(d.createTextNode("\n "));
            fs.appendChild(e);
        }

        fs.appendChild(d.createTextNode("\n"));
        d.appendChild(fs);
        
        return d;
    }

    public static List<Feed> loadFeedList(Document d)
    {
        NodeList nl = d.getElementsByTagName("feed");
        List<Feed> l = new ArrayList<Feed>(nl.getLength());
        for (int i = 0 ; i < nl.getLength() ; i++) {
            Element e = (Element)nl.item(i);
            
            String url = e.getAttribute("url");
            String loginPage = e.getAttribute("loginPage");
            if (loginPage.equals("")) {
                String username = e.getAttribute("username");
                if (username.equals("")) {
                    l.add(new Feed(url));
                } else {
                    l.add(new Feed(url, username, e.getAttribute("password")));
                }
            } else {
                l.add(new Feed(url, loginPage, e.getAttribute("username"), e.getAttribute("password")));
            }
        }
        
        return l;
    }
}
