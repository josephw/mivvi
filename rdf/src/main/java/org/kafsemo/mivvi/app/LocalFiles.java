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

import info.aduna.iteration.Iterations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.kafsemo.mivvi.rdf.HashUris;
import org.kafsemo.mivvi.rdf.IdentifierMappings;
import org.kafsemo.mivvi.rdf.Mivvi;
import org.kafsemo.mivvi.rdf.RdfUtil;
import org.kafsemo.mivvi.sesame.JarRDFXMLParser;
import org.kafsemo.mivvi.sesame.RelativeRDFXMLWriter;
import org.openrdf.model.Graph;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.sail.memory.MemoryStore;

/**
 * A standalone, thread-safe repository for information about files stored on the local
 * machine.
 *
 * @author Joseph Walton
 */
public class LocalFiles
{
    private RepositoryConnection localFiles;

    public void initLocalFiles() throws IOException, RepositoryException
    {
        Repository rep = new SailRepository(new MemoryStore());
        rep.initialize();

        initLocalFiles(rep.getConnection());
    }

    public void initLocalFiles(RepositoryConnection cn) throws IOException
    {
        this.localFiles = cn;
    }

    public void load(File rdfXmlFile) throws RDFParseException, RepositoryException, IOException
    {
        this.localFiles.add(rdfXmlFile,
                rdfXmlFile.toURI().toString(),
                RDFFormat.RDFXML);
    }

    public void save(File rdfXmlFile) throws IOException, RDFHandlerException, RepositoryException
    {
        File tf = new File(rdfXmlFile.getPath() + ".tmp");

        OutputStream out = new FileOutputStream(tf);
        try {
            RDFXMLWriter rxw = new RDFXMLWriter(out);

            localFiles.export(rxw);

            out.close();

            if(rdfXmlFile.exists() && !rdfXmlFile.delete()) {
                throw new IOException("Unable to remove old local file RDF");
            }

            if (!tf.renameTo(rdfXmlFile)) {
                throw new IOException("Unable to move local file RDF into place");
            }
        } finally {
            out.close();
        }
    }

    public void close() throws RepositoryException
    {
        localFiles.close();
    }

    public synchronized Resource localFileFor(Resource uri) throws RepositoryException
    {
        RepositoryResult<Statement> si = localFiles.getStatements(null, RdfUtil.Mvi.episode, uri, true);
        if (si.hasNext()) {
            Statement s = si.next();
            return s.getSubject();
        } else {
            return null;
        }
    }

    public synchronized boolean hasEpisodeForFile(URI file) throws RepositoryException
    {
        return localFiles.hasStatement(file, RdfUtil.Mvi.episode, null, true);
    }

    public synchronized void addFileEpisode(String uri, Resource episode) throws RepositoryException
    {
        addFileEpisode(new URIImpl(FileUtil.fixupUri(uri)), episode);
    }

    public synchronized void addFileEpisode(URI file, Resource episode) throws RepositoryException
    {
        localFiles.add(file, RdfUtil.Mvi.episode, episode);
    }

    public synchronized void addFileEpisode(File file, Resource episode) throws RepositoryException
    {
        addFileEpisode(FileUtil.createFileURI(file), episode);
    }

    public synchronized void getResourcesFor(Resource episode, Collection<Resource> target) throws RepositoryException
    {
        RepositoryResult<Statement> si;

        si = localFiles.getStatements(null, RdfUtil.Mvi.episode, episode, true);
        while (si.hasNext()) {
            target.add(si.next().getSubject());
        }
    }

    public synchronized void dropEpisode(Resource episode, Resource resource) throws RepositoryException
    {
        localFiles.remove(resource, RdfUtil.Mvi.episode, episode);
    }

    /**
     * Save an RDF index file, identifying known resources in the same
     * directory and subdirectories.
     *
     * @param f the index file to save
     * @throws RDFHandlerException
     * @throws RepositoryException
     * @throws URISyntaxException
     */
    public boolean saveIndex(File f)
        throws IOException, RDFHandlerException, RepositoryException, URISyntaxException
    {
        File d = f.getParentFile();

        Collection<File> c = FileUtil.gatherFilenames(d);

        return saveIndex(f, c);
    }

    public boolean saveIndex(File f, Collection<File> filesToIndex)
        throws IOException, RDFHandlerException, RepositoryException, URISyntaxException
    {
        Model ig = new LinkedHashModel();

        File parent = f.getParentFile();

        // XXX Get cleverer about catching faults here
        final java.net.URI base = new java.net.URI(FileUtil.fixupUri(parent.toURI().toString()));

        for (File rf : filesToIndex) {
            Resource r = FileUtil.createFileURI(rf);

            synchronized (this) {
                RepositoryResult<Statement> si = localFiles.getStatements(r, null, null, false);
                Iterations.addAll(si, ig);
            }
        }

        int statementCount = 0;

        OutputStream out = new FileOutputStream(f);
        try {
            RDFXMLWriter rxw = new RelativeRDFXMLWriter(out, base);
            rxw.handleNamespace("mvi", Mivvi.URI);
            rxw.startRDF();

            for (Statement s : ig) {
                rxw.handleStatement(s);
                statementCount++;
            }

            rxw.endRDF();
        } finally {
            out.close();
        }

        return statementCount > 0;
    }

    /**
     * Drop any statements about files that don't exist.
     *
     * @return the number of statements deleted
     * @throws RepositoryException
     */
    public int pruneMissingFiles() throws RepositoryException
    {
        Collection<Statement> allFileStatements = new ArrayList<Statement>();

        synchronized (this) {
            RepositoryResult<Statement> si = localFiles.getStatements(null, null, null, false);
            while (si.hasNext()) {
                Statement s = si.next();

                File f = FileUtil.fileFrom(s.getSubject());
                if (f != null) {
                    allFileStatements.add(s);
                }
            }
        }

        int removedCount = 0;

        /* Check for existence */
        Iterator<Statement> i = allFileStatements.iterator();
        while (i.hasNext()) {
            Statement s = i.next();

            File f = FileUtil.fileFrom(s.getSubject());

            if ((f != null) && !f.exists()) {
                synchronized (this) {
                    localFiles.remove(s);
                    removedCount++;
                }
            }
        }

        return removedCount;
    }

    /**
     * Process an index file, accepting all statements with real files, in
     * the current directory or subdirectories.
     *
     * @param f
     * @return
     * @throws RepositoryException
     * @throws RDFHandlerException
     * @throws RDFParseException
     */
    public int processIndex(File f, final IdentifierMappings im)
    {
        try {
            final File baseDir = f.getParentFile().getCanonicalFile();

            final Model g = new LinkedHashModel();

            RDFXMLParser rxp = new JarRDFXMLParser();
            rxp.setRDFHandler(new RDFHandlerBase() {
                @Override
                public void handleStatement(Statement stmt)
                {
                    File rf = FileUtil.fileFrom(stmt.getSubject());
                    if (rf != null && FileUtil.contains(baseDir, rf) && rf.exists()) {
                        Value obj = stmt.getObject();

                        /* Check for mappings */
                        if (im != null && stmt.getPredicate().equals(RdfUtil.Mvi.episode)) {
                            URI uri = RdfUtil.asUri(obj);
                            if (uri != null) {
                                uri = im.getUriFor(uri);
                                if (uri != null) {
                                    obj = uri;
                                }
                            }
                        }

                        g.add(stmt.getSubject(), stmt.getPredicate(), obj);
                    }
                }
            });

            rxp.parse(new FileInputStream(f), FileUtil.fixupUri(f.toURI().toString()));

            int c = 0;

            Iterator<Statement> si = g.iterator();
            while (si.hasNext()) {
                Statement s = si.next();
                synchronized (this) {
                    if (localFiles.hasStatement(s, false))
                        continue;

                    localFiles.add(s);
                }
/*
                if (s.getPredicate().equals(RdfUtil.Mvi.episode)) {
                    Value v = s.getObject();
                    if (v instanceof Resource && !seriesData.mviRepGraph.contains((Resource)v, RdfUtil.Rdf.type, RdfUtil.Mvi.Episode)) {
                        // Unknown episode; warn?
                        System.err.println("Unknown episode: " + v.toString());
                    }
                } */
                c++;
            }

            return c;
        } catch (IOException ioe) {
            System.err.println(ioe);
        } catch (RDFParseException e) {
            System.err.println(e);
        } catch (RDFHandlerException e) {
            System.err.println(e);
        } catch (RepositoryException e) {
            System.err.println(e);
        }

        return 0;
    }

    /**
     * Export all statements relevant to a specific episode into another graph.
     *
     * @param g
     * @param episode
     * @throws RepositoryException
     */
    public synchronized void exportRelevantStatements(Graph g, Resource episode)
            throws RepositoryException
    {
        RepositoryResult<Statement> si = localFiles.getStatements(null, RdfUtil.Mvi.episode, episode, true);
        while (si.hasNext()) {
            Statement s = si.next();
            g.add(s);

            /* Check for hashes; apply transitivity of dc:identifier */
            RepositoryResult<Statement> si2;

            si2 = localFiles.getStatements(s.getSubject(), RdfUtil.Dc.identifier, null, true);
            Iterations.addAll(si2, g);

            si2 = localFiles.getStatements(s.getSubject(), RdfUtil.Dc.identifier, null, true);
            while (si2.hasNext()) {
                s = si2.next();

                Resource idRes = RdfUtil.asResource(s.getObject());
                if (idRes instanceof URI) {
                    URI uri = (URI)idRes;
                    if (HashUris.isHashUri(uri.toString())) {
                        g.add(uri, RdfUtil.Mvi.episode, episode);
                    }
                }
            }
        }
    }

    public synchronized void replaceHashes(Resource r, Collection<URI> newHashes) throws RepositoryException
    {
        HashUris.replaceHashUris(localFiles, r, newHashes);
    }

    synchronized void exportResourceHashes(Resource r, Collection<URI> hashUris) throws RepositoryException
    {
        RepositoryResult<Statement> si = localFiles.getStatements(r, RdfUtil.Dc.identifier, null, true);

        while (si.hasNext()) {
            Value v = si.next().getObject();
            if (HashUris.isHashUri(v)) {
                hashUris.add((URI) v);
            }
        }
    }

    // XXX Return multiple files with the same hash?
    synchronized URI getResourceByHash(URI hash) throws RepositoryException
    {
        RepositoryResult<Statement> si = localFiles.getStatements(null, RdfUtil.Dc.identifier, hash, true);

        if (si.hasNext()) {
            return RdfUtil.asUri(si.next().getSubject());
        } else {
            return null;
        }
    }

    /**
     * Update any statements concerning old URIs to refer, only, to the new URIs.
     *
     * @param im
     * @throws RepositoryException
     */
    public synchronized void update(IdentifierMappings im) throws RepositoryException
    {
        Collection<Statement> toRemove = new ArrayList<Statement>(),
            replacements = new ArrayList<Statement>();

        RepositoryResult<Statement> si = localFiles.getStatements(null, RdfUtil.Mvi.episode, null, false);
        while (si.hasNext()) {
            Statement s = si.next();
            URI uri = RdfUtil.asUri(s.getObject());
            if (uri != null) {
                URI newUri = im.getUriFor(uri);
                if (newUri != null) {
                    toRemove.add(s);
                    replacements.add(new StatementImpl(s.getSubject(), s.getPredicate(), newUri));
                }
            }
        }

        Iterator<Statement> i = toRemove.iterator();
        while (i.hasNext()) {
            localFiles.remove(i.next());
        }

        localFiles.add(replacements);
    }
}
