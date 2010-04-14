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

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpCookieUtil
{
    public static void addCookieHeaders(URLConnection uc, Collection<String> cookies)
    {
        if (cookies.isEmpty())
            return;

        StringBuffer sb = new StringBuffer();

        Iterator<String> i = cookies.iterator();
        while (i.hasNext()) {
            if (sb.length() > 0)
                sb.append("; ");
            sb.append(i.next());
        }
        uc.setRequestProperty("Cookie2", "$Version=\"1\"");
        uc.setRequestProperty("Cookie", sb.toString());
    }

    public static List<String> getCookies(Map<String, List<String>> headerFields)
    {
        List<String> cookieHeaders = headerFields.get("Set-Cookie");

        if (cookieHeaders == null)
            return null;

        List<String> cookies = new ArrayList<String>(cookieHeaders.size());

        Iterator<String> j = cookieHeaders.iterator();
        while (j.hasNext()) {
            String s = j.next();
            String c = s.split(";\\s*")[0];

            cookies.add(c);
        }
        
        return cookies;
    }
}
