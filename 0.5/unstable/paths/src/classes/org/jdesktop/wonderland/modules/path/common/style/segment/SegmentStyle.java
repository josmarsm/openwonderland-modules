package org.jdesktop.wonderland.modules.path.common.style.segment;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
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

    @XmlTransient
    private final SegmentStyleTypeHolder styleTypeHolder;

    /**
     * No argument constructor for JAXB.
     */
    public SegmentStyle() {
        styleTypeHolder = new SegmentStyleTypeHolder();
    }

    /**
     * Create a new SegmentStyle which is linked to the specified wrapped SegmentStyle.
     *
     * @param wrappedStyle The SegmentStyle which is to be wrapped by this SegmentStyle.
     * @throws IllegalArgumentException If the specified SegmentStyle to be wrapped was null.
     */
    protected SegmentStyle(SegmentStyle wrappedStyle) throws IllegalArgumentException {
        super(wrappedStyle);
        styleTypeHolder = wrappedStyle.getStyleTypeHolder();
    }

    /**
     * Create a new instance of SegmentStyle for the specified SegmentStyleType.
     *
     * @param styleTypeHolder The type of SegmentStyle for which this SegmentStyle object
     *                  holds meta-data.
     */
    public SegmentStyle(SegmentStyleType styleType) {
        styleTypeHolder = new SegmentStyleTypeHolder(styleType);
    }

    /**
     * Get the SegmentStyleType of this NodeStyle.
     *
     * @return The SegmentStyleType of this NodeStyle (if set).
     */
    @Override
    @XmlAttribute(name="type")
    public SegmentStyleType getStyleType() {
        return styleTypeHolder.styleType;
    }

    /**
     * Set the SegmentStyleType of this NodeStyle.
     *
     * @param styleTypeHolder The NodeStyleType of this NodeStyle.
     */
    public void setStyleType(SegmentStyleType styleType) {
        styleTypeHolder.styleType = styleType;
    }

    /**
     * Get the internal SegmentStyleTypeHoler for use for keeping a
     * SegmentStyle wrapper class in synch with the SegmentStyle it
     * wraps.
     *
     * @return The internal SegmentStyleTypeHolder which provides an extra level of indirection
     *         to the SegmentStyleType so that both a SegmentStyle and its wrapping SegmentStyle
     *         can share a common SegmentStyleType which both change together.
     */
    @XmlTransient
    private SegmentStyleTypeHolder getStyleTypeHolder() {
        return styleTypeHolder;
    }

    /**
     * This class is a simple level of indirection which allows two
     * SegmentStyles to share a common SegmentStyleType and when one is
     * changed the the other is changed also.
     */
    private static class SegmentStyleTypeHolder {

        /**
         * Create a new SegmentStyleTypeHolder which does not have a SegmentStyleType set.
         */
        public SegmentStyleTypeHolder() { }
        
        /**
         * Create a new instance of SegmentStyleTypeHolder which has the specified SegmentStyleType set.
         * 
         * @param styleTypeHolder The SegmentStyleType which is to be stored in this SegmentStyleTypeHolder.
         */
        public SegmentStyleTypeHolder(SegmentStyleType styleType) {
            this.styleType = styleType;
        }

        public SegmentStyleType styleType;
    }
}
