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

package org.kafsemo.mivvi.tv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChannelAvailability
{
    private static ChannelAvailability singleton;

    public synchronized static ChannelAvailability getInstance() throws IOException
    {
        if (singleton == null) {
            singleton = new ChannelAvailability(parseChannelAvailability());
        }
        return singleton;
    }

    public final List<ChannelCollection> ccl;

    private ChannelAvailability(List<ChannelCollection> l)
    {
        this.ccl = l;
    }

    public static List<ChannelCollection> parseChannelAvailability() throws IOException
    {
        List<ChannelCollection> l = new ArrayList<ChannelCollection>();

        String NAME = "channel-availability.txt";

        InputStream in = ChannelAvailability.class.getResourceAsStream(NAME);
        
        if (in == null)
            throw new FileNotFoundException("Internal resource '" + NAME + "' not found.");
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "US-ASCII"));

        ChannelCollection cc = null;

        String s;
        
        while ((s = br.readLine()) != null) {
            s = s.trim();

            if (s.startsWith("#") || s.equals(""))
                continue;
            
            if (s.startsWith("=")) {
                ChannelCollection ncc = new ChannelCollection(s.substring(1));
                l.add(ncc);
                if (cc != null)
                    ncc.channels.addAll(cc.channels);
                cc = ncc;
            } else {
                if (cc == null) {
                    throw new IOException("Unexpected channel line in '" + NAME + "'");
                }
                cc.channels.add(s);
            }
        }
        
        return l;
    }
    
    public static class ChannelCollection
    {
        public final String name;
        public final Set<String> channels;
        
        public ChannelCollection(String name)
        {
            this.name = name;
            this.channels = new HashSet<String>();
        }

        public Set<String> available(Collection<String> all)
        {
            Set<String> s = new HashSet<String>();

            String bestITV1 = null;
            String bestBBC1 = null;

            for (String c : all) {
                /* Special cases with regional variations */
                if (c.equals("ITV1") || c.startsWith("ITV1 ")) {
                    if (bestITV1 == null || RadioTimesService.compareITV1Regional(bestITV1, c) < 0)
                        bestITV1 = c;
                    
                } else if (c.equals("BBC1") || c.startsWith("BBC1 ")) {
                    if (bestBBC1 == null || RadioTimesService.compareRegional(bestBBC1, c) < 0)
                        bestBBC1 = c;
                } else if (channels.contains(c)) {
                    s.add(c);
                }
            }

            if (bestBBC1 != null)
                s.add(bestBBC1);

            if (bestITV1 != null)
                s.add(bestITV1);

            return s;
        }
    }
}
