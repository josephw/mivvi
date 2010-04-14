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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kafsemo.mivvi.app.Progress;
import org.kafsemo.mivvi.app.SeriesData;
import org.kafsemo.mivvi.recognise.SeriesDataException;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryException;

public class RadioTimesService
{
//    public static final String DEFAULT_URL_ROOT = "http://xmltv.radiotimes.com/xmltv/";
    private final Downloader downloader;
    private final File baseDirectory;
    private final String rootUrl;

    public static Map<Integer, String> parseChannels(InputStream in) throws IOException
    {
        return parseChannels(in, null);
    }

    public static Map<Integer, String> parseChannels(InputStream in, Collection<String> channels) throws IOException
    {
        Map<Integer, String> m = new HashMap<Integer, String>();

        BufferedReader br = new BufferedReader(new InputStreamReader(in, "US-ASCII"));
        String s;
        while ((s = br.readLine()) != null) {
            int i = s.indexOf('|');
            if (i >= 0) {
                String c = s.substring(i + 1);
                m.put(Integer.valueOf(s.substring(0, i)), c);
                if (channels != null)
                    channels.add(c);
            }
        }
        
        return m;
    }

    public static List<Programme> parseListing(InputStream in, String channel) throws IOException
    {
        Pattern datePattern = Pattern.compile("\\~(\\d{2})/(\\d{2})/(\\d{4})\\~(\\d{2}):(\\d{2})\\~(\\d{2}):(\\d{2})\\~(\\d+)$");

        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("Europe/London"));

        List<Programme> l = new ArrayList<Programme>();

        BufferedReader br = new BufferedReader(new InputStreamReader(in, "windows-1252"));
        StringBuilder pending = new StringBuilder();
        String s;
        while ((s = br.readLine()) != null) {
            pending.append(s);
            if (s.endsWith(" ")) {
                continue;
            }

            s = pending.toString();
            pending.setLength(0);
            
            Programme p = new Programme();
            
            int i = s.indexOf('~');
            
            if (i >= 0) {
                p.title = s.substring(0, i);
                
                int j = s.indexOf('~', i + 2);
                if (j >= 0)
                    p.subtitle = s.substring(i + 2, j);
            
                Matcher m = datePattern.matcher(s.substring(i));
                if (m.find()) {
                    cal.clear();
                    cal.set(Calendar.YEAR, Integer.parseInt(m.group(3)));
                    cal.set(Calendar.MONTH, Integer.parseInt(m.group(2)) - 1);
                    cal.set(Calendar.DATE, Integer.parseInt(m.group(1)));
                    cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(4)));
                    cal.set(Calendar.MINUTE, Integer.parseInt(m.group(5)));
                    
                    p.start = cal.getTime();
                    
                    cal.add(Calendar.MINUTE, Integer.parseInt(m.group(8)));
                    p.end = cal.getTime();
                }
            }
            
            p.channel = channel;

            l.add(p);
        }
        
        return l;
    }
    
//    RadioTimesService(Downloader d, File baseDirectory)
//    {
//        this(d, baseDirectory, DEFAULT_URL_ROOT);
//    }

    public RadioTimesService(Downloader d, String rootUrl)
    {
        this.downloader = d;
        this.baseDirectory = d.getCacheDirectory();
        this.rootUrl = rootUrl;
    }

    private InputStream getChannelStream() throws IOException
    {
        File cf = new File(baseDirectory, "radiotimes-channels.dat");
        
        URL url = new URL(new URL(rootUrl), "channels.dat");
        
        downloader.refresh(url.toString(), cf, Downloader.HOUR_MILLIS * 24 * 7);
        
        if (cf.exists())
            return new FileInputStream(cf);
        else
            throw new IOException("Resource not found: " + url);
    }

    private Map<Integer, String> channelMap = null;
    private List<String> channelList = null;
    private Map<String, Integer> reverseChannelMap;

    synchronized void init() throws IOException
    {
        channelList = new ArrayList<String>();
        InputStream in = getChannelStream();
        channelMap = parseChannels(in, channelList);
        in.close();

        reverseChannelMap = new HashMap<String, Integer>();

        Iterator<Entry<Integer, String>> i = channelMap.entrySet().iterator();
        while (i.hasNext()) {
            Entry<Integer, String> e = i.next();
            reverseChannelMap.put(e.getValue(), e.getKey());
        }
    }
    
    List<String> getChannelList()
    {
        return channelList;
    }

    public int getChannelId(String name)
    {
        Integer id = reverseChannelMap.get(name);
        if (id == null)
            return -1;
        else
            return id.intValue();
    }

    private int[] channelIds = {};

    public void setChannels(String[] channelNames)
    {
        int[] a = new int[channelNames.length];
        for (int i = 0 ; i < channelNames.length ; i++) {
            int c = getChannelId(channelNames[i]);
            if (c < 0)
                throw new IllegalArgumentException("Unknown channel: " + channelNames[i]);
            a[i] = c;
        }
        
        this.channelIds = a;
    }

    Collection<File> refresh(Progress pro) throws IOException
    {
        Collection<File> files = new ArrayList<File>(channelIds.length);

        pro.setMaximum(channelIds.length);

        for (int i = 0 ; i < channelIds.length ; i++) {
            if (pro.isCanceled())
                return files;

            int c = channelIds[i];
            
            pro.setNote("Fetching " + getChannelName(c));
            
            File f = refreshChannel(c);
            
            if (f != null)
                files.add(f);
            
            pro.setProgress(i + 1);
        }
        
        return files;
    }
    
    File refreshChannel(int id) throws IOException
    {
        URL u = new URL(new URL(rootUrl), id + ".dat");
        File f = new File(baseDirectory, "radiotimes-" + id + ".dat");
        downloader.refresh(u.toString(), f, Downloader.HOUR_MILLIS * 24);
        if (f.exists())
            return f;
        else
            return null;
    }

    public String getChannelName(int i)
    {
        return channelMap.get(Integer.valueOf(i));
    }

    public static Resource recognise(SeriesData sd, Resource series,
            String episode) throws RepositoryException, SeriesDataException
    {
        Pattern p = Pattern.compile("^(\\d+)/(\\d+)\\b");
        
        Matcher m = p.matcher(episode);
        if (m.find()) {
            Resource ep = sd.getEpisodeByNumericSequence(series, m.group(0));
            
            Resource ep2 = sd.getEpisodeByTitleApprox(series, episode.substring(m.end()));
            
            if (ep2 == null)
                return ep;
            else
                return ep2;
        } else {
            return sd.getEpisodeByTitleApprox(series, episode);
        }
    }

    public static String getSingleChannel(List<String> l, String string)
    {
        if (l.contains(string))
            return string;

        if (string.equals("ITV1") && l.contains("ITV1 London"))
            return "ITV1 London";

        if (string.equals("BBC1") || string.equals("BBC2") || string.equals("ITV1")) {
            String b = null;

            Iterator<String> i = l.iterator();
            while (i.hasNext()) {
                String c = i.next();

                if (c.startsWith(string + " ")) {
                    if (b == null || c.length() < b.length())
                        b = c;
                }
            }
            
            return b;
        }
        
        return null;
    }
    
    public static int compareRegional(String a, String b)
    {
        int al = a.length(),
            bl = b.length();

        if (al < bl) {
            return 1;
        } else if (al > bl) {
            return -1;
        } else {
            return 0;
        }
    }
    
    public static int compareITV1Regional(String a, String b)
    {
        if (a.equals(b))
            return 0;
        
        if (a.equals("ITV1"))
            return 1;
        else if (b.equals("ITV1"))
            return -1;
        
        if (a.equals("ITV1 London"))
            return 1;
        else if (b.equals("ITV1 London"))
            return -1;

        return compareRegional(a, b);
    }
}
