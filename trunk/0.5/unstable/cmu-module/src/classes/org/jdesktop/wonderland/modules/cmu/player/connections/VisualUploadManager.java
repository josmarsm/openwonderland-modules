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
package org.jdesktop.wonderland.modules.cmu.player.connections;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.jdesktop.wonderland.modules.cmu.common.web.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes.VisualAttributesIdentifier;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.webdav.client.WebdavClientPlugin;

/**
 * Interfaces with content repository APIs to upload CMU visual data to
 * the repository.
 * @author kevin
 */
public class VisualUploadManager {

    static private final String REPO_COLLECTION_NAME = "visuals";
    static private ServerSessionManager manager = null;
    static private String username = null;

    // Should never be instantiated
    private VisualUploadManager() {
    }

    /**
     * Must be called before content can be uploaded.  Initializes
     * the content repository, and creates a collection for CMU visual data.
     * @param manager
     */
    static public void initialize(ServerSessionManager manager, String username) {
        if (!isInitialized()) {
            WebdavClientPlugin plugin = new WebdavClientPlugin();
            plugin.initialize(manager);
            VisualUploadManager.manager = manager;
            try {
                ContentCollection collection = ContentRepositoryRegistry.getInstance().getRepository(manager).getUserRoot();
                if (collection.getChild(REPO_COLLECTION_NAME) == null) {
                    collection.createChild(REPO_COLLECTION_NAME, ContentNode.Type.COLLECTION);
                }
            } catch (ContentRepositoryException ex) {
                Logger.getLogger(VisualUploadManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            VisualUploadManager.username = username;
        } else {
            Logger.getLogger(VisualUploadManager.class.getName()).log(Level.SEVERE, "Double initializabion of VisualUploadManager!");
        }
    }

    static public String getRepoRoot() {
        return "wlcontent://users/" + username + "/" + REPO_COLLECTION_NAME + "/";
    }

    /**
     * Upload the given visual to the content repository, choosing a node name
     * based on its identifier.
     * @param visual The visual to upload
     */
    static public void uploadVisual(VisualAttributes visual) {
        assert isInitialized();
        if (isInitialized()) {
            VisualAttributesIdentifier id = visual.getID();
            try {
                // Get the top-level collection of CMU resources
                ContentCollection collection = (ContentCollection) ContentRepositoryRegistry.getInstance().
                        getRepository(manager).getUserRoot().getChild(REPO_COLLECTION_NAME);

                // Upload this data if it hasn't already been uploaded.
                if (collection.getChild(id.getContentNodeName()) == null) {
                    ContentResource resource = (ContentResource) collection.createChild(id.getContentNodeName(), ContentNode.Type.RESOURCE);

                    File toUpload = File.createTempFile(id.getContentNodeName(), ".cmu");
                    FileOutputStream fos = new FileOutputStream(toUpload);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(visual);
                    oos.close();
                    fos.close();

                    resource.put(toUpload);

                    toUpload.delete();
                }
            } catch (IOException ex) {
                Logger.getLogger(VisualUploadManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ContentRepositoryException ex) {
                Logger.getLogger(VisualUploadManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Get the initialized state of the upload manager.
     * @return True if the manager is initialized, false otherwise
     */
    static public boolean isInitialized() {
        return manager != null;
    }
}