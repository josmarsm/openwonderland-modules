/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jdesktop.wonderland.modules.cmu.common.web.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.cmu.common.PersistentSceneData;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes.VisualAttributesIdentifier;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.webdav.client.WebdavClientPlugin;

/**
 * Interfaces with content repository APIs to upload CMU visual data and
 * upload/download persistent scene data to the repository.
 * @author kevin
 */
public class ContentManager {

    static private final String VISUAL_REPO_COLLECTION_NAME = "visuals";
    static private final String PERSISTENT_DATA_REPO_COLLECTION_NAME = "data";
    static private ServerSessionManager manager = null;
    static private String username = null;

    // Should never be instantiated
    private ContentManager() {
    }

    /**
     * Must be called before content can be uploaded.  Initializes
     * the content repository, and creates a collection for CMU visual data.
     * @param manager The manager for this session
     * @param username The username with which the program manager is connected
     */
    static public void initialize(ServerSessionManager manager, String username) {
        if (!isInitialized()) {
            WebdavClientPlugin plugin = new WebdavClientPlugin();
            plugin.initialize(manager);
            ContentManager.manager = manager;
            try {
                ContentCollection collection = ContentRepositoryRegistry.getInstance().getRepository(manager).getUserRoot();

                // Create visual repository if necessary
                if (collection.getChild(VISUAL_REPO_COLLECTION_NAME) == null) {
                    collection.createChild(VISUAL_REPO_COLLECTION_NAME, ContentNode.Type.COLLECTION);
                }

                // Create persistent data repository if necessary
                if (collection.getChild(PERSISTENT_DATA_REPO_COLLECTION_NAME) == null) {
                    collection.createChild(PERSISTENT_DATA_REPO_COLLECTION_NAME, ContentNode.Type.COLLECTION);
                }

            } catch (ContentRepositoryException ex) {
                Logger.getLogger(ContentManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            ContentManager.username = username;
        } else {
            Logger.getLogger(ContentManager.class.getName()).log(Level.SEVERE, "Double initialization of content manager!");
        }
    }

    /**
     * Get the root directory to which scene visuals will be uploaded.
     * @return Name of the directory containing scene visuals
     */
    static public String getVisualRepoRoot() {
        return "wlcontent://users/" + username + "/" + VISUAL_REPO_COLLECTION_NAME + "/";
    }

    /**
     * Get the root directory to which persistent scene data will be uploaded.
     * @return Name of the directory containing scene data
     */
    static public String getPersistentDataRepoRoot() {
        return "wlcontent://users/" + username + "/" + PERSISTENT_DATA_REPO_COLLECTION_NAME + "/";
    }

    /**
     * Serialize the given data and write it to a file in the content repository.
     * @param data Data to serialize
     * @param resource The resource in the repository to write to
     * @param asXML If true, write to XML; otherwise, just serialize to binary
     */
    static protected void uploadData(Serializable data, ContentResource resource, boolean asXML) {
        try {
            File toUpload = File.createTempFile(resource.getName(), null);
            FileOutputStream fos = new FileOutputStream(toUpload);
            if (asXML) {
                XMLEncoder enc = new XMLEncoder(fos);
                enc.writeObject(data);
                enc.close();
            } else {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(data);
                oos.close();
            }
            fos.close();

            resource.put(toUpload);

            toUpload.delete();
        } catch (ContentRepositoryException ex) {
            Logger.getLogger(ContentManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ContentManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static public void uploadSceneData(PersistentSceneData data, String filename) {
        if (isInitialized()) {
            try {
                ContentCollection collection = (ContentCollection) ContentRepositoryRegistry.getInstance().
                        getRepository(manager).getUserRoot().getChild(PERSISTENT_DATA_REPO_COLLECTION_NAME);
                collection.removeChild(filename);
                ContentResource resource = (ContentResource) collection.createChild(filename, ContentNode.Type.RESOURCE);

                uploadData(data, resource, true);

            } catch (ContentRepositoryException ex) {
                Logger.getLogger(ContentManager.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            Logger.getLogger(ContentManager.class.getName()).severe("Uninitialized use of content manager");
        }
    }

    static public PersistentSceneData downloadSceneData(String filename) {
        if (isInitialized()) {
            {
                try {
                    ContentCollection collection = (ContentCollection) ContentRepositoryRegistry.getInstance().
                            getRepository(manager).getUserRoot().getChild(PERSISTENT_DATA_REPO_COLLECTION_NAME);
                    ContentResource resource = (ContentResource) collection.getChild(filename);
                    if (resource != null) {
                        InputStream stream = resource.getInputStream();
                        XMLDecoder dec = new XMLDecoder(stream);
                        return (PersistentSceneData) dec.readObject();
                    }
                } catch (ContentRepositoryException ex) {
                    Logger.getLogger(ContentManager.class.getName()).log(Level.SEVERE, null, ex);
                }

                return null;
            }

        } else {
            Logger.getLogger(ContentManager.class.getName()).severe("Uninitialized use of content manager");
            return null;
        }
    }

    /**
     * Upload the given visual to the content repository, choosing a node name
     * based on its identifier.
     * @param visual The visual to upload
     */
    static public void uploadVisual(VisualAttributes visual) {
        if (isInitialized()) {
            VisualAttributesIdentifier id = visual.getID();
            try {
                // Get the top-level collection of CMU resources
                ContentCollection collection = (ContentCollection) ContentRepositoryRegistry.getInstance().
                        getRepository(manager).getUserRoot().getChild(VISUAL_REPO_COLLECTION_NAME);

                // Upload this data if it hasn't already been uploaded.
                if (collection.getChild(id.getContentNodeName()) == null) {
                    ContentResource resource = (ContentResource) collection.createChild(id.getContentNodeName(),
                            ContentNode.Type.RESOURCE);

                    uploadData(visual, resource, false);
                }
            } catch (ContentRepositoryException ex) {
                Logger.getLogger(ContentManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Logger.getLogger(ContentManager.class.getName()).severe("Uninitialized use of CMU visual upload manager");
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
