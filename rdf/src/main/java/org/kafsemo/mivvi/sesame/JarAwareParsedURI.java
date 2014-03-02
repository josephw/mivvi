package org.kafsemo.mivvi.sesame;

import java.util.Locale;

import info.aduna.net.ParsedURI;

public class JarAwareParsedURI extends ParsedURI
{
    private final String jarBase;
    private final ParsedURI jarPath;

    public JarAwareParsedURI(String urlSpec)
    {
        super(urlSpec);

        if (urlSpec.toLowerCase(Locale.ROOT).startsWith("jar:")) {
            int i = urlSpec.indexOf('!');
            if (i < 0) {
                throw new IllegalArgumentException("Bad jar: URI; no path specified: " + urlSpec);
            }

            this.jarBase = urlSpec.substring(0, i + 1);
            this.jarPath = new ParsedURI(urlSpec.substring(i + 1));
        } else {
            jarBase = null;
            jarPath = null;
        }
    }

    @Override
    public ParsedURI resolve(ParsedURI uri)
    {
        if (jarBase != null && uri.isRelative() && !uri.isSelfReference()) {
            ParsedURI path = jarPath.resolve(uri);

            return new JarAwareParsedURI(jarBase + path.toString());
        } else {
            return super.resolve(uri);
        }
    }
}
