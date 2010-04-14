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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class HttpUtil
{
    /**
     * Fetch a login page, fill in the login form, submit it, and return the cookies
     * that, presumably, identify the session.
     * 
     * @param loginPage
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    public static List<String> loginAndGetCookies(URL loginPage, String username, String password) throws IOException
    {
        URLConnection uc = loginPage.openConnection();

        List<HtmlUtil.Form> forms = HtmlUtil.parseForms(loginPage, uc.getInputStream());
        if (forms.size() != 1)
            throw new CookieLoginException("No forms (or too many) found on login page " + loginPage);
        
        HtmlUtil.Form f = forms.get(0);
        if (f.inputs.size() != 1)
            throw new CookieLoginException("No username input (or too many) found on login page " + loginPage);
        
        if (f.passwords.size() != 1)
            throw new CookieLoginException("No password input (or too many) found on login page " + loginPage);
        
        HttpURLConnection.setFollowRedirects(false);

        uc = f.action.openConnection();
        if (uc instanceof HttpURLConnection) {
            HttpURLConnection h = (HttpURLConnection)uc;
            h.setRequestMethod(f.getMethod());
        }
            
        StringBuilder postdata = new StringBuilder(f.inputs.get(0) + "=" + username + "&"
            + f.passwords.get(0) + "=" + password);

        Iterator<Entry<String, String>> i = f.hiddens.entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, String> e = i.next();

            postdata.append("&" + e.getKey() + "=" + URLEncoder.encode(e.getValue(), "utf-8"));
        }
        
        byte[] cba = postdata.toString().getBytes();
        
        uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        uc.setRequestProperty("Content-Length", Integer.toString(cba.length));
        
        uc.setDoOutput(true);

        uc.getOutputStream().write(cba);

        List<String> cookies = HttpCookieUtil.getCookies(uc.getHeaderFields());
        
        if (cookies == null)
            throw new CookieLoginException("No cookies in form submission response from " + f.action);
        
        return cookies;
    }

    static class CookieLoginException extends IOException
    {
        public CookieLoginException(String msg)
        {
            super(msg);
        }
    }
}
