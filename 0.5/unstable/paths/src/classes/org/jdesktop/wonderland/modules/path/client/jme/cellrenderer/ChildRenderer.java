package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer;

import com.jme.scene.Node;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.modules.path.common.Disposable;

/**
 * This interface represents a renderer which is used to render child components of a path
 * such as node and segments. This interface contains functionality which is common to all
 * types of child rendering objects.
 *
 * @author Carl Jokl
 */
public interface ChildRenderer extends CellRendererJME, Disposable {

    /**
     * Return the scene root, this is the node created by createSceneGraph.
     * The BasicRenderer also has a rootNode which contains the cell transform,
     * the rootNode is the parent of the scene root.
     * @return The SceneRoot node for this renderer.
     */
    public Node getSceneRoot();

    /**
     * Set whether the collision for the child component is enabled.
     *
     * @param collisionEnabled True if collision for the child component should be enabled or false otherwise..
     */
    public void setCollisionEnabled(boolean collisionEnabled);

    /**
     * Set whether picking should be enabled for the child component.
     *
     * @param pickingEnabled True if picking for this child component should be enabled or false otherwise.
     */
    public void setPickingEnabled(boolean pickingEnabled);

    /**
     * Set the parent renderer for this ChildRenderer.
     *
     * @param parentRenderer The CellRenderer which is the parent of this ChildRenderer.
     */
    public void setParentRenderer(CellRendererJME parentRenderer);
    /**
     * Set the CellRetriever used to find the Cell in which the child component rendered by this ChildRenderer is contained.
     *
     * @param containedCellRetriever The CellRetriever used to find the Cell which contains the child component rendered by this ChildRenderer.
     */
    public void setCellRetriever(CellRetriever containedCellRetriever);
}
