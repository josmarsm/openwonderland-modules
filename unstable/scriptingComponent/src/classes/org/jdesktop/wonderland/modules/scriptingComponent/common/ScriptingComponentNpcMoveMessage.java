/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.scriptingComponent.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author morrisford
 */
public class ScriptingComponentNpcMoveMessage  extends CellMessage
    {
//    private Vector3f npcPosition;
    private CellTransform transform;

    public ScriptingComponentNpcMoveMessage(CellID cellID, CellTransform transform)
        {
        super(cellID);
        this.transform = transform;
        }

//    public Vector3f getNpcPosition()
//        {
//        return npcPosition;
//        }

    public CellTransform getCellTransform()
        {
        return transform;
        }

//    public void setNpcPosition(Vector3f npcPosition)
//        {
//        this.npcPosition = npcPosition;
//        }

//    public NpcCellChangeMessage(CellID cellID, Vector3f npcPosition) {
//        super(cellID);
//        this.npcPosition = npcPosition;
//    }

}
