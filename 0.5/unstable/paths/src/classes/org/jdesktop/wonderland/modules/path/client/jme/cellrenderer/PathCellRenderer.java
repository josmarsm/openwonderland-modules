package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer;

import java.util.logging.Level;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.segment.PathSegmentRenderer;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.PathNodeRenderer;
import com.jme.scene.Node;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.path.client.ClientPathNode;
import org.jdesktop.wonderland.modules.path.client.PathCell;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.common.style.UnsupportedStyleException;
import org.jdesktop.wonderland.modules.path.common.style.segment.SegmentStyle;

/**
 * This renderer is used for rendering a PathCell. The rendering of PathNodes and
 * segments is delegated to child rendering components.
 *
 * @author Carl Jokl
 */
public class PathCellRenderer extends BasicRenderer {

    private Node cellRootNode;
    private PathRendererFactory rendererFactory;
    private List<PathNodeRenderer> nodeRenderers;

    /**
     * Create a new PathCellRenderer instance to render the specified PathCell.
     *
     * @param cell The PathCell to be rendered.
     */
    public PathCellRenderer(PathCell cell) {
        super(cell);
        rendererFactory = getRendererFactory();
        nodeRenderers = new ArrayList<PathNodeRenderer>(cell.getNoOfNodes());
    }

    protected final PathRendererFactory getRendererFactory() {
        //ToDo: Replace this implementation with another capable of detecting available renderers.
        return new NonDetectingPathRendererFactory();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected Node createSceneGraph(Entity entity) {
        if (cellRootNode != null) {
            cellRootNode.detachAllChildren();
            cellRootNode = null;
            nodeRenderers.clear();
        }
        cellRootNode = new Node(entity.getName());
        if (cell != null) {
            String name = cell.getCellID().toString();
            if (cell instanceof PathCell) {
                PathCell pathCell = (PathCell) cell;
                List<ClientPathNode> nodes = pathCell.getNodeList();
                final int noOfNodes = nodes.size();
                if (pathCell.isEditMode()) {
                    PathSegmentRenderer editModeSegmentRenderer = rendererFactory.getEditSegmentRenderer();
                    for (ClientPathNode currentNode : nodes) {
                        if (currentNode.hasNext()) {
                            addIfNotNull(editModeSegmentRenderer.render(null, currentNode, currentNode.getNext()));
                        }
                    }
                }
                else if (noOfNodes > 0) {
                    PathStyle pathStyle = pathCell.getPathStyle();
                    SegmentStyle currentSegmentStyle = null;
                    PathNodeRenderer currentNodeRenderer =  null;
                    PathSegmentRenderer currentSegmentRenderer = null;
                    for (ClientPathNode currentNode : nodes) {
                        try {
                            currentSegmentStyle = pathStyle.getSegmentStyle(currentNode.getSequenceIndex(), true);
                            currentSegmentRenderer = currentSegmentStyle != null ? rendererFactory.getSegmentRenderer(currentSegmentStyle.getStyleType()) : null;
                            if (currentSegmentRenderer != null) {
                                addIfNotNull(currentSegmentRenderer.render(currentSegmentStyle, currentNode, currentNode.getNext()));
                            }
                        }
                        catch (IllegalArgumentException iae) {
                            logger.log(Level.SEVERE, "Error with argument when rendering segment!", iae);
                        }
                        catch (UnsupportedStyleException use) {
                            logger.log(Level.SEVERE, "Segment style not supported while rendering segment!", use);
                        }
                    }
                }
            }
            cellRootNode.setName(String.format("Path Cell: %s (%s)", cell.getName(), cell.getCellID().toString()));
        }
        return cellRootNode;
    }

    /**
     * Add the specified JME child Node if the Node is not null.
     *
     * @param jmeChildNode the JME child Node to be added if it is not null.
     */
    private void addIfNotNull(Node jmeChildNode) {
        if (jmeChildNode != null) {
            cellRootNode.attachChild(jmeChildNode);
        }
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
            cellRootNode.setName(String.format("Path Cell: %s (%s)", cell.getName(), cell.getCellID().toString()));
        }
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
