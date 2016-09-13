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

package org.kafsemo.mivvi.sesame;

import org.eclipse.rdf4j.common.net.ParsedURI;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParser;

/**
 * A wrapper for {@link RDFXMLParser} that knows how relative jar: URIs work.
 */
public class JarRDFXMLParser extends RDFXMLParser
{
    private ParsedURI base;

    @Override
    public synchronized void parse(InputStream in, String baseURI)
        throws IOException, RDFParseException, RDFHandlerException
    {
        // A special case check for jar: URIs
        if (JarAwareParsedURI.isJarUri(baseURI)) {
            base = new JarAwareParsedURI(baseURI);
        } else {
            base = null;
        }

        super.parse(in, baseURI);
    }

    @Override
    protected IRI resolveURI(String uriSpec) throws RDFParseException
    {
        if (base != null) {
            return createURI(base.resolve(uriSpec).toString());
        } else {
            return super.resolveURI(uriSpec);
        }
    }
}
