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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.kafsemo.mivvi.rdf.RdfUtil;

/**
 * An <code>EpisodeResource</code> consisting of a file, identified by a combination
 * of URLs and hashes. The file may or may not be available locally.
 *
 * @author Joseph Walton
 */
public class FileEpisodeResource extends EpisodeResource
{
    IRI location;

    /**
     * A URI at which this file is locally available.
     *
     * @return
     */
    public IRI getLocation()
    {
        return location;
    }

    Set<IRI> hashUris = new HashSet<IRI>();

    public Collection<IRI> getHashUris()
    {
        return hashUris;
    }

    IRI source;

    public Resource getSource()
    {
        return source;
    }

    public String toString()
    {
        return "(" + location + "," + hashUris + "," + source + ")@" + Integer.toHexString(System.identityHashCode(this));
    }
    /*
     * To display:
     *  If has file, use filename, double-click launches
     *  If not, and has source, use name of source
     *  If no source, show magnet URI
     *   In either case, opening launches magnet URI
     */

    public String getLabel(SeriesData sd) throws RepositoryException
    {
        /* Is there a valid location? */
        if (location != null) {
            File f = FileUtil.fileFrom(location);
            if (f != null) {
                return f.getPath();
            } else {
                String uri = RdfUtil.resourceUri(location);
                if (uri != null) {
                    return uri;
                }
            }
        }

        String sourceTitle = getSourceTitle(sd);
        if (sourceTitle != null) {
            return "Episode capture from " + sourceTitle;
        }

        if (!hashUris.isEmpty()) {
            if (hashUris.size() == 1) {
                return ((Resource)hashUris.iterator().next()).toString();
            } else {
                return hashUris.toString();
            }
        }

        return "(No details)";
    }

    private String getSourceTitle(SeriesData sd) throws RepositoryException
    {
        if (source != null) {
            String sourceTitle = sd.getTitle(source);
            if (sourceTitle == null)
                sourceTitle = source.toString();
            return sourceTitle;
        } else {
            return null;
        }
    }

    public IRI getActionUri(SeriesData sd)
    {
        if (location != null) {
            return location;
        } else if (!hashUris.isEmpty()) {
            return getMagnetUri(sd);
        } else {
            return null;
        }
    }

    public IRI getMagnetUri(SeriesData sd)
    {
        List<String> uris = new ArrayList<String>(hashUris.size());
        for (IRI u : hashUris) {
            uris.add(u.toString());
        }
        Collections.sort(uris);

        StringBuffer sb = new StringBuffer("magnet:");

        if (uris.size() == 1) {
            sb.append("?xt=" + uris.get(0));
        } else {
            for (int i = 0 ; i < uris.size() ; i++) {
                if (i == 0)
                    sb.append("?");
                else
                    sb.append("&");
                sb.append("xt." + (i + 1) + "=" + uris.get(i));
            }
        }
        return SimpleValueFactory.getInstance().createIRI(sb.toString());
    }

    public String getDescription(SeriesData sd) throws RepositoryException
    {
        String sourceTitle = getSourceTitle(sd);
        if (sourceTitle != null) {
            return "From " + sourceTitle;
        } else {
            return null;
        }
    }
}
