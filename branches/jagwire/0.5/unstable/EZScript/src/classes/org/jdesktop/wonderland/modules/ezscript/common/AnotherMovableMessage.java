/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.common;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
//import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MoveAction;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.MovableMessage;
import org.jdesktop.wonderland.common.cell.security.MoveAction;
import org.jdesktop.wonderland.common.security.annotation.Actions;

/**
 *
 * @author JagWire
 */
@Actions(MoveAction.class)
public class AnotherMovableMessage extends MovableMessage {

    public AnotherMovableMessage(CellID cellID, ActionType type) {
        super(cellID, type);
    }
    
    public static AnotherMovableMessage anotherNewMovedMessage(CellID cellID, CellTransform transform) {
        AnotherMovableMessage ret = new AnotherMovableMessage(cellID, ActionType.MOVE_REQUEST);
        ret.setTranslation(transform.getTranslation(null));
        ret.setRotation(transform.getRotation(null));
        ret.setScale(transform.getScaling());
        
        return ret;
    }

        public static AnotherMovableMessage anotherNewMoveRequestMessage(CellID cellID, CellTransform transform) {
        return anotherNewMoveRequestMessage(cellID, transform.getTranslation(null), transform.getRotation(null), transform.getScaling());
    }

    public static AnotherMovableMessage anotherNewMoveRequestMessage(CellID cellID, Vector3f translation, Quaternion rotation) {
        return anotherNewMoveRequestMessage(cellID, translation, rotation, 1f);
    }

    public static AnotherMovableMessage anotherNewMoveRequestMessage(CellID cellID, Vector3f translation, Quaternion rotation, float scale) {
        AnotherMovableMessage ret = new AnotherMovableMessage(cellID, ActionType.MOVE_REQUEST);
        ret.setTranslation(translation);
        ret.setRotation(rotation);
        ret.setScale(scale);
        return ret;
    }


}
