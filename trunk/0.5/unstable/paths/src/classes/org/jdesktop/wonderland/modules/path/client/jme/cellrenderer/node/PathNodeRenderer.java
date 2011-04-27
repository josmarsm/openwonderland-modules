package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node;

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.common.Disposable;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;

/**
 * Implementors of this interface are intended to render PathNodes of a given type.
 *
 * @author Carl Jokl
 */
public interface PathNodeRenderer extends Disposable {

    /**
     * Get the ClientPathNode contained within this PathNodeRenderer.
     * 
     * @return The ClientPathNode contained within this PathNodeRenderer.
     */
    public ClientPathNode getPathNode();

    /**
     * Get the type of PathNode which this PathNodeRenderer is intended to render.
     *
     * @return The NodeStyleType which represents the specific type of PathNode
     *         which this PathNodeRenderer is intended to render.
     */
    public NodeStyleType getRenderedType();

    /**
     * Create the ScreenGraph for the the ClientPathNode contained within this
     * PathNodeRenderer.
     *
     * @param entity The Multi Threaded Game Entity used to represent this ClientPathNode in the Entity Hierarchy.
     * @return A JME node of the SceneGraph of objects used to render this ClientPathNode.
     */
    public Node createSceneGraph(Entity entity);

    /**
     * Inform the PathNodeRenderer that the CellStatus of the Cell in which it is contained has
     * changed. This can be used to perform optimizations as needed.
     *
     * @param status The new CellStatus which the Cell is to have.
     * @param increasing Whether the CellStatus is increasing to a more active state or decreasing to
     *                   a less active state.
     */
    public void statusChanged(CellStatus status, boolean increasing);

    /**
     * Get the current renderer Entity which represents the PathNode in the object hierarchy.
     *
     * @return The entity which represents the PathNode in the object hierarchy or null if none
     *         currently exists.
     */
    public Entity getEntity();
}
