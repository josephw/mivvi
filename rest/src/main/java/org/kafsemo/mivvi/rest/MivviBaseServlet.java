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

package org.kafsemo.mivvi.rest;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.kafsemo.mivvi.app.FileUtil;
import org.kafsemo.mivvi.app.SeriesData;
import org.kafsemo.mivvi.rdf.Mivvi;
import org.kafsemo.mivvi.rdf.RdfUtil;
import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class MivviBaseServlet extends HttpServlet
{
    SailRepository rep;
    SeriesData sd;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        String dataPath = config.getServletContext().getInitParameter("mivviDataPath");

        try {
            MemoryStore ms = new MemoryStore();
            ms.initialize();
            rep = new SailRepository(ms);
            this.sd = new SeriesData();
            sd.initMviRepository(rep);

            File base = new File(dataPath);

            Collection<File> fns = FileUtil.gatherFilenames(base);
            for (File f : fns) {
                if (f.getName().endsWith(".rdf")) {
                    sd.importMivvi(f);
                }
            }
        } catch (SailException e) {
            throw new ServletException(e);
        } catch (RepositoryException e) {
            throw new ServletException(e);
        } catch (RDFParseException e) {
            throw new ServletException(e);
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    void writeGraphAsRdfXml(Graph g, HttpServletResponse resp)
        throws IOException, RDFHandlerException
    {
        resp.setContentType("application/rdf+xml");

        RDFXMLPrettyWriter rxw = new RDFXMLPrettyWriter(resp.getOutputStream());
        rxw.handleNamespace("mvi", Mivvi.URI);
        rxw.handleNamespace("dc", RdfUtil.DC_URI);

        rxw.startRDF();

        for (Statement s : g) {
            rxw.handleStatement(s);
        }

        rxw.endRDF();
        rxw.close();
    }

    void sendError(HttpServletResponse resp, int status, String message)
        throws IOException
    {
        resp.setStatus(status);

        resp.setContentType("text/plain");

        PrintWriter pw = resp.getWriter();
        pw.print(message);
        pw.close();
    }
}
