package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;

/**
 * This class represents a simple description object for use as just a simple wrapper around a String value. This
 * can provide a level of indirection to the value when the value is to be shared between more than one object and
 * should change in both places when a value is updated.
 *
 * @author Carl Jokl
 */
public class SimpleDescription implements MutableDescribed, Comparable<Described>, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private String description;

    public SimpleDescription() {
        description = null;
    }

    /**
     * Create a new SimpleDescription instance to wrap the specified description value.
     *
     * @param description The description value which this SimpleDescription instance is to have.
     */
    public SimpleDescription(String description) {
        this.description = description;
    }

    /**
     * Get the description value.
     *
     * @return A String which is the description value.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Set the description value.
     *
     * @param description A String which is the description value.
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Whether the description value is set i.e. it is not null.
     *
     * @return True if the description value is set to a non null value.
     */
    @Override
    public boolean isDescriptionSet() {
        return description != null;
    }

    /**
     * Compare this SimpleDescription against another Described object for
     * ordering.
     *
     * @param described The Described object to which this object will be compared.
     * @return Positive if the specified Described object is greater than this object.
     *         Negative if the specified Described object is less that this object.
     *         Zero if the specified Described object has the same description as this object.
     */
    @Override
    public int compareTo(Described described) {
        if (described == null) {
            return 1;
        }
        else if (description == null) {
            return -1;
        }
        else {
            String otherDescription = described.getDescription();
            if (otherDescription == null) {
                return 1;
            }
            else {
                return description.compareTo(otherDescription);
            }
        }
    }

    /**
     * Create a clone of this SimpleDescription.
     *
     * @return The clone of this SimpleDescription.
     */
    @Override
    protected Object clone() {
        return new SimpleDescription(description);
    }

    /**
     * Check whether the other object is a SimpleName which is equal to this SimpleName.
     *
     * @param obj The object to be checked for equality against this SimpleName.
     * @return True if the specified Object is a SimpleName which is equivalent to this Object.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimpleDescription) {
            String otherDescription = ((SimpleDescription) obj).getDescription();
            return otherDescription == null ? description == null : otherDescription.equals(description);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return description != null ? description.hashCode() : 0;
    }

    /**
     * Get a String representation of this SimpleDescription.
     *
     * @return The name value of this SimpleDescription.
     */
    @Override
    public String toString() {
        return description;
    }
}
