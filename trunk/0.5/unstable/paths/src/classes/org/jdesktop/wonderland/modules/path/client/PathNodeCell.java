package org.jdesktop.wonderland.modules.path.client;

import com.jme.math.Vector3f;
import java.util.logging.Level;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.PathRendererFactory;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.PathRendererFactoryHolder;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.node.PathNodeRenderer;
import org.jdesktop.wonderland.modules.path.common.NodePath;
import org.jdesktop.wonderland.modules.path.common.style.PathStyle;
import org.jdesktop.wonderland.modules.path.common.style.UnsupportedStyleException;
import org.jdesktop.wonderland.modules.path.common.style.node.NodeStyle;

/**
 * This cell represents a PathNode which is a node along a given path of nodes.
 *  
 * @author Carl Jokl
 */
public class PathNodeCell extends Cell implements ClientPathNode {

    private int sequenceIndex;
    private ClientPathNode previous;
    private ClientPathNode next;
    private PathNodeRenderer renderer;
    private NodeStyle lastStyle;
    private boolean wasEditMode;

    /**
     * Create a new instance of a PathNodeCell to represent a PathNode within
     * a path.
     *
     * @param id The id used to uniquely identify the Cell.
     * @param cache The cache of information about the cell.
     */
    public PathNodeCell(CellID id, CellCache cache) {
        super(id, cache);
        sequenceIndex = -1;
    }

     /**
     * Create a new instance of a PathNodeCell to represent a PathNode within
     * a path.
     *
     * @param id The id used to uniquely identify the Cell.
     * @param cache The cache of information about the cell.
     * @param x The x position of this node on the path.
     * @param y The y position of this node on the path.
     * @param z The z position of this node on the path.
     * @param local Whether the specified coordinates are local. 
     *              True if the coordinates are local and false 
     *              if the coordinates are global.
     *
     * @param name The name of this path node cell.
     */
    public PathNodeCell(CellID id, CellCache cache, float x, float y, float z, boolean local, String name) {
        this(id, cache);
        setName(name);
        if (local) {
            getLocalTransform().setTranslation(new Vector3f(x, y, z));
        }
        else {
            getWorldTransform().setTranslation(new Vector3f(x, y, z));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNamed() {
        return getName() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3f getLocalPosition() {
        return super.getLocalTransform().getTranslation(new Vector3f());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3f getGlobalPosition() {
        return super.getWorldTransform().getTranslation(new Vector3f());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSequenceIndex() {
        return sequenceIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSequenceIndex(int sequenceIndex) {
        this.sequenceIndex = sequenceIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePath getPath() {
        Cell parentCell = getParent();
        return parentCell instanceof NodePath ? (NodePath) parentCell : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVisible() {
        CellStatus status = getStatus();
        return (status == CellStatus.ACTIVE || status == CellStatus.RENDERING || status == CellStatus.VISIBLE);
    }

    /**
     * Get the next node in the sequence for this PathCell.
     *
     * @return The next PathNodeCell in the sequence for this PathCell.
     */
    @Override
    public ClientPathNode getNext() {
        return next;
    }

    /**
     * Set the next node in the sequence for the PathCell.
     *
     * @param next The next PathNodeCell in the sequence for the PathCell.
     */
    @Override
    public void setNext(ClientPathNode next) {
        this.next = next;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return next != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNextVisible() {
        return next != null && next.isVisible();
    }
    
    /**
     * Get the previous PathNodeCell in the sequence for the PathCell.
     *
     * @return The previous PathNodeCell in the sequence for the PathCell.
     */
    @Override
    public ClientPathNode getPrevious() {
        return previous;
    }

    /**
     * Set the previous PathNodeCell in the sequence for the PathCell.
     *
     * @param previous The previous PathNodeCell in the sequence of the PathCell.
     */
    @Override
    public void setPrevious(ClientPathNode previous) {
        this.previous = previous;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPrevious() {
        return previous != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPreviousVisible() {
        return previous != null && previous.isVisible();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CellRenderer getCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            NodePath path = getPath();
            if (path != null) {
                if (path.isEditMode()) {
                    if (!(wasEditMode && renderer != null) && path instanceof PathRendererFactoryHolder) {
                        renderer = ((PathRendererFactoryHolder) path).getPathRendererFactory().getEditNodeRenderer().createRenderer(this);
                    }
                }
                else {
                    PathStyle pathStyle = path.getPathStyle();
                    NodeStyle nodeStyle = pathStyle.getNodeStyle(sequenceIndex, true);
                    if (renderer == null || lastStyle == null || !lastStyle.equals(nodeStyle)) {
                        if (path instanceof PathRendererFactoryHolder) {
                            PathRendererFactory rendererFactory = ((PathRendererFactoryHolder) path).getPathRendererFactory();
                            if (rendererFactory != null && pathStyle != null) {
                                if (nodeStyle != null) {
                                    try {
                                        renderer = rendererFactory.getNodeRendererFactory(nodeStyle.getStyleType()).createRenderer(this, nodeStyle);
                                    }
                                    catch (IllegalArgumentException iae) {
                                        logger.log(Level.SEVERE, "Failed to create node renderer due to an illegal argument!", iae);
                                    }
                                    catch (UnsupportedStyleException use) {
                                        logger.log(Level.SEVERE, "Failed to create node renderer as the node style is not supported!", use);
                                    }
                                }
                            }
                        }
                    }
                }
                return renderer;
            }
        }
        return super.getCellRenderer(rendererType);
    }
}