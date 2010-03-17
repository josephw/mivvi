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

package org.kafsemo.mivvi.recognise;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.kafsemo.mivvi.recognise.impl.SimpleFileNamingData;
import org.kafsemo.mivvi.recognise.impl.SimpleSeriesData;


/**
 * @author joe
 */
public class TestFilenameProcessor
    extends TestCase
{
    static URI EXAMPLE_SHOW;
    static URI EXAMPLE_EPISODE_1_1;
    static URI TLA_SHOW;

    FilenameProcessor<URI> fp;

    public TestFilenameProcessor() throws URISyntaxException
    {
        EXAMPLE_SHOW = new URI("http://www.example.com/#");
        EXAMPLE_EPISODE_1_1 = new URI("http://www.example.com/1/1#");
        TLA_SHOW = new URI("http://www.example.com/tla/#");
    }

    private void replaceData(String... series) throws IOException, URISyntaxException
    {
        replaceData(new SimpleFileNamingData(), series);
    }

    private void replaceData(FileNamingData fnd, String... series) throws IOException, URISyntaxException
    {
        SimpleSeriesData ssd = new SimpleSeriesData();

        for (String s : series) {
            ssd.load(getClass(), "TestFilenameProcessor-" + s + ".txt");
        }

        this.fp = new FilenameProcessor<URI>(ssd, fnd);
    }

    public static FileNamingData kw(String... kws)
    {
        SimpleFileNamingData fnd = new SimpleFileNamingData();
        for (String k : kws) {
            fnd.addKeyword(k);
        }
        return fnd;
    }

    public void setUp() throws Exception
    {
        replaceData();
    }

    public void tearDown() throws Exception
    {
        fp = null;
    }

    private FilenameMatch<URI> process(String s) throws SeriesDataException
    {
        return fp.process(new File(s));
    }

    /* Check series identification */
    private URI getSeries(String s) throws SeriesDataException
    {
        return fp.getSeries(s);
    }

    public void testEmptyString() throws SeriesDataException
    {
        assertNull(getSeries(""));
    }

    public void testUnknownSeries() throws SeriesDataException
    {
        assertNull(getSeries("No Such Series"));
    }

    public void testGetKnownSeries() throws Exception
    {
    	replaceData("example");
        assertEquals(EXAMPLE_SHOW, getSeries("Example Show"));
    }

    public void testGetKnownAbbreviatedSeries() throws Exception
    {
    	replaceData("tla");
        assertEquals(TLA_SHOW, getSeries("three-letter acronym"));
        assertEquals(TLA_SHOW, getSeries("tla"));
    }

    public void testGetSeriesWithoutThe() throws Exception
    {
        replaceData("the-example-show");

        assertEquals(EXAMPLE_SHOW, getSeries("example show"));
    }

    /* Check "best series" matching */
    public void testMatchAbbreviatedPrefix() throws Exception
    {
    	replaceData("tla");
        Matching<URI> m = fp.matchSeries("TLA 1 Episode Name.mov");
        assertNotNull(m);
        assertEquals(TLA_SHOW, m.matchedResource);
        assertEquals(0, m.start);
        assertEquals(3, m.end);
        assertEquals("TLA", m.realString);
    }

    public void testMatchSeriesAbbreviated() throws Exception
    {
    	replaceData("tla");
        Matching<URI> m = fp.matchSeries("tla");
        assertNotNull(m);
        assertEquals(TLA_SHOW, m.matchedResource);
        assertEquals(0, m.start);
        assertEquals(3, m.end);
        assertEquals("TLA", m.realString);
    }

    public void testFullSeriesNameDotted() throws Exception
    {
    	replaceData("example");
        Matching<URI> m = fp.matchSeries("Example.Show-Episode.Name.mpg");
        assertNotNull(m);
        assertEquals(EXAMPLE_SHOW, m.matchedResource);
        assertEquals(0, m.start);
        assertEquals(12, m.end);
        assertEquals("Example Show", m.realString);
    }

    public void testMatchSeriesWithoutThe() throws Exception
    {
        replaceData("the-example-show");
        Matching<URI> m = fp.matchSeries("Example Show xxxxxxxx");
        assertNotNull(m);
        assertEquals(EXAMPLE_SHOW, m.matchedResource);
        assertEquals(0, m.start);
        assertEquals(12, m.end);
        assertEquals("Example Show", m.realString);
    }

    void assertEquals(String uri, URI res)
    {
        try {
            assertEquals(new URI(uri), res);
        } catch (URISyntaxException use) {
            throw new RuntimeException(use);
        }
    }

    void assertEquals(String message, String uri, URI res)
    {
        try {
            assertEquals(message, new URI(uri), res);
        } catch (URISyntaxException use) {
            throw new RuntimeException(use);
        }
    }

    public void testGetByEpisodeNumber() throws Exception
    {
        replaceData("example");
        URI episode = fp.getByEpisodeNumber(EXAMPLE_SHOW, 1);
        assertEquals(EXAMPLE_EPISODE_1_1, episode);
    }

    public void testGetBySeasonAndProgramNumber() throws Exception
    {
        replaceData("example");
        URI episode = fp.getBySeasonAndProgramNumber(EXAMPLE_SHOW, 1, 2);
        assertEquals("http://www.example.com/1/2#", episode);
    }

    /* Find an episode from series and numeric string */

    public void testNumeric() throws Exception
    {
        replaceData("example");
        Matching<URI> m = fp.findEpisode(EXAMPLE_SHOW, "1");
        assertNotNull(m);
        assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
        assertEquals(0, m.start);
        assertEquals(1, m.end);
        assertEquals("1", m.realString);
    }

    public void testNumericWithChaff() throws Exception
    {
        replaceData("example");
        Matching<URI> m = fp.findEpisode(EXAMPLE_SHOW, " - 1 ");
        assertNotNull(m);
        assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
        assertEquals(3, m.start);
        assertEquals(4, m.end);
        assertEquals("1", m.realString);
    }

    public void testStringWithNumericSeasonAndEpisode() throws Exception
    {
        replaceData("example");
        Matching<URI> m = fp.findEpisode(EXAMPLE_SHOW, "1x2");
        assertNotNull(m);
        assertEquals("http://www.example.com/1/2#", m.matchedResource);
        assertEquals(0, m.start);
        assertEquals(3, m.end);
        assertEquals("1x2", m.realString);
    }

    public void testThreeDigitNumericSeasonAndEpisode() throws Exception
    {
        replaceData("example");
        List<Matching<URI>> l = fp.findEpisodes(EXAMPLE_SHOW, "101");
        assertEquals(2, l.size());
        Iterator<Matching<URI>> i = l.iterator();
        while (i.hasNext()) {
            Matching<URI> m = i.next();
            assertNotNull(m);
            assertEquals(0, m.start);
            assertEquals(3, m.end);

            if (m.realString.equals("101")) {
                assertNull(m.matchedResource);
            } else {
                assertEquals("1x1", m.realString);
                assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
            }
        }
    }

    public void testThreeDigitEpisodeNumber() throws Exception
    {
        replaceData("long-running-example");
        List<Matching<URI>> l = fp.findEpisodes(EXAMPLE_SHOW, "101");
        assertEquals(2, l.size());

        Iterator<Matching<URI>> i = l.iterator();
        while (i.hasNext()) {
            Matching<URI> m = i.next();
            assertNotNull(m);
            assertEquals(0, m.start);
            assertEquals(3, m.end);

            if (m.realString.equals("101")) {
                assertEquals("http://www.example.com/2/1#", m.matchedResource);
            } else if (m.realString.equals("1x1")) {
                assertEquals("http://www.example.com/1/1#", m.matchedResource);
            } else {
                fail("Unexpected string form for 101: " + m.realString);
            }
        }
    }

    public void testOutOfRangeEpisodeNumber() throws Exception
    {
    	replaceData("example");
        Matching<URI> m = fp.findEpisode(EXAMPLE_SHOW, "9999");
        assertNotNull(m);
        assertNull(m.matchedResource);
        assertEquals(0, m.start);
        assertEquals(4, m.end);
        assertEquals("9999", m.realString);
    }

    public void testOutOfRangeSeasonAndEpisodeNumber() throws Exception
    {
    	replaceData("example");
        Matching<URI> m = fp.findEpisode(EXAMPLE_SHOW, "1x999");
        assertNotNull(m);
        assertNull(m.matchedResource);
        assertEquals(0, m.start);
        assertEquals(5, m.end);
        assertEquals("1x999", m.realString);
    }

    public void testFindEpisodeNumericFailure() throws Exception
    {
    	replaceData("example");
        Matching<URI> m = fp.findEpisode(EXAMPLE_SHOW, "WordsX2");
        assertNull(m);
    }

    public void testNamedNumberedEpisode() throws Exception
    {
        replaceData("named-episode-example");
        Matching<URI> m = fp.findEpisode(EXAMPLE_SHOW, "1 Named Episode");
        assertNotNull(m);
        assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
        assertEquals(0, m.start);
        assertEquals(1, m.end);
        assertEquals("1", m.realString);
    }

    public void testNamedNumberedEpisodeFullFilename() throws Exception
    {
        replaceData("named-episode-example");
        FilenameMatch<URI> fnm = process("/srv/media/Example Show 1 Named Episode.mov");
        assertNotNull(fnm);
        assertEquals(EXAMPLE_EPISODE_1_1, fnm.episode);
        assertTrue(fnm.isExact);
    }

    /**
     * This shouldn't match. Or, at least, it should be flagged as
     * an extremely speculative suggestion.
     */
    public void testGetEpisodeByBadTitleFails() throws Exception
    {
        replaceData("named-episode-example");
        FilenameMatch<URI> fnm = process("/srv/media/Example.Show 1 Wrong Title For Episode.mov");
        assertNull(fnm);
    }

    public void testGetEpisodeByTitle() throws Exception
    {
        replaceData("named-episode-example");
        URI ep = fp.getEpisodeByTitle(EXAMPLE_SHOW, "Named Episode");
        assertNotNull(ep);
        assertEquals(EXAMPLE_EPISODE_1_1, ep);
    }

    public void testBadEpisodeNumber() throws Exception
    {
        replaceData("named-episode-example");
        FilenameMatch<URI> fnm = process("Example Show - 3 - Named Episode");
        assertNotNull(fnm);
        assertEquals("An episode with the wrong number should be identified by title",
        		EXAMPLE_EPISODE_1_1, fnm.episode);
        assertFalse(fnm.isExact);
    }

    public void testBadSeasonAndEpisodeNumber() throws Exception
    {
        replaceData("named-episode-example");
        FilenameMatch<URI> fnm = process("Example Show - 9x99 - Named Episode.avi");
        assertNotNull(fnm);
        assertEquals("An episode with the wrong series and episode should be identified by title",
        		EXAMPLE_EPISODE_1_1, fnm.episode);
        assertFalse(fnm.isExact);
    }

    public void testMatchEpisodePartial() throws Exception
    {
        replaceData("named-episode-example");
        Matching<URI> m = fp.matchEpisodeTitle(EXAMPLE_SHOW, " - Named Episode - Backup");
        assertNotNull(m);
        assertEquals(3, m.start);
        assertEquals(16, m.end);
        assertEquals("Named Episode", m.realString);
        assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
    }

    public void testMatchKeyword() throws Exception
    {
        SimpleFileNamingData fnd = new SimpleFileNamingData();
        fnd.addKeyword("keyword");

        replaceData(fnd);

        Matching<URI> m = fp.matchKeyword("Keyword");
        assertNotNull(m);
        assertEquals(0, m.start);
        assertEquals(7, m.end);
        assertEquals("keyword", m.realString);
//        assertNotNull(m.matchedResource);
    }

    public void testMatchIncludingKnownKeyword() throws Exception
    {
        replaceData(kw("Keyword"), "named-episode-example");
        FilenameMatch<URI> fnm = process("/Example Show - Named Episode - Keyword.avi");
        assertNotNull(fnm);
        assertEquals(EXAMPLE_EPISODE_1_1, fnm.episode);
        assertTrue(fnm.isExact);
    }

    public void testEpisodeNumberMatchWithKeyword() throws Exception
    {
        replaceData(kw("Keyword"), "named-episode-example");
        FilenameMatch<URI> fnm = process("Example Show - 1 - Named Episode - Keyword");
        assertNotNull(fnm);
        assertEquals(EXAMPLE_EPISODE_1_1, fnm.episode);
        assertTrue(fnm.isExact);
    }

    public void testSeriesFromDirectoryName() throws Exception
    {
        replaceData("named-episode-example");
        FilenameMatch<URI> fnm = process("/srv/media/Example Show/1 - Named Episode.avi");
        assertNotNull(fnm);
        assertEquals(EXAMPLE_EPISODE_1_1, fnm.episode);
        assertTrue(fnm.isExact);
    }

    public void testNoEpisodeNumber() throws Exception
    {
        replaceData("named-episode-example");
        FilenameMatch<URI> fnm = process("Example Show (Named Episode).avi");
        assertNotNull(fnm);
        assertEquals(EXAMPLE_EPISODE_1_1, fnm.episode);
        assertTrue(fnm.isExact);
    }

    public void testNoEpisodeNumberMissingTheAndWithKeywords() throws Exception
    {
        replaceData(kw("AAA", "bbbbb"), "named-episode-example");
        FilenameMatch<URI> fnm = process("Example.Show-Final.Episode-AAA-bbbbb.mpg");
        assertNotNull(fnm);
        assertEquals("http://www.example.com/2/2#", fnm.episode);
        assertTrue(fnm.isExact);
    }

    public void testEpisodeTitleDifferentCase() throws Exception
    {
        replaceData("named-episode-example");
        FilenameMatch<URI> fnm = process("Example Show - 1 - NAMED EPISODE.avi");
        assertNotNull(fnm);
        assertEquals(EXAMPLE_EPISODE_1_1, fnm.episode);
    }

    public void testMatchAlternateEpisodeTitle() throws Exception
    {
        replaceData("named-episode-with-alternates-example");
        Matching<URI> m = fp.matchEpisodeTitle(EXAMPLE_SHOW, " - Alternate Title");
        assertNotNull(m);
        assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
        assertEquals(3, m.start);
        assertEquals(18, m.end);
        assertEquals("Alternate Title", m.realString);
    }

    public void testFullTitleOutOfRangeEpisodeNumber() throws Exception
    {
        replaceData("named-episode-example");
        FilenameMatch<URI> fnm = process("Example Show - 99999 - Named Episode");
        assertNotNull(fnm);
        assertEquals(EXAMPLE_EPISODE_1_1, fnm.episode);
        assertFalse(fnm.isExact);
    }

    public void testGetClosestEpisodeEmptyString() throws Exception
    {
        replaceData("named-episode-example");
        List<? extends Matching<URI>> l = fp.getClosestEpisodes(EXAMPLE_SHOW, "", 0);
        assertEquals("No episodes match the empty string exactly", 0, l.size());

        l = fp.getClosestEpisodes(EXAMPLE_SHOW, "", Integer.MAX_VALUE);
        assertEquals("All named episodes are returned at maximum distance", 3, l.size());
        
        for(Matching<URI> m : l) {
		    assertEquals(0, m.start);
		    assertEquals(0, m.end);
        }
    }

    public void testGetClosestEpisodeExact() throws Exception
    {
        replaceData("named-episode-example");
        List<TitleMatching<URI>> l = fp.getClosestEpisodes(EXAMPLE_SHOW, "Named Episode", Integer.MAX_VALUE);
        assertFalse(l.isEmpty());

        TitleMatching<URI> m = l.get(0);

        assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
        assertTrue(m.isExact);
        assertEquals(0, m.start);
        assertEquals(13, m.end);
        assertEquals("Named Episode", m.realString);

        l = fp.getClosestEpisodes(EXAMPLE_SHOW, "Named Episode", 0);
        assertEquals(1, l.size());

        m = l.get(0);
        assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
        assertTrue(m.isExact);
        assertEquals(0, m.start);
        assertEquals(13, m.end);
        assertEquals("Named Episode", m.realString);
    }

    private void assertExactlyOneExactMatch(List<TitleMatching<URI>> l)
    {
        Iterator<TitleMatching<URI>> i = l.iterator();

        assertTrue(i.hasNext());

        assertTrue((i.next()).isExact);

        while (i.hasNext()) {
            TitleMatching<URI> tm = i.next();
            assertFalse("Should not be exact: " + tm.realString, tm.isExact);
        }
    }

    public void testGetClosestEpisodeBadPunctuation() throws Exception
    {
        replaceData("named-episode-example");
        List<TitleMatching<URI>> l;
        TitleMatching<URI> m;

        l = fp.getClosestEpisodes(EXAMPLE_SHOW, "Name'd Episode");
        assertTrue(l.size() >= 1);
        m = l.get(0);
        assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
        assertTrue(m.isExact);
        assertEquals("Named Episode", m.realString);
        assertEquals(0, m.start);
        assertEquals(14, m.end);
        assertExactlyOneExactMatch(l);

        l = fp.getClosestEpisodes(EXAMPLE_SHOW, "Namedepisode");
        assertTrue(l.size() >= 1);
        m = l.get(0);
        assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
        assertTrue(m.isExact);
        assertEquals("Named Episode", m.realString);
        assertEquals(0, m.start);
        assertEquals(12, m.end);
        assertExactlyOneExactMatch(l);
    }

    public void testGetClosestEpisodeTypos()
        throws Exception
    {
        replaceData("named-episode-example");

        List<TitleMatching<URI>> l;
        TitleMatching<URI> m;

        l = fp.getClosestEpisodes(EXAMPLE_SHOW, "Name Episode");
        assertTrue(l.size() >= 1);
        m = l.get(0);
        assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
        assertTrue(m.isExact);
        assertEquals("Named Episode", m.realString);
        assertEquals(0, m.start);
        assertEquals(12, m.end);
        assertExactlyOneExactMatch(l);
    }

    public void testGetClosestEpisodeExactWithSuffix() throws Exception
    {
        replaceData("named-episode-example");
        List<TitleMatching<URI>> l;
        TitleMatching<URI> m;

        l = fp.getClosestEpisodes(EXAMPLE_SHOW, "Named Episode (Keyword)");
        assertTrue(l.size() >= 1);
        m = l.get(0);
        assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
        assertTrue(m.isExact);
        assertEquals("Named Episode", m.realString);
        assertEquals(0, m.start);
        assertEquals(13, m.end);
        assertExactlyOneExactMatch(l);
    }

    public void testGetClosestEpisodeSuggestableWithSuffix()
        throws Exception
    {
        replaceData("named-episode-example");
        List<TitleMatching<URI>> l;
        TitleMatching<URI> m;

        l = fp.getClosestEpisodes(EXAMPLE_SHOW, "Named Differently Episode - Keyword");
        assertTrue(l.size() >= 1);
        m = l.get(0);
        assertFalse(m.isExact);
        assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
        assertEquals("Named Episode", m.realString);
        assertEquals(0, m.start);
        assertEquals(25, m.end);
    }

    public void testProcessFullTitleWithTypo() throws Exception
    {
        replaceData("named-episode-example");
        FilenameMatch<URI> fm = process("Example Show - 1 - Name Episode");
        assertNotNull(fm);
        assertEquals(EXAMPLE_EPISODE_1_1, fm.episode);
        assertTrue(fm.isExact);
    }

    public void testGetClosestEpisodeDistantSuggestion() throws Exception
    {
        replaceData("named-episode-example");
        List<TitleMatching<URI>> l;
        TitleMatching<URI> m;

        l = fp.getClosestEpisodes(EXAMPLE_SHOW, "Named Differently Episode");
        
        assertTrue(l.size() >= 1);
        m = l.get(0);
        assertEquals(EXAMPLE_EPISODE_1_1, m.matchedResource);
        assertEquals("Named Episode", m.realString);
        assertFalse(m.isExact);
        assertEquals(0, m.start);
    }

    public void testDateMatching()
    {
        Matching<URI> m;

        m= FilenameProcessor.matchDate("20000101");
        assertNotNull(m);
        assertEquals(0, m.start);
        assertEquals(8, m.end);
        assertEquals("2000-01-01", m.realString);

        m = FilenameProcessor.matchDate(" 20000101 ");
        assertNotNull(m);
        assertEquals(1, m.start);
        assertEquals(9, m.end);
        assertEquals("2000-01-01", m.realString);

        m = FilenameProcessor.matchDate(" 20000101 X");
        assertNotNull(m);
        assertEquals(1, m.start);
        assertEquals(9, m.end);
        assertEquals("2000-01-01", m.realString);

        assertNull(FilenameProcessor.matchDate("2000101"));
        assertNull(FilenameProcessor.matchDate("2000101 "));
        assertNull(FilenameProcessor.matchDate("18000101"));
        assertNull(FilenameProcessor.matchDate("30000101"));

        assertNull(FilenameProcessor.matchDate("X 20000101"));

        /* Require whitespace delimiting? */
        assertNull(FilenameProcessor.matchDate("20000101X"));
    }

    public void testProcessTitleWithDate() throws Exception
    {
        replaceData("named-episode-example");
        FilenameMatch<URI> fm = process("Example Show - 1 - 20010101 - Named Episode");
        assertNotNull(fm);
        assertEquals(EXAMPLE_EPISODE_1_1, fm.episode);
        assertTrue(fm.isExact);
    }

    public void testMatchKeywords() throws Exception
    {
        SimpleFileNamingData fnd = new SimpleFileNamingData();
        fnd.addKeyword("Aaaaaa");
        fnd.addKeyword("BbbB ccccc");

        replaceData(fnd);

        Matching<URI> m = fp.matchKeywords("");
        assertNotNull(m);
        assertEquals(0, m.start);
        assertEquals(0, m.end);
        assertEquals("", m.realString);

        m = fp.matchKeywords(" aaaaaa test");
        assertNotNull(m);
        assertEquals(1, m.start);
        assertEquals(7, m.end);
        assertEquals("Aaaaaa", m.realString);

        m = fp.matchKeywords(" bbbb ccccc  ");
        assertNotNull(m);
        assertEquals(1, m.start);
        assertEquals(11, m.end);
        assertEquals("BbbB ccccc", m.realString);

        m = fp.matchKeywords("No keywords");
        assertNotNull(m);
        assertEquals(0, m.start);
        assertEquals(0, m.end);
        assertEquals("", m.realString);
    }

    public void testMatchSeriesAllowingPrefix() throws Exception
    {
        replaceData("tla");
        Matching<URI> m;

        m = fp.matchSeriesAllowingPrefix("Xxx TLA Xxx");
        assertNotNull(m);
        assertEquals(TLA_SHOW, m.matchedResource);
        assertEquals("TLA", m.realString);
        assertEquals(4, m.start);
        assertEquals(7, m.end);

        m = fp.matchSeriesAllowingPrefix("Xxx TLA_Three-Letter Acronym Xxx");
        assertNotNull(m);
        assertEquals(TLA_SHOW, m.matchedResource);
        assertEquals("TLA", m.realString);
        assertEquals(4, m.start);
        assertEquals(7, m.end);

        m = fp.matchSeriesAllowingPrefix("Xxx TLA_Three-Letter Acronym Xxx", 8);
        assertNotNull(m);
        assertEquals(TLA_SHOW, m.matchedResource);
        assertEquals("Three-Letter Acronym", m.realString);
        assertEquals(8, m.start);
        assertEquals(28, m.end);

        m = fp.matchSeriesAllowingPrefix("Xxx TLA_Three-Letter Acronym Xxx", 29);
        assertNull(m);
    }

    public void testMatchSeriesAllowingPrefixMustBeSpaceDelimitedValid()
        throws Exception
    {
    	replaceData("tla");
        Matching<URI> m;

        m = fp.matchSeriesAllowingPrefix("TLA");
        assertNotNull(m);
        assertEquals(TLA_SHOW, m.matchedResource);
        assertEquals("TLA", m.realString);
        assertEquals(0, m.start);
        assertEquals(3, m.end);

        m = fp.matchSeriesAllowingPrefix(" TLA");
        assertNotNull(m);
        assertEquals(TLA_SHOW, m.matchedResource);
        assertEquals("TLA", m.realString);
        assertEquals(1, m.start);
        assertEquals(4, m.end);

        m = fp.matchSeriesAllowingPrefix("TLA ");
        assertNotNull(m);
        assertEquals(TLA_SHOW, m.matchedResource);
        assertEquals("TLA", m.realString);
        assertEquals(0, m.start);
        assertEquals(3, m.end);
    }

    public void testMatchSeriesAllowingPrefixMustBeSpaceDelimitedInvalid()
        throws Exception
    {
    	replaceData("tla");
        Matching<URI> m;

        m = fp.matchSeriesAllowingPrefix("TLAx");
        assertNull(m);

        m = fp.matchSeriesAllowingPrefix(" TLAx");
        assertNull(m);

        m = fp.matchSeriesAllowingPrefix("xTLA");
        assertNull(m);

        m = fp.matchSeriesAllowingPrefix("xTLA ");
        assertNull(m);
    }

    public void testUnwantedPrefix() throws Exception
    {
        replaceData("named-episode-example");
        FilenameMatch<URI> fnm = process("File - Example Show - Named Episode.mpg");
        assertNotNull(fnm);
        assertEquals(EXAMPLE_EPISODE_1_1, fnm.episode);
        assertTrue(fnm.isExact);
    }

    public void testUnwantedPrefixDuplicateTitle() throws Exception
    {
        replaceData("named-episode-tla");
        FilenameMatch<URI> fnm = process("data_TLA_three letter acronym - 1 - named episode.avi");
        assertNotNull(fnm);
        assertEquals("http://www.example.com/tla/1/1#", fnm.episode);
        assertTrue(fnm.isExact);
        assertEquals(1, fnm.ignored.size());
        Matching<URI> m = fnm.ignored.iterator().next();
        assertEquals(0, m.start);
        assertEquals(9, m.end);
    }

    public void testDoubleEpisodeNumber()
        throws Exception
    {
        replaceData(kw("[AA]"), "named-episode-example");

        FilenameMatch<URI> fnm = process("Example Show - S1Ep01(1) - Named Episode[AA]");
        assertNotNull(fnm);
        assertEquals(EXAMPLE_EPISODE_1_1, fnm.episode);
        assertTrue(fnm.isExact);
    }

    /**
     * If the real title is 'Theater', don't match 'Theatre' as 'Theatr' and
     * leave the 'e' as a surplus character.
     * 
     * @throws Exception
     */
    public void testTitleShouldMatchWholeWords() throws Exception
    {
        replaceData("named-episode-example");
        List<TitleMatching<URI>> l = fp.getClosestEpisodes(EXAMPLE_SHOW, "Theatre");
        assertEquals(1, l.size());
        TitleMatching<URI> tm = l.get(0);
        assertEquals("http://www.example.com/1/2#", tm.matchedResource);
        assertEquals(0, tm.start);
        assertEquals(7, tm.end);
        assertEquals("Theater", tm.realString);
        assertFalse(tm.isExact);
    }

    public void testWrongTitleHasCompleteExtraWord()
        throws Exception
    {
        replaceData("named-episode-example");

        FilenameMatch<URI> fnm = process("Example Show - 1 - Named Episode Extended");
        assertNotNull(fnm);
        assertEquals(EXAMPLE_EPISODE_1_1, fnm.episode);
        assertFalse(fnm.isExact);
        assertEquals(1, fnm.ignored.size());

        Matching<URI> m = fnm.ignored.iterator().next();
        assertEquals(m.start, 32);
        assertEquals(m.end, 41);
        assertNull(m.matchedResource);
    }

    public void testSeriesTitlesInEpisodeName() throws Exception
    {
        replaceData("example", "documentary");
        FilenameMatch<URI> fnm = process("Documentary/1x1- Episode About Example Show.avi");
        assertNotNull(fnm);
        assertEquals("http://www.example.com/doc/1/1#", fnm.episode);
        assertTrue(fnm.isExact);
    }

    public void testSeriesTitleNumericUnknownKeywords() throws Exception
    {
        replaceData("example");

        FilenameMatch<URI> fnm = process("example.show.s01e01.XXXXXX.avi");
        assertNotNull(fnm);
        assertFalse(fnm.isExact);
        assertEquals(EXAMPLE_EPISODE_1_1, fnm.episode);
        assertEquals(1, fnm.ignored.size());
        Matching<URI> m = fnm.ignored.iterator().next();
        assertEquals(m.start, 19);
        assertEquals(m.end, 26);
        assertNull(m.matchedResource);
    }

    public void testSeriesTitleNumericAmbiguous() throws Exception
    {
        replaceData("long-running-example");
        FilenameMatch<URI> fnm;
        fnm = process("example show 2x1");
        assertNotNull(fnm);
        assertEquals("http://www.example.com/2/1#", fnm.episode);

        fnm = process("example show 101");
        assertNull(fnm);
    }

    public void testSeriesNameMustBeSpaceDelimited() throws Exception
    {
        replaceData("stub-er");

        FilenameMatch<URI> fnm;
        fnm = process("er 2.mov");
        assertNotNull(fnm);
        assertEquals("http://www.example.com/1/2#", fnm.episode);

        fnm = process("trailer2_sorensen3.mov");
        assertNull(fnm);
    }

    public void testNumbersMustBeDelimited() throws Exception
    {
        replaceData("example");
        Matching<URI> m = fp.findEpisode(EXAMPLE_SHOW, "da39a3ee5e6b4b0d3255bfef95601890afd80709");
        assertNull(m);
    }

    public void testSingleSeasonSeriesProgrammerNumber()
        throws Exception
    {
        replaceData("example-single-season");
        FilenameMatch<URI> fnm = process("Single Season Series - 1");
        assertNotNull(fnm);
        assertEquals("http://www.example.com/1#", fnm.episode);
        assertFalse(fnm.isExact);
    }

    /**
     * For a single-season series, a 'PtX' abbreviation may be appropriate.
     *
     * @throws Exception
     */
    public void testPtPrefixForProgramNumber() throws Exception
    {
        replaceData("example-single-season");

        URI r = fp.getSeries("Single Season Series");
        assertNotNull(r);

        Matching<URI> m = fp.findEpisode(r, "Pt2");
        assertNotNull(m);
        assertEquals(new URI("http://www.example.com/2#"), m.matchedResource);
        assertEquals(0, m.start);
        assertEquals(3, m.end);
        assertEquals("1x2", m.realString);
    }

    /**
     * A bug caused this to throw a NullPointerException at one point.
     */
    public void testInexactMatchWithUnknownSuffix() throws Exception
    {
        replaceData("named-episode-example");
        FilenameMatch<URI> x = process("Example Show N_med E_isode X");
        assertNotNull(x);
        assertFalse(x.isExact);
    }

    public void testEnglishNamingConvention() throws Exception
    {
        replaceData("example");
        FilenameMatch<URI> fnm = process("Example Show Series 1 Episode 2.avi");
        assertNotNull(fnm);
        assertEquals(new URI("http://www.example.com/1/2#"), fnm.episode);
    }

    /**
     * The 'Part x' notation may be delimited by brackets; it should
     * still match.
     */
    public void testPartPrefixWithBrackets() throws Exception
    {
        replaceData("example-single-season");

        URI r = fp.getSeries("Single Season Series");
        assertNotNull(r);

        Matching<URI> m = fp.findEpisode(r, " (Part 1)");
        assertNotNull(m);
        assertEquals(new URI("http://www.example.com/1#"), m.matchedResource);
        assertEquals(2, m.start);
        assertEquals(8, m.end);
        assertEquals("1x1", m.realString);
    }

    /**
     * '24' is one of the shows most prone to false positives.
     * This case is an easy one to avoid, and we should do so.
     */
    public void testNotMatchingWithGreatChaffAtStart() throws Exception
    {
        replaceData("stub-24");

        FilenameMatch<URI> fnm;
        fnm = process("24 01.avi");
        assertNotNull(fnm);
        assertEquals(EXAMPLE_EPISODE_1_1, fnm.episode);

        fnm = process("An.Episode.of.Another.Show.From.24.01.avi");
        assertNull(fnm);
    }

    public void testHdtvMatches() throws Exception
    {
        replaceData(kw("hr.hdtv", "ac3.5.1"), "example");

        FilenameMatch<URI> fnm;

        fnm = process("example.show.s01e01.hr.hdtv.ac3.5.1.avi");
        assertNotNull(fnm);
        assertEquals(EXAMPLE_EPISODE_1_1, fnm.episode);

        fnm = process("example.show.s01e2.hr.hdtv.avi");
        assertNotNull(fnm);
        assertEquals(new URI("http://www.example.com/1/2#"),
                fnm.episode);

        fnm = process("Example.Show.S01E2.HR.HDTV.avi");
        assertNotNull(fnm);
        assertEquals(new URI("http://www.example.com/1/2#"),
                fnm.episode);
    }
}
