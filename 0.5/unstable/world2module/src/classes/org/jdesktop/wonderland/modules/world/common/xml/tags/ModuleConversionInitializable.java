package org.jdesktop.wonderland.modules.world.common.xml.tags;

import org.jdesktop.wonderland.modules.world.server.converter.ModuleMetaData;
import org.jdesktop.wonderland.modules.world.server.converter.ServerMetaData;

/**
 * This interface is used with module conversion components
 * such as TagContentHandlers which need to be initialized with
 * module and server setting in order to be able to carry out the
 * conversion.
 *
 * @author Carl Jokl
 */
public interface ModuleConversionInitializable {

    /**
     * Due to the fact that components such as TagContentHandlers may need to be
     * implemented with no-argument constructors in order to load them dynamically
     * from modules via reflection this method is provided to perform any needed
     * initialisation for the TagContentHandler instead of the constructor (if any is needed).
     *
     * @param moduleMetaData The meta-data about the Module to be created which may be needed by the TagContentHandler.
     * @param serverMetaData The meta-data about the Wonderland server on which may be needed by the TagContentHandler.
     * @return True if the TagContentHandler was successfully initialized.
     */
    public boolean init(ModuleMetaData moduleMetaData, ServerMetaData serverMetaData);
}
