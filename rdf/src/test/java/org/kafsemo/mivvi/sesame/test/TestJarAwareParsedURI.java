package org.kafsemo.mivvi.sesame.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

    @Test
    public void detectsJarUri()
    {
        assertTrue(JarAwareParsedURI.isJarUri("jar:"));
        assertTrue(JarAwareParsedURI.isJarUri("Jar:"));

        assertFalse(JarAwareParsedURI.isJarUri("jar"));
        assertFalse(JarAwareParsedURI.isJarUri("http:"));
    }

    @Test
    public void jarUrisAreAbsolute()
    {
        ParsedURI uri = new JarAwareParsedURI("jar:file:///some-file.jar!/some-nested-file");

        assertTrue(uri.isAbsolute());
        assertFalse(uri.isRelative());
        assertFalse(uri.isOpaque());
    }

    @Test
    public void x()
    {
        ParsedURI uri = new JarAwareParsedURI("jar:file:/Users/joe/Documents/source/mivvi/rdf/target/test-classes/org/kafsemo/mivvi/sesame/test/sample-data.zip!:/index.rdf");
// jar:file:/Users/joe/Documents/source/mivvi/rdf/target/test-classes/org/kafsemo/mivvi/sesame/test/sample-data.zip!:/empty-file.txt
        System.out.println(uri.toString());
    }
}
