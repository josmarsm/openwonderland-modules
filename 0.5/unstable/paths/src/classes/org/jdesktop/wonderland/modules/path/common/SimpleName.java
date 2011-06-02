package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;

/**
 * This name is a simple wrapper around a name String.
 * Other uses of the Named interface can provide indirection
 * when using proxy objects. This class provides the Name value
 * locally stored internally, however the indirection means that
 * more than one object could share the same SimpleName instance
 * and if the SimpleName is updated then the change is reflected
 * everywhere the instance is used.
 *
 * @author Carl Jokl
 */
public class SimpleName implements MutableNamed, Comparable<Named>, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private String name;

    /**
     * Create a new instance of s SimpleName which has no value.
     */
    public SimpleName() {
        name = null;
    }

    /**
     * Create a new instance of a SimpleName which has the specified value.
     *
     * @param name The name value which this SimpleName object is to contain.
     */
    public SimpleName(String name) {
        this.name = name;
    }

    /**
     * Get the name value.
     *
     * @return The name value.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the name value.
     *
     * @param name The name value.
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Whether the name value of this SimpleName is set.
     *
     * @return True if the name value of this SimpleName is set.
     */
    @Override
    public boolean isNamed() {
        return name != null;
    }

    /**
     * Compare this name to another name using basic string comparison logic.
     *
     * @param named The other Named instance with which this can be compared.
     * @return Positive if the specified name is greater than this name, zero if equal and
     *         negative if the specified name is less than the name of this object.
     */
    @Override
    public int compareTo(Named named) {
        if (named == null) {
            return 1;
        }
        else if (name == null) {
            return -1;
        }
        else {
            String otherName = named.getName();
            if (otherName == null) {
                return 1;
            }
            else {
                return name.compareTo(otherName);
            }
        }
    }

    /**
     * Create a clone of this SimpleName.
     *
     * @return The clone of this SimpleName.
     */
    @Override
    protected Object clone() {
        return new SimpleName(name);
    }

    /**
     * Check whether the other object is a SimpleName which is equal to this SimpleName.
     *
     * @param obj The object to be checked for equality against this SimpleName.
     * @return True if the specified Object is a SimpleName which is equivalent to this Object.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimpleName) {
            String otherName = ((SimpleName) obj).getName();
            return otherName == null ? name == null : otherName.equals(name);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    /**
     * Get a String representation of this SimpleName.
     *
     * @return The name value of this SimpleName.
     */
    @Override
    public String toString() {
        return name;
    }    
}
