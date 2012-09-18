package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;
import java.net.URI;

/**
 * This class represents a simple default implementation of a URIValue. The indirection which instances of this
 * object provides means that more than one object can share a reference to the same URL value which will be updated
 * in both places when a change is made.
 *
 * @author Carl Jokl
 */
public class SimpleURIValue implements MutableURIValue, Comparable<URIValue>, Cloneable, Serializable {

    private URI uri;

    /**
     * Create a new instance of a blank / unset URIValue.
     */
    public SimpleURIValue() {

    }

    /**
     * Create a new instance of a URIVale with the specified URI set within it.
     *
     * @param uri A URI value which is the value which this URIValue is to have.
     */
    public SimpleURIValue(URI uri) {
        this.uri = uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURI(URI uri) {
        this.uri = uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getURI() {
        return uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isURISet() {
        return uri != null;
    }

    /**
     * Compare the specified TextValue to this TextValue.
     */
    @Override
    public int compareTo(URIValue otherValue) {
        if (otherValue != null && otherValue.isURISet()) {
            if (uri != null) {
                return uri.toString().compareTo(otherValue.getURI().toString());
            }
            else {
                return otherValue.isURISet() ? 1 : 0;
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new SimpleURIValue(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimpleURIValue) {
            SimpleURIValue otherValue = (SimpleURIValue) obj;
            return (uri != null ? uri.equals(otherValue.getURI()) : !otherValue.isURISet());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return uri != null ? uri.hashCode() : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return uri != null ? uri.toString() : "";
    }
}
