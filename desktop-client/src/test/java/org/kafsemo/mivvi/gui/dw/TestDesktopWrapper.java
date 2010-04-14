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

package org.kafsemo.mivvi.gui.dw;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

/**
 * When tests are run with a Java 5 classpath and no JDIC, make sure these
 * attempts fail in a controlled manner.
 * 
 * @author Joseph Walton
 */
public class TestDesktopWrapper extends TestCase
{
    private URI sampleUri;
    private File sampleFile;
    
    public void setUp() throws URISyntaxException
    {
        sampleUri = new URI("http://localhost/");
        sampleFile = new File("sample-file.txt");
    }
    
    private static boolean isJava5()
    {
        String version = System.getProperty("java.version");
        
        return version.startsWith("1.5.");
    }
    
    public void testAttemptJava6DesktopBrowse() throws IOException
    {
        if (!isJava5())
        {
            return;
        }
        
        try 
        {
            DesktopWrapper.attemptJava6DesktopBrowse(sampleUri);
            fail();
        }
        catch (NoClassDefFoundError ncdfe)
        {
            // Pass
        }
    }
    
    public void testAttemptJava6DesktopLaunch() throws IOException
    {
        if (!isJava5())
        {
            return;
        }
        
        try
        {
            DesktopWrapper.attemptJava6DesktopOpen(sampleFile);
            fail();
        }
        catch (NoClassDefFoundError ncdfe)
        {
            // Pass
        }
    }
    
    public void testAttemptJdicDesktopBrowse() throws MalformedURLException
    {
        try
        {
            DesktopWrapper.attemptJdicBrowse(sampleUri.toURL(), null);
            fail();
        }
        catch (NoClassDefFoundError ncdfe)
        {
            // Pass
        }
        catch (UnsatisfiedLinkError ule)
        {
            // Pass
        }
    }
    
    public void testAttemptJdicDesktopLaunch()
    {
        try
        {
            DesktopWrapper.attemptJdicOpen(sampleFile, null);
            fail();
        }
        catch (NoClassDefFoundError ncdfe)
        {
            // Pass
        }
        catch (UnsatisfiedLinkError ule)
        {
            // Pass
        }
    }
}
