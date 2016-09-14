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

package org.kafsemo.mivvi.desktop;

import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Enquire and record decisions made by the user as to whether or not to allow
 * certain features.
 */
public final class Decisions
{
    private final Preferences p;

    Decisions()
    {
        p = getPreferencesNode();
    }

    private boolean permitDownloadListings;

    private Preferences getPreferencesNode()
    {
        return Preferences.userNodeForPackage(getClass()).node("decisions");
    }

    static final String DOWNLOAD_TV_LISTINGS = "download tv listings";
    static final String DOWNLOAD_MIVVI_DATA = "download mivvi data";
    
    public boolean ensureDownloadListingsPermission(JFrame jf)
    {
        if (permitDownloadListings)
            return true;
        
        permitDownloadListings = p.getBoolean(DOWNLOAD_TV_LISTINGS, false);
        if (permitDownloadListings)
            return true;
        
        int r = JOptionPane.showConfirmDialog(jf, "Mivvi fetches UK television listings from RadioTimes.com.\nOkay to go ahead and do this?", "Permission to Download", JOptionPane.OK_CANCEL_OPTION);
        
        permitDownloadListings = (r == JOptionPane.OK_OPTION);

        p.putBoolean(DOWNLOAD_TV_LISTINGS, permitDownloadListings);

        return permitDownloadListings;
    }

    private Boolean downloadDataPermission;

    public boolean ensureDownloadDataPermission()
    {
        /* Check for cached value */
        if (downloadDataPermission != null)
            return downloadDataPermission.booleanValue();

        /* Check for preference */
        downloadDataPermission = getDownloadDataPermission();

        if (downloadDataPermission != null)
            return downloadDataPermission.booleanValue();

        /* Ask the user */
        JCheckBox jcb;

        Object[] oa = {
                "Mivvi can stay up-to-date by fetching program data from its website.\n"
                + "Do you want to check for current data?",
                jcb = new JCheckBox("Don't ask again")
        };
        int r = JOptionPane.showConfirmDialog(null, oa, "Mivvi Permission", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

        boolean ok = (r == JOptionPane.OK_OPTION);

        if (jcb.isSelected()) {
            setDownloadDataPermission(ok);
            downloadDataPermission = Boolean.valueOf(ok);
        }
        
        return ok;
    }

    /**
     * Definitively set the user's decision about fetching data on startup.
     * 
     * @param b
     */
    public void setDownloadDataPermission(boolean ok)
    {
        p.put(DOWNLOAD_MIVVI_DATA, ok ? "true" : "false");
    }

    public Boolean getDownloadDataPermission()
    {
        String s = p.get(DOWNLOAD_MIVVI_DATA, null);
        if ("true".equals(s)) {
            return Boolean.TRUE;
        } else if ("false".equals(s)) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }

    private static String SHOW_ONLY_SUBSCRIBED = "showonlysubscribed";

    public void setShowOnlySubscribed(boolean b)
    {
        p.putBoolean(SHOW_ONLY_SUBSCRIBED, b);
    }
    
    public boolean getShowOnlySubscribed()
    {
        return p.getBoolean(SHOW_ONLY_SUBSCRIBED, false);
    }

    private static String DEFAULT_MEDIA_DIRECTORY = "defaultmediadirectory";
    
    public File getDefaultMediaDirectory()
    {
        String s = p.get(DEFAULT_MEDIA_DIRECTORY, null);
        if (s == null)
            return null;
        else
            return new File(s);
    }

    public void setDefaultMediaDirectory(File f)
    {
        p.put(DEFAULT_MEDIA_DIRECTORY, f.getAbsolutePath());
    }
}
