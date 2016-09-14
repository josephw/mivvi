/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright © 2004-2016 Joseph Walton
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
 * License along with this library.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright © 2004-2016 Joseph Walton
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */

package org.kafsemo.mivvi.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;

/**
 * @author joe
 */
public class RdfUtil
{
    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    private static String SEQ_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#_";

    public static final String DC_URI = "http://purl.org/dc/elements/1.1/";

    public static final String DC(String n)
    {
        return DC_URI + n;
    }

    public static final String MVI(String n)
    {
        return Mivvi.URI + n;
    }

    public static int index(IRI u)
    {
        String s = u.toString();
        if (s.startsWith(SEQ_PREFIX)) {
            try {
                return Integer.parseInt(s.substring(SEQ_PREFIX.length()));
            } catch (NumberFormatException nfe) {
                // Fall through
            }
        }

        return -1;
    }

    public static class Dc
    {
        public static final IRI title = VF.createIRI(DC("title")),
            description = VF.createIRI(DC("description"));
        public static final IRI contributor = VF.createIRI(DC("contributor")),
            date = VF.createIRI(DC("date"));
        public static final IRI identifier = VF.createIRI(DC("identifier"));
        public static final IRI source = VF.createIRI(DC("source"));
    }

    public static class Mvi
    {
        public static final IRI Series = VF.createIRI(MVI("Series"));
        public static final IRI Season = VF.createIRI(MVI("Season"));
        public static final IRI Episode = VF.createIRI(MVI("Episode"));

        public static final IRI seasonNumber = VF.createIRI(MVI("seasonNumber"));
        public static final IRI episodes = VF.createIRI(MVI("episodes"));
        public static final IRI episodeNumber = VF.createIRI(MVI("episodeNumber"));
        public static final IRI seasons = VF.createIRI(MVI("seasons"));
        public static final IRI episode = VF.createIRI(MVI("episode"));

        public static final IRI productionCode = VF.createIRI(MVI("productionCode"));

// These two predicates indicate that a resource "is" a particular season or series
//  (for example, a DVD). They also indicate the season and series an episode belongs
//        to.
        public static final IRI season = VF.createIRI(MVI("season"));
        public static final IRI series = VF.createIRI(MVI("series"));
    }

    public static class Rdf
    {
        public static final IRI type = RDF.TYPE;
    }

    public static class Rdfs
    {
        public static final IRI seeAlso = RDFS.SEEALSO;
    }

    public static class Doap
    {
        public static final String BASE = "http://usefulinc.com/ns/doap#";

        public static final IRI downloadPage = VF.createIRI(BASE + "download-page");

        public static final IRI release = VF.createIRI(BASE + "release");
        public static final IRI revision = VF.createIRI(BASE + "revision");
    }

    public static class Owl
    {
        public static final IRI sameAs = OWL.SAMEAS;
    }

    public static Resource asResource(Value v)
    {
        if (v instanceof Resource) {
            return (Resource)v;
        } else {
            return null;
        }
    }

    public static IRI asUri(Value v)
    {
        if (v instanceof IRI) {
            return (IRI)v;
        } else {
            return null;
        }
    }

    public static String literalString(Value v)
    {
        if (v instanceof Literal) {
            return ((Literal)v).getLabel();
        } else {
            return null;
        }
    }

    public static String resourceUri(Value v)
    {
        if (v instanceof IRI) {
            return ((IRI) v).toString();
        } else {
            return null;
        }
    }

    public static String getStringProperty(RepositoryConnection cn, Resource res, IRI prop) throws RepositoryException
    {
        RepositoryResult<Statement> si = cn.getStatements(res, prop, null, false);
        while (si.hasNext()) {
            String s = literalString(si.next().getObject());
            if (s != null)
                return s;
        }
        return null;
    }

    public static Resource getResProperty(RepositoryConnection cn, Resource res, IRI prop) throws RepositoryException
    {
        RepositoryResult<Statement> si = cn.getStatements(res, prop, null, false);
        while (si.hasNext()) {
            Resource r = asResource(si.next().getObject());
            if (r != null)
                return r;
        }
        return null;
    }
}
