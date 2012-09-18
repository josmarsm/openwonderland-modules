package org.jdesktop.wonderland.modules.world.server.converter;

/**
 * This interface holds default directory names used within Wonderland.
 *
 * @author Carl Jokl
 */
public interface WonderlandDirectoryConstants {

    /**
     * This is the default name of the Project Wonderland server data folder.
     */
    public static final String DEFAULT_WONDERLAND_SERVER_DIRECTORY_NAME = ".wonderland-server";

    /**
     * The default directory name used to denote the Wonderland file system.
     */
    public static final String DEFAULT_WONDERLAND_FILE_SYSTEM_DIRECTORY_NAME = "wfs";

    /**
     * This is the default name of the world Wonderland file system within a snapshot.
     */
    public static final String DEFAULT_WORLD_WONDERLAND_FILE_SYSTEM_DIRECTORY_NAME = "world-wfs";

    /**
     * This is the default name of the directory which is the parent of the content root.
     * This parent is by default also beneath the Wonderland root directory.
     */
    public static final String DEFAULT_CONTENT_ROOT_PARENT_DIRECTORY_NAME = "run";

    /**
     * The default name of the Wonderland content directory.
     */
    public static final String DEFAULT_CONTENT_ROOT_DIRECTORY_NAME = "content";

    /**
     * The default directory name for the directory in which the snapshots are stored.
     */
    public static final String DEFAULT_SNAPSHOTS_DIRECTORY_NAME = "snapshots";

    /**
     * The default directory name for the directory in which the worlds are stored.
     */
    public static final String DEFAULT_WORLDS_DIRECTORY_NAME = "worlds";

    /**
     * The default directory within the Wonderland content directory in which
     * per user content is stored.
     */
    public static final String DEFAULT_USERS_DIRECTORY_NAME = "users";

    /**
     * The default directory in which modules and module data is stored under the content root.
     */
    public static final String DEFAULT_MODULES_DIRECTORY_NAME = "modules";

    /**
     * The default directory in which installed modules and module data is stored inside the modules directory.
     */
    public static final String DEFAULT_INSTALLED_MODULES_DIRECTORY_NAME = "installed";

    /**
     * The default directory in which pending modules and module data is stored inside the modules directory.
     */
    public static final String DEFAULT_PENDING_MODULES_DIRECTORY_NAME = "pending";

    /**
     * The default directory in which created modules and module data is stored inside the modules directory.
     */
    public static final String DEFAULT_CREATED_MODULES_DIRECTORY_NAME = "created";

    /**
     * This is the default art directory used when modifying the URI
     * to where content can be found from a snapshot or world to a module.
     */
    public static final String DEFAULT_ART_DIRECTORY_NAME = "art";
}
