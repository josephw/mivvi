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

package org.kafsemo.mivvi.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * Methods to deal with <code>uri:{sha1,md5}:</code> URIs.
 * 
 * @author Joseph Walton
 */
public class HashUris
{
    public static Collection<URI> digest(File f) throws IOException
    {
        FileInputStream fis = new FileInputStream(f);
        try {
            FileChannel fc = fis.getChannel();
            try {
                return digestStream(fc);
            } finally {
                fc.close();
            }
        } finally {
            fis.close();
        }
    }
    
    public static Collection<URI> digestStream(ReadableByteChannel fc) throws IOException
    {
        try {
            MessageDigest mdSha1 = MessageDigest.getInstance("SHA1"),
                mdMd5 = MessageDigest.getInstance("MD5");
    
            byte[] ba = new byte[65536];
            ByteBuffer bb = ByteBuffer.wrap(ba);
            
            int l;
            
            while ((l = fc.read(bb)) >= 0) {
                bb.flip();

                mdSha1.update(ba, 0, l);
                bb.rewind();

                mdMd5.update(ba, 0, l);
                bb.rewind();
            }
            
            fc.close();
            
            Collection<URI> uris = Arrays.asList(new URI[]{
                    new URIImpl("urn:sha1:" + encode(mdSha1.digest())),
                    new URIImpl("urn:md5:" + encode(mdMd5.digest()))
            });

            return uris;
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException("Necessary digest algorithm not available", nsae);
        }
    }

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    static String encode(byte[] ba)
    {
        StringBuffer sb = new StringBuffer((ba.length * 8 + 4) / 5);

        int buff = 0;
        int pres = 0;

        for (int i = 0 ; i < ba.length ; i++) {
            int b = ba[i] & 0xff;

            buff = buff << 8;
            buff |= b;
            pres += 8;
            
            while (pres >= 5) {
                int t = (buff >> (pres - 5)) & 0x1F;
                sb.append(ALPHABET.charAt(t));
                pres -= 5;
            }
        }
        
        if (pres > 0) {
            int t = (buff << (5 - pres)) & 0x1F;
            sb.append(ALPHABET.charAt(t));
        }

        return sb.toString();
    }

    public static boolean isHashUri(String uri)
    {
        String l = uri.toLowerCase();
        return (l.startsWith("urn:sha1:") || l.startsWith("urn:md5:"));
    }
    
    public static boolean isHashUri(Value v)
    {
        String uri = RdfUtil.resourceUri(v);
        if (uri != null) {
            return isHashUri(uri);
        } else {
            return false;
        }
    }

    public static void replaceHashUris(
            RepositoryConnection cn, Resource r, Collection<? extends Resource> newHashes)
        throws RepositoryException
    {
        Collection<Statement> pendingRemoval = new ArrayList<Statement>();

        RepositoryResult<Statement> si = cn.getStatements(r, RdfUtil.Dc.identifier, null, false);
        while (si.hasNext()) {
            Statement s = si.next();
            
            String uri = RdfUtil.resourceUri(s.getObject());
            if (uri != null && isHashUri(uri))
                pendingRemoval.add(s);
        }
        
        for (Statement s : pendingRemoval) {
            cn.remove(s);
        }
        
        for (Resource nh : newHashes) {
            cn.add(r, RdfUtil.Dc.identifier, nh);
        }
    }
}
