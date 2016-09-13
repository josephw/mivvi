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
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.tv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.kafsemo.mivvi.tv.Downloader;

import com.sun.net.httpserver.HttpServer;

/**
 * @author Joseph Walton
 */
public class TestDownloader extends TestCase
{
    private URL sampleLocalFile;
    private HttpServer server;
    private int port;

    public void setUp() throws MalformedURLException, IOException
    {
        File tf = File.createTempFile("TestDownloader", ".txt");
        tf.deleteOnExit();

        OutputStream out = new FileOutputStream(tf);
        out.write("This file is 28 bytes long.\n".getBytes("us-ascii"));
        out.close();

        sampleLocalFile = tf.toURI().toURL();
        assertNotNull(sampleLocalFile);

        server = HttpServer.create();

        server.createContext("/static-resource",
                new StaticContentHandler(sampleLocalFile));

        InetSocketAddress addr = new InetSocketAddress("localhost", 0);
        server.bind(addr, 0);

        port = server.getAddress().getPort();

        server.start();
    }

    @Override
    protected void tearDown() throws Exception
    {
        server.stop(0);
        super.tearDown();
    }

    public void testInitialDownload() throws IOException
    {
        Downloader d  = new Downloader();

        File df = File.createTempFile(getClass().getName() + "-", ".tmp");
        df.deleteOnExit();

        assertTrue(d.downloadToFile(sampleLocalFile, df));
        assertEquals(28, df.length());
    }

    private String localUrl(String path)
    {
        return "http://localhost:" + port + path;
    }

    /**
     * This test requires a real web server, for conditional GET testing.
     *
     * @throws IOException
     */
    public void testRepeatedDownload() throws IOException
    {
        Downloader d  = new Downloader();

        /* This can be anything served by a proper web server */
        URL url = new URL(localUrl("/static-resource"));

        File df = File.createTempFile(getClass().getName(), "tmp");
        df.deleteOnExit();

        assertTrue(d.downloadToFile(url, df));
        assertEquals(28, df.length());

        assertFalse(d.downloadToFile(url, df));
    }

    /**
     * Make sure a Downloader can persist and restore its state.
     *
     * @throws IOException
     */
    public void testSerialisation() throws IOException
    {
        Downloader d = new Downloader();

        URL url = sampleLocalFile;
        File df = File.createTempFile(getClass().getName(), "tmp");
        df.deleteOnExit();

        assertTrue(d.downloadToFile(url, df));
        assertTrue(d.isFresh(url.toString(), df, 1000L * 60));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        d.saveState(baos);

        d = new Downloader();
        assertFalse(d.isFresh(url.toString(), df, 1000L * 60));
        d.loadState(new ByteArrayInputStream(baos.toByteArray()));
        assertTrue(d.isFresh(url.toString(), df, 1000L * 60));
    }

    public void testRemovedLocalFile() throws IOException
    {
        Downloader d = new Downloader();

        URL url = sampleLocalFile;
        File df = File.createTempFile(getClass().getName(), "tmp");
        df.deleteOnExit();

        assertTrue(d.downloadToFile(url, df));

        df.delete();
        assertTrue(d.downloadToFile(url, df));
    }

    public void testNoLocalFileFor404() throws IOException
    {
        Downloader d = new Downloader();

        URL url = new URL(localUrl("/missing-resource"));
        File df = File.createTempFile(getClass().getName(), "tmp");
        df.deleteOnExit();

        assertTrue(df.exists());

        assertTrue(d.downloadToFile(url, df));

        assertFalse(df.exists());
    }

    public void testNoLocalFileForMissingLocalFile() throws IOException
    {
        Downloader d = new Downloader();

        URL url = new File("there-is-no-file-with-this-name").toURI().toURL();
        File df = File.createTempFile(getClass().getName(), "tmp");
        df.deleteOnExit();

        assertTrue(df.exists());

        assertTrue(d.downloadToFile(url, df));

        assertFalse(df.exists());
    }

    public void testFreshness() throws IOException
    {
        Downloader d  = new Downloader();

        URL url = sampleLocalFile;

        File df = File.createTempFile(getClass().getName() + "-", ".tmp");
        df.deleteOnExit();

        d.downloadToFile(url, df);

        assertFalse(d.isFresh(url.toString(), df, -1));
        assertTrue(d.isFresh(url.toString(), df, 1000L * 60));

        df.delete();

        assertFalse(d.isFresh(url.toString(), df, 1000L * 60));
    }

    public void testSerialiseAfterMultipleFiles() throws IOException
    {
        Downloader d  = new Downloader();

        URL url = sampleLocalFile;

        File df = File.createTempFile(getClass().getName() + "-", ".tmp");
        df.deleteOnExit();

        d.downloadToFile(url, df);

        File f = new File("test-data/tv-listings/bleb-itv3-no-subtitles.xml");
        url = f.toURI().toURL();

        df = File.createTempFile(getClass().getName() + "-", ".tmp");
        df.deleteOnExit();

        d.downloadToFile(url, df);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        d.saveState(baos);

        d = new Downloader();
        d.loadState(new ByteArrayInputStream(baos.toByteArray()));
    }
}
