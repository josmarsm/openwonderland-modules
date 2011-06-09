package org.jdesktop.wonderland.modules.path.client.ui;

import com.jme.math.Vector3f;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.client.PathCell;
import org.jdesktop.wonderland.modules.path.client.PathNodeComponent;
import org.jdesktop.wonderland.modules.path.common.Disposable;
import org.jdesktop.wonderland.modules.path.common.PathNode;

/**
 * This class is used to listen for events to move a PathNode to the location of the Avatar.
 *
 * @author Carl Jokl
 */
public class MoveNodeToAvatarMenuListener implements ContextMenuActionListener {

    protected static final Logger logger = Logger.getLogger(MoveNodeToAvatarMenuListener.class.getName());

    /**
     * The display text for this command.
     */
    public static final String DISPLAY_TEXT = "Move to Avatar";

    /**
     * This method is called when the context menu item to move a PathNode to the avatar is fired.
     *
     * @param event The ContextMenuItemEvent containing information about the item moved to the avatar.
     */
    @Override
    public void actionPerformed(ContextMenuItemEvent event) {
        ContextMenuItem menuItem = event.getContextMenuItem();
        if (menuItem instanceof EntityContextMenuItem) {
            Entity entity = ((EntityContextMenuItem) menuItem).getEntity();
            if (entity != null && entity.hasComponent(PathNodeComponent.class)) {
                PathNodeComponent pathNodeComponent = entity.getComponent(PathNodeComponent.class);
                PathNode node = pathNodeComponent.getPathNode();
                Cell cell = event.getCell();
                if (node != null && cell instanceof PathCell) {
                    ClientNodePath path = ((PathCell) cell).getNodePath();
                    HeightChoiceDialogRunner.showHeightSelectionUsingAvatar(new NodeMovePositionable(node.getSequenceIndex(), path), node.getPosition(), cell.getLocalToWorldTransform());
                }
            }
        }
    }

    /**
     * This class is used for selection feedback when a destination height has been selected
     * to set where a node should be positioned.
     */
    private static class NodeMovePositionable implements Positionable, Disposable {

        private final int nodeIndex;
        private ClientNodePath nodePath;

        /**
         * Create a new NodeMovePositionable to handle setting the position of the PathNode
         * with the specified index after the destination position has been selected.
         *
         * @param nodeIndex The index of the PathNode to be moved.
         * @param nodePath The NodePath which contains the PathNode to be moved.
         */
        public NodeMovePositionable(final int nodeIndex, final ClientNodePath nodePath) {
            this.nodeIndex = nodeIndex;
            this.nodePath = nodePath;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void usePosition(float x, float y, float z) {
            nodePath.setNodePosition(nodeIndex, x, y, z);
            dispose();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
            nodePath = null;
        }
    }
}
