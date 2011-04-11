package org.jdesktop.wonderland.modules.path.common.style;

import java.io.Serializable;

/**
 * This is a common base interface for the styles of PathNodes and the segments between the nodes.
 *
 * @author Carl Jokl
 */
public interface ItemStyle<T extends StyleType> extends Serializable {

    /**
     * The number of item over which the style spans.
     *
     * @return The number of items over which the style spans.
     */
    public int span();

    /**
     * Set the number of items over which the style spans.
     *
     * @param span The number of items over which the style spans which must be
     * @throws IllegalArgumentException If the specified span is not 1 or greater.
     */
    public void setSpan(int span) throws IllegalArgumentException;

    /**
     * Get the type of style which this ItemStyle represents.
     *
     * @return A StyleType instance (or generic subclass) which represents the type of style
     *         which this ItemStyle represents.
     */
    public T getStyleType();
}
