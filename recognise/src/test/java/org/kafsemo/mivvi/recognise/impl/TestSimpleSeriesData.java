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

package org.kafsemo.mivvi.recognise.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.kafsemo.mivvi.recognise.Item;

public class TestSimpleSeriesData
{
    @Test(expected = IOException.class)
    public void missingResourceCausesException() throws Exception
    {
        SimpleSeriesData ssd = new SimpleSeriesData();
        ssd.load(getClass(), "no-resource-with-this-name");
    }

    @Test
    public void loadFromReader() throws Exception
    {
        String contents = "http://www.example.com/#\nExample Show\n\n";

        SimpleSeriesData ssd = new SimpleSeriesData();

        ssd.load(new StringReader(contents));

        List<Item<URI>> descs = new ArrayList<Item<URI>>(ssd.getSeriesTitles());

        assertEquals(1, descs.size());
        assertEquals(
                new Item<URI>("Example Show", new URI("http://www.example.com/#")),
                descs.get(0));
    }

    @Test
    public void loadDetailsFromReader() throws Exception
    {
        String contents = "http://www.example.com/#\nExample Show\n\n";

        SimpleSeriesDetails details = SimpleSeriesData.loadDetails(new StringReader(contents));

        assertEquals(new URI("http://www.example.com/#"), details.id);
        assertEquals("Example Show", details.getTitle());
        assertEquals(Collections.emptyList(), details.descriptions());

        assertEquals(Collections.emptyMap(), details.episodesByNumber);
        assertEquals(Collections.emptyList(), details.episodeTitlesAndDescriptions);
    }

    @Test(expected = IOException.class)
    public void invalidSeasonCodeCausesException() throws Exception
    {
        String contents = "http://www.example.com/#\nExample Show\n\nax1 http://www.example.com/a/1#\n";

        SimpleSeriesData.loadDetails(new StringReader(contents));
    }

    @Test(expected = IOException.class)
    public void unexpectedContinuationLineCausesException() throws Exception
    {
        String contents = "http://www.example.com/#\nExample Show\n\n Continuation Line\n";

        SimpleSeriesData.loadDetails(new StringReader(contents));
    }
}
