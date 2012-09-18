package org.jdesktop.wonderland.modules.path.common.style.node;

import java.io.Serializable;
import org.jdesktop.wonderland.modules.path.common.style.segment.CoreSegmentStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This enumeration represents the core built in NodeStyles provided by this module.
 *
 * @author Carl Jokl
 */
public enum CoreNodeStyleType implements NodeStyleType, Serializable {

    /**
     * This SegmentStyleType represents a path which is not visible such as one used for animation.
     */
    EDIT_MODE( 0, "Edit Mode", "This is a crude display of the node on a path as a simple geometric shape to show where the node is located.", CoreSegmentStyleType.EDIT_MODE),

    /**
     * This style has the node not be displayed at all except when being edited.
     */
    INVISIBLE( 1, "Invisible", "Node is not displayed.", CoreSegmentStyleType.INVISIBLE),

    /**
     * This style is used for square fence posts or similar styled paths.
     */
    SQUARE_POST( 2, "Square Post", "Node is displayed as rectangular post like that of a fence post.", CoreSegmentStyleType.BLOCK),

    /**
     * This style is used for poles or cylindrical fence posts.
     */
    POLE( 3, "Pole", "Node is displayed as a cylindrical pole.", CoreSegmentStyleType.POLE),

    /**
     * This style is used for a node which is rendered as a texture image.
     */
    IMAGE( 4, "Image", "Node is displayed as a specified texture image.", CoreSegmentStyleType.IMAGE),

    /**
     * This style is used for a node which is rendered as some kind of model.
     */
    MODEL( 5, "Model", "Nodes are rendered as a specified 3D model.", CoreSegmentStyleType.MODEL),

    /**
     * This style is intended for use with a hazard or police cordon to display hazard cones.
     */
    HAZARD_CONE( 20, "Hazard Cone", "Node is displayed as a hazard cone, intended for use with cordon tape.", CoreSegmentStyleType.TAPE);

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String description;
    private final SegmentStyleType defaultSegmentStyle;

    /**
     * Create a new instance of a NodeStyleType with the specified attributes.
     *
     * @param id The unique id of the NodeStyleType.
     * @param name The name of the NodeStyleType.
     * @param description The description of the NodeStyleType which is user friendly and can be presented to the user.
     */
    private CoreNodeStyleType(final int id, final String name, final String description, final SegmentStyleType defaultSegmentStyle) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.defaultSegmentStyle = defaultSegmentStyle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInternal() {
        return id <= 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SegmentStyleType getDefaultSegmentStyleType() {
        return defaultSegmentStyle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGroupName() {
        return "core:node-style-type";
    }
}
