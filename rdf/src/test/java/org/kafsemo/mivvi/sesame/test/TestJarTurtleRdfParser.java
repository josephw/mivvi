package org.kafsemo.mivvi.sesame.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.kafsemo.mivvi.sesame.JarTurtleParser;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

public class TestJarTurtleRdfParser
{
    /**
     * Ensure that relative paths in RDF from a jar: are resolved correctly.
     */
    @Test
    public void testLoading() throws Exception
    {
        URL zipfileUrl = TestJarRDFXMLParser.class.getResource("sample-with-turtle-data.zip");

        assertNotNull("The sample-data.zip file must be present for this test", zipfileUrl);

        String url = "jar:" + zipfileUrl + "!/index.ttl";

        RDFParser parser;
        parser = new JarTurtleParser();

        StatementCollector sc = new StatementCollector();
        parser.setRDFHandler(sc);

        InputStream in = new URL(url).openStream();

        parser.parse(in, url);

        Collection<Statement> stmts = sc.getStatements();

        assertEquals("There should be exactly one statement in index.ttl", 1, stmts.size());

        Statement stmt = stmts.iterator().next();

        Resource res = (Resource) stmt.getObject();

        String resourceUrl = res.stringValue();

        assertThat(resourceUrl, CoreMatchers.startsWith("jar:" + zipfileUrl + "!"));

        URL javaUrl = new URL(resourceUrl);
        assertEquals("jar", javaUrl.getProtocol());

        InputStream uc = javaUrl.openStream();
        assertEquals("The resource stream should be empty", -1, uc.read());
        uc.close();
    }
}
