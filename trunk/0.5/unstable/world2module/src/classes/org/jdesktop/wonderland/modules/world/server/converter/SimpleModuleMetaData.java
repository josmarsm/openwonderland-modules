package org.jdesktop.wonderland.modules.world.server.converter;

/**
 * This class represents a very simple implementation of ModuleMetaData
 * to hold the basic attributes of a module to be created.
 *
 * @author Carl Jokl
 */
public class SimpleModuleMetaData implements ModuleMetaData {

    private String name;
    private String description;
    private String author;
    private String vendor;
    private int majorVersion;
    private int minorVersion;
    private int miniVersion;

    /**
     * Create a new instance of SimpleModuleMetaData which has the specified core attributes.
     *
     * @param name The name of the Module.
     * @param description A description of what the Module represents.
     * @param majorVersion The major version of the Module.
     * @param minorVersion The minor version of the Module.
     */
    public SimpleModuleMetaData(String name, String description, int majorVersion, int minorVersion) {
        this.name = name;
        this.description = description;
        author = null;
        vendor = null;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        miniVersion = 0;
    }

    /**
     * Create a new instance of SimpleModuleMetaData which has the following complete set of
     * attributes.
     *
     * @param name The name of the Module.
     * @param description A description of what the Module represents.
     * @param author The individual who has created the Module.
     * @param vendor The organization or individual which acts as the vendor for the Module if any.
     * @param majorVersion The major version of the Module.
     * @param minorVersion The minor version of the Module.
     * @param miniVersion The mini version of the Module (if any) or zero if the mini version is not used.
     */
    public SimpleModuleMetaData(String name, String description, String author, String vendor, int majorVersion, int minorVersion, int miniVersion) {
        this.name = name;
        this.description = description;
        if (author != null) {
            author = author.trim();
            if (author.isEmpty()) {
                this.author = null;
            }
            else {
                this.author = author;
            }
        }
        else {
            this.author = null;
        }
        if (vendor != null) {
            vendor = vendor.trim();
            if (vendor.isEmpty()) {
                this.vendor = null;
            }
            else {
                this.vendor = vendor;
            }
        }
        else {
            this.vendor = null;
        }
        this.vendor = vendor;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.miniVersion = miniVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getModuleName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return description;
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
    public int getMiniVersion() {
        return miniVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthor() {
        return author;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorSet() {
        return author != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVendor() {
        return vendor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVendorSet() {
        return vendor != null;
    }
}
