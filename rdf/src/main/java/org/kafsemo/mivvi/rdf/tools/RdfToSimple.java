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

package org.kafsemo.mivvi.rdf.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kafsemo.mivvi.rdf.RdfMivviDataSource;
import org.kafsemo.mivvi.rdf.RdfUtil;
import org.kafsemo.mivvi.recognise.Item;
import org.kafsemo.mivvi.recognise.SeriesDetails;
import org.openrdf.model.Resource;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.memory.MemoryStore;

public class RdfToSimple
{
    public static void main(String[] args) throws Exception
    {
        MemoryStore ms = new MemoryStore();
        SailRepository sr = new SailRepository(ms);
        sr.initialize();

        SailRepositoryConnection cn = sr.getConnection();
        
        RdfMivviDataSource ds = new RdfMivviDataSource(cn);

//        ds.load("../../java/test-data/data/dr-katz.rdf");
        ds.load("../../java/test-data/data/the-power-of-nightmares.rdf");

        for(Item<Resource> i : ds.getSeriesTitles()) {
            SeriesDetails<Resource> d = ds.getSeriesDetails(i.resource);

            System.out.println(i.resource);
            System.out.println(i.label);
            System.out.println();

            List<String> epNums = new ArrayList<String>(d.episodesByNumber.keySet());
            Pattern p = Pattern.compile("(\\d+)x(\\d+)");
            for(Iterator<String> si = epNums.iterator(); si.hasNext();) {
                if (!p.matcher(si.next()).matches()) {
                    si.remove();
                }
            }
            
            Collections.sort(epNums, new EpNumComparator());
            
            for (String ep : epNums) {
                if (ep.contains("x")) {
                    Resource episode = d.episodesByNumber.get(ep);
                    String title = RdfUtil.getStringProperty(cn, episode, RdfUtil.Dc.title);
                    if (title != null) {
                        System.out.println(ep + " " + d.episodesByNumber.get(ep) + " " +
                                title);
                    } else {
                        System.out.println(ep + " " + d.episodesByNumber.get(ep));
                    }
                }
            }
        }
    }
    
    private static final class EpNumComparator implements Comparator<String>
    {
        Pattern p = Pattern.compile("(\\d+)x(\\d+)");
        
        public int compare(String o1, String o2)
        {
            Matcher m1 = p.matcher(o1),
                m2 = p.matcher(o2);
            
            if (!m1.matches() || !m2.matches()) {
                throw new RuntimeException(o1 + " " + o2);
//                return o1.compareTo(o2);
            }

            int s1 = Integer.parseInt(m1.group(1)),
                s2 = Integer.parseInt(m2.group(1));
        
            if (s1 > s2) {
                return 1;
            } else if (s1 < s2) {
                return -1;
            } else {
                int e1 = Integer.parseInt(m1.group(2)),
                    e2 = Integer.parseInt(m2.group(2));
                
                if (e1 > e2) {
                    return 1;
                } else if (e1 < e2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }
}
