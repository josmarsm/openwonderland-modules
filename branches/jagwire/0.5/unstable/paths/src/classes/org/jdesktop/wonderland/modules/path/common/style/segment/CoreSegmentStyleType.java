package org.jdesktop.wonderland.modules.path.common.style.segment;

import org.jdesktop.wonderland.modules.path.common.style.node.CoreNodeStyleType;
import java.io.Serializable;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;

/**
 * Different SegmentStyleType of segment styles which can be created.
 *
 * @author Carl Jokl
 */
public enum CoreSegmentStyleType implements SegmentStyleType, Serializable {

    /**
     * This SegmentStyleType represents a path which is not visible such as one used for animation.
     */
    EDIT_MODE( 0, "Edit Mode", "This is a crude display of a path which just draws a line from point to point to show where the path is when editing.", CoreNodeStyleType.EDIT_MODE),

    /**
     * This SegmentStyleType represents a path which is not visible such as one used for animation.
     */
    INVISIBLE( 1, "Invisible Path", "An invisible path which can be followed by an animation.", CoreNodeStyleType.INVISIBLE),

     /**
     * This kind of path represents a fence segment built out of 3D rectangular box blocks between the posts.
     */
    BLOCK( 2, "Block", "A fence segment with rectangular blocks between the posts.", CoreNodeStyleType.SQUARE_POST),

    /**
     * This kind of path represents a fence segment built out of 3D cylindrical poles between the posts.
     */
    POLE( 3, "Pole", "A fence segment with cylindrical poles between the posts.", CoreNodeStyleType.POLE),

    /**
     * This kind of path represents a fence segment built out of images between the posts.
     */
    IMAGE( 4, "Image", "A fence segment with posts and images for the panels.", CoreNodeStyleType.IMAGE),

    /**
     * This kind of path represents a fence segment built out of a between the posts.
     */
    MODEL( 5, "Model", "A fence segment with posts and images for the panels.", CoreNodeStyleType.MODEL),

    /**
     * This SegmentStyleType represents a cordon with tape such as hazard tape or police tape or such
     * as is often used when controlling queues of people.
     */
    TAPE( 20, "Tape", "A barrier made from a tape such as hazard or police tape or queue directing cordons.", CoreNodeStyleType.HAZARD_CONE);

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String description;
    private final NodeStyleType defaultNodeStyleType;

    /**
     * Create a new CoreSegmentStyleType enumeration instance with the specified attributes.
     *
     * @param id The unique id of the SegmentStyleType.
     * @param name The descriptive name of the SegmentStyleType.
     * @param description A description of what the SegmentStyleType represents.
     */
    private CoreSegmentStyleType(final int id, final String name, final String description, final NodeStyleType defaultNodeStyleType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.defaultNodeStyleType = defaultNodeStyleType;
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
    public NodeStyleType getDefaultNodeStyleType() {
        return defaultNodeStyleType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGroupName() {
        return "core:segment-style-type";
    }
}
