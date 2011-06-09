package org.jdesktop.wonderland.modules.path.client.ui;

import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.client.PathCell;
import org.jdesktop.wonderland.modules.path.client.PathNodeComponent;
import org.jdesktop.wonderland.modules.path.common.PathNode;

/**
 * This class is used to listen for events to insert a PathNode after a given PathNode in a NodePath.
 *
 * @author Carl Jokl
 */
public class InsertNodeAfterNodeMenuListener implements ContextMenuActionListener {

    /**
     * The display text for this command.
     */
    public static final String DISPLAY_TEXT = "Insert Node After";

    /**
     * This method is called when the context menu item to insert a PathNode into a NodePath is fired.
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
                    HeightChoiceDialogRunner.showHeightSelectionUsingAvatar(new InsertNodePositionable(path, false, node.getSequenceIndex()), node.getPosition(), cell.getLocalToWorldTransform());
                }
            }
        }
    }
}
