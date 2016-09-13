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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.kafsemo.mivvi.app.TokenSetFile;
import org.kafsemo.mivvi.app.UriSetFile;
import org.kafsemo.mivvi.gui.EpisodeItem;
import org.kafsemo.mivvi.rss.Feed;
import org.kafsemo.mivvi.rss.RssDownloading;

/**
 * What the user is interested in, and has seen.
 */
public class UserState
{
//    private final Config cfg;
    private UriSetFile watched;
    private UriSetFile subscriptions;
    private TokenSetFile channels;

    private List<Feed> feeds;

    public UserState(Config cfg, Decisions decisions) throws IOException
    {
//        this.cfg = cfg;
        watched = cfg.getUriSetFile("watched");
        watched.load();
        subscriptions = cfg.getUriSetFile("subscription");
        subscriptions.load();
        channels = cfg.getTokenSetFile("channels");
        channels.load();
        this.showOnlySubscribed = decisions.getShowOnlySubscribed();
    }

    public UriSetFile getWatched()
    {
        return watched;
    }

    public UriSetFile getSubscription()
    {
        return subscriptions;
    }

    public TokenSetFile getChannels()
    {
        return channels;
    }

    public void save(Decisions decisions, RssDownloading rssDownloadThread) throws IOException
    {
        if (subscriptions != null)
            subscriptions.save();

        if (watched != null)
            watched.save();

        if (channels != null)
            channels.save();

        decisions.setShowOnlySubscribed(getShowOnlySubscribed());

        if (feeds != null) {
            try {
                rssDownloadThread.saveFeeds(feeds);
            } catch (TransformerException te) {
                throw (IOException)new IOException("Unable to save feed configuration").initCause(te);
            }
        }
    }

    boolean showOnlySubscribed;

    public synchronized void setShowOnlySubscribed(boolean b)
    {
        this.showOnlySubscribed = b;
    }

    public synchronized boolean getShowOnlySubscribed()
    {
        return showOnlySubscribed;
    }

    /**
     * Should information about a resource be shown to the user? As a side-effect,
     * this method will set the <code>highlighted</code> property.
     */
    public boolean isInteresting(EpisodeItem ei)
    {
        boolean subscribed = (subscriptions.contains(ei.getEpisode()) || subscriptions.contains(ei.getDetails().season) || subscriptions.contains(ei.getDetails().series));

        if (showOnlySubscribed) {
            ei.setHighlighted(false);
            return subscribed;
        } else {
            ei.setHighlighted(subscribed);
            return true;
        }
    }

    /**
     * All access to this must be synchronized.
     */
    public synchronized List<Feed> getFeeds()
    {
        if (feeds == null)
            feeds = new ArrayList<Feed>();
        return feeds;
    }
}
