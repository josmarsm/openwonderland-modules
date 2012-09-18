package org.jdesktop.wonderland.modules.path.common.style.node;

import org.jdesktop.wonderland.modules.path.common.style.StyleTypeResolver;
import org.jdesktop.wonderland.modules.path.common.style.node.CoreNodeStyleType;

/**
 * This is the default built in StyleTypeResolver type which resolves core NodeStyleTypes
 * from their corresponding unique integer id values.
 *
 * @author Carl Jokl
 */
public class CoreNodeStyleTypeResolver implements StyleTypeResolver<NodeStyleType> {

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeStyleType resolveStyleType(int styleTypeId) {
        for (NodeStyleType type : CoreNodeStyleType.values()) {
            if (type.getId() == styleTypeId) {
                return type;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGroupName() {
        return "core:node-style-type";
    }
}
