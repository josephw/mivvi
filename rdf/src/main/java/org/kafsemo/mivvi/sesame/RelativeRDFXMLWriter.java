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

package org.kafsemo.mivvi.sesame;

import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;

/**
 * A decorator for RDF/XML writing that relativises subject URIs, where possible,
 * according to a given base URI.
 *
 * @author Joseph Walton
 */
public class RelativeRDFXMLWriter extends RDFXMLWriter
{
    private final java.net.URI base;

    public RelativeRDFXMLWriter(OutputStream out, URI baseUri)
    {
        super(out);
        base = baseUri;
    }

    @Override
    public void handleStatement(Statement stmt) throws RDFHandlerException
    {
        String s = stmt.getSubject().toString();

        try {
            String rel = base.relativize(new java.net.URI(s)).toString();

            if (!s.equals(rel)) {
                stmt = SimpleValueFactory.getInstance().createStatement(new FakeURI(s, rel), stmt.getPredicate(), stmt.getObject());
            }
        } catch (URISyntaxException use) {
            // Ignore - leave as-is
        }

        super.handleStatement(stmt);
    }

    private static final class FakeURI extends SimpleIRI
    {
        private final String relUri;

        public FakeURI(String uriString, String relUri)
        {
            super(uriString);
            this.relUri = relUri;
        }

        @Override
        public String toString()
        {
            return relUri;
        }
    }
}
