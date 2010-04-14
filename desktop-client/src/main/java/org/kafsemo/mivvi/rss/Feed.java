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

package org.kafsemo.mivvi.rss;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Feed
{
    /* Mandatory */
    public String url;

    /* Optional */
    public String loginPage;
    public String username, password;
    
    public Feed()
    {
    }

    public Feed(String url)
    {
        this(url, null, null, null);
    }
    
    public Feed(String url, String username, String password)
    {
        this(url, null, username, password);
    }

    public Feed(String url, String loginPage, String username, String password)
    {
        this.url = url;
        this.loginPage = loginPage;
        this.username = username;
        this.password = password;
    }
    
    public Feed(Feed f)
    {
        this(f.url, f.loginPage, f.username, f.password);
    }
    
    public boolean isEmpty()
    {
        return isEmpty(url) && isEmpty(loginPage) && isEmpty(username) && isEmpty(password);
    }

    private static boolean isEmpty(String s)
    {
        return (s == null || s.equals(""));
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Feed@" + url);
        if (loginPage != null) {
            sb.append("(" + loginPage + "," + username + ")");
        }
        
        return sb.toString();
    }

    public static List<Feed> deepCopy(List<Feed> l)
    {
        ArrayList<Feed> l2 = new ArrayList<Feed>(l.size());
        Iterator<Feed> i = l.iterator();
        while (i.hasNext())
            l2.add(new Feed(i.next()));
        return l2;
    }
}
