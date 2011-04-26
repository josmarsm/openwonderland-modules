package org.jdesktop.wonderland.modules.path.common.style.segment;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import org.jdesktop.wonderland.modules.path.common.style.AbstractItemStyle;

/**
 * This interface represents meta data information about the styling of a path segment.
 * Specific implementations can hold more data depending on the SegmentStyleType.
 * Some meta-data is only applicable to certain SegmentStyleType.
 *
 * @author Carl Jokl
 */
public class SegmentStyle extends AbstractItemStyle<SegmentStyleType> implements Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private SegmentStyleType styleType;

    /**
     * No argument constructor for JAXB.
     */
    public SegmentStyle() { }

    /**
     * Create a new instance of SegmentStyle for the specified SegmentStyleType.
     *
     * @param styleType The type of SegmentStyle for which this SegmentStyle object
     *                  holds meta-data.
     */
    public SegmentStyle(SegmentStyleType styleType) {
        this.styleType = styleType;
    }

    /**
     * Get the SegmentStyleType of this NodeStyle.
     *
     * @return The SegmentStyleType of this NodeStyle (if set).
     */
    @Override
    @XmlAttribute(name="type")
    public SegmentStyleType getStyleType() {
        return styleType;
    }

    /**
     * Set the SegmentStyleType of this NodeStyle.
     *
     * @param styleType The NodeStyleType of this NodeStyle.
     */
    public void setStyleType(SegmentStyleType styleType) {
        this.styleType = styleType;
    }
}
