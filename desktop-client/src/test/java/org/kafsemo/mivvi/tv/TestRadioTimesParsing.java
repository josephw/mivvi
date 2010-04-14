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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.kafsemo.mivvi.app.SeriesData;
import org.kafsemo.mivvi.tv.RadioTimesService;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class TestRadioTimesParsing extends TestCase
{
    public void testParseChannels() throws IOException
    {
        Map<Integer, String> m;

        InputStream in = getClass().getResourceAsStream("test-radiotimes-channels.dat");
        m = RadioTimesService.parseChannels(in);
        in.close();
        
        assertEquals(2, m.size());
        
        assertEquals("Channel One", m.get(Integer.valueOf(1)));
        assertEquals("Channel Two", m.get(Integer.valueOf(2)));
    }
    
    public void testParseListing() throws IOException
    {
        List<Programme> l;
        
        InputStream in = getClass().getResourceAsStream("radiotimes-sample.dat");
        l = RadioTimesService.parseListing(in, "Sample Channel");
        in.close();
        
        assertEquals(2, l.size());
        
        Programme p;
        
        p = l.get(0);
        assertEquals("Sample Show", p.getTitle());
        assertEquals("", p.getSubTitle());
        assertEquals(946684800000L, p.getStart().getTime());
        assertEquals(946688400000L, p.getEnd().getTime());
        assertEquals("Sample Channel", p.getChannel());
        
        p = l.get(1);
        assertEquals("Sample Show", p.getTitle());
        assertEquals("Named Episode", p.getSubTitle());
        assertEquals(946684800000L, p.getStart().getTime());
        assertEquals(946688400000L, p.getEnd().getTime());
        assertEquals("Sample Channel", p.getChannel());
    }
    
    public void testParseListingEndOfDay() throws IOException
    {
        String s = "Sample Show~~Episode Ending at Midnight~~~~~~~~~~~~~~~~28/03/2005~22:20~00:00~100";
        
        List<Programme> l = RadioTimesService.parseListing(new ByteArrayInputStream(s.getBytes("windows-1252")), "Sky One");
        
        assertEquals(1, l.size());
        
        Programme p;
        
        p = l.get(0);
        assertEquals("Sample Show", p.getTitle());
        assertEquals("Episode Ending at Midnight", p.getSubTitle());
        assertNotNull(p.getStart());
        assertEquals(1112044800000L, p.getStart().getTime());
        assertEquals(1112050800000L, p.getEnd().getTime());
    }
    
    public void testDetectionByEpisodeNumberWithinSingleSeason() throws Exception
    {
        Repository rep = new SailRepository(new MemoryStore());
        rep.initialize();
        
        SeriesData sd = new SeriesData();
        sd.initMviRepository(rep);
        
        InputStream in = getClass().getResourceAsStream("example-show-single-season.rdf");
        sd.importMivvi(in, "file:///");
        
        Resource series = new URIImpl("http://www.example.com/#");
        assertEquals(new URIImpl("http://www.example.com/1/1#"), RadioTimesService.recognise(sd, series, "1/2"));
    }
    
    public void testGetSingleChannel()
    {
        List<String> l = Arrays.asList(new String[]{
                "BBC News 24", "BBC1", "BBC1 East", "ITV1 Anglia", "ITV1 London", "ITV1 West"
        });

        String s;
        s = RadioTimesService.getSingleChannel(l, "BBC1");
        assertEquals("BBC1", s);

        s = RadioTimesService.getSingleChannel(l, "BBC News 24");
        assertEquals("BBC News 24", s);
        
        s = RadioTimesService.getSingleChannel(l, "No Such Channel");
        assertEquals(null, s);
        
        s = RadioTimesService.getSingleChannel(l, "ITV1");
        assertEquals("ITV1 London", s);
    }
    
    public void testGetSingleChannelWithMissingChannels()
    {
        List<String> l = Arrays.asList(new String[]{
                "BBC News 24", "BBC1 East", "ITV1 Anglia", "ITV1 West"
        });

        String s;
        s = RadioTimesService.getSingleChannel(l, "BBC1");
        assertEquals("BBC1 East", s);

        s = RadioTimesService.getSingleChannel(l, "ITV1");
        assertEquals("ITV1 West", s);
    }
    
    /**
     * Details should be extracted correctly for a multi-line entry in the listings.
     * 
     * @throws Exception
     */
    public void testParseListingMultiLineReview() throws Exception
    {
        String s = "Show Title~~~~~~~false~false~false~true~true~false~false~false~~~Category~This is a very long description \n"
            + " \n"
            + " Over multiple lines.~true~20/01/2009~16:00~18:00~120\n";
        
        List<Programme> l = RadioTimesService.parseListing(new ByteArrayInputStream(s.getBytes("us-ascii")), "BBC1");
        
        assertEquals(1, l.size());
        
        Programme p;
        
        p = l.get(0);
        assertEquals("Show Title", p.getTitle());
        assertNotNull(p.getStart());
        assertEquals(1232467200000L, p.getStart().getTime());
        assertEquals(1232474400000L, p.getEnd().getTime());
    }
}
