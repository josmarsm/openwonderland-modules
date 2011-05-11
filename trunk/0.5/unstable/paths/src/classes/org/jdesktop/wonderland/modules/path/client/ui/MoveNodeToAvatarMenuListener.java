package org.jdesktop.wonderland.modules.path.client.ui;

import com.jme.math.Vector3f;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
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
        logger.warning("Firing move node to Avatar command!");
        if (menuItem instanceof EntityContextMenuItem) {
            logger.warning("The source of the move node to avatar command is an EntityContextMenuItem!");
            Entity entity = ((EntityContextMenuItem) menuItem).getEntity();
            if (entity != null && entity.hasComponent(PathNodeComponent.class)) {
                logger.warning("The entity in the EntityContextMenuItem is not null and the Entity has a PathNodeComponent!");
                PathNodeComponent pathNodeComponent = entity.getComponent(PathNodeComponent.class);
                PathNode node = pathNodeComponent.getPathNode();
                Cell cell = event.getCell();
                if (node != null && cell instanceof PathCell) {
                    ClientNodePath path = ((PathCell) cell).getNodePath();
                    logger.warning("The source Cell from the ContextMenuItemEvent was not null and the Path node in the PathNodeComponent was not null!");
                    Vector3f position = node.getPosition();
                    if (position != null) {
                        logger.warning(String.format("The current PathNode position is: (%f, %f, %f)!", position.x, position.y, position.z));
                        Vector3f avatarWorldPosition = ClientContextJME.getViewManager().getPrimaryViewCell().getWorldTransform().getTranslation(null);
                        Vector3f avatarLocalPosition = cell.getWorldTransform().transform(avatarWorldPosition);
                        final float groundHeightDifference = 0.0f;
                        javax.swing.SwingUtilities.invokeLater(new HeightChoiceDialogRunner(path, node.getSequenceIndex(), avatarLocalPosition.x, avatarLocalPosition.y, avatarLocalPosition.z, node.getPosition().y, groundHeightDifference));
                    }
                }
            }
        }
    }

    /**
     * This class is a simple Runnable designed to run on the AWT thread to display dialogs to the user for making choices about the position of the new node.
     */
    private static class HeightChoiceDialogRunner implements Runnable , Disposable {

        private static final String SAME_ABSOLUTE_HEIGHT_OPTION = "Same Absolute Height";
        private static final String SAME_RELATIVE_HEIGHT_OPTION = "Same Relative Height";
        private static final String AVATAR_HEIGHT_OPTION = "Avatar Height";
        private static final String CUSTOM_HEIGHT_OPTION = "Custom Height";

        private ClientNodePath path;
        private final int nodeIndex;
        private final float destinationX;
        private final float destinationY;
        private final float destinationZ;
        private final float startY;
        private final float groundHeightDifference;

        /**
         * Create a new instance of the HeightChoiceDialogRunner to provide UI choice of what height to use for the new PathNode position.
         * The arguments are final as this object is not intended to perform actual modification of the objects but only reads values from them.
         *
         * @param path The ClientNodePath to use to update the PathNode position.
         * @param nodeIndex The sequence index of the PathNode in the NodePath which is to have it's position updated.
         * @param destinationX The destination (avatar) X position.
         * @param destinationY The destination (avatar) Y position.
         * @param destinationZ The destination (avatar) Z position.
         * @param startY The original height of the PathNode.
         * @param groundHeightDifference The difference in height between the ground / floor level at the original node position and the ground / floor level at the destination position.
         */
        public HeightChoiceDialogRunner(final ClientNodePath path, final int nodeIndex, final float destinationX, final float destinationY, final float destinationZ, final float startY, float groundHeightDifference) {
            this.path = path;
            this.nodeIndex = nodeIndex;
            this.destinationX = destinationX;
            this.destinationY = destinationY;
            this.destinationZ = destinationZ;
            this.startY = startY;
            this.groundHeightDifference = groundHeightDifference;
        }

        @Override
        public void run() {

            Object selection = javax.swing.JOptionPane.showInputDialog(null,
                                                                       "Do you wish to use the Avatar height as the new height or the original height?",
                                                                       "Select Destination Height.",
                                                                       javax.swing.JOptionPane.QUESTION_MESSAGE,
                                                                       null,
                                                                       new Object[] { SAME_ABSOLUTE_HEIGHT_OPTION,
                                                                                      SAME_RELATIVE_HEIGHT_OPTION,
                                                                                      AVATAR_HEIGHT_OPTION,
                                                                                      CUSTOM_HEIGHT_OPTION },
                                                                       SAME_RELATIVE_HEIGHT_OPTION);
            if (selection != null) {
                if (SAME_RELATIVE_HEIGHT_OPTION.equals(selection)) {
                    path.setNodePosition(nodeIndex, destinationX, startY + groundHeightDifference, destinationZ);
                }
                else if (SAME_ABSOLUTE_HEIGHT_OPTION.equals(selection)) {
                    path.setNodePosition(nodeIndex,  destinationX, startY, destinationZ);
                }
                else if (AVATAR_HEIGHT_OPTION.equals(selection)) {
                    path.setNodePosition(nodeIndex, destinationX, destinationY, destinationZ);
                }
                else if (CUSTOM_HEIGHT_OPTION.equals(selection)) {
                    //Show height selection dialog.
                }
            }
            dispose();
        }

        @Override
        public void dispose() {
            path = null;
        }
    }
}
