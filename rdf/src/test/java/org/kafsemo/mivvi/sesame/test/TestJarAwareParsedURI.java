package org.kafsemo.mivvi.sesame.test;

import info.aduna.net.ParsedURI;

import org.junit.Assert;
import org.junit.Test;
import org.kafsemo.mivvi.sesame.JarAwareParsedURI;

public class TestJarAwareParsedURI
{
    private static void assertEquals(String expected, ParsedURI actual)
    {
        Assert.assertEquals(expected, actual.toString());
    }

    @Test
    public void behavesAsNormalWithRelativeReferenceForNonJarUri()
    {
        ParsedURI uri = new JarAwareParsedURI("http://example.test/");
        assertEquals("http://example.test/index.rdf", uri.resolve("index.rdf"));
    }

    @Test
    public void behavesAsNormalWithAbsoluteReferenceForNonJarUri()
    {
        ParsedURI uri = new JarAwareParsedURI("http://example.test/");
        assertEquals("http://example2.test/", uri.resolve("http://example2.test/"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsWithJarUriWithoutPathDelimiter()
    {
        new JarAwareParsedURI("jar:file:///some-file.jar");
    }

    @Test
    public void acceptsAJarUriWithPathDelimiter()
    {
        new JarAwareParsedURI("jar:file:///some-file.jar!/some-nested-file");
    }

    @Test
    public void resolvesAnAbsoluteUriRelativeToABaseJarUri()
    {
        ParsedURI uri = new JarAwareParsedURI("jar:file:///some-file.jar!/some-nested-file");

        assertEquals("http://example.test/", uri.resolve("http://example.test/"));
    }

    @Test
    public void resolvesAPathRelativeUriRelativeToABaseJarUri()
    {
        ParsedURI uri = new JarAwareParsedURI("jar:file:///some-file.jar!/some-nested-file");

        assertEquals("jar:file:///some-file.jar!/another-file", uri.resolve("another-file"));
    }

    @Test
    public void resolvesAPathAbsoluteUriRelativeToABaseJarUri()
    {
        ParsedURI uri = new JarAwareParsedURI("jar:file:///some-file.jar!/nested-directory/some-nested-file");

        assertEquals("jar:file:///some-file.jar!/another-file", uri.resolve("/another-file"));
    }
}
