package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer;
import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.path.common.Disposable;

/**
 * This interface represents a renderer which is used to render child components of a path
 * such as node and segments. This interface contains functionality which is common to all
 * types of child rendering objects.
 *
 * @author Carl Jokl
 */
public interface ChildRenderer extends Disposable {

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

    /**
     * Get the Entity which represents the ChildComponent in the hierarchy.
     * If the Entity has not yet been created by the time this method has been called then it
     * will be created as part of this method call.
     *
     * @return the Entity which is used to represent the child component.
     */
    public Entity getEntity();

    /**
     * Update the child component's scene after some change has occurred requiring the scene to be updated.
     *
     * @return The JME node of the updated scene.
     */
    public Node updateScene();

    /**
     * Notify the renderer of a cell status change
     * @param status
     */
    public void setStatus(CellStatus status,boolean increasing);
}
