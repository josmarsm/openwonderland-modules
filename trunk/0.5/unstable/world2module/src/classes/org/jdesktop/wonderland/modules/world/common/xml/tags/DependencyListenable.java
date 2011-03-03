package org.jdesktop.wonderland.modules.world.common.xml.tags;

/**
 * This interface represents an item such as a TagContentHandler which can
 * have a DependentResourceNotificationListener registered on it to be notified
 * when the component detects dependencies such as dependent files which need to
 * be copied with the main file.
 *
 * @author Carl Jokl
 */
public interface DependencyListenable {

    /**
     * Set the DependentResourceNotificationListener to listen for notifications of resources
     * which are identified in an XML document as a dependent resource.
     *
     * @param dependencyListener The listener which is notified about dependent resources.
     * @return True if the DependentResourceNotificationListener was able to be set or false
     *         if this TagContentHandler does not support having a DependentResourceNotificationListener.
     */
    public boolean setDependencyListener(DependentResourceNotificationListener dependencyListener);
}
