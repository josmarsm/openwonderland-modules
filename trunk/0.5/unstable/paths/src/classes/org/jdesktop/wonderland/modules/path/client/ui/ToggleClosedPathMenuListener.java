package org.jdesktop.wonderland.modules.path.client.ui;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.client.PathCell;

/**
 * This class is used to listen for events to add a PathNode at the end of a given NodePath.
 *
 * @author Carl Jokl
 */
public class ToggleClosedPathMenuListener implements ContextMenuActionListener {

    /**
     * The display text for this command.
     */
    public static final String DISPLAY_TEXT = "Toggle Closed Path";

    /**
     * This method is called when the context menu item to toggle whether a NodePath should be drawn as a closed path.
     *
     * @param event The ContextMenuItemEvent containing information about the NodePath.
     */
    @Override
    public void actionPerformed(ContextMenuItemEvent event) {
        Cell cell = event.getCell();
        if (cell instanceof PathCell) {
            ClientNodePath path = ((PathCell) cell).getNodePath();
            path.setClosedPath(!path.isClosedPath());
        }    
    }
}
