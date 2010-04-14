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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.kafsemo.mivvi.tv.ChannelAvailability.ChannelCollection;

public class TestChannelAvailability extends TestCase
{
    /**
     * Make sure that the correct channels are picked
     * from a set of available ones.
     * 
     * @throws IOException
     */
    public void testTerrestrial() throws IOException
    {
        Collection<String> allChannels = Arrays.asList(
                "Test Channel",
                "BBC1 Regional",
                "BBC1",
                "BBC2",
                "ITV1 London",
                "ITV1 Regional",
                "Channel 4",
                "Five",
                "Another test channel"
            );
                
        ChannelAvailability ca = ChannelAvailability.getInstance();
        
        assertFalse(ca.ccl.isEmpty());
        
        Set<String> c = ca.ccl.get(0).available(allChannels);

        assertEquals(5, c.size());
        
        assertEquals(new HashSet<String>(Arrays.asList(
                "BBC1", "BBC2",
                "ITV1 London", "Channel 4",
                "Five"
        )), c);
    }
    
    public void testParseChannelAvailability() throws IOException
    {
        List<ChannelCollection> l = ChannelAvailability.parseChannelAvailability();
        assertNotNull(l);
        assertEquals(3, l.size());
        
        Set<String> s = new HashSet<String>();

        for (ChannelAvailability.ChannelCollection cc : l) {
            assertNotNull("All collections should be named", cc.name);
            assertTrue("All collections should be named", cc.name.length() > 0);

            assertTrue("Each collection should be a superset of all previous collections",
                    cc.channels.containsAll(s));
            
            s.addAll(cc.channels);
        }
    }
    
    public void testGetInstance() throws IOException
    {
        ChannelAvailability ca = ChannelAvailability.getInstance();
        assertNotNull(ca);
    }
    
    public void testGetsNationalVariation() throws IOException
    {
        ChannelAvailability.ChannelCollection ca = new ChannelAvailability.ChannelCollection(null);
        ca.channels.add("ITV1");
        ca.channels.add("BBC1");
        
        Set<String> all = new HashSet<String>(Arrays.asList("ITV1", "BBC1", "ITV1 London", "BBC1 Regional", "Other Channel"));
        
        Set<String> av = ca.available(all);
        
        Set<String> expected = new HashSet<String>(Arrays.asList("ITV1", "BBC1"));
        
        assertEquals("Non-regional stations are preferred when available", expected, av);
    }
}
