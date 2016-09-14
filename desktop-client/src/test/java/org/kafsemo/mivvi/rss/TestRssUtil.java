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
 * License along with this library.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.rss;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.kafsemo.mivvi.rss.Feed;
import org.kafsemo.mivvi.rss.RssUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class TestRssUtil extends TestCase
{
    private InputStream get(String name)
    {
        InputStream in = getClass().getResourceAsStream(name);
        assertNotNull("Expected file: " + name, in);
        return in;
    }
    
    public void testFindItems() throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = db.parse(get("simple-rss.xml"));
        
        List<Element> l = RssUtil.findItems(d);
        
        assertEquals(2, l.size());
    }

    public void testGetItemTitle() throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = db.parse(get("simple-rss.xml"));
        
        List<Element> l = RssUtil.findItems(d);
        
        Element e;
        
        e = l.get(0);
        assertNull(RssUtil.getItemTitle(e));
        
        e = l.get(1);
        assertEquals("Item Title", RssUtil.getItemTitle(e));
    }

    public void testGetValue() throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = db.parse(get("simple-rss.xml"));
        
        Element channel = RssUtil.findChannel(d);
        
        assertNotNull(channel);

        String title = RssUtil.getValue(channel, "title");
        
        assertEquals("Sample Feed", title);
    }

    public void testGetMivvi() throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document d = db.parse(get("mivvi-rss.xml"));
        
        List<Element> l = RssUtil.findItems(d);
        assertEquals(3, l.size());

        Element e;

        List<String> md;
        
        e = l.get(0);
        md = RssUtil.mivviDetails(e);
        assertEquals(
                Arrays.asList("http://www.example.com/episodes/1#"),
                md);
        
        e = l.get(1);
        md = RssUtil.mivviDetails(e);
        assertEquals(Arrays.asList(
                "http://www.example.com/#",
                "http://www.example.com/episodes/1#"),
                md);
        
        e = l.get(2);
        md = RssUtil.mivviDetails(e);
        assertEquals(0, md.size());
    }
    
    public void testFeedListXML() throws ParserConfigurationException
    {
        List<Feed> l = Arrays.asList(
        new Feed("http://www.example.com/"),
        new Feed("http://www.example.net/", "http://www.example.net/login", "user", "pass"),
        new Feed("http://localhost/", "user", "pass"));
        
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        
        Document d = RssUtil.saveFeedList(db, l);
        assertNotNull(d);
        
        assertEquals(3, d.getElementsByTagName("feed").getLength());
        
        List<Feed> l2 = RssUtil.loadFeedList(d);
        
        assertNotNull(l2);
        assertEquals(3, l2.size());
        Feed f;
        f = l2.get(0);
        assertEquals("http://www.example.com/", f.url);
        assertNull(f.loginPage);
        assertNull(f.username);
        assertNull(f.password);
        
        f = l2.get(1);
        assertEquals("http://www.example.net/", f.url);
        assertEquals("http://www.example.net/login", f.loginPage);
        assertEquals("user", f.username);
        assertEquals("pass", f.password);
        
        f = l2.get(2);
        assertEquals("http://localhost/", f.url);
        assertNull(f.loginPage);
        assertEquals("user", f.username);
        assertEquals("pass", f.password);
    }
}
