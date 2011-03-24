package org.jdesktop.wonderland.modules.ruler.common;

/**
 * Different types of ruler i.e. the shape of the ruler.
 *
 * @author Carl Jokl
 */
public enum RulerType {

    /**
     * This type of ruler is a simple straight line ruler used to
     * measure one dimension.
     */
    STRAIGHT,

    /**
     * This type of ruler is an L-Shaped ruler which is used to measure
     * two dimensions.
     */
    LSHAPE,

    /**
     * This type of ruler which is not often found in the real world is used
     * to measure distances over 3 dimensions.
     */
    THREE_AXIS;
}
