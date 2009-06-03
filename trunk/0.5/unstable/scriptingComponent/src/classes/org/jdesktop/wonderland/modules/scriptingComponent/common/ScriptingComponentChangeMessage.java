package org.jdesktop.wonderland.modules.scriptingComponent.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

public class ScriptingComponentChangeMessage extends CellMessage
    {
    private String cellName = null;
    private String[] eventNames;
    private String[] eventScriptType;

    public ScriptingComponentChangeMessage(CellID cellID, String cellName, String[] EventNames, String[] ScriptType)
        {
        super(cellID);
        this.cellName = cellName;
        this.eventNames = EventNames;
        this.eventScriptType = ScriptType;
        }

    public String getCellName()
        {
        return this.cellName;
        }
    
    public void setCellName(String cellName)
        {
        this.cellName = cellName;
        }
    
    public String[] getEventNames()
        {
        return this.eventNames;
        }
    
    public void setEventNames(String[] EventNames)
        {
        this.eventNames = EventNames;
        }
    
    public String[] getScriptType()
        {
        return this.eventScriptType;
        }
    
    public void setScriptType(String[] ScriptType)
        {
        this.eventScriptType = ScriptType;
        }
    }
