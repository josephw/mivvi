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

package org.kafsemo.mivvi.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.kafsemo.mivvi.rdf.Presentation;
import org.kafsemo.mivvi.rdf.RdfUtil;
import org.kafsemo.mivvi.recognise.FilenameMatch;
import org.kafsemo.mivvi.recognise.SeriesDataException;

public class RecogniseServlet extends MivviBaseServlet
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
        String pi = req.getPathInfo();
        if (pi == null || !pi.startsWith("/")) {
            /* Redirect to documentation */
            resp.sendRedirect(req.getContextPath());
            return;
        }

        // Strip leading slash
        pi = pi.substring(1);

        if (pi.equals("")) {
            /* Redirect to documentation */
            resp.sendRedirect(req.getContextPath());
            return;
        }

        try {
            FilenameMatch<Resource> fnm = sd.processName(pi);

            if (fnm == null) {
                // Return a 404
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "No resource found for: " + pi);
                return;
            }

            ValueFactory vf = SimpleValueFactory.getInstance();

            IRI myself = vf.createIRI(req.getRequestURL().toString());

            /* Generate output */
            Model g = new LinkedHashModel();

            g.add(myself, RdfUtil.Mvi.episode, fnm.episode);

            String aboutServletBase =
                req.getContextPath() + "/about";

            // Absolutise
            aboutServletBase = new java.net.URI(req.getRequestURL().toString()).resolve(aboutServletBase).toString();

            String seeAlso = seeAlsoUrl(aboutServletBase, fnm.episode.toString());

            g.add(myself, RdfUtil.Rdfs.seeAlso, vf.createIRI(seeAlso));

            writeGraphAsRdfXml(g, resp);
        } catch (SeriesDataException sde) {
            throw new ServletException(sde);
        } catch (RepositoryException e) {
            throw new ServletException(e);
        } catch (RDFHandlerException e) {
            throw new ServletException(e);
        } catch (URISyntaxException e) {
            throw new ServletException(e);
        }
    }

    public static String seeAlsoUrl(String base, String subject)
        throws IOException
    {
        return base + "?subject=" +
            URLEncoder.encode(subject, "utf-8");
    }
}
