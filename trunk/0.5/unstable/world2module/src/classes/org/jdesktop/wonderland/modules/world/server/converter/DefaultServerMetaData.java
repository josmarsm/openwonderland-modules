package org.jdesktop.wonderland.modules.world.server.converter;

import java.io.File;

/**
 * This class represents a simple defaults using version of Wonderland server meta-data.
 *
 * @author Carl Jokl
 */
public class DefaultServerMetaData implements ServerMetaData {

    /**
     * The default major version for the Wonderland Server.
     */
    private static int DEFAULT_MAJOR_VERSION = 0;

    /**
     * The default minor version for the Wonderland Server.
     */
    private static int DEFAULT_MINOR_VERSION = 5;

    /**
     * Derive the name of the server root directory for a specific version using the specified 
     * version attributes.
     * 
     * @param majorServerVersion The current major Wonderland server version in use.
     * @param minorServerVersion The current minor Wonderland server version in use.
     * @param developmentMode Whether the version of the server currently in use is a developer release.
     * @return The generated directory name from the specified version attributes.
     */
    public static String toDirName(int majorServerVersion, int minorServerVersion, boolean developmentMode) {
        return developmentMode ? String.format("%d.%d-dev", majorServerVersion, minorServerVersion) : String.format("%d.%d", majorServerVersion, minorServerVersion);
    }

    private File serverRoot;
    private int majorVersion;
    private int minorVersion;
    private boolean developerVersion;

    /**
     * Create a new instance of DefaultServerMetaData with the specified
     * server root directory.
     * 
     * @param serverRoot The server root directory which should be the server version directory
     *                   under the wonderland server root directory.
     */
    public DefaultServerMetaData(File serverRoot) {
        this.serverRoot = serverRoot;
        this.majorVersion = DEFAULT_MAJOR_VERSION;
        this.minorVersion = DEFAULT_MINOR_VERSION;
    }

    /**
     * Create a new instance of DefaultServerMetaData with the specified
     * server root directory.
     *
     * @param serverRoot The server root directory string which should be the server version directory
     *                   under the wonderland server root directory.
     */
    public DefaultServerMetaData(String serverRoot) {
        this(new File(serverRoot));
    }

    /**
     * Create a new instance of DefaultServerMetaData with the specified top level Wonderland server
     * directory and the specified server version parameters with which to find the server root directory
     * currently in used.
     *
     * @param dir This is either the directory in which the top most Wonderland server directory resides (such as the home folder
     *            of the user who runs the Server) or the top level Wonderland directory itself (which is normally hidden).
     * @param majorServerVersion The current major Wonderland server version in use.
     * @param minorServerVersion The current minor Wonderland server version in use.
     * @param developmentMode Whether the version of the server currently in use is a developer release.
     * @param isParent Whether the specified directory is the parent directory of the top most wonderland directory
     *                 or if false it is the top most directory itself.
     */
    public DefaultServerMetaData(File dir, int majorServerVersion, int minorServerVersion, boolean developmentMode, boolean isParent) {
        if (isParent) {
            this.serverRoot = new File(new File(dir, WonderlandDirectoryConstants.DEFAULT_WONDERLAND_SERVER_DIRECTORY_NAME), toDirName(majorServerVersion, minorServerVersion, developmentMode));
        }
        else {
            this.serverRoot = new File(dir, toDirName(majorServerVersion, minorServerVersion, developmentMode));
        }
    }

    /**
     * Create a new instance of DefaultServerMetaData with the specified top level Wonderland server
     * directory and the specified server version parameters with which to find the server root directory
     * currently in used.
     *
     * @param dir This is either the directory in which the top most Wonderland server directory resides (such as the home folder
     *            of the user who runs the Server) or the top level Wonderland directory itself (which is normally hidden).
     * @param majorServerVersion The current major Wonderland server version in use.
     * @param minorServerVersion The current minor Wonderland server version in use.
     * @param developmentMode Whether the version of the server currently in use is a developer release.
     * @param isParent Whether the specified directory is the parent directory of the top most wonderland directory
     *                 or if false it is the top most directory itself.
     */
    public DefaultServerMetaData(String dir, int majorServerVersion, int minorServerVersion, boolean developmentMode, boolean isParent) {
        this(new File(dir), majorServerVersion, minorServerVersion, developmentMode, isParent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMajorVersion() {
        return majorVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinorVersion() {
        return minorVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeveloperVersion() {
        return developerVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWFSDirectoryName() {
        return WonderlandDirectoryConstants.DEFAULT_WONDERLAND_FILE_SYSTEM_DIRECTORY_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWorldWFSDirectoryName() {
        return WonderlandDirectoryConstants.DEFAULT_WORLD_WONDERLAND_FILE_SYSTEM_DIRECTORY_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerRootParentDirectoryName() {
        return serverRoot.getParent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentRootParentDirectoryName() {
        return WonderlandDirectoryConstants.DEFAULT_CONTENT_ROOT_PARENT_DIRECTORY_NAME;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public File getServerRoot() {
        return serverRoot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getWFSRoot() {
        return new File(getServerRoot(), getWFSDirectoryName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getContentRoot() {
        return new File(new File(getServerRoot(), WonderlandDirectoryConstants.DEFAULT_CONTENT_ROOT_PARENT_DIRECTORY_NAME), WonderlandDirectoryConstants.DEFAULT_CONTENT_ROOT_DIRECTORY_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getSnapshotsDir() {
        return new File(getWFSRoot(), WonderlandDirectoryConstants.DEFAULT_SNAPSHOTS_DIRECTORY_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getWorldsDir() {
        return new File(getWFSRoot(), WonderlandDirectoryConstants.DEFAULT_WORLDS_DIRECTORY_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getUsersDir() {
        return new File(getContentRoot(), WonderlandDirectoryConstants.DEFAULT_USERS_DIRECTORY_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getModulesDir() {
        return new File(getContentRoot(), WonderlandDirectoryConstants.DEFAULT_MODULES_DIRECTORY_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getDeployedModulesDir() {
        return new File(getModulesDir(), WonderlandDirectoryConstants.DEFAULT_INSTALLED_MODULES_DIRECTORY_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getCreatedModulesDir() {
        return new File(getModulesDir(), WonderlandDirectoryConstants.DEFAULT_CREATED_MODULES_DIRECTORY_NAME);
    }


}
