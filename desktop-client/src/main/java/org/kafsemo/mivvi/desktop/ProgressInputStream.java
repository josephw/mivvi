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

package org.kafsemo.mivvi.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

public class ProgressInputStream extends InputStream
{
    private final InputStream in;
    private final ProgressStatus ps;
    private final long length;
    private long value;

    public ProgressInputStream(InputStream in, long length)
    {
        this(in, length, new ProgressStatus());
    }
    
    public ProgressInputStream(InputStream in, long length, ProgressStatus ps)
    {
        this.in = in;
        this.ps = ps;
        this.length = length;
        this.value = 0;
        updateProgress();
    }

    public ProgressStatus getProgress()
    {
        return ps;
    }
    
    private void updateProgress()
    {
        synchronized (ps) {
            ps.indeterminate = false;
            if (length <= Integer.MAX_VALUE) {
                ps.maximum = (int)length;
                ps.value = (int)value;
            } else {
                ps.maximum = Integer.MAX_VALUE;
                ps.value = (int)(value * (float)Integer.MAX_VALUE / length);
            }
        }
    }
    
    private void checkInterrupt() throws InterruptedIOException
    {
        synchronized (ps) {
            if (ps.cancelled)
                throw new InterruptedIOException();
        }
    }
    
    private void increment(long r)
    {
        this.value += r;
        updateProgress();
    }
    
    private void increment()
    {
        increment(1);
    }

    public int read() throws IOException
    {
        checkInterrupt();
        int c = in.read();
        if (c >= 0)
            increment();
        return c;
    }
    
    public int available() throws IOException
    {
        return in.available();
    }
    
    public int read(byte[] b, int off, int len) throws IOException
    {
        checkInterrupt();
        int r = in.read(b, off, len);
        increment(r);
        return r;
    }
    
    public long skip(long n) throws IOException
    {
        checkInterrupt();
        long s = in.skip(n);
        increment(s);
        return s;
    }
}
