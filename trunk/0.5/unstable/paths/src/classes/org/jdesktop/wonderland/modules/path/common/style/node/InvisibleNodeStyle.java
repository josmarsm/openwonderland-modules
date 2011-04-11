package org.jdesktop.wonderland.modules.path.common.style.node;

import org.jdesktop.wonderland.modules.path.common.style.node.CoreNodeStyleType;
import java.io.Serializable;
import org.jdesktop.wonderland.modules.path.common.style.AbstractItemStyle;

/**
 * This class represents an implementation of NodeStyle which is invisible. 
 * This can be used with the InvisiblePathType or any PathType where the nodes
 * are not intended to be drawn.
 *
 * @author Carl Jokl
 */
public class InvisibleNodeStyle extends AbstractItemStyle<NodeStyleType> implements NodeStyle, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new instance of InvisibleNodeStyle with the default span value of 1.
     */
    public InvisibleNodeStyle() {

    }

    /**
     * Create a new instance of InvisibleNodeStyle with the specified span value.
     *
     * @param span The number of nodes which should be spanned by this style.
     * @throws IllegalArgumentException If the specified span was not greater than zero.
     */
    public InvisibleNodeStyle(int span) throws IllegalArgumentException {
        super(span);
    }

    /**
     * Get the NodeStyleType for this InvisibleNodeStyle.
     *
     * @return The NodeStyleType of the InvisibleNodeStyle i.e. CoreNodeStyleType.INVISIBLE.
     */
    @Override
    public NodeStyleType getStyleType() {
        return CoreNodeStyleType.INVISIBLE;
    }
}
