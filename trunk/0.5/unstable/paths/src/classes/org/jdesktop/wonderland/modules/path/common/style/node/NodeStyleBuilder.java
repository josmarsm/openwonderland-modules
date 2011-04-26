package org.jdesktop.wonderland.modules.path.common.style.node;

/**
 * This is an interface for an object which builds default NodeStyle for a given NodeStyleType.
 *
 * @author Carl Jokl
 */
public interface NodeStyleBuilder {

    /**
     * Create the default NodeStyle for the particular NodeStyleStype which is supported
     * by this NodeStyleBuilder.
     * 
     * @return An instance of the default NodeStyle (which can be updated) for the NodeStyleType for which this
     *         NodeStyleBuilder builds NodeStyles.
     */
    public NodeStyle createNodeStyle();

    /**
     * Get the NodeStyleType for which this NodeStyleBuilder builds NodeStyles.
     *
     * @return The NodeStyleType for which this NodeStyleBuilder builds NodeStyles.
     */
    public NodeStyleType getBuiltStyleType();
}
