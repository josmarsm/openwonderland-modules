package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer;

import java.util.logging.Level;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment.PathSegmentRenderer;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.PathNodeRenderer;
import com.jme.scene.Node;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.client.PathCell;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.PathNodeRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment.PathSegmentRendererFactory;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.common.style.UnsupportedStyleException;
import org.jdesktop.wonderland.modules.path.common.style.node.CoreNodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.CoreSegmentStyleType;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyleType;

/**
 * This renderer is used for rendering a PathCell. The rendering of PathNodes and
 * segments is delegated to child rendering components.
 *
 * @author Carl Jokl
 */
public class PathCellRenderer extends BasicRenderer implements CellRetriever {

    /**
     * Render an individual segment of the NodePath.
     *
     * @param renderingEntity The entity supplied to the createSceneGraph method (representing the NodePath).
     * @param cellRootNode The root JME node for the PathCell.
     * @param parentRenderer The parent JME Renderer of the PathSegmentRenderer which will be created as part of this process.
     * @param cellRetriever The CellRetriever used to get a reference to the overall containing Cell for the NodePath.
     * @param segmentRendererFactory The factory used to create PathSegmentRenderers.
     * @param segmentIndex The index of the segment to be rendered.
     * @param currentNode The current node which is the beginning of the segment to be rendered.
     * @param segmentRenderers The destination list of PathSegmentRenderers which were created to render all the segments of the NodePath.
     *                         The new PathSegmentRenderer created as part of this process will be added to this list.
     * @return True if the segment was able to be rendered successfully.
     */
    private static boolean renderSegment(Entity renderingEntity, Node cellRootNode, CellRendererJME parentRenderer, CellRetriever cellRetriever, PathSegmentRendererFactory segmentRendererFactory, ClientPathNode currentNode, List<PathSegmentRenderer> segmentRenderers) {
        PathSegmentRenderer segmentRenderer = segmentRendererFactory.createRenderer(currentNode);
        if (segmentRenderer != null) {
            segmentRenderer.setParentRenderer(parentRenderer);
            segmentRenderer.setCellRetriever(cellRetriever);
            segmentRenderer.setCollisionEnabled(true);
            segmentRenderer.setPickingEnabled(true);
            //Should cause the scene graph to be created it it hasn't already.
            entityAddChild(renderingEntity, segmentRenderer.getEntity());
            //ToDo: Is this neccecary?
            Node childNode = segmentRenderer.getSceneRoot();
            if (childNode != null) {
                cellRootNode.attachChild(childNode);
            }
            return segmentRenderers == null || segmentRenderers.add(segmentRenderer);
        }
        return false;
    }

    /**
     * Render the individual PathNode in the NodePath being rendered.
     *
     * @param renderingEntity The entity supplied to the createSceneGraph method (representing the NodePath).
     * @param cellRootNode The root JME node for the PathCell.
     * @param parentRenderer The parent JME Renderer of the PathNodeRenderer which will be created as part of this process.
     * @param cellRetriever The CellRetriever used to get a reference to the overall containing Cell for the NodePath.
     * @param nodeRendererFactory The factory used to create PathNodeRenderers.
     * @param node The PathNode to be rendered.
     * @param nodeRenderers The destination list of PathNodeRenderers which were created to render all the PathNodes of the NodePath.
     *                      The new PathNodeRenderer created as part of this process will be added to this list.
     * @return True if the Path Node was able to be rendered successfully.
     */
    private static boolean renderNode(Entity renderingEntity, Node cellRootNode,  CellRendererJME parentRenderer, CellRetriever cellRetriever, PathNodeRendererFactory nodeRendererFactory, ClientPathNode node, List<PathNodeRenderer> nodeRenderers) {
        PathNodeRenderer nodeRenderer = nodeRendererFactory.createRenderer(node);
        if (nodeRenderer != null) {
            nodeRenderer.setParentRenderer(parentRenderer);
            nodeRenderer.setCellRetriever(cellRetriever);
            nodeRenderer.setCollisionEnabled(true);
            nodeRenderer.setPickingEnabled(true);
            //Should cause the scene graph to be created it it hasn't already.
            entityAddChild(renderingEntity, nodeRenderer.getEntity());
            //ToDo: Is this neccecary?
            Node childNode = nodeRenderer.getSceneRoot();
            if (childNode != null) {
                cellRootNode.attachChild(childNode);
            }
            return nodeRenderers.add(nodeRenderer);
        }
        return false;
    }

    private Node cellRootNode;
    private PathRendererFactory rendererFactory;
    private List<PathNodeRenderer> nodeRenderers;
    private List<PathSegmentRenderer> segmentRenderers;
    //private PathEventListener listener;

    /**
     * Create a new PathCellRenderer instance to render the specified PathCell.
     *
     * @param cell The PathCell to be rendered.
     */
    public PathCellRenderer(PathCell cell) {
        super(cell);
        rendererFactory = createRendererFactory();
        final int noOfNodes = cell != null ? cell.getNodePath().noOfNodes() : 0;
        nodeRenderers = new ArrayList<PathNodeRenderer>(noOfNodes);
        segmentRenderers = new ArrayList<PathSegmentRenderer>(noOfNodes);
    }

    protected final PathRendererFactory createRendererFactory() {
        //ToDo: Replace this implementation with another capable of detecting available renderers.
        return new NonDetectingPathRendererFactory();
    }

    /**
     * Get the PathRendererFactory in use within this PathCellRenderer.
     *
     * @return The PathRendererFactory which is being used by this PathCellRenderer.
     */
    public PathRendererFactory getRendererFactory() {
        return rendererFactory;
    }

    /**
     * Get the root JME node which represents the NodePath and to which the rendered PathNodes and segments
     * are added in the screen graph.
     *
     * @return The root JME node which represents the NodePath.
     */
    public Node getCellRootNode() {
        return cellRootNode;
    }

    /**
     * This is an internal method to get hold of a ClientPathNode in the rendered NodePath
     * at the specified index. If the nodeIndex is invalid, this internal method returns
     * null rather than throwing an IndexOutOfBoundsException which is done for public
     * methods.
     *
     * @param nodeIndex The index of the ClientPathNode to be retrieved.
     * @return The ClientPathNode at the specified index or null if no NodePath is available or the index is invalid.
     */
    protected ClientPathNode getPathNode(int nodeIndex) {
        if (cell instanceof PathCell) {
            ClientNodePath path = ((PathCell) cell).getNodePath();
            if (nodeIndex >= 0 && nodeIndex < path.noOfNodes()) {
                return path.getPathNode(nodeIndex);
            }
        }
        return null;
    }

    /**
     * Update the PathNodeRenderer at the specified index to take into
     * account any changes to the PathNode.
     *
     * @param nodeIndex The index of the PathNodeRenderer to be updated.
     * @return True if the specified PathNodeRenderer was able to be updated.
     */
    private boolean updatePathNodeRenderer(int nodeIndex) {
        if (cell instanceof PathCell && noOfRenderersEqualsNodes() && nodeIndex >= 0 && nodeIndex < nodeRenderers.size()) {
            PathNodeRenderer nodeRenderer = nodeRenderers.get(nodeIndex);
            NodeStyleType renderedNodeStyleType = nodeRenderer.getRenderedType();
            ClientNodePath path = ((PathCell) cell).getNodePath();
            ClientPathNode node = path.getPathNode(nodeIndex);
            NodeStyleType actualNodeStyleType = node.getStyleType();
            cellRootNode.detachChild(nodeRenderer.getSceneRoot());
            if ((path.isEditMode() && renderedNodeStyleType == CoreNodeStyleType.EDIT_MODE) || (renderedNodeStyleType != null && renderedNodeStyleType.equals(actualNodeStyleType))) {
                cellRootNode.attachChild(nodeRenderer.updateScene());
            }
            else {
                nodeRenderer.dispose();
                nodeRenderer = getRenderer(node);
                nodeRenderer.getEntity();
                cellRootNode.attachChild(nodeRenderer.getSceneRoot());
                nodeRenderers.set(nodeIndex, nodeRenderer);
            }
            return true;
        }
        return false;
    }

    /**
     * Update the PathSegmentRenderer at the specified index to take into account any changes to
     * the style or attached PathNodes.
     *
     * @param segmentIndex The index of the PathSegment to be updated.
     * @return True if the specified PathSegmentRenderer was able to be updated.
     */
    private boolean updatePathSegmentRenderer(int segmentIndex) {
        if (cell instanceof PathCell && noOfRenderersEqualsSegments() && segmentIndex >= 0 && segmentIndex < nodeRenderers.size()) {
            PathSegmentRenderer segmentRenderer = segmentRenderers.get(segmentIndex);
            SegmentStyleType renderedSegmentStyleType = segmentRenderer.getRenderedType();
            ClientNodePath path = ((PathCell) cell).getNodePath();
            SegmentStyleType actualNodeStyleType = path.getSegmentStyleType(segmentIndex);
            cellRootNode.detachChild(segmentRenderer.getSceneRoot());
            if ((path.isEditMode() && renderedSegmentStyleType == CoreSegmentStyleType.EDIT_MODE) || (renderedSegmentStyleType != null && renderedSegmentStyleType.equals(actualNodeStyleType))) {
                cellRootNode.attachChild(segmentRenderer.updateScene());
            }
            else {
                segmentRenderer.dispose();
                segmentRenderer = getRenderer(segmentIndex);
                segmentRenderer.getEntity();
                cellRootNode.attachChild(segmentRenderer.getSceneRoot());
                segmentRenderers.set(segmentIndex, segmentRenderer);
            }
            return true;
        }
        return false;
    }

    /**
     * Whether the PathCell is set within this PathCellRenderer.
     *
     * @return True if the PathCell is set within the PathCellRenderer.
     */
    public boolean isPathCellSet() {
        return cell != null;
    }

    /**
     * Whether the root JME node which represents the NodePath is set.
     *
     * @return True if the JME node which represents the NodePath is set.
     */
    public boolean isCellRootNodeSet() {
        return cellRootNode != null;
    }

    /**
     * Whether the current number of PathNodeRenderers is equal to the current number of PathNodes.
     * If the two are not equal this can be an indication that a PathNode has been added or removed
     * but the graphical rendering has not yet been updated to take this into account.
     *
     * @return True if the number of PathNodeRenderers is equal to the number of PathNodes. False
     *         if the two are not equal.
     */
    public boolean noOfRenderersEqualsNodes() {
        return noOfNodes() == nodeRenderers.size();
    }

    /**
     * Whether the current number of PathNodeRenderers is equal to the current number of PathNodes.
     * If the two are not equal this can be an indication that a PathNode has been added or removed
     * but the graphical rendering has not yet been updated to take this into account.
     *
     * @return True if the number of PathNodeRenderers is equal to the number of PathNodes. False
     *         if the two are not equal.
     */
    public boolean noOfRenderersEqualsSegments() {
        return noOfNodes() == nodeRenderers.size();
    }

    /**
     * The number of PathNodes in the NodePath rendered by this PathCellRenderer.
     *
     * @return The number of PathNodes in this NodePathRenderer.
     */
    public int noOfNodes() {
        return cell instanceof PathCell ? ((PathCell) cell).getNodePath().noOfNodes() : 0;
    }

    /**
     * This method creates an appropriate PathNodeRendererFactory for the specified ClientPathNode.
     * Any exceptions produced as part of the process are logged and null will be silently returned.
     *
     * @param node The ClientPathNode for which to create a PathNodeRendererFactory.
     * @return A PathNodeRendererFactory for the specified ClientPathNode or null if one failed to be created.
     */
    public PathNodeRendererFactory getRendererFactory(ClientPathNode node) {
        if (node != null && rendererFactory != null) {
            try {
                return rendererFactory.getNodeRendererFactory(node.getStyleType());
            }
            catch (IllegalArgumentException iae) {
                logger.log(Level.SEVERE, "Failed to create PathNodeRendererFactory due to an illegal argument!", iae);
            }
            catch (UnsupportedStyleException use) {
                logger.log(Level.SEVERE, "Failed to create PathNodeRendererFactory due to the NodeStyleType of the specified PathNode not being supported on this client!", use);
            }
        }
        return null;
    }

    /**
     * This method creates an appropriate PathSegmentRendererFactory for the segment of the specified ClientNodePath with the specified segment index.
     * Any exceptions produced as part of the process are logged and null will be silently returned.
     *
     * @param segmentIndex The index of the NodePath segment for which to get a PathSegmentRenderer. This index should be the same as the PathNode index of the ClientPathNode at
     *                     the start of the segment.
     * @return A PathSegmentendererFactory for the specified ClientNodePath segment or null if one failed to be created.
     */
    public PathSegmentRendererFactory getRendererFactory(int segmentIndex) {
        if (cell instanceof PathCell) {
            ClientNodePath path = ((PathCell) cell).getNodePath();
            if (rendererFactory != null && segmentIndex >= 0 && segmentIndex < path.noOfNodes()) {
                try {
                    return rendererFactory.getSegmentRendererFactory(path.getSegmentStyleType(segmentIndex));
                }
                catch (IllegalArgumentException iae) {
                    logger.log(Level.SEVERE, "Failed to create PathSegmentRendererFactory due to an illegal argument!", iae);
                }
                catch (UnsupportedStyleException use) {
                    logger.log(Level.SEVERE, "Failed to create PathSegmentRendererFactory due to the SegmentStyleType of the specified NodePath segment not being supported on this client!", use);
                }
            }
        }
        return null;
    }

    /**
     * This method creates an appropriate PathNodeRenderer for the specified ClientPathNode.
     * Any exceptions produced as part of the process are logged and null will be silently returned.
     *
     * @param node The ClientPathNode for which to create a PathNodeRenderer.
     * @return A PathNodeRenderer for the specified ClientPathNode or null if one failed to be created.
     */
    public PathNodeRenderer getRenderer(ClientPathNode node) {
        if (node != null && rendererFactory != null) {
            PathNodeRendererFactory nodeRendererFactory = getRendererFactory(node);
            return nodeRendererFactory != null ? nodeRendererFactory.createRenderer(node) : null;
        }
        return null;
    }

    /**
     * This method creates an appropriate PathSegmentRenderer for the segment of the specified ClientNodePath with the specified segment index.
     * Any exceptions produced as part of the process are logged and null will be silently returned.
     *
     * @param segmentIndex The index of the NodePath segment for which to get a PathSegmentRenderer. This index should be the same as the PathNode index of the ClientPathNode at
     *                     the start of the segment.
     * @return A PathSegmentRenderer for the specified ClientNodePath or null if one failed to be created.
     */
    public PathSegmentRenderer getRenderer(int segmentIndex) {
        PathSegmentRendererFactory segmentRendererFactory = getRendererFactory(segmentIndex);
        if (segmentRendererFactory != null) {
            return segmentRendererFactory.createRenderer(getPathNode(segmentIndex));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node createSceneGraph(Entity entity) {
        if (cellRootNode != null) {
            cellRootNode.detachAllChildren();
            cellRootNode = null;
            for (PathNodeRenderer renderer : nodeRenderers) {
                renderer.dispose();
            }
            nodeRenderers.clear();
            for (PathSegmentRenderer renderer : segmentRenderers) {
                renderer.dispose();
            }
            segmentRenderers.clear();
        }
        cellRootNode = new Node(String.format("Path Cell: %s (%s)", cell.getName(), cell.getCellID().toString()));
        if (cell != null) {
            if (cell instanceof PathCell) {
                ClientNodePath path = ((PathCell) cell).getNodePath();
                final int noOfNodes = path.noOfNodes();
                //int segmentIndex = 0;
                PathNodeRendererFactory currentNodeRendererFactory = null;
                PathSegmentRendererFactory currentSegmentRendererFactory = null;
                ClientPathNode currentNode = null;
                if (path.isEditMode()) {
                    currentSegmentRendererFactory = rendererFactory.getEditSegmentRendererFactory();
                    currentNodeRendererFactory = rendererFactory.getEditNodeRendererFactory();
                    for (int nodeIndex = 0; nodeIndex < noOfNodes; nodeIndex++) {
                        currentNode = path.getPathNode(nodeIndex);
                        renderSegment(entity, cellRootNode, this, this, currentSegmentRendererFactory, currentNode, segmentRenderers);
                        renderNode(entity, cellRootNode, this, this, currentNodeRendererFactory, currentNode, nodeRenderers);
                    }
                }
                else if (noOfNodes > 0) {
                    PathStyle pathStyle = path.getPathStyle();
                    NodeStyle currentNodeStyle = null;
                    SegmentStyle currentSegmentStyle = null;
                    for (int nodeIndex = 0; nodeIndex < noOfNodes; nodeIndex++) {
                        currentNode = path.getPathNode(nodeIndex);
                        try {
                            currentSegmentStyle = pathStyle.getSegmentStyle(currentNode.getSequenceIndex(), true);
                            currentSegmentRendererFactory = currentSegmentStyle != null ? rendererFactory.getSegmentRendererFactory(currentSegmentStyle.getStyleType()) : null;
                            if (currentSegmentRendererFactory != null) {
                                renderSegment(entity, cellRootNode, this, this, currentSegmentRendererFactory, currentNode, segmentRenderers);
                            }
                            currentNodeStyle = pathStyle.getNodeStyle(currentNode.getSequenceIndex(), true);
                            currentNodeRendererFactory = currentNodeStyle != null ? rendererFactory.getNodeRendererFactory(currentNodeStyle.getStyleType()) : null;
                            if (currentNodeRendererFactory != null) {
                                renderNode(entity, cellRootNode, this, this, currentNodeRendererFactory, currentNode, nodeRenderers);
                            }   
                        }
                        catch (IllegalArgumentException iae) {
                            logger.log(Level.SEVERE, "Error with argument when rendering node or segment!", iae);
                        }
                        catch (UnsupportedStyleException use) {
                            logger.log(Level.SEVERE, "Segment style not supported while rendering node or segment!", use);
                        }
                    }
                }
            }
        }
        //cellRootNode.setModelBound(new BoundingBox());
        //cellRootNode.updateModelBound();
        //cellRootNode.setIsCollidable(false);
        return cellRootNode;
    }

    /**
     * Update the graphical representation of the PathNode at the specified index within the path.
     *
     * @param index The index of the PathNode which needs to have it's graphical representation updated.
     * @param updateAttachedSegments Whether the segments attached to the node should also be updated. This should be true
     *                               in cases such as when a PathNode is moved and both the PathNode and the segments
     *                               which attach to it need to be redrawn.
     * @return True if the request to update the PathNode at the specified index was submitted successfully.
     */
    public boolean updatePathNode(int index, boolean updateAttachedSegments) {
        if (index >= 0 && index < noOfNodes()) {
            SceneWorker.addWorker(new PathNodeUpdateWorker(this, index, updateAttachedSegments));
            return true;
        }
        return false;
    }

    /**
     * Update the graphical representation of the path segment with the specified index.
     *
     * @param index The index of the segment to be updated.
     * @return True if the request to update to the path segment was updated successfully.
     */
    public boolean updatePathSegment(int index) {
        return false;
    }

    /**
     * Update the graphical representation of the entire path.
     *
     * @return True if the request to update the graphical representation of the NodePath is submitted successfully.
     */
    public boolean updatePath() {
        SceneWorker.addWorker(new PathUpdateWorker(this));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        for (ChildRenderer renderer : nodeRenderers) {
            renderer.setStatus(status, increasing);
        }
        for (ChildRenderer renderer : segmentRenderers) {
            renderer.setStatus(status, increasing);
        }
        /*Entity pathEntity = getEntity();
        if (pathEntity != null) {
           if (status == CellStatus.INACTIVE && !increasing && listener != null) {
                listener.removeFromEntity(entity);
                listener.dispose();
                listener = null;
           }
           else if (status == CellStatus.RENDERING && increasing && listener == null) {
                listener = new PathEventListener((PathCell) cell);
                listener.addToEntity(entity);
           }
        }*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cell getContainingCell() {
        return cell;
    }

    /**
     * This class is used for performing graphical updates of PathNodes on the proper
     * graphics thread.
     */
    private static class PathNodeUpdateWorker implements WorkCommit {

        /**
         * This method is used to replace a PathCellRenderer with one which is updated to the latest NodeStyleType.
         *
         * @param owner The owning PathCellRenderer for this operation.
         * @param nodeRenderer
         * @param cellRootNode
         * @param node
         * @param nodeRenderers
         */
        private static void replaceChildRenderer(PathCellRenderer owner, PathNodeRenderer nodeRenderer, Node cellRootNode, ClientPathNode node, List<PathNodeRenderer> nodeRenderers) {
            nodeRenderer.dispose();
            nodeRenderer = owner.getRenderer(node);
            nodeRenderer.getEntity();
            cellRootNode.attachChild(nodeRenderer.getSceneRoot());
            nodeRenderers.set(node.getSequenceIndex(), nodeRenderer);
        }

        private PathCellRenderer owner;
        private int nodeIndex;
        private boolean updateAttachedSegments;

        /**
         * Create a new PathNodeUpdateWorker to perform a graphical update of the node
         * at the specified index.
         *
         * @param owner The owning PathCellRender which needs to render the PathNode update.
         * @param nodeIndex The index of the PathNode to be updated.
         * @param updateAttachedSegments Whether the segments attached to the node should also be updated.
         */
        public PathNodeUpdateWorker(PathCellRenderer owner, int nodeIndex, boolean updateAttachedSegments) {
            this.owner = owner;
            this.nodeIndex = nodeIndex;
            this.updateAttachedSegments = updateAttachedSegments;
        }

        /**
         * Commit update changes to the PathCell rendering.
         */
        @Override
        public void commit() {
            if (owner != null && owner.isCellRootNodeSet() && nodeIndex >= 0 && nodeIndex < owner.noOfNodes() && owner.noOfRenderersEqualsNodes()) {
                owner.updatePathNodeRenderer(nodeIndex);
                if (updateAttachedSegments) {
                    owner.updatePathSegmentRenderer(nodeIndex);
                    ClientPathNode node = owner.getPathNode(nodeIndex);
                    if (node != null && node.hasPrevious()) {
                        owner.updatePathSegmentRenderer(node.getPreviousIndex());
                    }
                }
            }
        }
    }

    /**
     * This class is used for performing graphical updates of the NodePath on the proper
     * graphics thread.
     */
    private static class PathUpdateWorker implements WorkCommit {

        private PathCellRenderer owner;

        /**
         * Create a new PathNodeUpdateWorker to perform a graphical update of the entire NodePath on the
         * graphics thread.
         *
         * @param owner The owning PathCellRender which needs to render the PathNode update.
         */
        public PathUpdateWorker(PathCellRenderer owner) {
            this.owner = owner;
        }

        /**
         * Commit update changes to the PathCell rendering.
         */
        @Override
        public void commit() {
            if (owner != null) {
                owner.cleanupSceneGraph(owner.getEntity());
                owner.createSceneGraph(owner.getEntity());
            }
        }
    }
}
