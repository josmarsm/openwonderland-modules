package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;

/**
 * This interface is a common interface for all types of style whether the styles types be for PathNodes or segments or
 * anything else.
 *
 * @author Carl Jokl
 */
public interface StyleType extends Serializable {

    /**
     * Whether this StyleType is an internal StyleType which is not
     * intended to be user selectable. This would cover StyleTypes such
     * as the edit mode style types.
     *
     * @return True if this StyleType is internal and not intended to be user selectable.
     */
    public boolean isInternal();

    /**
     * Get the integer id value which identifies this StyleType. The id is should be globally unique for the sake of ease of
     * serialization and de-serialization.
     *
     * @return The integer id value which represents the StyleType. This value is used in serialization and de-serialization.
     */
    public int getId();

    /**
     * Get a user friendly name for the StyleType.
     *
     * @return A string containing a user friendly name for the StyleType.
     */
    public String getName();

    /**
     * Get a friendly description of what the StyleType represents.
     *
     * @return A friendly description which can be presented to the user which states what the StyleType represents.
     */
    public String getDescription();

    /**
     * Get the name of the StyleType group for which this is a StyleType.
     * This is generally node style or segment style etc. This interface
     * is generally extended by a more specific type but the group name
     * can be used in serialization to make it easier to serialize and
     * de-serialize style type generically.
     *
     * @return The name of the StyleType group identifying the specific group of StyleTypes to which this
     *         StyleType belongs.
     */
    public String getGroupName();
}
