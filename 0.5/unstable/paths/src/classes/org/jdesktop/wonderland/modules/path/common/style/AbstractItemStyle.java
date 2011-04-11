package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;

/**
 * This is an abstract base class for ItemStyles which
 * implements some common functionality.
 *
 * @author Carl Jokl
 */
public abstract class AbstractItemStyle<T extends StyleType> implements ItemStyle<T>, Serializable {

    /**
     * The version number for serialization.
     */
    private static final long serialVersionUID = 1L;

    private int span;

    /**
     * Initialize this AbstractItemStyle with the default span of 1.
     */
    protected AbstractItemStyle() {
        span = 1;
    }

    /**
     * Initialize this AbstractItemStyle to span the specified number of items.
     *
     * @param span The number of items which are to be spanned by this style which must be 1 or greater.
     * @throws IllegalArgumentException If the specified span is not greater than zero.
     */
    protected AbstractItemStyle(int span) throws IllegalArgumentException {
        if (span > 0) {
            this.span = span;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified span %d is invalid because the span must be greater than zero!", span));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int span() {
        return span;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpan(int span) throws IllegalArgumentException {
        if (span > 0) {
            this.span = span;
        }
        else {
            throw new IllegalArgumentException(String.format("The specified span %d is invalid because the span must be greater than zero!", span));
        }
    }
}
