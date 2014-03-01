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

package org.kafsemo.mivvi.tv;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StaticContentHandler implements HttpHandler
{
    private final String etag = '"' + Double.toString(Math.random()) + '"';
    private final URL content;
    
    public StaticContentHandler(URL c)
    {
        this.content = c;
    }
    
    public void handle(HttpExchange x) throws IOException
    {
        /* Only allow GET */
        if (!"GET".equals(x.getRequestMethod())) {
            x.sendResponseHeaders(501, -1);
            return;
        }

        /* If they supply the right ETag, let them know
         *  the content hasn't changed.
         */
        List<String> inm = x.getRequestHeaders().get("If-None-Match");
        if (inm != null && inm.contains(etag)) {
            x.sendResponseHeaders(304, 0);
            x.getResponseBody().close();
            return;
        }

        /* Provide the content */
        x.getResponseHeaders().add("Content-Type", "text/plain");
        x.getResponseHeaders().add("ETag", etag);
        x.sendResponseHeaders(200, 0);
        
        InputStream in = content.openStream();
        
        OutputStream bdy = x.getResponseBody();
        
        byte[] buffer = new byte[65535];
        
        int l;
        
        while ((l = in.read(buffer)) >= 0) {
            bdy.write(buffer, 0, l);
        }
        bdy.close();
    }
}
