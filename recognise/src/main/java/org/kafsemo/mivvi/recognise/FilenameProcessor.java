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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Use filenames to recognise and identity episodes from titles
 *  and numbers.
 *
 * @author joe
 */
public class FilenameProcessor<T>
{
    private final SeriesData<T> data;
    private final FileNamingData namingData;

    private Iterable<Item<T>> seriesTitles;
    private Iterable<Item<T>> seriesDescriptions;

    public FilenameProcessor(SeriesData<T> d, FileNamingData n)
    {
        this.data = d;
        this.namingData = n;
    }

    public FilenameMatch<T> process(File file) throws SeriesDataException
    {
//        boolean isInconsistent = false;

        String s = file.getPath();

        T directorySeries = null;

        String[] parts = s.split("\\" + File.separatorChar);
        for (int i = 0; i < parts.length - 1; i++) {
            Matching<T> m = matchSeries(parts[i]);
            if ((m != null) && (parts[i].length() == m.matchLength())) {
                directorySeries = m.matchedResource;
            }
        }

        s = parts[parts.length - 1];

        int j = s.lastIndexOf('.');

        if (j >= 0) {
            String suffix = s.substring(j + 1).toLowerCase();
            // XXX Should check 'suffix' for a list of known types
            if (suffix.length() <= 4) {
                s = s.substring(0, j);
            }
        }

        return processName(s, directorySeries);
    }

    public FilenameMatch<T> processName(String s) throws SeriesDataException
    {
        return processName(s, null);
    }

    public FilenameMatch<T> processName(String s, T directorySeries)
        throws SeriesDataException
    {
        boolean isInconsistent = false;

        Matching<T> m = matchSeriesAllowingPrefix(s);

        /* Only allow ten characters of chaff */
        if (m != null && m.start > 10) {
            m = null;
        }

        int o = 0;

        while (m != null) {
            T series = m.matchedResource;
            if (directorySeries != null) {
                if (!series.equals(directorySeries)) {
                    isInconsistent = true;
                }
            }

            String remainder = s.substring(m.end);

            FilenameMatch<T> fnm = processFromSeries(series, remainder, isInconsistent, m.end);
            if (fnm != null) {
                if (o > 0) {
                    fnm.ignored.add(new Matching<T>(s, 0, o, null, null));
                }
                return fnm;
            }

            o = m.end + 1;

            m = matchSeriesAllowingPrefix(s, o);
        }

        if (directorySeries != null) {
            return processFromSeries(directorySeries, s, isInconsistent, 0);
        } else {
            return null;
        }
    }


    private FilenameMatch<T> processFromSeries(T series, String remainder, boolean isInconsistent, int o) throws SeriesDataException
    {
        List<Matching<T>> ecl = new ArrayList<Matching<T>>();

        List<Matching<T>> e;

        do {
            /* Match a numeric episode code */
            e = findEpisodes(series, remainder);
            if (e.size() > 0) {
                Matching<T> m2 = e.get(0);

                /* Skip over the matched episode number */
                remainder = remainder.substring(m2.end);
                o += m2.end;

                // Bad: assuming all episode code matches are the same length

                ecl.addAll(e);
            }
        } while (!e.isEmpty());

        /* A date could really be anywhere, but just check here for now */
        NormalisedString ns = new NormalisedString(remainder);
        Matching<T> m = matchDate(ns.toString());
        if (m != null) {
            remainder = remainder.substring(ns.getOriginalPosition(m.end));
            o += ns.getOriginalPosition(m.end);
        }

        List<TitleMatching<T>> l = getClosestEpisodes(series, remainder);

        /* If numericGuesses is empty, that means no numbers referred to
         *  real episodes. If it's null, there were no valid numeric
         *  tokens.
         */
        Set<T> numericGuesses;

        if (ecl.size() >= 1) {
            numericGuesses = new HashSet<T>();
            Iterator<Matching<T>> i = ecl.iterator();
            while (i.hasNext()) {
                T uri = i.next().matchedResource;
                if (uri != null)
                    numericGuesses.add(uri);
            }
            Collections.sort(l, TitleMatching.episodePriorityComparator(numericGuesses));
        } else {
            numericGuesses = null;
        }

        Iterator<TitleMatching<T>> i = l.iterator();

        while (i.hasNext()) {
            TitleMatching<T> tm = i.next();

            String trailer = remainder.substring(tm.end);

            /* Discard keywords */
            trailer = trailer.substring(matchKeywords(trailer).end);

            if (new NormalisedString(trailer).toString().length() == 0) {
                if (numericGuesses == null)
                    return new FilenameMatch<T>(tm.matchedResource, true);
                else
                    return new FilenameMatch<T>(tm.matchedResource, numericGuesses.contains(tm.matchedResource));
            }

        }

        /* No exact match, so suggest the best one */
        if (l.size() >= 1) {
            TitleMatching<T> tm = l.get(0);

            /**
             * Enforce numeric matching if a match isn't exact.
             */
            if (tm.isExact || numericGuesses == null || numericGuesses.isEmpty() || numericGuesses.contains(tm.matchedResource)) {
                FilenameMatch<T> fnm = new FilenameMatch<T>(tm.matchedResource, false);
                fnm.ignored.add(new Matching<T>(remainder, o + tm.end, o + remainder.length(), null, null));
                return fnm;
            }
        } else if ((numericGuesses != null) && !numericGuesses.isEmpty()) {
            /* Does it match with a number and no title? */

            if (numericGuesses.size() > 1) {
                return null;
            }

            T numericGuess = numericGuesses.iterator().next();

            int ke = matchKeywords(remainder).end;
            String trailer = remainder.substring(ke);
            if (new NormalisedString(trailer).toString().length() == 0) {
                return new FilenameMatch<T>(numericGuess, false);
            }

            // No exact match - mark remainder as ignored
            Matching<T> ig = new Matching<T>(trailer, o + ke, o + remainder.length(), null, null);
            if (ig.matchLength() > 12)
                return null;

            FilenameMatch<T> fnm = new FilenameMatch<T>(numericGuess, false);
            fnm.ignored.add(ig);
            return fnm;
        }

        return null;
    }

    static final String ABBREVS = " USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#>,"
        + "  mvi FOR <http://mivvi.net/rdf#>,"
        + "  dc FOR <http://purl.org/dc/elements/1.1/>";

//    private static Value[][] VA = {};
//
//    private Value[][] cachedTitles = null;

    private Iterable<Item<T>> getTitlesAndDescriptions() throws SeriesDataException
    {
        Collection<Item<T>> c = new ArrayList<Item<T>>();

        for (Item<T> i : getSeriesTitles()) {
            c.add(i);
        }

        for (Item<T> i : getSeriesDescriptions()) {
            c.add(i);
        }

        /* Add titles without definite article */
        for (Item<T> i : getSeriesTitles()) {
            if (i.label.toLowerCase().startsWith("the ")) {
                c.add(new Item<T>(i.label.substring(4), i.resource));
            }
        }

        return c;
    }

    private Iterable<Item<T>> getSeriesTitles() throws SeriesDataException
    {
        if (seriesTitles == null) {
            seriesTitles = data.getSeriesTitles();
        }
        return seriesTitles;
    }

    private Iterable<Item<T>> getSeriesDescriptions() throws SeriesDataException
    {
        if (seriesDescriptions == null) {
            seriesDescriptions = data.getSeriesDescriptions();
        }
        return seriesDescriptions;
    }


    public T getSeries(String s) throws SeriesDataException
    {
        for (Item<T> i : getTitlesAndDescriptions()) {
            if (s.equalsIgnoreCase(i.label)) {
                    return i.resource;
            }
        }

        return null;
    }

    /**
     * Match a series name at the start of the string. Take the longest match,
     * preferring titles to descriptions.
     *
     * @param s
     * @return
     * @throws SeriesDataException
     */
    public Matching<T> matchSeries(String s) throws SeriesDataException
    {
        NormalisedString ns = new NormalisedString(s);

        String lns = ns.toString().toLowerCase();

        Matching<T> m = null;

        for (Item<T> i : getTitlesAndDescriptions()) {
            T series = i.resource;
            String label = i.label;

            NormalisedString nls = new NormalisedString(label);

            if (nls.toString().length() == 0)
                continue;

            if (lns.startsWith(nls.toString().toLowerCase())) {
                /* The amount of the original string covered by this match */
                int origMatchAmount = ns.getOriginalPosition(nls.toString().length());
                if ((m == null) || (origMatchAmount > m.matchLength()))
                    m = new Matching<T>(s, 0, origMatchAmount, i.label, series);
            }
        }

        return m;
    }

    public Matching<T> matchSeriesAllowingPrefix(String s) throws SeriesDataException
    {
        return matchSeriesAllowingPrefix(s, 0);
    }

    public Matching<T> matchSeriesAllowingPrefix(String s, int o) throws SeriesDataException
    {
        NormalisedString ns = new NormalisedString(s);

        String lns = ns.toString().toLowerCase();

        Matching<T> m = null;

        for (Item<T> i : getTitlesAndDescriptions()) {
            T series = i.resource;
            String label = i.label;

            NormalisedString nls = new NormalisedString(label);

            if (nls.toString().length() == 0)
                continue;

            int p = lns.indexOf(nls.toString(), o);

            if (p >= 0) {
                int nsEnd = p + nls.toString().length();
                /* Require word delimiting */
                if (((p == 0) || (lns.charAt(p - 1) == ' '))
                    && ((nsEnd == lns.length()) || (lns.charAt(nsEnd) == ' ')))
                {
                    if ((m == null) || (p < m.start) || (p == m.start && nls.toString().length() > m.matchLength())) {
                        m = new Matching<T>(s, ns.getOriginalPosition(p), ns.getOriginalPosition(p + nls.toString().length()),
                                         label, series);
                    }
                }
            }
        }

        return m;
    }

    /* Patterns for season and episode numbers */
    Pattern[] pa = {
            Pattern.compile("^(\\d+)x(\\d+)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^s(\\d+)ep?(\\d+)\\b", Pattern.CASE_INSENSITIVE),

            /* "Series", rather than "Season" - en_GB terminology */
            Pattern.compile("^series\\s+(\\d+)\\s+episode\\s+(\\d+)\\b", Pattern.CASE_INSENSITIVE)
    };

    /* A single number */
    Pattern p2 = Pattern.compile("^(\\d+)\\b");

    /* A 'part' number, for single-season series */
    Pattern p3 = Pattern.compile("^p(?:ar)?t\\s*(\\d+)\\b", Pattern.CASE_INSENSITIVE);

    private void addBySeasonAndProgramNumber(T series, String s, NormalisedString ns, Matcher m, int season, int num, List<Matching<T>> l) throws SeriesDataException
    {
        T episode = getBySeasonAndProgramNumber(series, season, num);

        l.add(new Matching<T>(s, ns.getOriginalPosition(m.start()),
                            ns.getOriginalPosition(m.end()),
                            season + "x" + num,
                            episode));
    }

    public Matching<T> findEpisode(T series, String s) throws SeriesDataException
    {
        List<Matching<T>> l = findEpisodes(series, s);
        if (l.size() >= 1)
            return l.get(0);
        else
            return null;
    }

    public List<Matching<T>> findEpisodes(T series, String s)
        throws SeriesDataException
    {
        List<Matching<T>> l = new ArrayList<Matching<T>>(2);

        NormalisedString ns = new NormalisedString(s);

        Matcher m = null;

        for (int i = 0; (m == null) && (i < pa.length); i++) {
            m = pa[i].matcher(ns.toString());
            if (!m.find()) {
                m = null;
            }
        }

        if (m != null) {
            int season = Integer.parseInt(m.group(1)),
                num = Integer.parseInt(m.group(2));

            addBySeasonAndProgramNumber(series, s, ns, m, season, num, l);
        } else {

            m = p2.matcher(ns.toString());

            if (m.find()) {
                int num = Integer.parseInt(m.group(1));

                T episode = getByEpisodeNumber(series, num);
                l.add(new Matching<T>(s, ns.getOriginalPosition(m.start(1)),
                                             ns.getOriginalPosition(m.end(1)),
                                             Integer.toString(num),
                                             episode));

                if (num >= 100) {
                    addBySeasonAndProgramNumber(series, s, ns, m, num / 100, num % 100, l);
                }
            } else {
                m = p3.matcher(ns.toString());
                if (m.find()) {
                    int num = Integer.parseInt(m.group(1));

                    addBySeasonAndProgramNumber(series, s, ns, m, 1, num, l);
                }
            }
        }

        return l;
    }

    Pattern sequenceInRangePattern = Pattern.compile("(\\d+)/(\\d+)");

    /**
     * Get a single episode, for single-season programs, from its sequence number.
     * This syntax (a/b) is often used in GB listings.
     *
     * @param series
     * @param string
     * @return
     * @throws SeriesDataException
     */
    public T getEpisodeByNumericSequence(T series, String string) throws SeriesDataException
    {
        Matcher m = sequenceInRangePattern.matcher(string);

        if (m.matches()) {
            int seq = Integer.parseInt(m.group(1))/*,
                range = Integer.parseInt(m.group(2))*/;

            List<T> l = getAllEpisodesWithEpisodeNumber(series, seq);
            if (l.size() == 1)
                return l.get(0);
        }

        return null;
    }

    public T getByEpisodeNumber(T series, int i) throws SeriesDataException
    {
        return getSeriesDetails(series).episodesByNumber.get(Integer.toString(i));
    }

    public T getBySeasonAndProgramNumber(T series, int season, int i) throws SeriesDataException
    {
        return getSeriesDetails(series).episodesByNumber.get(season + "x" + i);
    }

    /**
     * Fetch all episodes at position 'i' in their season.
     *
     * @param series
     * @param i
     * @return
     * @throws SeriesDataException
     */
    public List<T> getAllEpisodesWithEpisodeNumber(T series, int n) throws SeriesDataException
    {
        List<T> l = new ArrayList<T>();

        String s = "x" + n;

        Map<String, T> m = getSeriesDetails(series).episodesByNumber;

        Iterator<Entry<String, T>> i = m.entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, T> e = i.next();

            if (e.getKey().endsWith(s)) {
                l.add(e.getValue());
            }
        }

        return l;
    }

    /**
     * Find an episode with this specific title (modulo normalisation).
     *
     * @param seriesUri
     * @param title
     * @return
     * @throws SeriesDataException
     */
    public T getEpisodeByTitle(T series, String title) throws SeriesDataException
    {
        NormalisedString nt = new NormalisedString(title);

        List<EpisodeTitleDetails<T>> l = getSeriesDetails(series).episodeTitlesAndDescriptions;

        Iterator<EpisodeTitleDetails<T>> i = l.iterator();
        while (i.hasNext()) {
            EpisodeTitleDetails<T> etd = i.next();

            if (nt.toString().equals(new NormalisedString(etd.title).toString()))
                return etd.res;
        }

        return null;
    }

    public T getEpisodeByTitleApprox(T series, String title) throws SeriesDataException
    {
        T res = getEpisodeByTitle(series, title);

        if (res != null)
            return res;

        for (TitleMatching<T> tm : getClosestEpisodes(series, title)) {
//            System.out.println(title + ", " + tm.weight + ", " + tm.realString);

            if (tm.matchLength() == title.length())
                return tm.getResource();
            /*
            if (tm.isExact && tm.matchLength() == title.length()) {
                // If there's ambiguity, fail
                if (res != null)
                    return null;
                res = tm.getResource();
            }
            */
        }

        return res;
    }

    private Map<T, SeriesDetails<T>> seriesDetails = new HashMap<T, SeriesDetails<T>>();

    private SeriesDetails<T> getSeriesDetails(T series) throws SeriesDataException
    {
        SeriesDetails<T> sd = seriesDetails.get(series);
        if (sd == null) {
            sd = getSeriesDetailsImpl(series);
            seriesDetails.put(series, sd);
        }

        if (sd == null) {
            throw new SeriesDataException("No data available for series " + series);
        }

        return sd;
    }

    private List<EpisodeTitleDetails<T>> getEpisodeTitles(T series) throws SeriesDataException
    {
        return getSeriesDetails(series).episodeTitlesAndDescriptions;
    }

    public Matching<T> matchEpisodeTitle(T series, String s) throws SeriesDataException
    {
        Matching<T> m = null;

        NormalisedString ns = new NormalisedString(s);

        for (EpisodeTitleDetails<T> etd : getEpisodeTitles(series)) {
            String label = etd.title;

            NormalisedString nls = new NormalisedString(label);

            if (nls.toString().length() == 0)
                continue;

            if (ns.toString().startsWith(nls.toString())) {
                /* The amount of the original string covered by this match */
                int origMatchAmount = ns.getOriginalPosition(nls.toString().length());
                if ((m == null) || (origMatchAmount > m.matchLength()))
                    m = new Matching<T>(s, ns.getOriginalPosition(0), origMatchAmount, label, etd.res);
            }
        }

        return m;
    }

//    private static URI KW_URI = new URIImpl("tag:kafsemo.org,2004:mivvi#keyword");

    private List<NormalisedString> keywords = null;

    public Matching<T> matchKeyword(String s) throws SeriesDataException
    {
        NormalisedString ns = new NormalisedString(s);

        if (keywords == null) {
            List<NormalisedString> l = new ArrayList<NormalisedString>();

            for (String label : namingData.getKeywords()) {
                l.add(new NormalisedString(label));
            }

            this.keywords = l;
        }

        Matching<T> m = null;

        for (NormalisedString nls : keywords) {
            if (nls.toString().length() == 0)
                continue;

            if (ns.toString().startsWith(nls.toString())) {
                /* The amount of the original string covered by this match */
                int origMatchAmount = ns.getOriginalPosition(nls.toString().length());
                if ((m == null) || (origMatchAmount > m.matchLength()))
//                    m = new Matching<T>(s, ns.getOriginalPosition(0), origMatchAmount, nls.getOriginal(), KW_URI);
                    m = new Matching<T>(s, ns.getOriginalPosition(0), origMatchAmount, nls.getOriginal(), null);
            }
        }
        return m;
    }

    public List<TitleMatching<T>> getClosestEpisodes(T series, String s, float maxDistance) throws SeriesDataException
    {
        return getClosestEpisodes(series, new NormalisedString(s), maxDistance);
    }

    private List<TitleMatching<T>> getClosestEpisodes(T series, NormalisedString ns, float maxDistance) throws SeriesDataException
    {
        List<TitleMatching<T>> matches = new ArrayList<TitleMatching<T>>();

        float maxDistPerChar = StringUtil.weight(maxDistance, ns.toString().length());
        float suggestionDistPerChar = StringUtil.weight(StringUtil.suggestionFactor(maxDistance), ns.toString().length());
//        float bestDistPerChar = Float.MAX_VALUE;
//        int bestLength = 0;

        List<EpisodeTitleDetails<T>> l = getEpisodeTitles(series);

        Iterator<EpisodeTitleDetails<T>> i = l.iterator();
        while (i.hasNext()) {
            EpisodeTitleDetails<T> etd = i.next();

            String label = etd.title;

            NormalisedString nls = new NormalisedString(label);

            StringUtil.LevenshteinResult lr;
            
            lr = StringUtil.levenshteinDistance(nls.toString(), ns.toString());

            int dist = lr.distanceWithoutSuffix;
            int length = lr.lengthWithoutSuffix;

            /* Pad out to the next word boundary */
            int p = ns.toString().indexOf(' ', length);
            if (p < 0)
                p = ns.toString().length();

            dist += (p - length);
            length = p;

            float distPerChar = StringUtil.weight(dist, length);


            if (distPerChar <= suggestionDistPerChar) {
                TitleMatching<T> m = new TitleMatching<T>(ns.getOriginal(), 0, ns.getOriginalPosition(length), label, etd.res,
                                 distPerChar <= maxDistPerChar,
                                 etd.isPrimary,
                                 distPerChar);
                matches.add(m);
            }
        }

//        Collections.sort(matches, TitleMatching.MATCHING_COMPARATOR);
        Collections.sort(matches, new TitleMatching.MatchPriorityComparator());

        return matches;
    }

    public List<TitleMatching<T>> getClosestEpisodes(T series, String s) throws SeriesDataException
    {
        NormalisedString ns = new NormalisedString(s);

        return getClosestEpisodes(series, ns, StringUtil.maxDistance(ns.toString()));
    }

    static Pattern pDate = Pattern.compile("^\\s*(\\d{4})(\\d{2})(\\d{2})\\b");

    /**
     * Match a whitespace-delimited eight-digit date in a normalised string.
     *
     * @param string
     * @return
     */
    public static <T> Matching<T> matchDate(String s)
    {
        Matcher m = pDate.matcher(s);

        if (m.find()) {
            int y = Integer.parseInt(m.group(1));
            if (y >= 1900 && y < 3000) {
                return new Matching<T>(s, m.start(1), m.end(3),
                                    m.group(1) + "-" + m.group(2) + "-" + m.group(3), null);
            }
        }

        return null;
    }

    public Matching<T> matchKeywords(String string) throws SeriesDataException
    {
        int start = 0, end = 0;
        StringBuffer sb = new StringBuffer(string.length());

        Matching<T> m;

        do {
            m = matchKeyword(string);

            if (m != null) {
                if (end > 0) {
                    sb.append(" ");
                } else {
                    start = m.start;
                }
                end += m.end;
                sb.append(m.realString);

                string = string.substring(m.end);
            }
        } while (m != null);

        return new Matching<T>(string, start, end, sb.toString(), null);
    }

//    private void addEpisodeTitlesAndDescriptions(List<EpisodeTitleDetails> l, Resource ep)
//        throws RepositoryException
//    {
//        RepositoryResult<Statement> si;
//
//        si = rep.getStatements(ep, RdfUtil.Dc.title, null, true);
//        while (si.hasNext()) {
//            Statement stmt = si.next();
//            String t = RdfUtil.literalString(stmt.getObject());
//            if (t != null)
//                l.add(new EpisodeTitleDetails(ep, t, true));
//        }
//
//        si = rep.getStatements(ep, RdfUtil.Dc.description, null, true);
//        while (si.hasNext()) {
//            Statement stmt = si.next();
//            String t = RdfUtil.literalString(stmt.getObject());
//            if (t != null)
//                l.add(new EpisodeTitleDetails(ep, t, false));
//        }
//    }

    private SeriesDetails<T> getSeriesDetailsImpl(T series) throws SeriesDataException
    {
        return data.getSeriesDetails(series);
    }
}
