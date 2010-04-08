/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright (C) 2004, 2005, 2006, 2010  Joseph Walton
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kafsemo.mivvi.rdf.RdfUtil;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

public class Doap
{
    public static final URI MIVVI_DESKTOP_CLIENT = new URIImpl("http://mivvi.net/code/#desktop-client");
    private static final URI DOWNLOAD_PAGE = new URIImpl("http://usefulinc.com/ns/doap#download-page");

    private final Version latest;
    private final String downloadUrl;

    private Doap(Version latest, String downloadUrl)
    {
        this.latest = latest;
        this.downloadUrl = downloadUrl;
    }

    public static Doap check(RepositoryConnection cn)
        throws RepositoryException
    {
        return new Doap(getLatestAvailableVersion(cn),
                getDownloadPage(cn));
    }

    public Version getLatestAvailableVersion()
    {
        return latest;
    }

    public String getDownloadPage()
    {
        return downloadUrl;
    }

    public static Version getLatestAvailableVersion(RepositoryConnection cn)
        throws RepositoryException
    {
        List<Version> allVersions = new ArrayList<Version>();

        RepositoryResult<Statement> res = cn.getStatements(MIVVI_DESKTOP_CLIENT, RdfUtil.Doap.release, null, false);
        
        while (res.hasNext()) {
            Statement stmt = res.next();
            
            Resource v = RdfUtil.asResource(stmt.getObject());
            if (v != null) {
                String rev = RdfUtil.getStringProperty(cn, v, RdfUtil.Doap.revision);
                if (rev != null) {
                    try {
                        allVersions.add(Version.parse(rev));
                    } catch (ParseException pe) {
                        // Ignore this version
                    }
                }
            }
        }
        
        Collections.sort(allVersions);
        
        if (allVersions.size() > 0) {
            return allVersions.get(allVersions.size() - 1);
        } else {
            return null;
        }
    }
    
    public static String getDownloadPage(RepositoryConnection cn) throws RepositoryException
    {
        Resource r = RdfUtil.getResProperty(cn, MIVVI_DESKTOP_CLIENT, DOWNLOAD_PAGE);
        if (r instanceof URI) {
            return ((URI)r).toString();
        } else {
            return null;
        }
    }
}
