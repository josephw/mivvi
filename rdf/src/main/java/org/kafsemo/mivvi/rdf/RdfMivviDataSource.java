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

package org.kafsemo.mivvi.rdf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kafsemo.mivvi.recognise.EpisodeTitleDetails;
import org.kafsemo.mivvi.recognise.FileNamingData;
import org.kafsemo.mivvi.recognise.FilenameProcessor;
import org.kafsemo.mivvi.recognise.Item;
import org.kafsemo.mivvi.recognise.SeriesDataSource;
import org.kafsemo.mivvi.recognise.SeriesDataException;
import org.kafsemo.mivvi.recognise.SeriesDetails;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;

/**
 * @author joe
 */
public class RdfMivviDataSource implements SeriesDataSource<Resource>, FileNamingData
{
    private final RepositoryConnection rep;

    public RdfMivviDataSource(RepositoryConnection mviRep) throws IOException
    {
        this.rep = mviRep;
    }

    public Collection<Item<Resource>> getSeriesTitles()
        throws SeriesDataException
    {
        try {
            return getSeriesLabels(RdfUtil.Dc.title);
        } catch (RepositoryException re) {
            throw new SeriesDataException("Unable to fetch series titles", re);
        }
    }
    
    private Collection<Item<Resource>> getSeriesLabels(URI p) throws RepositoryException
    {
        Collection<Item<Resource>> titles = new ArrayList<Item<Resource>>();
        
        RepositoryResult<Statement> seriesIterator = rep.getStatements(null, RdfUtil.Rdf.type, RdfUtil.Mvi.Series, true);

        while (seriesIterator.hasNext()) {
            Statement stmt = seriesIterator.next();
            
            Resource series = stmt.getSubject();

            RepositoryResult<Statement> i;
            i = rep.getStatements(series, p, null, true);
            while (i.hasNext()) {
                Statement s = i.next();
                String t = RdfUtil.literalString(s.getObject());
                if (t != null) {
                    titles.add(new Item<Resource>(t, series));
                }
            }
        }
        
        return titles;
    }
    
    public Collection<Item<Resource>> getSeriesDescriptions() throws SeriesDataException
    {
        try {
            return getSeriesLabels(RdfUtil.Dc.description);
        } catch (RepositoryException re) {
            throw new SeriesDataException("Unable to fetch series descriptions", re);
        }
    }
    
    public SeriesDetails<Resource> getSeriesDetails(Resource series)
        throws SeriesDataException
    {
        try {
            SeriesDetails<Resource> sd = new SeriesDetails<Resource>();
    
            RepositoryResult<Statement> si;
    
            Resource seasonBag;
            
            seasonBag = RdfUtil.getResProperty(rep, series, RdfUtil.Mvi.seasons);
            
            if (seasonBag != null) {
                si = rep.getStatements(seasonBag, null, null, true);
                
                while (si.hasNext()) {
                    Statement s = si.next();
    
                    if (RdfUtil.index(s.getPredicate()) < 0)
                        continue;
                    
                    Resource re = RdfUtil.asResource(s.getObject());
                    if (re == null)
                        continue;
    
                    String seasonNum = RdfUtil.getStringProperty(rep, re, RdfUtil.Mvi.seasonNumber);
                    
                    Resource episodeSeq = RdfUtil.getResProperty(rep, re, RdfUtil.Mvi.episodes);
                    if (episodeSeq != null) {
                        RepositoryResult<Statement> esi = rep.getStatements(episodeSeq, null, null, true);
                        while (esi.hasNext()) {
                            Statement e = esi.next();
    
                            int p = RdfUtil.index(e.getPredicate());
                            if (p < 0)
                                continue;
                            
                            Resource episode = RdfUtil.asResource(e.getObject());
                            if (episode != null)
                                addEpisodeTitlesAndDescriptions(sd.episodeTitlesAndDescriptions, episode);
                            
    
                            /* Record the series-unique episode number */
                            String episodeNumber = RdfUtil.getStringProperty(rep, episode, RdfUtil.Mvi.episodeNumber);
                            if (episodeNumber != null) {
                                try {
                                    episodeNumber = Integer.toString(Integer.parseInt(episodeNumber));
                                    sd.episodesByNumber.put(episodeNumber, episode);
                                } catch (NumberFormatException nfe) {
                                    // Do nothing
                                }
                            }
                            
                            /* Record the season/episode number */
                            if (seasonNum != null) {
                                sd.episodesByNumber.put(seasonNum + "x" + p, episode);
                            }
                        }
                    }
                }
            } else {
                throw new SeriesDataException("No data available for series " + series);
            }
            
            return sd;
        } catch (RepositoryException re) {
            throw new SeriesDataException("Unable to fetch series data", re);
        }
    }
    
    private void addEpisodeTitlesAndDescriptions(List<EpisodeTitleDetails<Resource>> l, Resource ep)
        throws RepositoryException
    {
        RepositoryResult<Statement> si;
        
        si = rep.getStatements(ep, RdfUtil.Dc.title, null, true);
        while (si.hasNext()) {
            Statement stmt = si.next();
            String t = RdfUtil.literalString(stmt.getObject());
            if (t != null)
                l.add(new EpisodeTitleDetails<Resource>(ep, t, true));
        }
    
        si = rep.getStatements(ep, RdfUtil.Dc.description, null, true);
        while (si.hasNext()) {
            Statement stmt = si.next();
            String t = RdfUtil.literalString(stmt.getObject());
            if (t != null)
                l.add(new EpisodeTitleDetails<Resource>(ep, t, false));
        }
    }

    public void load(String filename) throws IOException, RDFParseException, RepositoryException
    {
        File f = new File(filename);
        
        rep.add(f, f.toURI().toString(), RDFFormat.RDFXML);
    }
    
    public void load(InputStream in, String uri) throws RDFParseException, RepositoryException, IOException
    {
        rep.add(in, uri, RDFFormat.RDFXML);
    }
    

    private static URI KW_URI = new URIImpl("tag:kafsemo.org,2004:mivvi#keyword");

    public Iterable<String> getKeywords() throws SeriesDataException
    {
        try {
            List<String> l = new ArrayList<String>();

            RepositoryResult<Statement> i = rep.getStatements(null,
                    KW_URI, null, true);

            while (i.hasNext()) {
                Statement st = i.next();
                
                String label = RdfUtil.literalString(st.getObject());
                if (label != null) {
                    l.add(label);
                }
            }
            
            return l;
        } catch (RepositoryException re) {
            throw new SeriesDataException("Unable to retrieve keywords", re);
        }
    }
    
    public static FilenameProcessor<Resource> processorFrom(RepositoryConnection rep) throws IOException
    {
        RdfMivviDataSource ds = new RdfMivviDataSource(rep);
        return new FilenameProcessor<Resource>(ds, ds);
    }
}
