package org.jdesktop.wonderland.modules.world.common.xml;

import java.util.List;

/**
 * This is an interface to provide access for things such as TagContentHandlers to get
 * hold of the state of the parser / handler which invoked them. This is extra
 * information which may be needed.
 *
 * @author Carl Jokl
 */
public interface XmlStateAttributes {

    /**
     * Get a list of the pending prefix mappings.
     *
     * @return A list of the pending prefix mappings.
     */
    public List<PrefixMapping> getPendingPrefixMappings();

    /**
     * Clear any pending prefix mappings.
     */
    public void clearPendingPrefixMappings();

    /**
     * Get the current tag depth of the parser.
     * 
     * @return The tag depth of the parser.
     */
    public int getDepth();

    /**
     * Whether the parser / handler is currently inside character data.
     *
     * @return True if the parser is currently inside character data.
     */
    public boolean isInCDATA();
}
