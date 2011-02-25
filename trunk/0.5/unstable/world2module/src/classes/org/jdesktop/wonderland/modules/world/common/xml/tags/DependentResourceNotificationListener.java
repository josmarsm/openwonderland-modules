package org.jdesktop.wonderland.modules.world.common.xml.tags;

/**
 * This interface represents a listener which is notified of resources upon which
 * a processed XML file has a dependency. When any content is identified in an XML
 * file which shows a dependency on some external file or resource this listener
 * should be informed of the dependency.
 * 
 * When converting a world to a module for example the Wonderland file system
 * includes xml files which frequently are linked to art.
 *
 * @author Carl Jokl
 */
public interface DependentResourceNotificationListener {

    /**
     * Method which is called when a dependency is identified.
     * 
     * @param originalURI This is the URI of the resource as it is currently stored.
     * @param destinationURI This is the URI to which the resource should be moved.
     */
    public void dependencyFound(String originalURI, String destinationURI);

    /**
     * Method which is called to suggest a location file/directory which should
     * be checked as an optional dependency. This location may not exist but if
     * it does exist then it is a dependency.
     *
     * @param checkURI The URI to be checked for the presence of a file/directory
     *                 and if it exists then it is a dependency.
     * @param destinationURI If the resource exist then this is the destinationURI
     *                       to use to store it.
     */
    public void optionalDependencyCheck(String checkURI, String destinationURI);
}
