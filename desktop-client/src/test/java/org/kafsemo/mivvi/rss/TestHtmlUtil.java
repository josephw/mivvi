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

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.kafsemo.mivvi.rss.HtmlUtil;
import org.kafsemo.mivvi.rss.RssUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestHtmlUtil extends TestCase
{
    private InputStream get(String name)
    {
        InputStream in = getClass().getResourceAsStream(name);
        assertNotNull("Expected file: " + name, in);
        return in;
    }
    
    public void testScrape() throws Exception
    {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        
        String base = "http://www.example.com/browse/";
        Document d = HtmlUtil.scrape(db, base, get("sample-browse.html"));
        assertNotNull(d);

        Element channel = RssUtil.findChannel(d);
        assertNotNull(channel);
        assertEquals("Sample Browse Page", RssUtil.getValue(channel, "title"));
        assertEquals(base, RssUtil.getValue(channel, "link"));
        
        NodeList nl = d.getElementsByTagName("item");
        assertEquals(3, nl.getLength());

        Element e;
        e = (Element) nl.item(0);
        assertEquals("http://www.example.com/first-file.torrent",
                RssUtil.getValue(e, "link"));
        assertEquals("First File", RssUtil.getValue(e, "title"));
    }
    
    public void testParseForm() throws Exception
    {
        List<HtmlUtil.Form> c = HtmlUtil.parseForms(new URL("http://www.example.com/login-page"), get("sample-login.html"));

        assertEquals(1, c.size());
        
        HtmlUtil.Form f = c.get(0);
        
        assertEquals("POST", f.getMethod());
        assertEquals(new URL("http://www.example.com/login"), f.action);

        assertEquals(1, f.inputs.size());
        assertEquals("username", f.inputs.get(0));

        assertEquals(1, f.passwords.size());
        assertEquals("password", f.passwords.get(0));

        assertEquals(1, f.hiddens.size());
        assertTrue(f.hiddens.containsKey("hiddenfield"));
        assertEquals("hiddenvalue", f.hiddens.get("hiddenfield"));
    }
}
