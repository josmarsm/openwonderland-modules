package org.jdesktop.wonderland.modules.path.client.ui;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.client.PathCell;
import org.jdesktop.wonderland.modules.path.client.PathNodeComponent;
import org.jdesktop.wonderland.modules.path.common.Disposable;
import org.jdesktop.wonderland.modules.path.common.PathNode;

/**
 * This class is used to listen for events to delete a PathNode in a NodePath.
 *
 * @author Carl Jokl
 */
public class DeleteNodeMenuListener implements ContextMenuActionListener {

    /**
     * The display text for this command.
     */
    public static final String DISPLAY_TEXT = "Delete Node";

    /**
     * This method is called when the context menu item to delete a PathNode from a NodePath is fired.
     *
     * @param event The ContextMenuItemEvent containing information about the item moved to the avatar.
     */
    @Override
    public void actionPerformed(ContextMenuItemEvent event) {
        Cell cell = event.getCell();
        if (cell instanceof PathCell) {
            PathCell pathCell = (PathCell) cell;
            ContextMenuItem menuItem = event.getContextMenuItem();
            if (menuItem instanceof EntityContextMenuItem) {
                Entity entity = ((EntityContextMenuItem) menuItem).getEntity();
                if (entity != null && entity.hasComponent(PathNodeComponent.class)) {
                    PathNodeComponent pathNodeComponent = entity.getComponent(PathNodeComponent.class);
                    PathNode node = pathNodeComponent.getPathNode();
                    javax.swing.SwingUtilities.invokeLater(new DeleteNodeConfirmationRunner(pathCell.getNodePath(), node.getSequenceIndex()));
                }
            }
        }
    }

    /**
     * This is a runnable object to display a deletion confirmation dialog for the node deletion.
     */
    private static class DeleteNodeConfirmationRunner implements Runnable, Disposable {

        private ClientNodePath path;
        private final int nodeIndex;

        /**
         * Create a new instance of DeleteNodeConfirmationRunner to display a confirmation before
         * deleting a PathNode.
         *
         * @param path The NodePath from which to delete the PathNode at the specified index.
         * @param nodeIndex The index of the PathNode which is to be deleted.
         */
        public DeleteNodeConfirmationRunner(final ClientNodePath path, final int nodeIndex) {
            this.path = path;
            this.nodeIndex = nodeIndex;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            java.awt.Component parent = (ClientContext.getRendererType() == RendererType.RENDERER_JME) ? JmeClientMain.getFrame().getFrame() : null;
            if (javax.swing.JOptionPane.showConfirmDialog(parent, "Are you sure you wish to delete?", "Confirm Deletion", javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE) == javax.swing.JOptionPane.OK_OPTION) {
                path.removeNodeAt(nodeIndex);
            }
            dispose();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
            path = null;
        }

    }
}
