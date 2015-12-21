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

/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright © 2004-2014 Joseph Walton
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

import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * @author joe
 */
public class RdfUtil
{
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
    
    public static int index(URI u)
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
        public static final URI title = new URIImpl(DC("title")),
            description = new URIImpl(DC("description"));
        public static final URI contributor = new URIImpl(DC("contributor")),
            date = new URIImpl(DC("date"));
        public static final URI identifier = new URIImpl(DC("identifier"));
        public static final URI source = new URIImpl(DC("source"));
    }
    
    public static class Mvi
    {
        public static final URI Series = new URIImpl(MVI("Series"));
        public static final URI Season = new URIImpl(MVI("Season"));
        public static final URI Episode = new URIImpl(MVI("Episode"));

        public static final URI seasonNumber = new URIImpl(MVI("seasonNumber"));
        public static final URI episodes = new URIImpl(MVI("episodes"));
        public static final URI episodeNumber = new URIImpl(MVI("episodeNumber"));
        public static final URI seasons = new URIImpl(MVI("seasons"));
        public static final IRI episode = new URIImpl(MVI("episode"));

        public static final URI productionCode = new URIImpl(MVI("productionCode"));

// These two predicates indicate that a resource "is" a particular season or series
//  (for example, a DVD). They also indicate the season and series an episode belongs
//        to.
        public static final URI season = new URIImpl(MVI("season"));
        public static final URI series = new URIImpl(MVI("series"));
    }
    
    public static class Rdf
    {
        public static final URI type = RDF.TYPE;
    }
    
    public static class Rdfs
    {
        public static final URI seeAlso = RDFS.SEEALSO;
    }
    
    public static class Doap
    {
        public static final String BASE = "http://usefulinc.com/ns/doap#";
        
        public static final URI downloadPage = new URIImpl(BASE + "download-page");

        public static final URI release = new URIImpl(BASE + "release");
        public static final URI revision = new URIImpl(BASE + "revision");
    }

    public static class Owl
    {
        public static final String BASE = "http://www.w3.org/2002/07/owl#";
        
        public static final URI sameAs = new URIImpl(BASE + "sameAs");
    }
    
    public static Resource asResource(Value v)
    {
        if (v instanceof Resource) {
            return (Resource)v;
        } else {
            return null;
        }
    }
    
    public static URI asUri(Value v)
    {
        if (v instanceof URI) {
            return (URI)v;
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
        if (v instanceof URI) {
            return ((URI) v).toString();
        } else {
            return null;
        }
    }

    public static String getStringProperty(RepositoryConnection cn, Resource res, URI prop) throws RepositoryException
    {
        RepositoryResult<Statement> si = cn.getStatements(res, prop, null, false);
        while (si.hasNext()) {
            String s = literalString(si.next().getObject());
            if (s != null)
                return s;
        }
        return null;
    }

    public static Resource getResProperty(RepositoryConnection cn, Resource res, URI prop) throws RepositoryException
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
