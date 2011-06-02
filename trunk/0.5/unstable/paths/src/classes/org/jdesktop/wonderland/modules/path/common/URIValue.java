package org.jdesktop.wonderland.modules.path.common;

import java.io.Serializable;
import java.net.URI;

/**
 * This interface represents an object which holds a URL.
 *
 * @author Carl Jokl
 */
public interface URIValue extends Serializable {

    /**
     * Get the URL value contained within this URIValue.
     * 
     * @return A URL which is contained within this URIValue object or null
     *         if the URL is not set.
     */
    public URI getURI();

    /**
     * Get whether the URI value contained within this URIValue is set.
     *
     * @return True if the URI value within this URIValue is not null. False if the URL String is null.
     */
    public boolean isURISet();
}
