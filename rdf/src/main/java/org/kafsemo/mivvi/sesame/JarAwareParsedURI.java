package org.kafsemo.mivvi.sesame;

import java.util.Locale;

import info.aduna.net.ParsedURI;

public class JarAwareParsedURI extends ParsedURI
{
    private final String jarBase;
    private final ParsedURI jarPath;

    public static boolean isJarUri(String urlSpec)
    {
        return urlSpec.toLowerCase(Locale.ROOT).startsWith("jar:");
    }

    public JarAwareParsedURI(String urlSpec)
    {
        super(urlSpec);

        if (isJarUri(urlSpec)) {
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
    public boolean isOpaque()
    {
        return jarBase == null;
    }

    public String toString()
    {
        if (jarBase != null) {
            return jarBase + jarPath;
        } else {
            return super.toString();
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
