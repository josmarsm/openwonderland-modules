package org.jdesktop.wonderland.modules.path.client;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.path.client.jme.cellrenderer.PathCellRenderer;
import org.jdesktop.wonderland.modules.path.client.receiver.FlagStateChangeMessageReceiver;
import org.jdesktop.wonderland.modules.path.client.receiver.PathCellComponentMessageReceiver;
import org.jdesktop.wonderland.modules.path.client.receiver.PathNodeChangeMessageReceiver;
import org.jdesktop.wonderland.modules.path.client.receiver.PathStyleChangeMessageReceiver;
import org.jdesktop.wonderland.modules.path.client.ui.NodePathContextMenuFactory;
import org.jdesktop.wonderland.modules.path.common.PathCellState;
import org.jdesktop.wonderland.modules.path.common.message.AllPathNodesRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.message.EditModeChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathClosedChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeAddedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeInsertedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeNameChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodePositionChangeMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathNodeRemovedMessage;
import org.jdesktop.wonderland.modules.path.common.message.PathStyleChangeMessage;

/**
 * The client instance of a PathCell with client rendering.
 *
 * @author Carl Jokl
 */
public class PathCell extends Cell {

    @UsesCellComponent private ContextMenuComponent contextMenuComponent;
    private ContextMenuFactorySPI contextMenuFactory;
    private PathCellRenderer renderer;
    private ClientNodePath internalPath;
    private ClientNodePath messageSendingPath;
    private PathCellComponentMessageReceiver flagChangeMessageReceiver;
    private PathCellComponentMessageReceiver nodeChangeMessageReceiver;
    private PathCellComponentMessageReceiver styleChangeMessageReceiver;

    /**
     * Create a new instance of a PathCell.
     *
     * @param cellID The id of the PathCell.
     * @param cellCache The CellCache used in the creation of the PathCell.
     */
    public PathCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        internalPath = new InternalClientNodePath(this);
        messageSendingPath = new ServerMessageSendingClientNodePath(this, internalPath);
        setName(String.format("Fence / Cordon / Path (%s)", cellID.toString()));
    }

    /**
     * Set the state of this PathCell.
     *
     * @param state The state which this PathCell is going to be give.
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        if (state instanceof PathCellState) {
            internalPath.setFrom((PathCellState) state);
        }
    }

    /**
     * Create the CellRenderer of this PathCell.
     *
     * @param rendererType The type of renderer which needs to be created.
     * @return The CellRenderer of the PathCell to display this PathCell.
     */
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            renderer = new PathCellRenderer(this);
            return renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    /**
     * Set the status of this PathCell.
     *
     * @param status The status which this PathCell is to have.
     * @param increasing If the PathCell is moving from a lower activity state to a higher activity state.
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        if (contextMenuFactory == null && contextMenuComponent != null) {
            contextMenuFactory = new NodePathContextMenuFactory();
            contextMenuComponent.addContextMenuFactory(contextMenuFactory);
        }
        if (renderer != null) {
            renderer.setStatus(status, increasing);
        }
        if (status == CellStatus.ACTIVE && increasing) {
            addChannelMessageListeners();
        }
        else if (status == CellStatus.INACTIVE && !increasing) {
            removeChannelMessageListeners();
        }
    }

    /**
     * Add all the listeners which the PathCell requires in order to get notifications
     * of changes to the NodePath state.
     */
    protected void addChannelMessageListeners() {
        ChannelComponent channelComponent = getComponent(ChannelComponent.class);
        if (channelComponent != null) {
            if (flagChangeMessageReceiver != null) {
                flagChangeMessageReceiver.dispose();
                flagChangeMessageReceiver = null;
            }
            flagChangeMessageReceiver = new FlagStateChangeMessageReceiver(this, internalPath);
            channelComponent.addMessageReceiver(EditModeChangeMessage.class, flagChangeMessageReceiver);
            if (nodeChangeMessageReceiver != null) {
                nodeChangeMessageReceiver.dispose();
                nodeChangeMessageReceiver = null;
            }
            nodeChangeMessageReceiver = new PathNodeChangeMessageReceiver(this, internalPath);
            channelComponent.addMessageReceiver(PathNodePositionChangeMessage.class, nodeChangeMessageReceiver);
            channelComponent.addMessageReceiver(PathNodeAddedMessage.class, nodeChangeMessageReceiver);
            channelComponent.addMessageReceiver(PathNodeInsertedMessage.class, nodeChangeMessageReceiver);
            channelComponent.addMessageReceiver(PathNodeRemovedMessage.class, nodeChangeMessageReceiver);
            channelComponent.addMessageReceiver(AllPathNodesRemovedMessage.class, nodeChangeMessageReceiver);
            channelComponent.addMessageReceiver(PathNodeNameChangeMessage.class, nodeChangeMessageReceiver);
            if (styleChangeMessageReceiver != null) {
                styleChangeMessageReceiver.dispose();
                styleChangeMessageReceiver = null;
            }
            styleChangeMessageReceiver = new PathStyleChangeMessageReceiver(this, internalPath);
            channelComponent.addMessageReceiver(PathStyleChangeMessage.class, styleChangeMessageReceiver);
        }
    }

    /**
     * Remove all the listeners for messages about cell state changes before the cell
     * goes into an inactive state.
     */
    protected void removeChannelMessageListeners() {
        ChannelComponent channelComponent = getComponent(ChannelComponent.class);
        if (channelComponent != null) {
            channelComponent.removeMessageReceiver(EditModeChangeMessage.class);
            channelComponent.removeMessageReceiver(PathClosedChangeMessage.class);
            if (flagChangeMessageReceiver != null) {
                flagChangeMessageReceiver.dispose();
                flagChangeMessageReceiver = null;
            }
            channelComponent.removeMessageReceiver(PathNodePositionChangeMessage.class);
            channelComponent.removeMessageReceiver(PathNodeAddedMessage.class);
            channelComponent.removeMessageReceiver(PathNodeInsertedMessage.class);
            channelComponent.removeMessageReceiver(PathNodeRemovedMessage.class);
            channelComponent.removeMessageReceiver(AllPathNodesRemovedMessage.class);
            channelComponent.removeMessageReceiver(PathNodeNameChangeMessage.class);
            if (nodeChangeMessageReceiver != null) {
                nodeChangeMessageReceiver.dispose();
                nodeChangeMessageReceiver = null;
            }
            channelComponent.removeMessageReceiver(PathStyleChangeMessage.class);
            if (styleChangeMessageReceiver != null) {
                styleChangeMessageReceiver.dispose();
                styleChangeMessageReceiver = null;
            }
        }
    }

    /**
     * Update the graphical representation of the PathNode at the specified index.
     *
     * @param nodeIndex The index of the IndexedPathNode to have its graphical representation updated.
     * @return True if the request to update the graphical representation was handled successfully.
     */
    public boolean updateNodeUI(int nodeIndex, boolean updateAttachedSegments) {
        if (renderer != null) {
            return renderer.updatePathNode(nodeIndex, updateAttachedSegments);
        }
        return false;
    }

    /**
     * Update the graphical representation of the NodePath segment at the specified index.
     *
     * @param segmentIndex The index of the NodePath segment to be updated.
     * @return True if the request to update the graphical representation of the specified NodePath segment.
     */
    public boolean updateSegmentUI(int segmentIndex) {
        if (renderer != null) {
            return renderer.updatePathSegment(segmentIndex);
        }
        return false;
    }

    /**
     * Update the graphical representation of the NodePath in its entirety.
     *
     * @return True if the request to update the graphical representation of the entire NodePath was submitted
     *         successfully.
     */
    public boolean updatePathUI() {
        if (renderer != null) {
            return renderer.updatePath();
        }
        return false;
    }

    /**
     * Get the ClientNodePath for this PathCell. This ClientNodePath can act
     * as a proxy, not only holding state but also sending state synchronization
     * messages to the server.
     *
     * @return The ClientNodePath of this PathCell.
     */
    public ClientNodePath getNodePath() {
        return messageSendingPath;
    }
}
