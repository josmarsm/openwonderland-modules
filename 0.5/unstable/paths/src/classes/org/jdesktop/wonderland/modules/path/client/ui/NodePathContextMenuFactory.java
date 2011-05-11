package org.jdesktop.wonderland.modules.path.client.ui;

import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
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

    private ContextMenuActionListener moveNodeToAvatar = new MoveNodeToAvatarMenuListener();
    private ContextMenuActionListener insertNode = new InsertNodeMenuListener();
    private ContextMenuActionListener deleteNode = new DeleteNodeMenuListener();

    @Override
    public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
        Entity entity = event.getPrimaryEntity();
        logger.warning("Creating context menu for NodePath item....");
        if (entity != null) {
            logger.warning("The item clicked on has an entity...");
            if (entity.hasComponent(PathNodeComponent.class)) {
                logger.warning("The entity clicked upon has a PathNodeComponent...");
                return new ContextMenuItem[] { new EntityContextMenuItem(MoveNodeToAvatarMenuListener.DISPLAY_TEXT, moveNodeToAvatar, entity),
                                               new EntityContextMenuItem(DeleteNodeMenuListener.DISPLAY_TEXT, deleteNode, entity)};
            }
            else if (entity.hasComponent(PathSegmentComponent.class)) {
                logger.warning("The entity clicked upon has a PathSegmentComponent...");
                return new ContextMenuItem[] { new EntityContextMenuItem(InsertNodeMenuListener.DISPLAY_TEXT, insertNode, entity)};
            }
        }
        return new ContextMenuItem[] {  };
    }
}
