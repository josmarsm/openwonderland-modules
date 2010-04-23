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
package org.jdesktop.wonderland.modules.evolvermulti.client.evolver;

import imi.character.CharacterParams;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.FileUtils;
import org.jdesktop.wonderland.common.ThreadManager;
import org.jdesktop.wonderland.modules.avatarbase.client.AvatarSessionLoader;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter.WlAvatarCharacterBuilder;
import org.jdesktop.wonderland.modules.avatarbase.client.registry.AvatarRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshConfigElement;
import org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config.MultimeshModelConfigElement;

/**
 * Manager for Evolver avatars. This class manages all of the configured Evolver
 * avatars configured locally and synchronizes with all servers connected.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class MultimeshEvolverAvatarConfigManager {

    private static final Logger LOGGER = Logger.getLogger(
            MultimeshEvolverAvatarConfigManager.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/evolvermulti/client/evolver/"
            + "resources/Bundle");

    // A map of current server sessions and that the threads that manage the
    // communcations with each.
    private final Map<ServerSessionManager, ServerSyncThread> avatarConfigServers = new HashMap();

    // A map of local avatar names and the poiners to their configurations that
    // are stored locally on the user's repository.
    private final Map<String, MultimeshEvolverAvatar> localAvatars = new HashMap();

    // Returns the base directory (content collection) that stores all avatar
    // related configuration information on the user's local machine.
    private ContentCollection evolverCollection = null;

    /**
     * Default constructor
     */
    private MultimeshEvolverAvatarConfigManager() {
        // Fetch the Evolver base content collection for configuration, which is
        // the evolver/ directory beneath the avatar base.
        AvatarRegistry registry = AvatarRegistry.getAvatarRegistry();
        ContentCollection bc = registry.getAvatarCollection();
        try {
            evolverCollection = (ContentCollection) bc.getChild("multimesh-evolver");
            if (evolverCollection == null) {
                evolverCollection = (ContentCollection) bc.createChild("multimesh-evolver", Type.COLLECTION);
            }
            LOGGER.info("Using local Evolver avatar collection " +
                    evolverCollection.getPath());
        } catch (ContentRepositoryException excp) {
            LOGGER.log(Level.WARNING, "Unable to fetch evolver/ collection", excp);
            return;
        }

        // Initialize the set of local avatars. We register them with the system
        // but a SYNC should be called before these avatars can be relied upon
        // so that we know we have the most up-to-date state. The SYNC will
        // correct any avatars that we registered with the system here.
        try {
            List<ContentNode> avatarList = evolverCollection.getChildren();
            for (ContentNode node : avatarList) {
                if (node instanceof ContentResource) {
                    MultimeshEvolverAvatar avatar = new MultimeshEvolverAvatar((ContentResource) node);
                    localAvatars.put(avatar.getName(), avatar);
                    registry.registerAvatar(avatar, false);
                }
            }
        } catch (ContentRepositoryException excp) {
            LOGGER.log(Level.WARNING, "Unable to fetch local avatars from " +
                    "collection " + evolverCollection.getPath(), excp);
            return;
        }
    }

    /**
     * Singleton to hold instance of AvatarConfigManager. This holder class is
     * loader on the first execution of AvatarConfigManager.getAvatarConfigManager().
     */
    private static class EvolverAvatarConfigHolder {
        private final static MultimeshEvolverAvatarConfigManager manager = new MultimeshEvolverAvatarConfigManager();
    }

    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final MultimeshEvolverAvatarConfigManager getEvolverAvatarConfigManager() {
        return EvolverAvatarConfigHolder.manager;
    }

    /**
     * Returns the URL representing the configuration file for the given avatar
     * on the given server.
     *
     * @param session The current server session
     * @param avatar The avatar
     */
    public URL getAvatarURL(ServerSessionManager session, MultimeshEvolverAvatar avatar)
            throws InterruptedException {

        // First fetch the thread for the given session and then ask it for
        // the URL.
        ServerSyncThread t = null;
        synchronized (avatarConfigServers) {
            t = avatarConfigServers.get(session);
        }
        return t.getAvatarServerURL(avatar);
    }

    /**
     * Returns true if the given server is managed and at least the initial
     * synchronization has been performed.
     *
     * @param session The server session to check
     * @return True if the server session is ready
     */
    public boolean isServerManaged(ServerSessionManager session) {
        synchronized (avatarConfigServers) {
            return avatarConfigServers.containsKey(session);
        }
    }

    /**
     * Adds a new server session for this manager to track and performs an
     * initial synchronization with the avatar configurations found on that
     * server. This method blocks until the initial synchronization is
     * complete, or has been interrupted.
     *
     * @param session The new session to add
     * @throw InterruptedException If the initialization has been interrupted
     */
    public void addServerAndSync(ServerSessionManager session) throws InterruptedException {
        // We do not wish for multiple calls to this method to happen at once
        // for the same server, but we do not want to synchronize this whole
        // method across 'avatarConfigServers' because that would block activity
        // on other servers. So we just synchronize on 'this' which means only
        // a single addServerAndSync() call can be active at once.
        synchronized (this) {
            LOGGER.info("Adding server " + session.getServerURL());
            
            // First check to see if the session already exists in the map. If
            // so then just return
            synchronized (avatarConfigServers) {
                if (avatarConfigServers.containsKey(session) == true) {
                    LOGGER.info("Server " + session.getServerURL() +
                            " is already present in the manager.");
                    return;
                }
            }

            // Go ahead and synchronize the avatar's local configuration with
            // the server's. Wait for this to complete so we know we are in
            // a 'ready' state.
            ServerSyncThread t = null;
            try {
                LOGGER.info("Starting sychronization with server " +
                        session.getServerURL());

                t = new ServerSyncThread(session);
                t.scheduleSync(true);

                LOGGER.info("Sychronization with server " +
                        session.getServerURL() + " is done.");
            } catch (ContentRepositoryException excp) {
                LOGGER.log(Level.WARNING, "Unable to create sync thread for " +
                        "server " + session.getServerURL(), excp);
                return;
            }

            // Finally, add this server to the list of servers indicating that
            // it is ready.
            synchronized (avatarConfigServers) {
                avatarConfigServers.put(session, t);
                LOGGER.info("Added server " + session.getServerURL() +
                        " to map of managed sessions.");
            }
        }
    }

    /**
     * Removes the session from being managed. If it is not being managed, this
     * method does nothing.
     *
     * @param session The session to remove.
     */
    public void removeServer(ServerSessionManager session) {
        // Remove the server session. If we find a thread, then stop it and
        // remove it from the map.
        synchronized (avatarConfigServers) {
            ServerSyncThread t = avatarConfigServers.remove(session);
            if (t != null) {
                t.setConnected(false);
            }
        }
    }

    /**
     * Given the avatar, removes the avatar and from all of the server's we
     * are currently connected to.
     *
     * @param avatar The avatar to remove
     */
    public void deleteAvatar(MultimeshEvolverAvatar avatar) {
        // First remove the avatar from the local list, synchronized around the
        // local this so other threads don't update this list at the same time.
        // Also, iterator through all of the servers we know about and tell
        // them to remove the avatar too.
        synchronized (localAvatars) {
            localAvatars.remove(avatar.getName());

            // Remove the avatar from the system
            AvatarRegistry registry = AvatarRegistry.getAvatarRegistry();
            registry.unregisterAvatar(avatar);

            try {
                // Remove from the user's local repository.
                String fileName = avatar.getResource().getName();
                evolverCollection.removeChild(fileName);

                // Schedule asynchronous jobs to remove the avatar configuration
                // file from all of the server's we are connected to.
                // XXX This is not quite correct as it will not remove older
                // versions of a file
                synchronized (avatarConfigServers) {
                    for (ServerSyncThread c : avatarConfigServers.values()) {
                        try {
                            c.scheduleDelete(avatar, false);
                        } catch (InterruptedException excp) {
                            LOGGER.log(Level.WARNING, "Attempt to delete the" +
                                    " avatar " + avatar.getName() + " from " +
                                    "the server " + c.toString() + " was " +
                                    "interrupted.", excp);
                        }
                    }
                }
            } catch (ContentRepositoryException excp) {
                LOGGER.log(Level.WARNING, "Unable to remove avatar", excp);
            }
        }
    }

    /**
     * Creates, uploads, registers and returns a new avatar given the zip file
     * that contains the artwork.
     *
     * @param zipFile The zip file containing the avatar artwork
     * @param avatarInfo The essential avatar information (e.g. name, gender)
     * @return A new EvolverAvatar object
     */
    public MultimeshEvolverAvatar createAvatar(ZipFile zipFile, MultimeshEvolverAvatarInfo avatarInfo) {
        // Next find out the name of the Evolver avatar by looking for the
        // .dae file within the zip archive. If there is no name, then flag
        // an error
        JFrame frame = JmeClientMain.getFrame().getFrame();
        String avatarName = avatarInfo.getAvatarName();

        LOGGER.info("Creating avatar with name " + avatarName);

        // Otherwise, check to see if the name already exists in the user's
        // content repository. If so, then ask whether the user wishes to
        // overwrite the existing avatar.
        MultimeshEvolverAvatar existingAvatar = null;
        if ((existingAvatar = isAvatarExists(avatarName)) != null) {
            String message = BUNDLE.getString("Replace_Avatar_Message");
            message = MessageFormat.format(message, avatarName);
            int result = JOptionPane.showConfirmDialog(frame, message,
                    BUNDLE.getString("Replace_Avatar_Title"),
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) {
                return null;
            }
        }
        
        // Delete the existing avatar locally if there is one. We need to do
        // this before touching the 'avatar' object because they may be one
        // in the same.
        if (existingAvatar != null) {
            ContentResource resource = existingAvatar.getResource();
            ContentCollection parent = resource.getParent();
            String resourceName = resource.getName();
            try {
                LOGGER.info("Deleting exsiting avatar locally with " +
                        "resource path " + resource.getPath());
                parent.removeChild(resourceName);
            } catch (ContentRepositoryException excp) {
                LOGGER.log(Level.WARNING, "Unable to remove local avatar " +
                        resource.getPath(), excp);
            }

            // Also remove the directory containing the assets. It has the
            // same name minus the '.xml' extension.
            if (resourceName.endsWith(".xml") == true) {
                int length = resourceName.length();
                String dirName = resourceName.substring(0, length - 4);

                LOGGER.info("Deleting existing avatar directory locally" +
                        " with name " + dirName);
                try {
                    parent.removeChild(dirName);
                } catch (ContentRepositoryException excp) {
                    LOGGER.log(Level.WARNING, "Unable to remove local avatar " +
                        "directory with path " + resource.getPath(), excp);
                }
            }
            else {
                LOGGER.info("Did not find evolver avatar directory locally" +
                        " with path " + resource.getPath());
            }
            
        }

        // For each server we are connected to, delete the old file from the
        // server
        synchronized (avatarConfigServers) {
            LOGGER.info("Attempting to delete avatar to server, number=" +
                avatarConfigServers.size());

            for (ServerSyncThread t : avatarConfigServers.values()) {
                // If there is an old version, we ask the server to delete the
                // file. We wait for its completion. We need to do this first
                // to get rid of the old avatar before we upload the new one.
                if (existingAvatar != null) {
                    try {
                        LOGGER.info("Schedule delete of existing avatar " +
                                "named " + avatarName + " from server.");
                        t.scheduleDelete(existingAvatar, true);
                    }
                    catch (InterruptedException excp) {
                        LOGGER.log(Level.WARNING, "Attempt to delete the" +
                                " avatar " + avatarName + " from " +
                                "the server " + t.toString() + " was " +
                                "interrupted.", excp);
                    }
                }
            }
        }

        LOGGER.info("Uploading avatar name " + avatarName + " to user's" +
                " local repository.");


        // Create a new avatar object with the name and initial version number.
        // We point it to the resource of file file.
        MultimeshEvolverAvatar newAvatar = new MultimeshEvolverAvatar(avatarName, 1);

        // If we already have an avatar, check it's version number, increment
        // it and assign the new version number to this file.
        if (existingAvatar != null) {
            LOGGER.info("Already have an avatar named " + avatarName +
                    " with version " + existingAvatar.getVersion());

            int version = existingAvatar.getVersion();
            newAvatar.setVersion(version);
            newAvatar.incrementVersion();
        }

        // Go ahead and upload the contents of the zip file to the user's
        // local repository.
        String dirName = newAvatar.getName() + "_" + newAvatar.getVersion();
        try {
            uploadAvatar(zipFile, dirName);
        } catch (java.io.IOException excp) {
            JOptionPane.showMessageDialog(frame,
                    BUNDLE.getString("Avatar_Upload_Failed"),
                    BUNDLE.getString("Upload_Failed"),
                    JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.WARNING, "Failed to upload avatar to local user" +
                    " repository named " + avatarName, excp);
            return null;
        }

        // We need to fetch the base URL of the local content repo (without the
        // user name. This will be the base URL for all art in the avatar. Assume
        // this is on the local disk
        String userName = getUserName();
        String localRepoBasePath = getLocalRepoBasePath() + "/";
        String localRepoBaseURL = "file:///" + localRepoBasePath;

        LOGGER.warning("Using local repo base URL of " + localRepoBaseURL);

        // Create new character parameters and apply the configuration elements
        // given.
        CharacterParams params = new CharacterParams();
        for (MultimeshConfigElement ce : avatarInfo.getConfigElements()) {
            if (ce instanceof MultimeshModelConfigElement) {
                String relativePath = userName + "/avatars/multimesh-evolver/" +
                        dirName + "/";
                ((MultimeshModelConfigElement)ce).setRelativePath(relativePath);
            }
            ce.apply(params);
            LOGGER.warning("ATTR: " + ce.toString());
        }
        params.setBaseURL(localRepoBaseURL);

        // Next, generate a new character, this is necessary to write out its
        // configuration
        WorldManager wm = ClientContextJME.getWorldManager();
        WlAvatarCharacter avatar =
                new WlAvatarCharacterBuilder(params, wm).addEntity(false).build();

        // Create a file to hold the avatar configuration locally if it does
        // not yet exist.
        ContentResource file = null;
        String fileName = newAvatar.getFilename();
        try {
            file = (ContentResource) evolverCollection.createChild(fileName, Type.RESOURCE);
            if (file == null) {
                file = (ContentResource) evolverCollection.createChild(fileName, Type.RESOURCE);
            }

            LOGGER.info("Writing avatar to resource " + file.getPath());

            // Write out the avatar configuration to a byte avatar.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            avatar.saveConfiguration(out);
            out.close();

            // Then write out the XML to the local repository. Update the avatar
            // to point to this local avatar resource
            file.put(out.toString().getBytes());
        } catch (java.lang.Exception excp) {
            LOGGER.log(Level.WARNING, "Unable to write avatar configuration" +
                    " to resource " + fileName, excp);
            return null;
        }

        // Tell the new avatar of the its resource configuration file
        newAvatar.setResource(file);

        // Update the map of local avatars with the new avatar. We always do
        // this no matter whether the avatar is new or whether we are updating
        // an existing avatar.
        synchronized (localAvatars) {
            LOGGER.info("Put avatar named " + avatarName + " in list of avatars");
            localAvatars.put(avatarName, newAvatar);
        }
        
        // For each server we are connected to, upload the new file.
        synchronized (avatarConfigServers) {
            LOGGER.info("Attempting to upload avatar to server, number=" +
                avatarConfigServers.size());

            for (ServerSyncThread t : avatarConfigServers.values()) {
                // We ask the server to upload the file. We need to wait for
                // it to complete before we tell other clients to use it.
                try {
                    LOGGER.info("Schedule upload of avatar named " + avatarName +
                            " to server " + t.toString());
                    t.scheduleUpload(newAvatar, true);
                } catch (InterruptedException excp) {
                    LOGGER.log(Level.WARNING, "Attempt to upload the avatar " +
                            avatar.getName() + " to the server " + t.toString() +
                            " was interrupted.", excp);
                }
            }
        }

        // If the avatar is new, tell the system.
        if (existingAvatar == null) {
            LOGGER.info("Registering avater named " + avatarName + " in the system");
            AvatarRegistry registry = AvatarRegistry.getAvatarRegistry();
            registry.registerAvatar(newAvatar, false);
        }
        return newAvatar;
    }

    /**
     * Returns the base URL for the user's local repository, without the
     * user name.
     */
    private String getLocalRepoBasePath() {
        // Really should get this from the Content Repo stuff
        return ClientContext.getUserDirectory("localRepo").getAbsolutePath();
    }

    /**
     * Returns the user's name for the current primary session.
     */
    private String getUserName() {
        ServerSessionManager manager = LoginManager.getPrimary();
        return manager.getUsername();
    }

    /**
     * Returns true if the given avatar name already exists within the user's
     * content repository
     */
    private MultimeshEvolverAvatar isAvatarExists(String avatarName) {
        synchronized (localAvatars) {
            return localAvatars.get(avatarName);
        }
    }

    /**
     * Uploads the avatar to the user's content repository given the avatar
     * name.
     */
    private void uploadAvatar(ZipFile zipFile, String avatarName) throws IOException {

        // Create the directory to hold the contents of the model. We place it
        // in a directory named after the avatar. If the directory already
        // exists, then just use it.
        ContentCollection avatarRoot = null;
        try {
            ContentNode node = evolverCollection.getChild(avatarName);
            if (node == null) {
                node = evolverCollection.createChild(avatarName, Type.COLLECTION);
            }
            avatarRoot = (ContentCollection)node;
        } catch (ContentRepositoryException excp) {
            LOGGER.log(Level.WARNING, "Unable to create content directory for" +
                    " avatar " + avatarName, excp);
            throw new IOException("Unable to create content directory for " +
                    "avatar " + avatarName);
        }

        // Loop through each file in the zip file and upload to the avatar root
        // directory in the content repository. If the file is the avatar model
        // (.dae extension), then keep the resource and return it.
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements() == true) {
                ZipEntry zipEntry = entries.nextElement();
                String zipEntryName = zipEntry.getName();
                write(zipFile, zipEntry, avatarRoot, zipEntryName);
            }
        } catch (ZipException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    /**
     * Utility method that saves the contents of an entry in a zip file to the
     * content repository. Overwrites any existing file
     */
    private void write(ZipFile zipFile, ZipEntry zipEntry,
            ContentCollection root, String fileName) throws IOException {

        // Fetch the file in which we should put this new resource beneath
        // the root creating subdirectories as necessary.
        ContentResource contentFile;
        try {
            contentFile = mkfile(root, fileName);
        } catch (ContentRepositoryException excp) {
            LOGGER.log(Level.WARNING, "Unable to create file for " +
                    fileName + " in the repository", excp);
            throw new IOException("Unable to create file for " + fileName +
                    " in the repository");
        }

        // Fetch the input stream for the zip entry and write to the content
        // resource
        InputStream is = zipFile.getInputStream(zipEntry);
        File tmpFile = File.createTempFile("uploader", ".file");
        tmpFile.deleteOnExit();
        FileUtils.copyFile(is, new FileOutputStream(tmpFile));
        try {
            contentFile.put(tmpFile);
        } catch (ContentRepositoryException excp) {
            LOGGER.log(Level.WARNING, "Unable to write content from " + fileName +
                    " to resource", excp);
            throw new IOException("Unable to write content from " + fileName +
                    " to resource");
        }
    }

    /**
     * Given the content collection root, returns the node representing the
     * given file beneath the root, creating all subdirectories as
     * necessary. This assumes the final element in the given path is the file
     * name.
     */
    private ContentResource mkfile(ContentCollection root, String path)
            throws ContentRepositoryException {

        // First parse the path into a collection of directory names. Loop
        // through each and attempt to fetch the subdirectory. If it does not
        // exist then create it.
        ContentCollection subdir = root;
        String paths[] = path.split("/");
        for (int i = 0; i < paths.length - 1; i++) {
            ContentCollection newdir = (ContentCollection)subdir.getChild(paths[i]);
            if (newdir == null) {
                newdir = (ContentCollection)subdir.createChild(paths[i], Type.COLLECTION);
            }
            subdir = newdir;
        }

        // Create the file resource and return it
        String fileName = paths[paths.length - 1];
        ContentResource resource = (ContentResource)subdir.getChild(fileName);
        if (resource == null) {
            resource = (ContentResource)subdir.createChild(fileName, Type.RESOURCE);
        }
        return resource;
    }

    /**
     * The ServerSyncThread maintains synchronization of avatar information with
     * a particular server session. It runs its own thread to serialize updates
     * with the server (sync, add, remove, etc).
     */
    private class ServerSyncThread extends Thread {

        // The server we are currently collected to
        private ServerSessionManager manager = null;

        // A queue of jobs to execute on the thread.
        private LinkedBlockingQueue<Job> jobQueue = new LinkedBlockingQueue();

        // The collection where all IMI avatar information is kept on the server
        private ContentCollection serverCollection = null;

        // A map of avatar names to pointers to their configuration files on the
        // server
        private Map<String, EvolverServerAvatar> serverAvatars = new HashMap();
        
        // True if we are connected to the server, false if we have disconnected
        private boolean isConnected = true;

        public ServerSyncThread(final ServerSessionManager manager)
                throws ContentRepositoryException {

            super(ThreadManager.getThreadGroup(), "AvatarServerSyncThread");
            this.manager = manager;

            // Fetch the base directory in which all IMI avatar configuration info
            // is found on the server.
            serverCollection = getBaseServerCollection(manager);

            // Finally, start the thread off
            this.start();
        }

        /**
         * Sets whether the session is connect or not. If the session has been
         * disconnected, then this should be set to false. Once the thread has
         * been put into the not connected state, the thread is effectively dead.
         *
         * @param isConnected True if the session is connected, false if not.
         */
        public void setConnected(boolean isConnected) {
            this.isConnected = isConnected;
        }

        /**
         * Returns the URL corresponding to the configuration file on the server
         * of given avatar.
         *
         * @param avatar The local avatar
         * @return The URL of the avatar configuration file on the server
         * @throw InterruptedException If the job has been interrupted
         */
        public URL getAvatarServerURL(MultimeshEvolverAvatar avatar) throws InterruptedException {
            Job job = Job.newGetURLJob(avatar);
            jobQueue.add(job);
            job.waitForJob();
            return job.url;
        }

        /**
         * Schedules an asynchronous job to synchronous the IMI avatar
         * configuration between the client and server. The caller may block
         * until the job is complete, by giving isWait of true.
         *
         * @param isWait True to block until the job has completed
         * @throw InterruptedException If the job has been interrupted
         */
        public void scheduleSync(boolean isWait) throws InterruptedException {
            Job job = Job.newSyncJob();
            jobQueue.add(job);
            if (isWait == true) {
                job.waitForJob();
            }
        }

        /**
         * Schedules an asynchronous job to delete an avatar from the server.
         * The caller may block until the job is complete, by giving isWait of
         * true.
         *
         * @param isWait True to block until the job has completed
         * @throw InterruptedException If the job has been interrupted
         */
        public void scheduleDelete(MultimeshEvolverAvatar avatar, boolean isWait) throws InterruptedException {
            Job job = Job.newDeleteJob(avatar);
            jobQueue.add(job);
            if (isWait == true) {
                job.waitForJob();
            }
        }

        /**
         * Schedules an asynchronous job to upload an avatar to the server.
         * The caller may block until the job is complete, by giving isWait of
         * true.
         *
         * @param isWait True to block until the job has completed
         * @throw InterruptedException If the job has been interrupted
         */
        public void scheduleUpload(MultimeshEvolverAvatar avatar, boolean isWait) throws InterruptedException {
            Job job = Job.newUploadJob(avatar);
            jobQueue.add(job);
            if (isWait == true) {
                job.waitForJob();
            }
        }

        /**
         * Returns the collection at the root of all of the avatar configuration
         * information.
         *
         * @return A ContentCollection of avatar configuration info on the server
         * @throw ContentRepositoryException Upon error finding the collection
         */
        private ContentCollection getBaseServerCollection(ServerSessionManager session)
                throws ContentRepositoryException {

            // Fetch the avatars/evolver directory, creating each if necessary
            ContentCollection dir = AvatarSessionLoader.getBaseServerCollection(session);
            ContentCollection evolverDir = (ContentCollection) dir.getChild("multimesh-evolver");
            if (evolverDir == null) {
                evolverDir = (ContentCollection) dir.createChild("multimesh-evolver", Type.COLLECTION);
            }
            return evolverDir;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            while (isConnected == true) {
                // Fetch the next job. If we are interrupted in doing so, then
                // simply log an error and continue.
                Job job = null;
                try {
                    job = jobQueue.take();
                } catch (InterruptedException excp) {
                    LOGGER.log(Level.WARNING, "Attempt to fetch the next job" +
                            " in queue was interrupted for server " +
                            manager.getServerURL(), excp);
                    continue;
                }

                // Dispatch to the implementation based upon the job type. When
                // done indicate we are done.
                switch (job.jobType) {
                    case SYNC:
                        syncImpl();
                        break;
                    case DELETE:
                        deleteImpl(job.avatar);
                        break;
                    case UPLOAD:
                        uploadFilesImpl(job.avatar);
                        break;
                    case GETURL:
                        job.url = getURLImpl(job.avatar);
                        break;
                }
                job.setJobDone();
            }
        }

        /**
         * Synchronous implementation of removing an avatar from the server
         * given the avatar to remove.
         *
         * @param avatar The avatar to remove from the server
         */
        private void deleteImpl(MultimeshEvolverAvatar avatar) {
            // Fetch the name of the avatar and see if an entry exists on the
            // server. If so, then remove it from the server and the map.
            String avatarName = avatar.getName();
            EvolverServerAvatar serverAvatar = serverAvatars.get(avatarName);
            if (serverAvatar != null) {
                // Remove the .xml configuration file from the server
                ContentResource resource = serverAvatar.resource;
                String resourceName = resource.getName();
                ContentCollection parent = resource.getParent();
                try {
                    parent.removeChild(resourceName);
                } catch (ContentRepositoryException excp) {
                    LOGGER.log(Level.WARNING, "Unable to delete avatar from" +
                            " server " + avatarName, excp);
                }

                // Also remove the directory containing the assets. It has the
                // same name minus the '.xml' extension.
                if (resourceName.endsWith(".xml") == true) {
                    int length = resourceName.length();
                    String dirName = resourceName.substring(0, length - 4);

                    LOGGER.info("Deleting existing avatar directory locally" +
                            " with name " + dirName);
                    try {
                        parent.removeChild(dirName);
                    } catch (ContentRepositoryException excp) {
                        LOGGER.log(Level.WARNING, "Unable to delete avatar" +
                                " directory from server " + avatarName, excp);
                    }
                }
                else {
                    LOGGER.info("Did not find evolver avatar directory locally" +
                            " with path " + resource.getPath());
                }

                // Finally, remove from the list of server avatars.
                serverAvatars.remove(avatarName);
            }
        }

        /**
         * Synchronous implementation of fetching the URL of the server
         * configuration file for the given avatar. Returns the URL.
         */
        private URL getURLImpl(MultimeshEvolverAvatar avatar) {

            // Fetch the server version using the name of the avatar. If it
            // does not exist, log an error and return null
            String avatarName = avatar.getName();
            EvolverServerAvatar serverAvatar = serverAvatars.get(avatarName);
            if (serverAvatar == null) {
                LOGGER.severe("No record of avatar " + avatarName +
                        " on server " + manager.getServerURL());
                return null;
            }

            // Otherwise, return its URL or null upon error
            try {
                return serverAvatar.resource.getURL();
            } catch (ContentRepositoryException excp) {
                LOGGER.log(Level.WARNING, "Unable to fetch URL for server" +
                        " avatar " + avatarName + " on server " +
                        manager.getServerURL(), excp);
                return null;
            }
        }

        /**
         * Synchronous implementation of synchronizing the client and server
         * IMI avatar configuration information.
         */
        private void syncImpl() {
            LOGGER.info("Beginning sychronization of server " +
                    manager.getServerURL());

            // Keep arrays of configuration files that need to be uploaded to
            // the server and downloaded from the server.
            List<MultimeshEvolverAvatar> uploadList = new ArrayList();
            List<EvolverServerAvatar> downloadList = new ArrayList();

            // Look through the list of avatars on the server. If there was
            // a previous entry in the map (perhaps from a previous SYNC), then
            // remove the old entry. The 'serverAvatars' map contains all of
            // the avatars on the server.
            try {
                List<ContentNode> avatarList = serverCollection.getChildren();
                for (ContentNode node : avatarList) {
                    if (node instanceof ContentResource) {
                        ContentResource resource = (ContentResource)node;

                        LOGGER.info("Found server avatar resource " +
                                resource.getPath());
                        
                        EvolverServerAvatar serverAvatar = new EvolverServerAvatar(resource);
                        String avatarName = serverAvatar.avatarName;

                        LOGGER.info("Looking at server avatar named " +
                                avatarName + " with version " + serverAvatar.version);

                        // Check to see if the server avatar already exists in
                        // the known list of server avatars.
                        EvolverServerAvatar previous = serverAvatars.put(avatarName, serverAvatar);
                        if (previous != null && previous.version > serverAvatar.version) {

                            LOGGER.info("Found a previous version named " +
                                    avatarName + " with version " + previous.version);

                            // If we somehow find a more recent avatar in our
                            // list, then we remove the one we just found on
                            // the server. (The most recent one will be added
                            // back to the server later).
                            serverAvatars.put(previous.avatarName, previous);
                            String fileName = serverAvatar.getFilename();
                            serverCollection.removeChild(fileName);

                            // Also need to delete the directory containing the
                            // file associated with the avatar on the server
                            if (fileName.endsWith(".xml") == false) {
                                LOGGER.info("Previous version of avatar" +
                                        " does not have .xml " + fileName +
                                        " for avatar " + previous.avatarName);
                            }
                            else {
                                int length = fileName.length();
                                String dirName = fileName.substring(0, length - 4);
                                serverCollection.removeChild(dirName);
                            }
                        }
                    }
                }

                // Make a copy of the map of server avatars. This will serve
                // as a list of all avatars that need to be downloaded from
                // the server.
                Map<String, EvolverServerAvatar> tmpServerAvatars = new HashMap(serverAvatars);

                // Loop through all of the avatars we know about locally. See
                // if one by the same name exists on the server. If so, and
                // the server version needs to be updated, then do so. If the
                // local copy needs to be updated then do so.
                synchronized (localAvatars) {

                    LOGGER.info("Taking a look at all of our local avatars");

                    for (MultimeshEvolverAvatar avatar : localAvatars.values()) {
                        String avatarName = avatar.getName();
                        EvolverServerAvatar serverVersion = tmpServerAvatars.get(avatarName);

                        LOGGER.info("Looking at local avatar named " +
                                avatarName + " server version " + serverVersion);

                        // If the local avatar is not on the server, or if the
                        // version of the server is older than locally, then
                        // mark this avatar for upload.
                        if (serverVersion == null ||
                                serverVersion.version < avatar.getVersion()) {

                            LOGGER.info("Server version for avatar named " +
                                    avatarName + " does not exist or is older" +
                                    " than version " + avatar.getVersion());

                            uploadList.add(avatar);
                            tmpServerAvatars.remove(avatarName);
                        }
                        else if (serverVersion.version > avatar.getVersion()) {
                            LOGGER.info("Server version for avatar named " +
                                    avatarName + " is more recent than local " +
                                    "with server version " + serverVersion.version +
                                    " and local version " + avatar.getVersion());

                            // Otherwise, if the server has a more recent copy
                            // of the avatar, then mark this avatar for
                            // download.
                            downloadList.add(serverVersion);
                            tmpServerAvatars.remove(avatarName);
                        }
                        else if (serverVersion.version == avatar.getVersion()) {
                            LOGGER.info("Server version for avatar named " +
                                    avatarName + " is same as local version " +
                                    avatar.getVersion());
                            
                            // If the two versions are the same, we just want
                            // to do nothing with it.
                            // XXX Why do we need to re-add it to the server
                            // avatar list? It should already be there.
                            tmpServerAvatars.remove(avatarName);
                            serverAvatars.put(avatarName, serverVersion);
                        }
                    }
                }

                // Avatars left in the serverAvatars map are only on the server,
                // and not on the client (so the previous code block did not
                // see them), so add them to the download list.
                for (EvolverServerAvatar serverAvatar : tmpServerAvatars.values()) {
                    LOGGER.info("Adding Server avatar to download list " +
                            serverAvatar.avatarName + " version " +
                            serverAvatar.version);

                    downloadList.add(serverAvatar);
                }


                // For all of the avatar configuration files that we wish to
                // upload, do so synchronously.
                LOGGER.info("Doing upload of local avatars, number to upload " +
                        uploadList.size());

                for (MultimeshEvolverAvatar avatar : uploadList) {
                    LOGGER.info("Uploading Local avatar to server " +
                            avatar.getName() + " version " + avatar.getVersion());

                    uploadFilesImpl(avatar);
                }

                // Keep a list around of all of the avatars we have just
                // downloaded, to be added to the set of local avatars later.
                List<MultimeshEvolverAvatar> newAvatarList = new ArrayList();

                // For all of the avatar configuration files that we wish to
                // download, do so synchronously.
                LOGGER.info("Doing download of local avatars to the server," +
                        " number to download " + downloadList.size());

                for (EvolverServerAvatar serverAvatar : downloadList) {
                    LOGGER.info("Downloading server avatar named " +
                            serverAvatar.avatarName + " to file name " +
                            serverAvatar.resource.getName());

                    ContentResource resource = downloadFilesImpl(serverAvatar);
                    if (resource == null) {
                        LOGGER.info("Error download files from server");
                    }
                    else {
                        // Create a new entry to put on the local list. These
                        // will be added below.
                        MultimeshEvolverAvatar newAvatar = new MultimeshEvolverAvatar(resource);
                        newAvatarList.add(newAvatar);
                    }
                }

                // For all of the new avatars we just downloaded, upload the
                // list of local avatars. We also fire an event to indicate
                // that a new avatar has been added.
                LOGGER.info("Adding new local avatars to system, " +
                        "number of avatars " + newAvatarList.size());

                synchronized (localAvatars) {
                    for (MultimeshEvolverAvatar newAvatar : newAvatarList) {
                        LOGGER.info("Adding new local avatar to system " +
                                "named " + newAvatar.getName());

                        localAvatars.put(newAvatar.getName(), newAvatar);
                        AvatarRegistry registry = AvatarRegistry.getAvatarRegistry();
                        registry.registerAvatar(newAvatar, false);
                    }
                }
            } catch (ContentRepositoryException excp) {
                LOGGER.log(Level.WARNING, "Error synchronizing with server " +
                        manager.getServerURL(), excp);
            }
        }

        /**
         * Synchronous implementation of uploading an avatar to the server given
         * the avatar. This assumes not of the files exist on the server.
         *
         * @param avatar The avatar to upload to the server
         */
        private void uploadFilesImpl(MultimeshEvolverAvatar avatar) {

            // Fetch the avatar we wish to upload and the resource that corresponds
            // to the local configuration file.
            String avatarName = avatar.getName();
            ContentResource resource = avatar.getResource();
            ContentCollection parent = resource.getParent();
            String fileName = resource.getName();

            // Create a resource on the server with the same as the local resource.
            // Assume the file does not yet exist on the server. Then go ahead
            // and upload the local file to the server.
            ContentResource file = null;
            try {
                file = (ContentResource) serverCollection.createChild(fileName, Type.RESOURCE);
                InputStream is = resource.getInputStream();
                file.put(new BufferedInputStream(is));
            } catch (java.lang.Exception excp) {
                LOGGER.log(Level.WARNING, "Unable to upload xml config file " +
                        "named " + fileName + " for avatar " + avatarName, excp);
                return;
            }

            // Upload all of the other files associated with the config xml
            // file. We strip off the XML and transfer the entire directory
            if (fileName.endsWith(".xml") == false) {
                LOGGER.info("Invalid file name for xml config file " +
                        fileName + " for avatar named " + avatarName);
                return;
            }

            // Go ahead and transfer the files from the local to server
            // machine.
            String dirName = fileName.substring(0, fileName.length() - 4);
            ContentCollection local = null;
            try {
                local = (ContentCollection) parent.getChild(dirName);
                if (local == null) {
                    LOGGER.info("Unable to find local avatar directory " +
                            "named " + dirName + " for avatar " + avatarName);
                    return;
                }
            } catch (ContentRepositoryException excp) {
                LOGGER.log(Level.WARNING, "Unable to find local avatar " +
                        "directory named " + dirName + " for avatar " +
                        avatarName, excp);
                return;
            }

            try {
                transfer(local, serverCollection);
            } catch (java.lang.Exception excp) {
                LOGGER.log(Level.WARNING, "Unable to transfer files from " +
                        local.getPath() + " to " + serverCollection.getPath() +
                        " with name " + dirName + " for avatar " + avatarName, excp);
                return;
            }

            // Add an entry to the map of avatars on the server.
            EvolverServerAvatar serverAvatar = new EvolverServerAvatar(file);
            serverAvatars.put(serverAvatar.avatarName, serverAvatar);
        }

        /**
         * Synchronous implementation of download an avatar from the server given
         * the avatar. This assumes not of the files exist on the client.
         * Returns the content resource associated with the xml configuration
         * file locally on the client.
         */
        private ContentResource downloadFilesImpl(EvolverServerAvatar serverAvatar) {
            // Create an entry for the configuration file locally. Write the
            // contents of the server version to this file.
            String fileName = serverAvatar.resource.getName();
            String avatarName = serverAvatar.avatarName;
            ContentResource localFile = null;
            ContentCollection parent = serverAvatar.resource.getParent();
            try {
                // Create an entry for the configuration file locally.
                // Write the contents of the server version to this file.
                localFile = (ContentResource) evolverCollection.createChild(fileName, Type.RESOURCE);
                InputStream is = serverAvatar.resource.getInputStream();
                localFile.put(new BufferedInputStream(is));

                LOGGER.info("Local avatar created in resource " +
                        localFile.getPath());

            } catch (java.lang.Exception excp) {
                LOGGER.log(Level.WARNING, "Error downloading server " +
                        "avater named " + avatarName, excp);
                return null;
            }

            // Download all of the other files associated with the config xml
            // file. We strip off the XML and transfer the entire directory
            if (fileName.endsWith(".xml") == false) {
                LOGGER.warning("Invalid file name for xml config file " +
                        fileName + " for avatar named " + avatarName);
                return null;
            }

            // Go ahead and transfer the files from the server to the local
            // machine.
            String dirName = fileName.substring(0, fileName.length() - 4);
            ContentCollection remote = null;
            try {
                remote = (ContentCollection) parent.getChild(dirName);
                if (remote == null) {
                    LOGGER.warning("Unable to find remote avatar directory " +
                            "named " + dirName + " for avatar " + avatarName);
                    return null;
                }
            } catch (ContentRepositoryException excp) {
                LOGGER.log(Level.WARNING, "Unable to find remote avatar " +
                        "directory named " + dirName + " for avatar " +
                        avatarName, excp);
                return null;
            }

            try {
                transfer(remote, evolverCollection);
            } catch (java.lang.Exception excp) {
                LOGGER.log(Level.WARNING, "Unable to transfer files from " +
                        remote.getPath() + " to " + serverCollection.getPath() +
                        " with name " + dirName + " for avatar " + avatarName, excp);
                return null;
            }
            return localFile;
        }

        @Override
        public String toString() {
            return manager.getServerURL();
        }
    }

    /**
     * Transfers the entire contents of a content collection to a similarly
     * named content collection under the given parent. This assumes there is
     * only one level beneath the given directory (i.e. does not copy sub-
     * directories).
     */
    private void transfer(ContentCollection from, ContentCollection toParent)
            throws ContentRepositoryException, IOException {

        // First check to see if the content collection exists on the parent,
        // create it if necessary
        String dirName = from.getName();
        ContentCollection c = (ContentCollection)toParent.getChild(dirName);
        if (c == null) {
            c = (ContentCollection)toParent.createChild(dirName, Type.COLLECTION);
        }

        // Loop through all of the children and create them and copy them.
        List<ContentNode> children = from.getChildren();
        for (ContentNode child : children) {
            if (child instanceof ContentResource) {
                ContentResource resource = (ContentResource)child;
                String childName = resource.getName();

                // Create the child on the destination if necessary
                ContentResource r = (ContentResource)c.getChild(childName);
                if (r == null) {
                    r = (ContentResource)c.createChild(childName, Type.RESOURCE);
                }

                // Transfer the data
                InputStream is = resource.getInputStream();
                r.put(new BufferedInputStream(is));
            }
        }
    }

    /**
     * A static inner class that represents an avatar configuration on the
     * server
     */
    private static class EvolverServerAvatar {
        // The content repository resource pointing to the remote config file
        // on the server.
        public ContentResource resource = null;
        
        // The version number of the configuration file
        public int version = 0;

        // The name of the configuration file, stripped of the version number.
        public String avatarName = null;

        private static final String EXTENSION = ".xml";

        /**
         * Constructor, takes the content resource
         * @param resource
         */
        public EvolverServerAvatar(ContentResource resource) {
            this.resource = resource;
            String name = resource.getName();
            version = getAvatarVersion(name);
            int i = name.lastIndexOf('_');
            if (i == -1) {
                avatarName = name;
            } else {
                avatarName = name.substring(0, i);
            }
        }

        /**
         * Return the file name corresponding to the avatar's configuration file.
         *
         * @return The configuration file name
         */
        public String getFilename() {
            return avatarName + "_" + version + EXTENSION;
        }

        /**
         * Returns the version number embedded in the configuration file name.
         */
        private int getAvatarVersion(String filename) {
            // The version number is found between the final underscore and
            // the file extension.
            int underscore = filename.lastIndexOf('_');
            int ext = filename.lastIndexOf('.');

            if (underscore == -1 || ext == -1) {
                return -1;
            }
            String verStr = filename.substring(underscore + 1, ext);
            try {
                return Integer.parseInt(verStr);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }

    /**
     * A static inner class that represents a 'job' submitted to the 'server
     * sync' thread. Threads may block on the completion of a thread by
     * invoking waitForJob().
     */
    private static class Job {

        public enum JobType {
            SYNC, DELETE, UPLOAD, GETURL
        };

        public JobType jobType;
        public MultimeshEvolverAvatar avatar = null;
        public Semaphore jobDone = null;
        public URL url = null;

        private Job(JobType jobType, MultimeshEvolverAvatar avatar) {
            this.jobType = jobType;
            this.avatar = avatar;
            this.jobDone = new Semaphore(0);
            this.url = null;
        }

        public static Job newSyncJob() {
            return new Job(JobType.SYNC, null);
        }

        public static Job newDeleteJob(MultimeshEvolverAvatar avatar) {
            return new Job(JobType.DELETE, avatar);
        }

        public static Job newUploadJob(MultimeshEvolverAvatar avatar) {
            return new Job(JobType.UPLOAD, avatar);
        }

        public static Job newGetURLJob(MultimeshEvolverAvatar avatar) {
            return new Job(JobType.GETURL, avatar);
        }

        /**
         * Waits for this job to be done.
         * @throw InterruptedException If the job has been interrupted
         */
        public void waitForJob() throws InterruptedException {
            jobDone.acquire();
        }

        /**
         * Used by jobs to indicate they have completed.
         */
        public void setJobDone() {
            jobDone.release();
        }
    }
}

