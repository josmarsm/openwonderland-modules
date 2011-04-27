package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment;

import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.path.common.Disposable;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This interface is used to define a class which is responsible for rendering a segment of a path.
 *
 * @author Carl Jokl
 */
public interface PathSegmentRenderer extends Disposable {

    /**
     * Create the ScreenGraph for the the ClientPathNode contained within this
     * PathNodeRenderer.
     *
     * @param entity The Multi Threaded Game Entity used to represent this ClientPathNode in the Entity Hierarchy.
     * @return A JME node of the SceneGraph of objects used to render this ClientPathNode.
     */
    public Node createSceneGraph(Entity entity);

    /**
     * Get SegmentStyleType which this PathSegmentRenderer is used to render.
     *
     * @return The SegmentStyleType of the type of segment which this PathSegmentRenderer
     *         is intended to render. This method can return null if the PathSegmentRenderer
     *         is not specific to any given SegmentStyleType.
     */
    public SegmentStyleType getRenderedType();

    /**
     * Inform the PathSegmentRenderer that the CellStatus of the Cell in which it is contained has
     * changed. This can be used to perform optimizations as needed.
     *
     * @param status The new CellStatus which the Cell is to have.
     * @param increasing Whether the CellStatus is increasing to a more active state or decreasing to
     *                   a less active state.
     */
    public void statusChanged(CellStatus status, boolean increasing);

    /**
     * Get the current renderer Entity which represents the path segment in the object hierarchy.
     *
     * @return The entity which represents the path segment in the object hierarchy or null if none
     *         currently exists.
     */
    public Entity getEntity();
}
