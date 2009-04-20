package org.jdesktop.wonderland.modules.scriptingComponent.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

public class ScriptingComponentICEMessage extends CellMessage
    {
    private int iceCode = 0;
    private String payload;

    public ScriptingComponentICEMessage(CellID cellID, int IceCode, String Payload)
        {
        super(cellID);
        this.iceCode = IceCode;
        this.payload = Payload;
        }
    
    public int getIceCode()
        {
        return this.iceCode;
        }
    
    public void setIceCode(int IceCode)
        {
        this.iceCode = IceCode;
        }
    
    public String getPayload()
        {
        return this.payload;
        }
    
    public void setPayload(String Payload)
        {
        this.payload = Payload;
        }
    
    }
