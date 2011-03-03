package org.jdesktop.wonderland.modules.world.common.xml.tags;

import java.io.FileFilter;
import org.jdesktop.wonderland.modules.world.server.converter.ModuleMetaData;
import org.jdesktop.wonderland.modules.world.server.converter.ServerMetaData;

/**
 * This is an abstract base class which extends the SimpleTagContentHandlerAdapter
 * to add module conversion specific functionality such as the ability to 
 * initialize for module conversion and support for a DependentResourceNotificationListener
 *
 * @author Carl Jokl
 */
public abstract class SimpleModuleConversionTagContentHandler extends SimpleTagContentHandlerAdapter implements DependencyListenable, ModuleConversionInitializable {

    protected String moduleName;
    private DependentResourceNotificationListener dependentResourceListener;

    /**
     * Protected constructor to be called by implementing classes. This feeds constructor parameters
     * down to the base constructor.
     *
     * @param handledFiles A FileFilter which filters out files with which this TagContentHandler is designed to work.
     * @param handledTags The XML tags which this TagContentHandler is designed to handle.
     */
    protected SimpleModuleConversionTagContentHandler(FileFilter handledFiles, String... handledTags) {
        super(handledFiles, handledTags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean init(ModuleMetaData moduleMetaData, ServerMetaData serverMetaData) {
        moduleName = moduleMetaData.getModuleName();
        return moduleName != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setDependencyListener(DependentResourceNotificationListener dependencyListener) {
        dependentResourceListener = dependencyListener;
        return true;
    }

    /**
     * Method which can be called by the implementing class to inform the DependentResourceNotificationListener
     * (if set) that the XML file depends on an external resource.
     *
     * @param originalURI The URI of the current original location of the resource.
     * @param destinationURI The URI of the destination location to which the resource is expected to be moved.
     */
    protected void dependencyFound(String originalURI, String destinationURI) {
        if (dependentResourceListener != null) {
            dependentResourceListener.dependencyFound(originalURI, destinationURI);
        }
    }

    /**
     * Method which can be called by the implementing class to inform the DependentResourceNotificationListener
     * (if set) that the XML file may depend on the specified optional external resource.
     *
     * @param checkURI The URI of the location where the optional resource can be found if it exists.
     * @param destinationURI The URI of the destination location to which the resource is expected to be moved if it exists.
     */
    protected void optionalDependencyCheck(String checkURI, String destinationURI) {
        if (dependentResourceListener != null) {
            dependentResourceListener.optionalDependencyCheck(checkURI, destinationURI);
        }
    }
}
