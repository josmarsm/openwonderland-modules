package org.jdesktop.wonderland.modules.world.common.xml.tags;

import org.jdesktop.wonderland.modules.world.common.xml.tags.TagContentHandler;
import java.util.Collection;

/**
 * This interface represents an object which can load TagContentHandlers. The idea of this interface is that
 * it may not be possible to know in this module about all possible TagContentHandlers which are needed in order
 * to support creating a module from a snapshot. 
 * 
 * A default implementation of this interface will exist for the known TagContentHandlers but a different TagHandlerLoader
 * can be implemented which handles other TagContentHandlers for tags which this module is not aware of.
 *
 * @author Carl Jokl
 */
public interface TagHandlerLoader {

    /**
     * Load all the TagContentHandlers which the current Wonderland Server instance supports for use in exporting snapshots
     * and worlds.
     * 
     * @return A collection of all the loaded tag content handlers.
     */
    public Collection<TagContentHandler> loadTagConentHandlers();
}
