/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.navigateto.client;

import com.jme.math.Vector3f;
import imi.character.avatar.AvatarContext;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.behavior.Task;
import imi.objects.SpatialObject;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;

/**
 *
 * @author Abhishek Upadhyay
 */
public class TurnTo implements Task {

    private boolean bDone = false;
    private Vector3f goalDir = null;
    private TriggerNames lastTrigger = null;
    
    public TurnTo(Vector3f goalDir) {
        this.goalDir = goalDir;
    }
    
    public String getDescription() {
        return "TurnTo";
    }

    public String getStatus() {
        return "TurnTo";
    }

    public boolean verify() {
        if (bDone)
            return false;
        return true;
    }

    public void update(float f) {
        Cell avatarCell1 = ClientContextJME.getViewManager().getPrimaryViewCell();
        CellRenderer rend = avatarCell1.getCellRenderer(Cell.RendererType.RENDERER_JME);
        WlAvatarCharacter myAvatar = ((AvatarImiJME) rend).getAvatarCharacter();
        AvatarContext context = (AvatarContext) myAvatar.getContext();
        Vector3f rightVec = context.getController().getRightVector();
        float dot = goalDir.dot(rightVec);
        if (Math.abs(dot) <= 0.1) {
            if(lastTrigger!=null) {
                context.triggerReleased(lastTrigger.ordinal());
            }
            bDone = true;
        } else {
            if (dot > 0.1) {
                lastTrigger = TriggerNames.Move_Right;
                context.triggerPressed(TriggerNames.Move_Right.ordinal());
            }
            else if (dot < -0.1) {
                lastTrigger = TriggerNames.Move_Left;
                context.triggerPressed(TriggerNames.Move_Left.ordinal());
            }
        }
    }

    public void onHold() {
        
    }

    public SpatialObject getGoal() {
        return null;
    }
    
}
