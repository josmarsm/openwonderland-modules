/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.eventplayer.client.npcplayer;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableAvatarComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ComponentLookupClass;
import org.jdesktop.wonderland.common.cell.messages.MovableAvatarMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableMessage;

/**
 * A component that extends MovableAvatarComponent to add additional information
 * for animating avatars. We can't use MovableAvatarComponent here because it explicitly casts cell to
 * AvatarCell. Adapted from the NPC MovableNpcPlayerComponent written by Paul Byrne.
 *
 * @author Bernard Horan
 */
@ComponentLookupClass(MovableComponent.class)
public class MovableNpcPlayerComponent extends MovableAvatarComponent {
    public MovableNpcPlayerComponent(Cell cell) {
        super(cell);
    }


    @Override
    protected void serverMoveRequest(MovableMessage msg) {
        System.err.println("MovableNpcPlayerComponent.serverMoveRequest: " + msg);
        CellTransform transform = msg.getCellTransform();
        applyLocalTransformChange(transform, TransformChangeListener.ChangeSource.REMOTE);
        notifyServerCellMoveListeners(msg, transform, CellMoveSource.REMOTE);

        MovableAvatarMessage mam = (MovableAvatarMessage) msg;
        //System.err.println("Move message "+msg.getCellTransform().getTranslation(null)+"  "+mam.getTrigger()+" "+mam.getAnimationName());
        if (mam.getTrigger()!=NO_TRIGGER) {
            ((NpcPlayerCell)cell).triggerAction(mam.getTrigger(), mam.isPressed(), mam.getAnimationName());
        }
    }


}
