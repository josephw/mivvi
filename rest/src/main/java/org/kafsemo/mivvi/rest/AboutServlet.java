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

package org.kafsemo.mivvi.rest;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kafsemo.mivvi.rdf.Presentation;
import org.kafsemo.mivvi.rdf.RdfUtil;
import org.kafsemo.mivvi.rdf.Presentation.Details;
import org.openrdf.model.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;

public class AboutServlet extends MivviBaseServlet
{
    Presentation pres;
    
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        
        try {
            pres = new Presentation(rep.getConnection());
        } catch (RepositoryException e) {
            throw new ServletException(e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        String subject = req.getParameter("subject");
        
        if (subject == null || subject.equals("")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "You must provide a subject URI.");
            return;
        }
        
        try {
            URI uri = new URIImpl(subject);
            
            /* Generate output */
            Graph g = new GraphImpl();

            StringBuffer myself = req.getRequestURL();
            String q = req.getQueryString();
            if (q != null) {
                myself.append('?');
                myself.append(q);
            }
            
            g.add(new URIImpl(myself.toString()),
                    RdfUtil.Dc.title,
                    new LiteralImpl("Mivvi data about " + subject));
                    
            /* Is it an episode? */
            if (sd.hasType(uri, RdfUtil.Mvi.Episode)) {
                sd.exportRelevantStatements(g, uri);
                
                Details details = pres.getDetailsFor(uri);
                if (details != null) {
                    g.add(uri, RdfUtil.Mvi.season, details.season);
                    g.add(uri, RdfUtil.Mvi.series, details.series);

                    sd.exportStatementsAbout(g, details.season);
                    sd.exportStatementsAbout(g, details.series);
                }
            } else {
                /* Generic facts */
                sd.exportStatementsAbout(g, uri);
            }

//            localFiles.exportRelevantStatements(g, res);
            
            writeGraphAsRdfXml(g, resp);
        } catch (RepositoryException e) {
            throw new ServletException(e);
        } catch (RDFHandlerException e) {
            throw new ServletException(e);
        } catch (IllegalArgumentException ie) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Bad subject URI: " + subject);
        }
    }
}
