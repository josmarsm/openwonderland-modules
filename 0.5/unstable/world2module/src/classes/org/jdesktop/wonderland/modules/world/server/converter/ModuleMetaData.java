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

    /**
     * Get the mini version of the module if any.
     *
     * @return The mini version of the module if any.
     *         If the module has no mini version then
     *         this value will be zero.
     */
    public int getMiniVersion();

    /**
     * Get the author of the module.
     *
     * @return The Author of the module if any.
     */
    public String getAuthor();

    /**
     * Get whether the Author of the module is set.
     *
     * @return True if the author of the module is set.
     *         False otherwise.
     */
    public boolean isAuthorSet();

    /**
     * Get the vendor of the module.
     *
     * @return The name of the vendor of the module if any.
     */
    public String getVendor();

    /**
     * Whether the module vendor is set.
     *
     * @return True if the module vendor is set.
     */
    public boolean isVendorSet();
}