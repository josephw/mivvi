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

package org.kafsemo.mivvi.recognise.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kafsemo.mivvi.recognise.EpisodeTitleDetails;
import org.kafsemo.mivvi.recognise.Item;
import org.kafsemo.mivvi.recognise.SeriesDataSource;
import org.kafsemo.mivvi.recognise.SeriesDetails;


public class SimpleSeriesData implements SeriesDataSource<URI>
{
    private final Collection<Item<URI>> titles = new ArrayList<Item<URI>>();
    private final Collection<Item<URI>> descriptions = new ArrayList<Item<URI>>();
    private final Map<URI, SeriesDetails<URI>> seriesDetails = new HashMap<URI, SeriesDetails<URI>>();

    public Collection<Item<URI>> getSeriesTitles()
    {
        return titles;
    }

    public Collection<Item<URI>> getSeriesDescriptions()
    {
        return descriptions;
    }

    public SeriesDetails<URI> getSeriesDetails(URI series)
    {
        return seriesDetails.get(series);
    }

    private static final Pattern EPLINE = Pattern.compile("(\\d+|[A-Z])x(\\d+)\\s+(\\S+)(?:\\s+(.+))?");
    private static final Pattern CONTINUATION = Pattern.compile("\\s+(.*)");

    public void load(Class<?> c, String filename) throws IOException, URISyntaxException
    {
        InputStream in = c.getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("Missing resource '" + filename
            		+ "' for class " + c.getName());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));

        String uri = br.readLine();
        URI u = new URI(uri);
        String title = br.readLine();

        titles.add(new Item<URI>(title, u));


        String desc;
        while((desc = br.readLine()).length() > 0) {
            descriptions.add(new Item<URI>(desc, u));
        }

        SeriesDetails<URI> sd = new SeriesDetails<URI>();

        int episode = 0;
        URI mostRecentEpisode = null;


        String epLine;
        while((epLine = br.readLine()) != null) {
            Matcher m = CONTINUATION.matcher(epLine);
            if (m.matches()) {
                if (mostRecentEpisode == null) {
                    throw new IOException("Continuation line without episode before");
                }
                EpisodeTitleDetails<URI> etd = new EpisodeTitleDetails<URI>(mostRecentEpisode, m.group(1), false);
                sd.episodeTitlesAndDescriptions.add(etd);
                continue;
            }

            m = EPLINE.matcher(epLine);
            if (!m.matches()) {
                throw new IOException("Bad episode line: " + epLine);
            }

            URI ep = new URI(m.group(3));

            // TODO Constrain to number or single uppercase letter

            sd.episodesByNumber.put(
//                    Integer.parseInt(m.group(1)) +
                    m.group(1)
                    + "x" + Integer.parseInt(m.group(2)),
                    ep);

            episode++;
            sd.episodesByNumber.put(
                    Integer.toString(episode),
                    ep);

            if(m.group(4) != null) {
                EpisodeTitleDetails<URI> etd = new EpisodeTitleDetails<URI>(ep, m.group(4), true);
                sd.episodeTitlesAndDescriptions.add(etd);
            }

            mostRecentEpisode = ep;
        }

        seriesDetails.put(u, sd);
    }
}
