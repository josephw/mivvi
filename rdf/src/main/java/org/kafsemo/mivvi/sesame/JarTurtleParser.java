package org.kafsemo.mivvi.sesame;

import info.aduna.net.ParsedURI;

import org.openrdf.rio.turtle.TurtleParser;

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
