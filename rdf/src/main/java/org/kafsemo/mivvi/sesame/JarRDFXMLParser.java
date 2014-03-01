/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright © 2004-2014 Joseph Walton
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

import info.aduna.net.ParsedURI;

import java.io.IOException;
import java.io.InputStream;

import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.rdfxml.RDFXMLParser;

/**
 * A wrapper for {@link RDFXMLParser} that knows how relative jar: URIs work.
 * 
 * @author Joseph
 */
public class JarRDFXMLParser extends RDFXMLParser
{
    private String jarBase;
    private ParsedURI jarPath;
    
    @Override
    public synchronized void parse(InputStream in, String baseURI)
        throws IOException, RDFParseException, RDFHandlerException
    {
        jarBase = null;
        jarPath = null;
        
        // A special case check for jar: URIs
        if (baseURI.toLowerCase().startsWith("jar:")) {
                int i = baseURI.indexOf('!');
                if (i < 0) {
                    reportError("Bad jar: URI; no path specified",
                            BasicParserSettings.VERIFY_RELATIVE_URIS);
                } else {
                    jarBase = baseURI.substring(0, i + 1);
                    jarPath = new ParsedURI(baseURI.substring(i + 1));
                }
        }
        
        super.parse(in, baseURI);
    }
    
    @Override
    protected URI resolveURI(String uriSpec) throws RDFParseException
    {
        if(jarBase != null && jarPath != null) {
            ParsedURI uri = new ParsedURI(uriSpec);
        
            if (uri.isRelative() && !uri.isSelfReference()) {
                ParsedURI path = jarPath.resolve(uri);
                
                return createURI(jarBase + path.toString());
            }
        }
        
        return super.resolveURI(uriSpec);
    }
}
