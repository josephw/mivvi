package org.kafsemo.mivvi.sesame;

import org.eclipse.rdf4j.common.net.ParsedURI;

import org.eclipse.rdf4j.rio.turtle.TurtleParser;

public class JarTurtleParser extends TurtleParser
{
    @Override
    protected void setBaseURI(ParsedURI baseURI)
    {
        String s = baseURI.toString();

        // A special case check for jar: URIs
        if (JarAwareParsedURI.isJarUri(s)) {
            super.setBaseURI(new JarAwareParsedURI(s));
        } else {
            super.setBaseURI(baseURI);
        }
    }
}
