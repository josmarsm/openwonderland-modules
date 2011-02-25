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
    private int majorVersion;
    private int minorVersion;

    /**
     * Create a new instance of SimpleModuleMetaData which has the specified attributes.
     *
     * @param name The name of the Module.
     * @param description A description of what the Module represents.
     * @param majorVersion The major version of the Module.
     * @param minorVersion The minor version of the Module.
     */
    public SimpleModuleMetaData(String name, String description, int majorVersion, int minorVersion) {
        this.name = name;
        this.description = description;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
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
}
