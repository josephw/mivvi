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

package org.kafsemo.mivvi.tv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kafsemo.mivvi.app.SeriesData;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Tests for {@link RadioTimesService}.
 * 
 * @author joe
 */
public class TestRadioTimesParsing
{
    @Test
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

    @Test
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
        assertNull(p.getSubTitle());
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

    public static List<Programme> parse(String s) throws UnsupportedEncodingException, IOException
    {
        return RadioTimesService.parseListing(new ByteArrayInputStream(s.getBytes("windows-1252")), "Channel");
    }
    
    @Test
    public void initialNonListingLinesAreIgnored() throws Exception
    {
        assertEquals("An empty line is ignored",
                Collections.emptyList(), parse("\n"));
        assertEquals("A line with no tildes is ignored",
                Collections.emptyList(), parse("This line is a textual note.\n"));
    }
    
    @Test
    public void testParseListingEndOfDay() throws IOException
    {
        String s = "Sample Show~~Episode Ending at Midnight~~~~~~~~~~~~~~~~~28/03/2005~22:20~00:00~100";
        
        List<Programme> l = parse(s);
        
        assertEquals(1, l.size());
        
        Programme p;
        
        p = l.get(0);
        assertEquals("Sample Show", p.getTitle());
        assertEquals("Episode Ending at Midnight", p.getSubTitle());
        assertNotNull(p.getStart());
        assertEquals(1112044800000L, p.getStart().getTime());
        assertEquals(1112050800000L, p.getEnd().getTime());
    }
    
    @Test
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

    @Test
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

    @Test
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
    @Test
    public void testParseListingMultiLineReview() throws Exception
    {
        String s = "Show Title~~~~~~~~~~~~~~~~~This is a very long description \n"
            + " \n"
            + " Over multiple lines.~true~20/01/2009~16:00~18:00~120\n";
        
        List<Programme> l = parse(s);
        
        assertEquals(1, l.size());
        
        Programme p;
        
        p = l.get(0);
        assertEquals("Show Title", p.getTitle());
        assertNotNull(p.getStart());
        assertEquals(1232467200000L, p.getStart().getTime());
        assertEquals(1232474400000L, p.getEnd().getTime());
    }

    @Test
    public void getSeasonNumberAndLengthWhenPresent() throws Exception
    {
        String s = "Show Title~2/13, series 4~Episode Title~~~~~~~~~~~~~~~~~~~~45";

        List<Programme> l = parse(s);

        assertEquals(1, l.size());
        
        Programme p;
        
        p = l.get(0);
        assertEquals("Show Title", p.getTitle());
        assertEquals("Episode Title", p.getSubTitle());
        assertEquals(Integer.valueOf(2), p.getEpisodeNumber());
        assertEquals(Integer.valueOf(13), p.getSeasonLength());
        assertEquals(Integer.valueOf(4), p.getSeasonNumber());
    }
}
