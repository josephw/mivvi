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
import java.io.FileInputStream;
import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 * @author joe
 */
public class Presentation
{
    final RepositoryConnection repCn;

    public Presentation(RepositoryConnection repCn)
    {
        this.repCn = repCn;
    }

    public void load(String filename) throws IOException, RDFParseException, RepositoryException
    {
        File f = new File(filename);
        
        this.repCn.add(new FileInputStream(f), f.toURI().toString(), RDFFormat.RDFXML);
    }
    
    public String getFilenameFor(Resource episode) throws RepositoryException
    {
        Details d = getDetailsFor(episode);
        
        if (d != null)
            return filenameFor(d);
        else
            return null;
    }
    
    public Details getDetailsFor(Resource ep) throws RepositoryException
    {
        String title = RdfUtil.getStringProperty(repCn, ep, RdfUtil.Dc.title);
        
        RepositoryResult<Statement> res;
        
        Resource bag = null;
        int index = -1;

        res = repCn.getStatements(null, null, ep, true);
        while (res.hasNext()) {
            Statement s = res.next();
            index = RdfUtil.index(s.getPredicate());
            if (index >= 0) {
                bag = s.getSubject();
                break;
            }
        }
        
        if (bag == null)
            return null;
        
        int seasonEpNum = index;

        Resource season = null;

        res = repCn.getStatements(null, RdfUtil.Mvi.episodes, bag, true);
        while (res.hasNext()) {
            Statement s = res.next();
            season = s.getSubject();
        }
        
        if (season == null)
            return null;
        
        String seasonNumber = RdfUtil.getStringProperty(repCn, season, RdfUtil.Mvi.seasonNumber);
        if (seasonNumber == null)
            return null;
        
        bag = null;

        res = repCn.getStatements(null, null, season, true);
        while (res.hasNext()) {
            Statement s = res.next();
            index = RdfUtil.index(s.getPredicate());
            if (index >= 0) {
                bag = s.getSubject();
                break;
            }
        }
        
        if (bag == null)
            return null;
        
        Resource series = null;

        res = repCn.getStatements(null, RdfUtil.Mvi.seasons, bag, true);
        while (res.hasNext()) {
            Statement s = res.next();
            series = s.getSubject();
        }
        
        if (series == null)
            return null;
        
        String seriesTitle = RdfUtil.getStringProperty(repCn, series, RdfUtil.Dc.title);
        if (seriesTitle == null)
            return null;
        
        String pos = Integer.toString(seasonEpNum);
        if (pos.length() == 1)
            pos = "0" + pos;

        Details d = new Details();
        
        d.series = series;
        d.season = season;
        
        d.seriesTitle = seriesTitle;

        d.seasonNumber = seasonNumber;
        d.episodeNumber = seasonEpNum;
        
        d.title = title;
        
        return d;
    }
    
    public static String filenameFor(Details d)
    {
        String pos = Integer.toString(d.episodeNumber);
        if (pos.length() == 1)
            pos = "0" + pos;

        if (d.title != null) {
            return d.seriesTitle + " - " + d.seasonNumber + "x"
                +  pos + " - " + d.title;
        } else {
            return d.seriesTitle + " - " + d.seasonNumber + "x"
                +  pos;
        }
    }
    
    public static class Details
    {
        public Resource series;
        public Resource season;
        
//        public String filename;
        public String seriesTitle;
        public String seasonNumber;
        public int episodeNumber;
        public String title;
    }
}
