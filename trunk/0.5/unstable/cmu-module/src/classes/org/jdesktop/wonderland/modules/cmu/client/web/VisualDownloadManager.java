/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.cmu.client.web;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes.VisualRepoIdentifier;

/**
 * Manager to download CMU visual attributes from the content repository based
 * on their identifiers.
 * @author kevin
 */
public class VisualDownloadManager {

    // Should never be instantiated
    private VisualDownloadManager() {
    }

    /**
     * Get the visual from the repository which is defined by the given ID.
     * @param id The ID defining the visual
     * @return The visual downloaded from the repository
     */
    public static VisualAttributes downloadVisual(VisualRepoIdentifier id, String username, Cell cell) {
        VisualAttributes attr = null;
        String visualURI = "wlcontent://users/" + username + "/" + VisualRepoIdentifier.REPO_COLLECTION_NAME + "/" + id.getContentNodeName();
        try {
            URL visualURL = AssetUtils.getAssetURL(visualURI, cell);
            InputStream urlStream = visualURL.openStream();
            ObjectInputStream objectURLStream = new ObjectInputStream(urlStream);
            attr = (VisualAttributes) objectURLStream.readObject();
            objectURLStream.close();
            urlStream.close();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(VisualDownloadManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            // File not properly uploaded
            attr = null;
        } catch (EOFException ex) {
            attr = null;
        } catch (IOException ex) {
            Logger.getLogger(VisualDownloadManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return attr;
    }
}
