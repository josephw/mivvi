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

package org.kafsemo.mivvi.app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;

public class EpisodeResourceTestUtils
{
    private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

    /**
     * A utility class for creating temporary test files full of RDF.
     *
     * @author Joseph Walton
     */
    static class TempRdfFile
    {
        private final File f;
        private final Writer w;
        private RDFXMLWriter rxw;

        TempRdfFile() throws IOException
        {
            f = File.createTempFile("mivvi-test-temp", ".rdf");
            f.deleteOnExit();

            w = new FileWriter(f);
            rxw = new RDFXMLWriter(w);
            rxw.startRDF();
        }

        File endRxw() throws IOException, RDFHandlerException
        {
            rxw.endRDF();
            w.close();
            return f;
        }

        void writeStatement(Resource subject, IRI predicate, Value object)
            throws IOException, RDFHandlerException
        {
            rxw.handleStatement(VALUE_FACTORY.createStatement(subject, predicate, object));
        }
    }
}
