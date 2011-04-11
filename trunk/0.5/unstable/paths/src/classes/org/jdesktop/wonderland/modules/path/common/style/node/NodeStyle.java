package org.jdesktop.wonderland.modules.path.common.style.node;

import org.jdesktop.wonderland.modules.path.common.style.ItemStyle;

/**
 * This interface represents meta-data information about styling of a node in a path.
 * Specific implementations can hold more data depending on the NodeStyleType.
 * Some meta-data is only applicable to certain NodeStyleTypes.
 *
 * @author Carl Jokl
 */
public interface NodeStyle extends ItemStyle<NodeStyleType> {

}
