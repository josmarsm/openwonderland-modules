package org.jdesktop.wonderland.modules.world.server.converter;

import java.io.File;

/**
 * This interface represents an object which has or can retrieve server meta-data which is of use
 * when exporting a snapshot and convert it into a module. This includes the Wonderland server data
 * repository which contains the data to be exported.
 *
 * @author Carl Jokl
 */
public interface ServerMetaData {

    /**
     * Get the name used for a Wonderland File System directory.
     * This name should be consistent in different locations within the server.
     * 
     * @return The name used for a general Wonderland File System directory on this server.
     */
    String getWFSDirectoryName();

    /**
     * Get the name used for an individual world Wonderland File System directory on this server.
     * This is normally different from the general Wonderland File System directory but should
     * be consistent across worlds and snapshots.
     *
     * @return The name used for a world and snapshot Wonderland File System directory on this server.
     */
    String getWorldWFSDirectoryName();

    /**
     * Get the name of the directory which is the parent of the server root directory.
     *  
     * @return The name of the directory which is the parent of the server root directory.
     */
    String getServerRootParentDirectoryName();

    /**
     * Get the name of the directory which is the parent of the content root.
     * (this is normally the directory between the content root and the server root.
     * 
     * @return The name of the directory which is the parent of the content root directory.
     */
    String getContentRootParentDirectoryName();

    /**
     * Get the root directory in which all of the Wonderland server data is stored from content to the 
     * Wonderland file system. This can be used to find the snapshots as well as the directories 
     * in which the content used by snapshot are stored. This is specific to the sub directory
     * for the version of the server which is currently running i.e. the top directory may
     * be .wonderland-server but the server content root would be the directory within that
     * which is being used by the server e.g. 0.5-dev.
     * 
     * @return A file which represents the root directory of the Wonderland server repository.
     */
    File getServerRoot();

    /**
     * Get the root of the Wonderland File System directory.
     *
     * @return A file which represents the root directory of the Wonderland File System directory.
     */
    File getWFSRoot();

    /**
     * Get the Wonderland directory on the server which contains the content. This directory will
     * generally have subdirectories for content stored under individuals users as well as module
     * data etc.
     *
     * @return A File representing the directory which is the the root for all the content in
     *         the Wonderland Server.
     */
    File getContentRoot();

    /**
     * Get the directory containing the snapshots.
     *
     * @return A File representing the directory containing all the snapshots.
     */
    File getSnapshotsDir();

    /**
     * Get the directory containing the deployed worlds.
     *
     * @return A File which represents the directory containing the deployed worlds.
     */
    File getWorldsDir();

    /**
     * Get the directory (generally a subdirectory of the content root) which holds content
     * for individuals users (usually called users).
     *
     * @return A File which represents the directory containing the content on the server stored
     *         per user which imported it.
     */
    File getUsersDir();

    /**
     * Get the modules directory within the content repository.
     *
     * @return A File representing the Modules directory within the content directory.
     */
    File getModulesDir();

    /**
     * Get the directory which contains the deployed modules on the server.
     *
     * @return A File representing the directory in which deployed modules are stored.
     */
    File getDeployedModulesDir();

    /**
     * Get the directory containing the created modules stored on the Wonderland server.
     *
     * @return A File representing the location where created modules are stored on the server.
     */
    File getCreatedModulesDir();

    /**
     * Get the major server version.
     *
     * @return The major server version.
     */
    public int getMajorVersion();

    /**
     * Get the minor server version.
     *
     * @return The minor server version.
     */
    public int getMinorVersion();

    /**
     * Whether the version of Wonderland running is a developer release.
     *
     * @return True if the current running version of Wonderland is a developer release.
     */
    public boolean isDeveloperVersion();
}
