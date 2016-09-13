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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.kafsemo.mivvi.rdf.RdfUtil;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.repository.RepositoryException;

public abstract class EpisodeResource
{
//    public static void extractResources(Resource episode, Collection<EpisodeResource> c, AppState state)
//        throws RepositoryException
//    {
//        extractResources(episode, c, state.getSeriesData(), state.getLocalFiles());
//    }

    public static void extractResources(
            Resource episode, Collection<EpisodeResource> c, SeriesData sd, LocalFiles lf)
        throws RepositoryException
    {
        extractFileResources(episode, c, sd, lf);
        extractSkuEpisodeResources(episode, c, sd);
        extractWebEpisodeResources(episode, c, sd);
    }

    public static void extractFileResources(
            Resource episode, Collection<? super FileEpisodeResource> c, SeriesData sd, LocalFiles lf)
        throws RepositoryException
    {
        Collection<FileEpisodeResource> fers = new ArrayList<FileEpisodeResource>();
        Set<Resource> coveredHashes = new HashSet<Resource>();
        Map<URI, FileEpisodeResource> fersByLocation = new HashMap<URI, FileEpisodeResource>();

        /*
         * Create a new FER for each local resource.
         * For each resource, populate it with local hashes.
         */
        Collection<Resource> localResources = new ArrayList<Resource>();

        lf.getResourcesFor(episode, localResources);
        
        Iterator<Resource> i;
        
        i = localResources.iterator();
        while (i.hasNext()) {
            Resource r = i.next();
            
            URI u = RdfUtil.asUri(r);
            if (u == null)
                continue;

            FileEpisodeResource fer = new FileEpisodeResource();
            fer.location = u;
            lf.exportResourceHashes(r, fer.hashUris);
            findSource(fer, sd);
            coveredHashes.addAll(fer.hashUris);

            fers.add(fer);
            
            fersByLocation.put(u, fer);
        }

        /*
         * For all hashes for that episode,
         *  drop any that are already present in a FER
         *  Maintain a map hash -> source
         */
        
        // XXX
        Collection<URI> episodeHashes = new ArrayList<URI>();

        sd.exportAllIdentifiedHashes(episode, episodeHashes);
        
        Iterator<URI> i2 = episodeHashes.iterator();
        while (i2.hasNext()) {
            URI r = i2.next();

            URI hash = RdfUtil.asUri(r);
            if (hash == null)
                continue;
            
            if (coveredHashes.contains(hash))
                continue;

            URI location = lf.getResourceByHash(hash);

            FileEpisodeResource fer;

            fer = fersByLocation.get(location);
            if (fer == null) {
                fer = new FileEpisodeResource();
                fer.location = location;
                if (fer.location != null) {
                    lf.exportResourceHashes(fer.location, fer.hashUris);
                    fersByLocation.put(location, fer);
                }
                fers.add(fer);
            }
            fer.hashUris.add(r);
            fer.source = sd.getSource(hash);
        }
        
        /* Search for any indirect hashes */
        for (FileEpisodeResource fer : fers) {
            sd.exportHashSynonyms(new ArrayList<URI>(fer.hashUris), fer.hashUris);
        }

        /*
         *  For all files with that hash:
         *   If FER for file exists, add hash and assign source
         *   Otherwise, create (f, h, s) FER
         *   If no file, create (null, h, s) FER
         */
        
        
        /*
         * NB: Check for statements-about-hashes in either graph
         */
        
        c.addAll(fers);
    }
    
    private static void findSource(FileEpisodeResource fer, SeriesData sd)
        throws RepositoryException
    {
        Iterator<URI> hi = fer.hashUris.iterator();
        while (fer.source == null && hi.hasNext()) {
            fer.source = sd.getSource(hi.next());
        }
    }
    
    public static void extractSkuEpisodeResources(
            Resource episode, Collection<? super SkuEpisodeResource> c, SeriesData sd)
        throws RepositoryException
    {
        sd.extractEpisodeResources(episode, c);
    }


    public static void extractWebEpisodeResources(
            Resource episode, Collection<? super WebEpisodeResource> c, SeriesData sd)
        throws RepositoryException
    {
        sd.extractWebEpisodeResources(episode, c);
    }

    /**
     * A representative label for this resource, or <code>null</code> if none
     * is available.
     */
    public abstract String getLabel(SeriesData sd) throws RepositoryException;
    
    /**
     * A URI, most probably a URL, that can be used to access this resource, or
     * <code>null</code> if none is available.
     */
    public abstract URI getActionUri(SeriesData sd);

    /**
     * @return a description of this resource, suitable for a tooltip
     */
    public abstract String getDescription(SeriesData sd) throws RepositoryException;
}
