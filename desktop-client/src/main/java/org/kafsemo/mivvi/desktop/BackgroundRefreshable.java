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

package org.kafsemo.mivvi.desktop;


public abstract class BackgroundRefreshable implements Runnable
{
    private final long maximumTime;
    
    public BackgroundRefreshable(long millis)
    {
        this.maximumTime = millis;
    }

    private boolean running = true;
    private boolean needsRefresh = false;

    private long nextRefresh = Long.MIN_VALUE;

    public void run()
    {
        boolean r;
        
        do {
            synchronized (this) {
                long now;
                while (running && !needsRefresh && nextRefresh > (now = System.currentTimeMillis())) {
                    try {
                        wait(nextRefresh - now);
                    } catch (InterruptedException ie) {
                        
                    }
                }
                
                r = running;
                needsRefresh = false;
            }
            
            if (r) {
//                System.err.println("Refreshing " + this + "...");
                refresh();
                nextRefresh = System.currentTimeMillis() + maximumTime;
//                System.err.println("Next refresh " + this + ": " + new Date(nextRefresh));
            }
        } while (r);
        
        System.err.println("Finished: " + this);
    }

    public synchronized void scheduleRefresh()
    {
        needsRefresh = true;
        notify();
    }
    
    public synchronized void end()
    {
        running = false;
        notify();
    }
    
    public synchronized boolean running()
    {
        return running;
    }

    public abstract void refresh();
}
