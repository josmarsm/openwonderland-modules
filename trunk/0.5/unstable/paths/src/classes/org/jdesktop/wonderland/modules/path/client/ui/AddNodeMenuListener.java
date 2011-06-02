package org.jdesktop.wonderland.modules.path.client.ui;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.client.PathCell;
import org.jdesktop.wonderland.modules.path.common.Disposable;

/**
 * This class is used to listen for events to add a PathNode at the end of a given NodePath.
 *
 * @author Carl Jokl
 */
public class AddNodeMenuListener implements ContextMenuActionListener {

    /**
     * The display text for this command.
     */
    public static final String DISPLAY_TEXT = "Add / Append Node";

    /**
     * This method is called when the context menu item to add a PathNode to the end of a NodePath is fired.
     *
     * @param event The ContextMenuItemEvent containing information about the NodePath to which a PathNode is to be added.
     */
    @Override
    public void actionPerformed(ContextMenuItemEvent event) {
        ContextMenuItem menuItem = event.getContextMenuItem();
        Cell cell = event.getCell();
        if (cell instanceof PathCell) {
            ClientNodePath path = ((PathCell) cell).getNodePath();
            HeightChoiceDialogRunner.showHeightSelectionUsingAvatar(new AddNodePositionable(path), cell.getWorldTransform().transform(new Vector3f()), cell.getWorldTransform());
        }    
    }

    private static class AddNodePositionable implements Positionable, Disposable {

        private ClientNodePath path;

        /**
         * Create a new instance of AddNodePositionable to handle the callback to add a new PathNode at a given position.
         * 
         * @param path The NodePath into which to add a PathNode.
         * @throws IllegalArgumentException If the specified NodePath is null.
         */
        public AddNodePositionable(final ClientNodePath path) throws IllegalArgumentException {
            if (path == null) {
                throw new IllegalArgumentException("The specified node path for the add position callback was null!");
            }
            this.path = path;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void usePosition(float x, float y, float z) {
            path.addNode(x, y, z, null);
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
