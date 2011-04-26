/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.common;

import org.jdesktop.wonderland.common.messages.Message;

/**
 *
 * @author JagWire
 */
public class CommonMessage extends Message {
    private String identifier;
    private Object payload;

    public CommonMessage(String identifier, Object payload) {
        this.identifier = identifier;
        this.payload = payload;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Object getPayload() {
        return payload;
    }

}
