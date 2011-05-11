package org.jdesktop.wonderland.modules.path.client.ui;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.modules.path.client.PathCell;

/**
 * This class is used to listen for events to insert a PathNode in a NodePath.
 *
 * @author Carl Jokl
 */
public class InsertNodeMenuListener implements ContextMenuActionListener {

    /**
     * The display text for this command.
     */
    public static final String DISPLAY_TEXT = "Insert Node";

    /**
     * This method is called when the context menu item to insert a PathNode into a NodePath is fired.
     *
     * @param event The ContextMenuItemEvent containing information about the item moved to the avatar.
     */
    @Override
    public void actionPerformed(ContextMenuItemEvent event) {
        Cell cell = event.getCell();
        if (cell instanceof PathCell) {
            PathCell pathCell = (PathCell) cell;
        }
    }
}
