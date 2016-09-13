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
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.RDFInserter;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.ntriples.NTriplesParser;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.kafsemo.mivvi.rdf.HashUris;
import org.kafsemo.mivvi.rdf.IdentifierMappings;
import org.kafsemo.mivvi.rdf.Presentation;
import org.kafsemo.mivvi.rdf.Presentation.Details;
import org.kafsemo.mivvi.rdf.RdfMivviDataSource;
import org.kafsemo.mivvi.rdf.RdfUtil;
import org.kafsemo.mivvi.recognise.FilenameMatch;
import org.kafsemo.mivvi.recognise.FilenameProcessor;
import org.kafsemo.mivvi.recognise.SeriesDataException;
import org.kafsemo.mivvi.sesame.JarRDFXMLParser;
import org.kafsemo.mivvi.sesame.JarTurtleParser;

public class SeriesData
{
//    private Repository mviRep;
    RepositoryConnection mviRepCn;
    RdfMivviDataSource mviDataSource;

    private FilenameProcessor<Resource> fp;
    private Presentation presentation = null;


    public synchronized void initMviRepository() throws IOException, RepositoryException
    {
        Sail sail;

        sail = new MemoryStore();
        /*
        RepositoryConfig cfg = new RepositoryConfig("mviRepository", "MVI repository");
        cfg.addSail(new NativeRdfRepositoryConfig(Const.MVI_REPOSITORY));

        cfg.setWorldReadable(true);
        cfg.setWorldWriteable(true);

        mviRep = Sesame.getService().createRepository(cfg);
        */

        SailRepository rep = new SailRepository(sail);
        rep.initialize();
        initMviRepository(rep);
    }

    public synchronized void initMviRepository(Repository rep) throws IOException, RepositoryException
    {
//        this.mviRep = rep;
        mviRepCn = rep.getConnection();
        mviDataSource = new RdfMivviDataSource(rep.getConnection());
        fp = new FilenameProcessor<Resource>(mviDataSource, mviDataSource);
        presentation = new Presentation(mviRepCn);
    }

    public synchronized void closeMviRepository() throws RepositoryException
    {
        presentation = null;
        fp = null;
        mviRepCn.close();
//        mviRep.shutDown();
    }

    public synchronized String getTitle(Resource res) throws RepositoryException
    {
        return RdfUtil.getStringProperty(mviRepCn, res, RdfUtil.Dc.title);
    }

    private static final Resource[] RA = {};

    public synchronized Resource[] getAllSeries() throws RepositoryException
    {
//        ValueFactory vf = mviRepGraph.getValueFactory();

        ArrayList<Resource> al = new ArrayList<Resource>(32);

        RepositoryResult<Statement> si = mviRepCn.getStatements(null, RdfUtil.Rdf.type, RdfUtil.Mvi.Series, true);

        while (si.hasNext()) {
            Statement stmt = si.next();

            if (stmt.getSubject() != null)
                al.add(stmt.getSubject());
        }

        return al.toArray(RA);
    }

    public synchronized Resource[] getCollectionIds(Resource uri, IRI pred)
        throws RepositoryException
    {
        RepositoryResult<Statement> si;

        Resource c = RdfUtil.getResProperty(mviRepCn, uri, pred);

        if (c == null)
            return RA;

        SortedMap<Integer, Value> sm = new TreeMap<Integer, Value>();

        si = mviRepCn.getStatements(c, null, null, true);
        while (si.hasNext()) {
            Statement s = si.next();

            int i = RdfUtil.index(s.getPredicate());
            if (i >= 0) {
                sm.put(Integer.valueOf(i), s.getObject());
            }
        }

        return sm.values().toArray(RA);
    }

    public synchronized Resource[] getSeasons(Resource series) throws RepositoryException
    {
        return getCollectionIds(series, RdfUtil.Mvi.seasons);
    }

    public synchronized Resource[] getEpisodes(Resource season) throws RepositoryException
    {
        return getCollectionIds(season, RdfUtil.Mvi.episodes);
    }

    public synchronized void importMivvi(File file) throws MalformedURLException, IOException, RDFParseException, RepositoryException
    {
        URL u = file.toURI().toURL();
        mviRepCn.add(file, u.toString(), RDFFormat.RDFXML);
    }

    public synchronized void importMivvi(InputStream in, String uri) throws RDFParseException, RepositoryException, IOException
    {
        mviRepCn.add(in, uri, RDFFormat.RDFXML);
    }

    public synchronized void importMivvi(String url) throws MalformedURLException, IOException, RDFParseException, RepositoryException
    {
        URL u = new URL(url);

        RDFInserter inserter = new RDFInserter(mviRepCn);

        RDFParser parser;

        RDFFormat format = Startup.typeFor(url);

        if (format == RDFFormat.RDFXML) {
            parser = new JarRDFXMLParser();
        } else if (format == RDFFormat.TURTLE) {
            parser = new JarTurtleParser();
        } else if (format == RDFFormat.NTRIPLES) {
            parser = new NTriplesParser();
        } else {
            throw new RDFParseException("Unexpected RDF format: " + format);
        }

        parser.setRDFHandler(inserter);

        try {
            parser.parse(u.openStream(), url);
        } catch (RDFHandlerException e) {
            // RDFInserter only throws wrapped RepositoryExceptions
            throw (RepositoryException)e.getCause();
        }
    }

    public static final URI ROOT_IDENTIFIER = new URIImpl("http://en.wikipedia.org/wiki/Television");

    public static URI getRootIdentifier()
    {
        return ROOT_IDENTIFIER;
    }

    public synchronized String getFullEpisodeTitle(Resource res) throws RepositoryException
    {
        return presentation.getFilenameFor(res);
    }

    public synchronized List<String> getStringList(Resource res, URI pred) throws RepositoryException
    {
        List<String> l = new ArrayList<String>();

        RepositoryResult<Statement> si = mviRepCn.getStatements(res, pred, null, true);
        while (si.hasNext()) {
            Value v = si.next().getObject();
            if (v instanceof Literal)
                l.add(((Literal)v).getLabel());
        }

        Collections.sort(l);
        return l;
    }

    public synchronized String getStringProperty(Resource res, IRI p) throws RepositoryException
    {
        return RdfUtil.getStringProperty(mviRepCn, res, p);
    }

    public synchronized List<NamedResource> getContributors(Resource res) throws RepositoryException
    {
        List<NamedResource> l = new ArrayList<NamedResource>();

        RepositoryResult<Statement> si = mviRepCn.getStatements(res, RdfUtil.Dc.contributor, null, true);
        while (si.hasNext()) {
            Value v = si.next().getObject();
            if (v instanceof Resource) {
                Resource c = (Resource)v;

                String s = getNameOf(c);

                if (s != null) {
                    NamedResource nr = new NamedResource();
                    nr.res = c;
                    nr.name = s;

                    l.add(nr);
                }
            }
        }

        return l;
    }

    public static class NamedResource
    {
        Resource res;
        String name;

        public Resource getResource()
        {
            return res;
        }

        public String getName()
        {
            return name;
        }
    }

    private String getNameOf(Resource contributor) throws RepositoryException
    {
        String s = getStringProperty(contributor, RdfUtil.Dc.title);
        if (s == null)
            s = getStringProperty(contributor, RdfMiscVocabulary.foafName);
        return s;
    }

    public synchronized String getSeasonNumber(Resource s) throws RepositoryException
    {
        return getStringProperty(s, RdfUtil.Mvi.seasonNumber);
    }

    public synchronized void getResourcesFor(Resource res, Collection<Resource> c)
        throws RepositoryException
    {
        RepositoryResult<Statement> si;

        si = mviRepCn.getStatements(null, RdfUtil.Mvi.episode, res, true);
        while (si.hasNext()) {
            c.add(si.next().getSubject());
        }
    }

    public synchronized FilenameMatch<Resource> process(File f) throws RepositoryException, SeriesDataException
    {
        return fp.process(f);
    }

    public synchronized FilenameMatch<Resource> processName(String name) throws RepositoryException, SeriesDataException
    {
        return fp.processName(name);
    }

    public synchronized Details getDetailsFor(Resource episode) throws RepositoryException
    {
        return presentation.getDetailsFor(episode);
    }
/*
    public LocalRepository getMivviRepository()
    {
        return mviRep;
    }
*/
    public synchronized Resource getSeries(String title) throws RepositoryException, SeriesDataException
    {
        return fp.getSeries(title);
    }

    public synchronized Resource getEpisodeByNumericSequence(Resource series, String epTitle)
        throws RepositoryException, SeriesDataException
    {
        return fp.getEpisodeByNumericSequence(series, epTitle);
    }

    public synchronized Resource getEpisodeByTitleApprox(Resource series, String epTitle)
        throws RepositoryException, SeriesDataException
    {
        return fp.getEpisodeByTitleApprox(series, epTitle);
    }

    /**
     * Export all statements relevant to a specific episode into another graph.
     *
     * @param g
     * @param episode
     * @throws RepositoryException
     */
    public synchronized void exportRelevantStatements(Graph g, Resource episode) throws RepositoryException
    {
        RepositoryResult<Statement> si;

        si = mviRepCn.getStatements(episode, null, null, true);
        while (si.hasNext()) {
            Statement s = si.next();
            g.add(s);
        }

        si = mviRepCn.getStatements(null, RdfUtil.Mvi.episode, episode, true);
        while (si.hasNext()) {
            Statement s = si.next();
            g.add(s);
        }
    }

    synchronized void extractEpisodeResources(Resource episode, Collection<? super SkuEpisodeResource> c)
        throws RepositoryException
    {
        RepositoryResult<Statement> si;

        Collection<Resource> resl = new ArrayList<Resource>();

        si = mviRepCn.getStatements(null, RdfUtil.Mvi.episode, episode, true);
        while (si.hasNext()) {
            Statement s = si.next();

            resl.add(s.getSubject());
        }

        // Resource -> Set
        Map<Resource, Set<Resource>> equivs = new HashMap<Resource, Set<Resource>>();

        Iterator<Resource> i;

        i = resl.iterator();
        while (i.hasNext()) {
            Resource r = i.next();

            Set<Resource> s = equivs.get(r);
            if (s == null) {
                s = new HashSet<Resource>();
                s.add(r);
                equivs.put(r, s);
            }

            Collection<Resource> linked = new ArrayList<Resource>();
            linked.add(r);
            findAllLinkedIdentifiers(r, linked);

            Iterator<Resource> j = linked.iterator();
            while (j.hasNext()) {
                Resource ir = j.next();

                Set<Resource> s2 = equivs.get(ir);

                Set<Resource> combined = new HashSet<Resource>();
                combined.addAll(s);

                if (s2 != null) {
                    combined.addAll(s2);
                }

                combined.add(ir);

                Iterator<Resource> ci = combined.iterator();
                while (ci.hasNext()) {
                    equivs.put(ci.next(), combined);
                }
            }
        }

        Iterator<Set<Resource>> ei = new HashSet<Set<Resource>>(equivs.values()).iterator();
        while (ei.hasNext()) {
            Set<Resource> equivClass = ei.next();

            SkuEpisodeResource sr = new SkuEpisodeResource();

            Iterator<Resource> ri = equivClass.iterator();

            while (ri.hasNext()) {
                Resource ir = ri.next();

                if (SkuEpisodeResource.isIsbn(ir)) {
                    sr.isbn = ir;
                } else if (SkuEpisodeResource.isAsin(ir)) {
                    sr.asin = ir;
                }
            }

            if (sr.isbn != null || sr.asin != null) {
                entitle(sr);
                c.add(sr);
            }
        }
    }

    /**
     * Extract resources that are web locations, rather than local files
     * or identifiers.
     *
     * @param episode
     * @param c
     * @throws RepositoryException
     */
    synchronized void extractWebEpisodeResources(Resource episode, Collection<? super WebEpisodeResource> c) throws RepositoryException
    {
        RepositoryResult<Statement> si = mviRepCn.getStatements(null, RdfUtil.Mvi.episode, episode, true);
        while (si.hasNext()) {
            Statement stmt = si.next();

            IRI uri = RdfUtil.asUri(stmt.getSubject());

            if (WebEpisodeResource.isWebResource(uri)) {
                WebEpisodeResource w = new WebEpisodeResource(uri, mviRepCn);

                c.add(w);
            }
        }
    }

    private void entitle(SkuEpisodeResource sr) throws RepositoryException
    {
        if (sr.isbn != null) {
            sr.title = RdfUtil.getStringProperty(mviRepCn, sr.isbn, RdfUtil.Dc.title);
        }

        if (sr.asin != null && sr.title == null) {
            sr.title = RdfUtil.getStringProperty(mviRepCn, sr.asin, RdfUtil.Dc.title);
        }
    }

    /**
     * Fetch all resources owl:sameAs this resource, symmetrically.
     *
     * @param r
     * @param c
     * @throws RepositoryException
     */
    private void findAllLinkedIdentifiers(Resource r, Collection<Resource> c)
        throws RepositoryException
    {
        RepositoryResult<Statement> si;

        si = mviRepCn.getStatements(null, RdfUtil.Owl.sameAs, r, true);
        while (si.hasNext()) {
            c.add(si.next().getSubject());
        }

        si = mviRepCn.getStatements(r, RdfUtil.Owl.sameAs, null, true);
        while (si.hasNext()) {
            Resource nr = RdfUtil.asResource(si.next().getObject());
            if (nr != null)
                c.add(nr);
        }
    }

    /**
     * Get all hashes that are identified as representing a particular episode.
     *
     * @param episode
     * @param c
     * @throws RepositoryException
     */
    synchronized void exportAllIdentifiedHashes(Resource episode, Collection<IRI> c) throws RepositoryException
    {
        RepositoryResult<Statement> si =
            mviRepCn.getStatements(null, RdfUtil.Mvi.episode, episode, true);

        while (si.hasNext()) {
            Resource r = si.next().getSubject();
            if (HashUris.isHashUri(r)) {
                c.add((IRI) r);
            }
        }
    }

    /**
     * Find one-degree synonyms of for a collection of hashes, storing the
     * result in a second set. That is, all hashes that are owl:sameAs
     * hashes in the original set (or vice-versa, for symmetry).
     *
     * @param origHashes
     * @param c
     * @throws RepositoryException
     */
    synchronized void exportHashSynonyms(Collection<IRI> origHashes, Set<IRI> c) throws RepositoryException
    {
        RepositoryResult<Statement> si;
        Iterator<IRI> i;

        /* Go to two degrees:
         *  Get all hashes that are identifiers for this hash.
         */
        i = origHashes.iterator();
        while (i.hasNext()) {
            IRI uri = i.next();
            si = mviRepCn.getStatements(uri, RdfUtil.Owl.sameAs, null, true);

            while (si.hasNext()) {
                Value v = si.next().getObject();
                if (HashUris.isHashUri(v)) {
                    c.add((IRI) v);
                }
            }
        }

        /*
         * And all hashes this hash is an identifier for
         */
        i = origHashes.iterator();
        while (i.hasNext()) {
            IRI uri = i.next();
            si = mviRepCn.getStatements(null, RdfUtil.Owl.sameAs, uri, true);

            while (si.hasNext()) {
                Resource r = si.next().getSubject();
                if (HashUris.isHashUri(r)) {
                    c.add((IRI) r);
                }
            }
        }
    }

    synchronized IRI getSource(IRI hash) throws RepositoryException
    {
        return RdfUtil.asUri(RdfUtil.getResProperty(mviRepCn, hash, RdfUtil.Dc.source));
    }

    /**
     * Extract identifier mappings from the loaded series data.
     *
     * @return
     * @throws RepositoryException
     */
    public synchronized IdentifierMappings createIdentifierMappings() throws RepositoryException
    {
        IdentifierMappings im = new IdentifierMappings();
        im.deriveFrom(this.mviRepCn);
        return im;
    }

    /**
     * Extract up-to-date version information from available DOAP.
     *
     * @return
     * @throws RepositoryException
     */
    public synchronized Doap getDoap() throws RepositoryException
    {
        return Doap.check(this.mviRepCn);
    }

    /**
     * Export extra statements about a resource that are
     * either simple literals or which indicate its type.
     *
     * @param g
     * @param res
     * @throws RepositoryException
     */
    public void exportStatementsAbout(Graph g, Resource res) throws RepositoryException
    {
        RepositoryResult<Statement> si;

        si = mviRepCn.getStatements(res, null, null, true);
        while (si.hasNext()) {
            Statement s = si.next();

            if (s.getObject() instanceof Literal
                    || s.getPredicate().equals(RdfUtil.Rdf.type))
            {
                g.add(s);
            }
        }
    }

    public synchronized boolean hasType(URI res, URI type) throws RepositoryException
    {
        return mviRepCn.hasStatement(res, RdfUtil.Rdf.type, type, false);
    }

    public synchronized List<IRI> getResourceIcons(Resource res) throws RepositoryException
    {
        List<IRI> icons = new ArrayList<IRI>();

        /* A specific icon */
        icons.addAll(getSpecificIcons(res));

        /* A generic class-based icon */
        RepositoryResult<Statement> si = mviRepCn.getStatements(res, RdfUtil.Rdf.type, null, true);
        while (si.hasNext()) {
            Value v = si.next().getObject();
            if (v instanceof Resource) {
                icons.addAll(getSpecificIcons((Resource)v));
            }
        }

        return icons;
    }

    public synchronized List<IRI> getSpecificIcons(Resource res) throws RepositoryException
    {
        List<IRI> icons = new ArrayList<IRI>();

        RepositoryResult<Statement> si = mviRepCn.getStatements(res, RdfMiscVocabulary.smIcon, null, true);
        while (si.hasNext()) {
            Value o = si.next().getObject();
            if (o instanceof IRI) {
                icons.add((IRI) o);
            }
        }

        return icons;
    }
}
