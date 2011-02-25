package org.jdesktop.wonderland.modules.world.server.converter;

/**
 * This interface represents Module meta-data. This meta-data is important in the process of converting
 * a snapshot or world into a module as it is required for creating the manifest/properties as well
 * as for naming some of the directories.
 *
 * @author Carl Jokl
 */
public interface ModuleMetaData {

    /**
     * Get the name of the module.
     * This is used as the name of certain sub folders within the module structure.
     *
     * @return The name of the module.
     */
    public String getModuleName();

    /**
     * Get the description of the module which explains its purpose.
     *
     * @return The description of the module.
     */
    public String getDescription();

    /**
     * Get the major version of the module.
     *
     * @return The major version of the module.
     */
    public int getMajorVersion();

    /**
     * Get the minor version of the module.
     *
     * @return The minor version of the module.
     */
    public int getMinorVersion();
}