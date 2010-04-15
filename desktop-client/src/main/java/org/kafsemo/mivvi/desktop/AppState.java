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

package org.kafsemo.mivvi.desktop;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.kafsemo.mivvi.app.FileUtil;
import org.kafsemo.mivvi.app.LocalFiles;
import org.kafsemo.mivvi.app.SeriesData;
import org.kafsemo.mivvi.gui.Gui;
import org.kafsemo.mivvi.rdf.IdentifierMappings;
import org.kafsemo.mivvi.recognise.FilenameMatch;
import org.kafsemo.mivvi.recognise.SeriesDataException;
import org.kafsemo.mivvi.rss.RssDownloading;
import org.kafsemo.mivvi.tv.ChannelAvailability;
import org.kafsemo.mivvi.tv.Downloader;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

public class AppState
{
    /*
     * Decisions the user has made, and directories and paths to use.
     */
    final Config cfg;

    public Config getConfig()
    {
        return cfg;
    }

    final UserState userState;

    public UserState getUserState()
    {
        return userState;
    }
    
    final Decisions decisions;
    
    public Decisions getDecisions()
    {
        return decisions;
    }

    /*
     * Data and feed files that have been fetched from the web and stored locally.
     */
    final Downloader downloader;

    public Downloader getDownloader()
    {
        return downloader;
    }

    final Repository sesameRep;
    
    private final Desktop desktop;
    
    public AppState() throws IOException, URISyntaxException,
            RepositoryException, ParserConfigurationException
    {
        this.cfg = Config.getInstance();
        this.decisions = new Decisions();
        this.userState = new UserState(cfg, this.decisions);
        
        sesameRep = new SailRepository(new MemoryStore());
        sesameRep.initialize();

        localFiles = new LocalFiles();
        localFiles.initLocalFiles();
        
        this.downloader = new Downloader(cfg.getCacheDirectory());
        channelAvailability = ChannelAvailability.getInstance();

        this.rssDownloadThread = new RssDownloading(this);

        if (Desktop.isDesktopSupported()) {
            this.desktop = Desktop.getDesktop();
        } else {
            this.desktop = null;
        }
        
        gui = new GuiState(this);
    }
    
    private final RssDownloading rssDownloadThread;

    public void close() throws TransformerConfigurationException,
            TransformerException, TransformerFactoryConfigurationError,
            IOException, RepositoryException
    {
        try {
            gui.dispose();
        } catch (Exception e) {
            System.err.println("Exception shutting down GUI: " + e);
        }
        try {
            localFiles.save(new File(Const.LOCAL_RESOURCE_FILE));
        } catch (Exception e) {
            System.err.println("Exception saving local file RDF: " + e);
        }
        localFiles.close();
        seriesData.closeMviRepository();
        sesameRep.shutDown();
        userState.save(this.decisions, rssDownloadThread);
        downloader.save();
    }

    /*
     * The current Mivvi RDF database.
     */
    SeriesData seriesData;
    MetaData metaData;

    public boolean loadData() throws RepositoryException, RDFParseException,
            IOException, InterruptedException, InvocationTargetException
    {
        return loadData(null);
    }

    public boolean loadData(List<String> dataUrls) throws RepositoryException,
            IOException, RDFParseException, InterruptedException,
            InvocationTargetException
    {
        seriesData = new SeriesData();
        seriesData.initMviRepository(sesameRep);
        metaData = new MetaData(seriesData);
        
        return Gui.loadMivviData(seriesData, dataUrls);
    }

    public SeriesData getSeriesData()
    {
        return seriesData;
    }

    public MetaData getMetaData()
    {
        return metaData;
    }

    /* TV data */
    final ChannelAvailability channelAvailability;

    public ChannelAvailability getChannelAvailability()
    {
        return channelAvailability;
    }

    /*
     * Recognised local files.
     */
    final LocalFiles localFiles;

    public LocalFiles getLocalFiles()
    {
        return localFiles;
    }

    /*
     * All windows.
     */
    GuiState gui;
    
    
    /* Public methods */
    public boolean process(File f) throws RepositoryException, SeriesDataException
    {
        URI fileUri = FileUtil.createFileURI(f);

        if (localFiles.hasEpisodeForFile(fileUri))
            return false;

        FilenameMatch<Resource> fm = seriesData.process(f);
        
        if (fm != null) {
            localFiles.addFileEpisode(fileUri, fm.episode);
            
            return true;
        } else {
            return false;
        }
    }

    private boolean guiRunning = false;
    
    synchronized void setGUIRunning(boolean state)
    {
        this.guiRunning = state;
        notifyAll();
    }

    public synchronized void waitForGUIClosure() throws InterruptedException
    {
        while (this.guiRunning)
            wait();
    }

    // Must be called from the EDT
    public void updatedSubscriptions()
    {
        gui.getUpcomingBroadcastFrame().updatedSubscriptions();
        gui.getAvailableDownloadFrame().updatedSubscriptions();
    }

    public RssDownloading getRssDownloading()
    {
        return rssDownloadThread;
    }

    // Must be called from the EDT
    public void setShowOnlySubscribed(boolean b)
    {
//        decisions.setShowOnlySubscribed(b);
        userState.setShowOnlySubscribed(b);
        updatedSubscriptions();
    }

    private IdentifierMappings identifierMappings;

    /**
     * Extract any identifier mappings from the loaded series data and use it
     * to update the user state.
     * @throws RepositoryException 
     *
     */
    public void updateMappedUris() throws RepositoryException
    {
        if (identifierMappings == null) {
            identifierMappings = seriesData.createIdentifierMappings();
        }

        /* Update URIs used in the user state */
        userState.getSubscription().update(identifierMappings);
        userState.getWatched().update(identifierMappings);

        /* Update URIs used for local resources */
        localFiles.update(identifierMappings);
    }

    public IdentifierMappings getIdentifierMappings()
    {
        return identifierMappings;
    }

    public Desktop getDesktop()
    {
        return this.desktop;
    }
}
