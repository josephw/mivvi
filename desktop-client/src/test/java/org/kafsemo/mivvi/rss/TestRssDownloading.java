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

package org.kafsemo.mivvi.rss;

import java.net.MalformedURLException;
import java.net.URL;

import org.kafsemo.mivvi.rss.RssDownloading;

import junit.framework.TestCase;

public class TestRssDownloading extends TestCase
{
    public void testSafeFilename() throws MalformedURLException
    {
        String u = "http://www.example.com/~user/file-name-3";
        
        String f = RssDownloading.safeFilename(new URL(u));
        
        assertEquals("http___www_example_com__user_file-name-3", f);
    }
}
