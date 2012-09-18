package org.jdesktop.wonderland.modules.path.common;

import java.net.URI;

/**
 * This interface represents a URIValue for which the contained URL can be changed and set to different values.
 *
 * @author Carl Jokl
 */
public interface MutableURIValue extends URIValue {

    /**
     * Set the URI value of this URIValue.
     *
     * @param url URI to which this URIValue will be set.
     */
    public void setURI(URI uri);
}
