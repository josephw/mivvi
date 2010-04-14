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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.naming.ConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.kafsemo.mivvi.app.Startup;
import org.kafsemo.mivvi.rdf.Mivvi;
import org.kafsemo.mivvi.rdf.Presentation;
import org.kafsemo.mivvi.rdf.RdfMivviDataSource;
import org.kafsemo.mivvi.rdf.RdfUtil;
import org.kafsemo.mivvi.recognise.FilenameMatch;
import org.kafsemo.mivvi.recognise.FilenameProcessor;
import org.kafsemo.mivvi.recognise.Matching;
import org.kafsemo.mivvi.recognise.SeriesDataException;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.megginson.sax.XMLWriter;

/**
 * @author joe
 */
public class Annotator
{
    static RepositoryConnection createRepository() throws IOException,
            RDFParseException, RepositoryException
    {
        MemoryStore ms = new MemoryStore();

        SailRepository sr = new SailRepository(ms);
        
        RepositoryConnection cn = sr.getConnection();
        
        List<String> l = Startup.gatherDataURLs();
        Iterator<String> i = l.iterator();
        while (i.hasNext()) {
            String url = i.next();
            cn.add(new URL(url), url, org.openrdf.rio.RDFFormat.RDFXML);
        }

        return cn;
    }

    RepositoryConnection mviRep;
    FilenameProcessor<Resource> fp;
    Presentation pres;
    DocumentBuilder db;

    Annotator() throws ParserConfigurationException, IOException,
            RDFParseException, RepositoryException
    {
        mviRep = createRepository();
        fp = RdfMivviDataSource.processorFrom(mviRep);
        pres = new Presentation(mviRep);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        db = dbf.newDocumentBuilder();
    }

    public static void main(String[] args) throws ParserConfigurationException,
            FactoryConfigurationError, SAXException, TransformerException,
            IOException, ConfigurationException, RDFParseException,
            RepositoryException, SeriesDataException
    {
        if (args.length == 0) {
            System.err.println("Usage: Annotator <rss file> ...");
            System.err.println("Output is to like-named files with '-mivvi-annotated' appended");
            System.exit(0);
        }

        Annotator a = new Annotator();

        for (int i = 0; i < args.length; i++) {
            try {
                a.annotate(new File(args[i]));
            } catch (SAXException se) {
                System.err.println("Failed to process " + args[i] + ": " + se);
            }
        }
    }

    public static File annotatedName(File f)
    {
        String n = f.getName();
        int i = n.lastIndexOf('.');
        if (i < 0)
            i = n.length();

        File af = new File(f.getParentFile(), n.substring(0, i)
                + "-mivvi-annotated" + n.substring(i));
        
        return af;
    }
    
    public void annotate(File f) throws SAXException, IOException,
            TransformerException, RepositoryException, SeriesDataException
    {
        Document d = db.parse(f);
        
        annotate(d);
        
        Transformer t = TransformerFactory.newInstance().newTransformer();

        File af = annotatedName(f);
        XMLWriter xw = new XMLWriter(new FileWriter(af));
        xw.forceNSDecl(Mivvi.URI, "mvi");
        xw.forceNSDecl(RdfUtil.DC_URI, "dc");

        t.transform(new DOMSource(d), new SAXResult(xw));
    }

    public void annotate(Document d) throws TransformerException,
            RepositoryException, SeriesDataException
    {
        List<Element> l = RssUtil.findItems(d);

        Iterator<Element> i = l.iterator();
        while (i.hasNext()) {
            Element e = i.next();

            /* Skip if this already has Mivvi decoration */
            if (e.getElementsByTagNameNS(Mivvi.URI, "*").getLength() > 0)
                continue;
            
            String title = RssUtil.getItemTitle(e);

            FilenameMatch<Resource> fm = fp.processName(title);

            if (fm != null) {
                int ignoredLength = ignoredLength(fm);
                
                /**
                 * Usual length of a file typing. This needs to be smarter,
                 * and to use feed-specific heuristics. For example, skip
                 * anything in brackets, or unbroken by spaces.
                 */
                if (ignoredLength > 14) {
                    continue;
                }

                Presentation.Details det = pres.getDetailsFor(fm.episode);

                createAndAppend(d, e, Mivvi.URI, "episode", RdfUtil.resourceUri(fm.episode));
                createAndAppend(d, e, Mivvi.URI, "season", RdfUtil.resourceUri(det.season));
                createAndAppend(d, e, Mivvi.URI, "series", RdfUtil.resourceUri(det.series));
                createAndAppend(d, e, RdfUtil.DC_URI, "description", Presentation.filenameFor(det));
            }
        }
    }
    
    private static <X> int ignoredLength(FilenameMatch<X> fm)
    {
        int count = 0;

        Iterator<Matching<X>> i = fm.ignored.iterator();
        while (i.hasNext()) {
            Matching<X> m = i.next();
            count += m.matchLength();
        }
        
        return count;
    }

    private static void createAndAppend(Document d, Element item, String ns, String name,
            String value)
    {
        if (value == null)
            return;

        Element e = d.createElementNS(ns, name);
        e.appendChild(d.createTextNode(value));

        item.appendChild(e);
        item.appendChild(d.createTextNode("\n"));
    }
}