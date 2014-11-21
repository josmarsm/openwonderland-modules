/*ADDED FOR ESL AUDIO
 * 
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author WISH001
 * CREATED FOR ESL AUDIO
 */
public class TempMessage extends CellMessage {

    private String shapeType = null;

    public TempMessage(CellID cellID, String shapeType) {
        super(cellID);
        this.shapeType = shapeType;
    }

    public String getShapeType() {
        return this.shapeType;
    }

    public void setShapeType(String shapeType) {
        this.shapeType = shapeType;
    }




}
