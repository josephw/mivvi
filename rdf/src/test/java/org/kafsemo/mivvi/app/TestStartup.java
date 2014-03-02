package org.kafsemo.mivvi.app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestStartup
{
    @Test
    public void rdfXmlRecognisedAsRdf()
    {
        assertTrue(Startup.isRdfFile("anything.rdf"));
    }

    @Test
    public void otherFilesNotRecognisedAsRdf()
    {
        assertFalse(Startup.isRdfFile("anything.txt"));
        assertFalse(Startup.isRdfFile("anything.png"));
        assertFalse(Startup.isRdfFile("anything"));
    }
}
