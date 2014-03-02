package org.kafsemo.mivvi.app;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.rio.RDFFormat;

public class TestStartup
{
    @Test
    public void rdfXmlRecognisedAsRdf()
    {
        assertTrue(Startup.isRdfFile("anything.rdf"));
        assertTrue(Startup.isRdfFile("anything.RDF"));
    }

    @Test
    public void otherFilesNotRecognisedAsRdf()
    {
        assertFalse(Startup.isRdfFile("anything.txt"));
        assertFalse(Startup.isRdfFile("anything.png"));
        assertFalse(Startup.isRdfFile("anything"));
    }

    @Test
    public void typeForRecognisesRdfXml()
    {
        assertEquals(RDFFormat.RDFXML, Startup.typeFor("anything.rdf"));
    }

    @Test
    public void typeForFailsForOtherTypes()
    {
        assertNull(Startup.typeFor("anything.txt"));
    }

    @Test
    public void recogniseTurtle()
    {
        assertTrue(Startup.isRdfFile("anything.ttl"));
        assertEquals(RDFFormat.TURTLE, Startup.typeFor("anything.ttl"));
    }

    @Test
    public void recogniseNTriples()
    {
        assertTrue(Startup.isRdfFile("anything.nt"));
        assertEquals(RDFFormat.NTRIPLES, Startup.typeFor("anything.nt"));
    }
}
