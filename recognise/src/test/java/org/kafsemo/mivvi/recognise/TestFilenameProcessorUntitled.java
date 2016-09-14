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
 * License along with this library.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.recognise;

import java.net.URI;
import java.util.Arrays;

import org.kafsemo.mivvi.recognise.impl.SimpleFileNamingData;
import org.kafsemo.mivvi.recognise.impl.SimpleSeriesData;

import junit.framework.TestCase;

/**
 * Try to recognise files, with no episode title, for episodes that
 * don't have a title anyway. Use relatively generic stopwords to
 * allow a successful match.
 *
 * @author joe
 */
public class TestFilenameProcessorUntitled extends TestCase
{
    FilenameProcessor<URI> fp;

    public void tearDown()
    {
        fp = null;
    }

    private void checkResult(String expected, String filename) throws Exception
    {
        FilenameMatch<URI> m = fp.processName(filename);
        assertNotNull(m);
        assertEquals(new URI(expected), m.episode);
    }

    public void testExampleShow() throws Exception
    {
        SimpleSeriesData sd = new SimpleSeriesData();
        sd.load(getClass(), "TestFilenameProcessorUntitled-example-show.txt");
        fp = new FilenameProcessor<URI>(sd, new SimpleFileNamingData());

        checkResult("http://www.example.com/3/1#", "Example Show s03e01.avi");
        checkResult("http://www.example.com/3/3#", "Example Show s03e03.avi");
        checkResult("http://www.example.com/3/4#", "example.show.s03e04.avi");
        checkResult("http://www.example.com/3/5#", "Example Show s03e05.avi");
        checkResult("http://www.example.com/3/6#", "Example Show s03e06.avi");
    }

    public void testExampleShowWithStopWords() throws Exception
    {
        Iterable<String> stopWords = Arrays.asList("aa", "bbbb", "cccc");

        SimpleSeriesData sd = new SimpleSeriesData();
        sd.load(getClass(), "TestFilenameProcessorUntitled-example-show.txt");

        SimpleFileNamingData fnd = new SimpleFileNamingData();
        for (String s : stopWords) {
            fnd.addKeyword(s);
        }

        fp = new FilenameProcessor<URI>(sd, fnd);

        checkResult("http://www.example.com/3/1#", "Example Show s03e01.aa.bbbb.cccc-test.avi");
        checkResult("http://www.example.com/3/3#", "Example Show s03e03.aa.bbbb.cccc-test.avi");
        checkResult("http://www.example.com/3/4#", "example.show.s03e04.aa.bbbb.cccc-sample.avi");
        checkResult("http://www.example.com/3/5#", "Example Show s03e05.aa.bbbb.cccc-test.avi");
        checkResult("http://www.example.com/3/6#", "Example Show s03e06.aa.bbbb.cccc-test.avi");
    }
}
