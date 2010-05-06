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

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.kafsemo.mivvi.app.Versioning;
import org.kafsemo.mivvi.desktop.platform.LaunchProfile;
import org.kafsemo.mivvi.gui.Gui;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

public class Launch
{
    public static void main(String[] args) throws InterruptedException,
            InvocationTargetException, TransformerConfigurationException,
            TransformerException, TransformerFactoryConfigurationError,
            ParserConfigurationException, IOException, URISyntaxException,
            ClassNotFoundException, InstantiationException,
            IllegalAccessException, UnsupportedLookAndFeelException,
            RepositoryException
    {
        // For Mac OS X
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Mivvi");

        LaunchProfile lp = LaunchProfile.forOsName(System.getProperty("os.name"));

        lp.initialiseLookAndFeel();
        
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        
        
        Versioning v = Versioning.from(Launch.class);
        
        Config cfg = new Config(lp.getAppPaths());
        
        AppState appState = new AppState(cfg);

        List<File> files = new MivviDataStartup().refreshLocalData(appState.getConfig(), appState.getDownloader());

        /*
         * TODO
         * Load files in order, stopping on a missing file or a parse error.
         * If no files were loaded, fall back onto the bundled data.
         */

        List<String> urls;
        
        if (files != null) {
            urls = MivviDataStartup.gatherDataUrls(files);

            if (urls.isEmpty()) {
                Gui.synchronousAlert("Unable to download current data; falling back on local copy");
                urls = null;
            }
        } else {
            urls = null;
        }

        try {
            if (!appState.loadData(urls))
                System.exit(10);
        } catch (RDFParseException rpe) {
            System.err.println(rpe);
            System.exit(10);
        }

        appState.updateMappedUris();

        MainWindow mw = new MainWindow(appState.gui, appState.seriesData.getDoap(), v);
        
        appState.setGUIRunning(true);

        mw.showAll();

        appState.waitForGUIClosure();
        
        System.err.println("GUI closed. Shutting down...");

        appState.close();
        
        mw.disposeAll();

        System.err.println("Exiting launch thread.");
        
        /* 1.4 on Linux (at least) doesn't exit cleanly, so exit to be safe */
        System.exit(0);
    }
}
