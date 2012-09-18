/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author JagWire
 */
public class AttachModelMessage extends CellMessage {
    private String modelURL;
    private String modelID;


    public AttachModelMessage(CellID cellID, String modelURL, String modelID) {
        super(cellID);
        this.modelURL = modelURL;
        this.modelID = modelID;
    }

    public String getModelURL() {
        return modelURL;
    }

    public void setModelURL(String modelURL) {
        this.modelURL = modelURL;
    }

    public String getModelID() {
        return modelID;
    }

    public void setModelID(String modelID) {
        this.modelID = modelID;
    }
}
