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
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.client.PathCell;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.PathNodeRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment.PathSegmentRendererFactory;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.common.style.UnsupportedStyleException;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;

/**
 * This renderer is used for rendering a PathCell. The rendering of PathNodes and
 * segments is delegated to child rendering components.
 *
 * @author Carl Jokl
 */
public class PathCellRenderer extends BasicRenderer implements CellRetriever {

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
        rendererFactory = getRendererFactory();
        nodeRenderers = new ArrayList<PathNodeRenderer>(cell.getNoOfNodes());
        segmentRenderers = new ArrayList<PathSegmentRenderer>(cell.getNoOfNodes());
    }

    protected final PathRendererFactory getRendererFactory() {
        //ToDo: Replace this implementation with another capable of detecting available renderers.
        return new NonDetectingPathRendererFactory();
    }

    private boolean renderSegment(ClientNodePath path, PathSegmentRendererFactory segmentRendererFactory, int segmentIndex, ClientPathNode currentNode) {
        if (currentNode.hasNext()) {
            PathSegmentRenderer segmentRenderer = segmentRendererFactory.createRenderer(path, segmentIndex, currentNode.getSequenceIndex(), currentNode.getNext().getSequenceIndex());
            if (segmentRenderer != null) {
                segmentRenderer.setParentRenderer(this);
                segmentRenderer.setCellRetriever(this);
                Entity segmentEntity = segmentRenderer.getEntity();
                if (addIfNotNull(segmentRenderer.getSceneRoot())) {
                    return segmentRenderers.add(segmentRenderer);
                }
            }
        }
        return false;
    }

    private boolean renderNode(PathNodeRendererFactory nodeRendererFactory, ClientPathNode node) {
        PathNodeRenderer nodeRenderer = nodeRendererFactory.createRenderer(node);
        if (nodeRenderer != null) {
            nodeRenderer.setParentRenderer(this);
            nodeRenderer.setCellRetriever(this);
            Entity nodeEntity = nodeRenderer.getEntity();
            if (addIfNotNull(nodeRenderer.getSceneRoot())) {
                return nodeRenderers.add(nodeRenderer);
            }
        }
        return false;
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
            String name = cell.getCellID().toString();
            if (cell instanceof PathCell) {
                PathCell pathCell = (PathCell) cell;
                List<ClientPathNode> nodes = pathCell.getNodeList();
                final int noOfNodes = nodes.size();
                int segmentIndex = 0;
                PathNodeRendererFactory currentNodeRendererFactory = null;
                PathSegmentRendererFactory currentSegmentRendererFactory = null;
                if (pathCell.isEditMode()) {
                    currentSegmentRendererFactory = rendererFactory.getEditSegmentRendererFactory();
                    currentNodeRendererFactory = rendererFactory.getEditNodeRendererFactory();
                    for (ClientPathNode currentNode : nodes) {
                        if (renderSegment(pathCell, currentSegmentRendererFactory, segmentIndex, currentNode)) {
                            segmentIndex++;
                        }
                        renderNode(currentNodeRendererFactory, currentNode);
                    }
                }
                else if (noOfNodes > 0) {
                    PathStyle pathStyle = pathCell.getPathStyle();
                    NodeStyle currentNodeStyle = null;
                    SegmentStyle currentSegmentStyle = null;
                    for (ClientPathNode currentNode : nodes) {
                        try {
                            if (currentNode.hasNext()) {
                                currentSegmentStyle = pathStyle.getSegmentStyle(segmentIndex, true);
                                currentSegmentRendererFactory = currentSegmentStyle != null ? rendererFactory.getSegmentRendererFactory(currentSegmentStyle.getStyleType()) : null;
                                if (currentSegmentRendererFactory != null) {
                                    if (renderSegment(pathCell, currentSegmentRendererFactory, segmentIndex, currentNode)) {
                                        segmentIndex++;
                                    }
                                }
                            }
                            currentNodeStyle = pathStyle.getNodeStyle(currentNode.getSequenceIndex(), true);
                            currentNodeRendererFactory = currentNodeStyle != null ? rendererFactory.getNodeRendererFactory(currentNodeStyle.getStyleType()) : null;
                            if (currentNodeRendererFactory != null) {
                                renderNode(currentNodeRendererFactory, currentNode);
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
     * Add the specified JME child Node if the Node is not null.
     *
     * @param jmeChildNode the JME child Node to be added if it is not null.
     * @return True if the supplied child node was not null and was able to be
     *         added to the root node of the path rendering screen graph.
     */
    private boolean addIfNotNull(Node jmeChildNode) {
        if (jmeChildNode != null) {
            cellRootNode.attachChild(jmeChildNode);
            return true;
        }
        return false;
    }

    /**
     * Update the representation of this PathCell after a state change.
     */
    public void updateUI() {
        if (cell != null && cellRootNode != null) {
            final String name = cell.getCellID().toString();
            if (cell instanceof PathCell) {
                //final CoreSegmentStyleType shapeType = ((PathCell) cell).getShapeType();
                SceneWorker.addWorker(new PathCellUpdateWorker());
                     /* TriMesh mesh = getShapeMesh(name, shapeType);
                        if (mesh != null) {
                            node.attachChild(mesh);
                            node.setModelBound(getBoundingVolume(shapeType));
                            node.updateModelBound();
                            ClientContextJME.getWorldManager().addToUpdateList(node);
                        } */
            }
            cellRootNode.setName(String.format("Path Cell: %s (%s)", name, cell.getCellID().toString()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        logger.warning(String.format("Status change to: %s increacing: %s in class: %s.", status.name(), Boolean.toString(increasing), getClass().getName()));
        for (PathSegmentRenderer renderer : segmentRenderers) {
            renderer.setStatus(status, increasing);
        }
        for (PathNodeRenderer renderer : nodeRenderers) {
            renderer.setStatus(status, increasing);
        }
        super.setStatus(status, increasing);
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

    private static class PathCellUpdateWorker implements WorkCommit {

        /**
         * Commit update changes to the PathCell rendering.
         */
        @Override
        public void commit() {
            
        }

    }
}
