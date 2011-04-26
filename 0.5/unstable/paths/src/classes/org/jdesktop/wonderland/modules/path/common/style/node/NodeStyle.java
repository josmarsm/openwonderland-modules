package org.jdesktop.wonderland.modules.path.common.style.node;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.modules.path.common.style.AbstractItemStyle;

/**
 * This interface represents meta-data information about styling of a node in a path.
 * Specific implementations can hold more data depending on the NodeStyleType.
 * Some meta-data is only applicable to certain NodeStyleTypes.
 *
 * @author Carl Jokl
 */
@XmlRootElement(name="node-style")
public class NodeStyle extends AbstractItemStyle<NodeStyleType> implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    @XmlTransient
    private NodeStyleType styleType;

    /**
     * No argument constructor for JAXB.
     */
    public NodeStyle() {}

    /**
     * Create a new instance of a NodeStyle for the specified NodeStyleType.
     *
     * @param styleType The type of NodeStyle for which this NodeStyle holds meta-data.
     */
    public NodeStyle(NodeStyleType styleType) {
        this.styleType = styleType;
    }

    /**
     * Get the NodeStyleType of this NodeStyle.
     *
     * @return The NodeStyleType of this NodeStyle (if set).
     */
    @Override
    @XmlAttribute(name="type")
    public NodeStyleType getStyleType() {
        return styleType;
    }

    /**
     * Set the NodeStyleType of this NodeStyle.
     *
     * @param styleType The NodeStyleType of this NodeStyle.
     */
    public void setStyleType(NodeStyleType styleType) {
        this.styleType = styleType;
    }    
}
