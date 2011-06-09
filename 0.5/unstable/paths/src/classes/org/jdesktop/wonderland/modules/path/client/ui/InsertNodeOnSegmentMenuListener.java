package org.jdesktop.wonderland.modules.path.client.ui;

import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.modules.path.client.ClientNodePath;
import org.jdesktop.wonderland.modules.path.client.PathCell;
import org.jdesktop.wonderland.modules.path.client.PathSegmentComponent;
import org.jdesktop.wonderland.modules.path.common.PathNode;

/**
 * This class is used to listen for events to insert a PathNode in a NodePath between two PathNodes of a segment.
 *
 * @author Carl Jokl
 */
public class InsertNodeOnSegmentMenuListener implements ContextMenuActionListener {

    /**
     * The display text for this command.
     */
    public static final String DISPLAY_TEXT = "Insert Node";

    /**
     * This method is called when the context menu item to insert a PathNode into a NodePath is fired.
     *
     * @param event The ContextMenuItemEvent containing information about the NodePath segment onto which a new PathNode will be inserted.
     */
    @Override
    public void actionPerformed(ContextMenuItemEvent event) {
        ContextMenuItem menuItem = event.getContextMenuItem();
        if (menuItem instanceof EntityContextMenuItem) {
            Entity entity = ((EntityContextMenuItem) menuItem).getEntity();
            if (entity != null && entity.hasComponent(PathSegmentComponent.class)) {
                PathSegmentComponent pathSegmentComponent = entity.getComponent(PathSegmentComponent.class);
                PathNode startNode = pathSegmentComponent.getStartNode();
                Cell cell = event.getCell();
                if (startNode!= null && cell instanceof PathCell) {
                    ClientNodePath path = ((PathCell) cell).getNodePath();
                    HeightChoiceDialogRunner.showHeightSelectionUsingAvatar(new InsertNodePositionable(path, false, startNode.getSequenceIndex()), startNode.getPosition(), cell.getLocalToWorldTransform());
                }
            }
        }
    }
}
