/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package com.wonderbuilders.modules.animation.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * @author Vladimir Djurovic
 */
public class AnimationComponentMessage extends CellMessage {
    
    private String methodCall;
    
    private String parameter;
    
    public AnimationComponentMessage(CellID cellID, String methodCall, String param){
        super(cellID);
        this.methodCall = methodCall;
        this.parameter = param;
    }
    
    public void setMethodCall(String call){
        methodCall = call;
    }

    public String getMethodCall() {
        return methodCall;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
    
    
}
