package org.jdesktop.wonderland.modules.path.common.style.node;

/**
 * This interface represents an object which can retrieve a collection of node styles which apply
 * to a given type of path. This is used to keep together NodeStyles which can be used with a PathType 
 * and avoid having a NodeStyleType be applied to a path which is not intended to be used with it.
 *
 * @author Carl Jokl
 */
public interface NodeStyleGroup {

    /**
     * Get the NodeStyles which are part of the NodeStyleGroup.
     * 
     * @return An array of all the NodeStyles in the NodeStyleGroup.
     */
    public NodeStyleType[] getNodeStyles();

    /**
     * Get the default NodeStyle within this NodeStyleType group.
     * 
     * @return The default NodeStyleType within this NodeStyleType group.
     */
    public NodeStyleType getDefaultStyle();

    /**
     * Get the node style from this NodeStyleGroup which has the specified id
     * value (if any).
     *
     * @param id An integer value which uniquely identifies a given NodeStyleType within this
     *        node style group.
     * @return The NodeStyleType with the specified name or null if no NodeStyleType exists in this
     *         group with the specified id.
     */
    public NodeStyleType getNodeStyle(int id);
}
