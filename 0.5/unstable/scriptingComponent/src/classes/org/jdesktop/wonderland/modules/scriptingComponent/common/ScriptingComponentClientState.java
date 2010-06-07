
package org.jdesktop.wonderland.modules.scriptingComponent.common;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * Client state for sample cell component
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ScriptingComponentClientState extends CellComponentClientState 
    {

    private String info;
    private String[] eventNames;
    private String[] eventScriptType;
    private Boolean[] eventResource;
    private String cellOwner;
    private boolean useGlobalScripts;
    
    /** Default constructor */
    public ScriptingComponentClientState() 
        {
        System.out.println("ScriptingComponentClientState : In constructor");
        }

    public String getInfo() 
        {
        System.out.println("ScriptingComponentClientState : In getInfo - info = " + info);
        return info;
        }

    public void setInfo(String info) 
        {
        System.out.println("ScriptingComponentClientState : In setInfo - info = " + info);
        this.info = info;
        }

    public String getCellOwner()
        {
        System.out.println("ScriptingComponentClientState : In getCellOwner - " + cellOwner);
        return cellOwner;
        }

    public void setCellOwner(String cellOwner)
        {
        System.out.println("ScriptingComponentClientState : In setCellOwner");
        this.cellOwner = cellOwner;
        }

    public boolean getUseGlobalScripts()
        {
        System.out.println("ScriptingComponentClientState : In getUseGlobalScripts - " + useGlobalScripts);
        return useGlobalScripts;
        }

    public void setUseGlobalScripts(boolean useGlobalScripts)
        {
        System.out.println("ScriptingComponentClientState : In setUseGlobalScripts");
        this.useGlobalScripts = useGlobalScripts;
        }

    public String[] getEventNames() 
        {
        System.out.println("ScriptingComponentClientState : In getEventNames");
        return eventNames;
        }

    public void setEventNames(String[] EventNames) 
        {
        System.out.println("ScriptingComponentClientState : In setEventNames");
        this.eventNames = EventNames;
        }
    
    public String[] getScriptType() 
        {
        System.out.println("ScriptingComponentClientState : In getScriptType");
        return eventScriptType;
        }

    public void setScriptType(String[] ScriptType) 
        {
        System.out.println("ScriptingComponentClientState : In setScriptType");
        this.eventScriptType = ScriptType;
        }

    public Boolean[] getEventResource()
        {
        System.out.println("ScriptingComponentClientState : In getEventResource");
        return eventResource;
        }

    public void setEventResource(Boolean[] EventResource)
        {
        System.out.println("ScriptingComponentClientState : In setScriptType");
        this.eventResource = EventResource;
        }
    }
