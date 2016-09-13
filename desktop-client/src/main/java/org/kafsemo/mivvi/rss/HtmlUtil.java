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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.parser.ParserDelegator;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HtmlUtil
{
    public static Document scrape(DocumentBuilder db, String base, InputStream stream) throws IOException, URISyntaxException
    {
        final URL uri = new URL(base);

        final Document d = db.newDocument();

        Element rss = d.createElement("rss");
        rss.setAttribute("version", "2.0");
        
        final Element channel = d.createElement("channel");

        final StringBuffer pageTitle = new StringBuffer();

        HTMLEditorKit.ParserCallback cb = new HTMLEditorKit.ParserCallback(){
            final StringBuffer sb = new StringBuffer();
            
            public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos)
            {
                handleStartTag(t, a, pos);
                handleEndTag(t, pos);
            }

            String href = null;

            public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
            {
                if (t.equals(Tag.A) || t.equals(Tag.TITLE))
                    sb.setLength(0);

                if (t.equals(Tag.A)) {
                    this.href = (String)a.getAttribute(HTML.Attribute.HREF);
                }
            }
            
            Set<String> urlsIncluded = new HashSet<String>();

            public void handleEndTag(Tag t, int pos)
            {
                if (t.equals(Tag.TITLE)) {
                    if (sb.length() > pageTitle.length()) {
                        pageTitle.setLength(0);
                        pageTitle.append(sb);
                    }
                    
                    sb.setLength(0);
                } else if (t.equals(Tag.A)) {
                    if (href == null)
                        return;
                    
                    if (href.toLowerCase().endsWith(".torrent")
                            || href.indexOf("?info_hash=") >= 0
                            || (href.indexOf('.') < 0) && href.indexOf("/torrent/") >= 0)
                    {
                        try {
                            URL u = new URL(uri, href);
                            href = u.toString();

                            /* Skip already-processed URLs */
                            if (urlsIncluded.contains(href)) {
                                return;
                            }
                            
                            Element item = d.createElement("item");

                            
                            String s;
                            
                            try {
                                s = URLDecoder.decode(u.getPath(), "UTF-8");
                            } catch (IOException ioe) {
                                s = u.getPath();
                            }

                            int i = s.lastIndexOf('/');
                            if (i >= 0)
                                s = s.substring(i + 1);
    
                            if (s.endsWith(".torrent"))
                                s = s.substring(0, s.length() - ".torrent".length());
                            
                            if (sb.length() > 0) {
                                s = sb.toString();
                                sb.setLength(0);
                            }
    
                            Element title = d.createElement("title");
                            title.appendChild(d.createTextNode(s));
                            item.appendChild(title);
    
                            Element link = d.createElement("link");
                            link.appendChild(d.createTextNode(href));
    
                            item.appendChild(link);
                            channel.appendChild(item);
                            
                            urlsIncluded.add(href);
                        } catch (MalformedURLException mue) {
                            // Silently discard
                        }
                    }
                }
            }
            
            public void handleText(char[] data, int pos)
            {
                sb.append(data);
            }
            
        };

        Element link = d.createElement("link");
        link.appendChild(d.createTextNode(base));
        channel.appendChild(link);

        new ParserDelegator().parse(new InputStreamReader(stream), cb, true);
        
        if (pageTitle.length() > 0) {
            Element t = d.createElement("title");
            t.appendChild(d.createTextNode(pageTitle.toString()));
            
            channel.appendChild(t);
        }

        rss.appendChild(channel);
        d.appendChild(rss);

        return d;
    }

    public static List<Form> parseForms(final URI base, InputStream stream) throws IOException
    {
        final List<Form> forms = new ArrayList<Form>();

        HTMLEditorKit.ParserCallback cb = new HTMLEditorKit.ParserCallback() {
            Form f = null;

            public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos)
            {
                handleStartTag(t, a, pos);
                handleEndTag(t, pos);
            }

            public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
            {
                if (t.equals(Tag.FORM)) {
                    String method = (String)a.getAttribute(HTML.Attribute.METHOD);
                    if (!"POST".equalsIgnoreCase(method))
                        return;

                    f = new Form();
                    f.method = method;

                    String action = (String)a.getAttribute(HTML.Attribute.ACTION);
                    if (action != null) {
                        f.action = base.resolve(action);
                    } else {
                        f.action = base;
                    }
                    
                    forms.add(f);
                } else if (t.equals(Tag.INPUT)) {
                    if (f == null)
                        return;

                    String name = (String)a.getAttribute(HTML.Attribute.NAME);
                    if (name == null)
                        return;

                    String type = (String)a.getAttribute(HTML.Attribute.TYPE);
                    if (type == null)
                        return;
                    
                    type = type.toLowerCase();
                    if (type.equals("text"))
                        f.inputs.add(name);
                    else if (type.equals("password"))
                        f.passwords.add(name);
                    else if (type.equals("hidden")) {
                        f.hiddens.put(name, (String)a.getAttribute(HTML.Attribute.VALUE));
                    }
                }
            }
            
            public void handleEndTag(Tag t, int pos)
            {
                if (t.equals(Tag.FORM))
                    this.f = null;
            }
        };

        new ParserDelegator().parse(new InputStreamReader(stream), cb, true);
        
        return forms;
    }

    public static class Form
    {
        String method;
        public String getMethod()
        {
            return (method != null) ? method.toUpperCase() : null;
        }

        public URI action;
        
        public List<String> inputs = new ArrayList<String>();
        public List<String> passwords = new ArrayList<String>();
        public Map<String, String> hiddens = new HashMap<String, String>();
    }
}
