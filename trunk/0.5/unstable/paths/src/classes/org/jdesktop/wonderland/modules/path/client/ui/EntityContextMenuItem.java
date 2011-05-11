package org.jdesktop.wonderland.modules.path.client.ui;

import java.awt.Image;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;

/**
 * This class represents a ContextMenuItem which is bound to a specific Entity.
 *
 * @author Carl Jokl
 */
public class EntityContextMenuItem extends SimpleContextMenuItem {

    private Entity entity;

    /**
     * Create a new instance of an EntityContextMenuItem with the specified label and listener and to be
     * associated with the specified Entity.
     *
     * @param label The label of the ContextMenuItem.
     * @param listener The ContextMenuActionListener which listens for when the ContextMenuItem is selected.
     * @param entity The Entity with which this ContextMenuItem is associated.
     */
    public EntityContextMenuItem(String label, ContextMenuActionListener listener, Entity entity) {
        super(label, listener);
        this.entity = entity;
    }

    /**
     * Create a new instance of an EntityContextMenuItem with the specified label, icon image and listener and to be
     * associated with the specified Entity.
     *
     * @param label The label of the ContextMenuItem.
     * @param image The icon image used for this ContextMenuItem in the ContextMenu.
     * @param listener The ContextMenuActionListener which listens for when the ContextMenuItem is selected.
     * @param entity The Entity with which this ContextMenuItem is associated.
     */
    public EntityContextMenuItem(String label, Image image, ContextMenuActionListener listener, Entity entity) {
        super(label, image, listener);
        this.entity = entity;
    }

    /**
     * Get the Entity associated with this EntityContextMenuItem.
     *
     * @return The Entity which is associated with this EntityContextMenuItem.
     */
    public Entity getEntity() {
        return entity;
    }
}
