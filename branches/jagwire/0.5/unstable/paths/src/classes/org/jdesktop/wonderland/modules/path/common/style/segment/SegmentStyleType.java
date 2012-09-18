package org.jdesktop.wonderland.modules.path.common.style.segment;

import org.jdesktop.wonderland.modules.path.common.style.StyleType;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;

/**
 * This is an interface which represents the different types of segment styles.
 * An interface is used to allow other modules to create their own segment style
 * types without being constrained to use the built in types from the
 * built in enumeration. As enumerations cannot be cleanly extended
 * the module codes to this interface.
 *
 * @author Carl Jokl
 */
public interface SegmentStyleType extends StyleType {

    /**
     * Get the default NodeStyleType to be used with this SegmentStyleType.
     *
     * @return The default NodeStyleType for this SegmentStyleType.
     */
    public NodeStyleType getDefaultNodeStyleType();
}
