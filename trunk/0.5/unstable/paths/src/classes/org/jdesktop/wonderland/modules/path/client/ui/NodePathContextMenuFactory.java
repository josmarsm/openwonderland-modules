package org.jdesktop.wonderland.modules.path.client.ui;

import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.EntityComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.modules.path.client.PathNodeComponent;
import org.jdesktop.wonderland.modules.path.client.PathSegmentComponent;

/**
 * This class is used to create a context menu for a NodePath, PathNode or segment.
 *
 * @author Carl Jokl
 */
public class NodePathContextMenuFactory implements ContextMenuFactorySPI {

    protected static final Logger logger = Logger.getLogger(NodePathContextMenuFactory.class.getName());

    private ContextMenuActionListener toggleEditMode = new ToggleEditModeMenuListener();
    private ContextMenuActionListener toggleClosedPath = new ToggleClosedPathMenuListener();
    private ContextMenuActionListener moveNodeToAvatar = new MoveNodeToAvatarMenuListener();
    private ContextMenuActionListener addNode = new AddNodeMenuListener();
    private ContextMenuActionListener insertNodeOnSegment = new InsertNodeOnSegmentMenuListener();
    private ContextMenuActionListener insertNodeBefore = new InsertNodeBeforeNodeMenuListener();
    private ContextMenuActionListener insertNodeAfter = new InsertNodeAfterNodeMenuListener();
    private ContextMenuActionListener deleteNode = new DeleteNodeMenuListener();

    @Override
    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
        Entity entity = event.getPrimaryEntity();
        logger.warning("Creating context menu for NodePath item....");
        if (entity != null) {
            logger.warning("The item clicked on has an entity...");
            int total = 0;
            for (EntityComponent comp : entity.getComponents()) {
                logger.warning(String.format("Entity component %d: %s", total, comp.getClass().getName()));
                total++;
            }
            logger.warning(String.format("The entity has: %d components.", total));
            if (entity.hasComponent(PathNodeComponent.class)) {
                logger.warning("The entity clicked upon has a PathNodeComponent...");
                return new ContextMenuItem[] { new SimpleContextMenuItem(ToggleEditModeMenuListener.DISPLAY_TEXT, toggleEditMode),
                                               new SimpleContextMenuItem(ToggleClosedPathMenuListener.DISPLAY_TEXT, toggleClosedPath),
                                               new EntityContextMenuItem(MoveNodeToAvatarMenuListener.DISPLAY_TEXT, moveNodeToAvatar, entity),
                                               new SimpleContextMenuItem(AddNodeMenuListener.DISPLAY_TEXT, addNode),
                                               new EntityContextMenuItem(InsertNodeBeforeNodeMenuListener.DISPLAY_TEXT, insertNodeBefore, entity),
                                               new EntityContextMenuItem(InsertNodeAfterNodeMenuListener.DISPLAY_TEXT, insertNodeAfter, entity),
                                               new EntityContextMenuItem(DeleteNodeMenuListener.DISPLAY_TEXT, deleteNode, entity) };
            }
            else if (entity.hasComponent(PathSegmentComponent.class)) {
                logger.warning("The entity clicked upon has a PathSegmentComponent...");
                return new ContextMenuItem[] { new SimpleContextMenuItem(ToggleEditModeMenuListener.DISPLAY_TEXT, toggleEditMode),
                                               new SimpleContextMenuItem(ToggleClosedPathMenuListener.DISPLAY_TEXT, toggleClosedPath),
                                               new SimpleContextMenuItem(AddNodeMenuListener.DISPLAY_TEXT, addNode),
                                               new EntityContextMenuItem(InsertNodeOnSegmentMenuListener.DISPLAY_TEXT, insertNodeOnSegment, entity) };
            }
        }
        return new ContextMenuItem[] { new SimpleContextMenuItem(ToggleEditModeMenuListener.DISPLAY_TEXT, toggleEditMode),
                                       new SimpleContextMenuItem(ToggleClosedPathMenuListener.DISPLAY_TEXT, toggleClosedPath),
                                       new SimpleContextMenuItem(AddNodeMenuListener.DISPLAY_TEXT, addNode) };
    }
}
