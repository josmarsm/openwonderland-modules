package org.jdesktop.wonderland.modules.path.common.style.node;

/**
 * This is a builder used to build NodeStyle for the invisible NodeStyleType.
 *
 * @author Carl Jokl
 */
public class InvisibleNodeStyleBuilder implements NodeStyleBuilder {

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyle createNodeStyle() {
        return new NodeStyle(CoreNodeStyleType.INVISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyleType getBuiltStyleType() {
        return CoreNodeStyleType.INVISIBLE;
    }
}
