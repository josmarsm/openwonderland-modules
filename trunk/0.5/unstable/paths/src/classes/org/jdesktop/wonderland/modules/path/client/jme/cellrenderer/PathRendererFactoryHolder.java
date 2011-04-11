package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer;

/**
 * This interface represents any object which holds or is able to get hold of the current PathRendererFactory.
 * This is particularly useful for PathNodes to use to create new rendering object instances for themselves if their style
 * changes.
 *
 * @author Carl Jokl
 */
public interface PathRendererFactoryHolder {

    /**
     * Get the current PathRendererFactory.
     *
     * @return The PathRendererFactory retrieved from or through this object.
     */
    public PathRendererFactory getPathRendererFactory();
}
