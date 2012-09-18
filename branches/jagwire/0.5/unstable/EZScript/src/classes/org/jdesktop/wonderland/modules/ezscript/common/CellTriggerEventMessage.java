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
public class CellTriggerEventMessage extends CellMessage {

    private String eventName;
    private Object[] arguments;

    public CellTriggerEventMessage(CellID cellID, String eventName, Object[] args) {
        super(cellID);
        this.eventName = eventName;
        this.arguments = args;
    }

    public String getEventName() {
        return eventName;
    }

    public Object[] getArguments() {
        return arguments;
    }
}
