package org.jdesktop.wonderland.modules.scriptingComponent.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

public class ScriptingComponentChangeMessage extends CellMessage
    {
    private String[] eventNames;
    private String[] eventScriptType;
    private Boolean[] eventResource;

    private String   cellOwner;
    private boolean  useGlobalScripts;
    private int      changeType;

    public ScriptingComponentChangeMessage(CellID cellID, String[] EventNames, String[] ScriptType, Boolean[] EventResource, int changeType)
        {
        super(cellID);
        this.eventNames = EventNames;
        this.eventScriptType = ScriptType;
        this.eventResource = EventResource;
        this.changeType = changeType;
        }

    public ScriptingComponentChangeMessage(CellID cellID, String cellOwner, boolean useGlobalScripts, int changeType)
        {
        super(cellID);
        this.cellOwner = cellOwner;
        this.useGlobalScripts = useGlobalScripts;
        this.changeType = changeType;
        }

    public Boolean[] getEventResource()
        {
        return this.eventResource;
        }

    public void setEventResource(Boolean[] EventResource)
        {
        this.eventResource = EventResource;
        }

    public int getChangeType()
        {
        return this.changeType;
        }

    public void setChangeType(int changeType)
        {
        this.changeType = changeType;
        }

    public String getCellOwner()
        {
        return this.cellOwner;
        }

    public void setCellOwner(String cellOwner)
        {
        this.cellOwner = cellOwner;
        }

    public boolean getUseGlobalScripts()
        {
        return this.useGlobalScripts;
        }

    public void setUseGlobalScripts(boolean useGlobalScripts)
        {
        this.useGlobalScripts = useGlobalScripts;
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
