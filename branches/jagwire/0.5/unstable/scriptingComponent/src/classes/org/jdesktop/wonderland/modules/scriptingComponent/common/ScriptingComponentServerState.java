/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.scriptingComponent.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Server state for sample cell component
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="scripting-component")
@ServerState
public class ScriptingComponentServerState extends CellComponentServerState 
    {
    private final int totalEvents = 30;

    @XmlElement(name="info")
    private String info;
    @XmlElement(name="event-names")
    private String[] eventNames;
    @XmlElement(name="event-script-type")
    private String[] eventScriptType;
    @XmlElement(name="event-resource")
    private Boolean[] eventResource;
    @XmlElement(name="cell-owner")
    private String cellOwner;
    @XmlElement(name="use-global-scripts")
    private boolean useGlobalScripts;

    /** Default constructor */
    public ScriptingComponentServerState() 
        {
        System.out.println("ScriptingComponentServerState : In constructor");
        }

    @Override
    public String getServerComponentClassName() 
        {
        System.out.println("ScriptingComponentServerState : In getServerComponentClassName");
        return "org.jdesktop.wonderland.modules.scriptingComponent.server.ScriptingComponentMO";
        }
    
    @XmlTransient public String getInfo() 
        {
        System.out.println("ScriptingComponentServerState : In getInfo - info = " + info);
        return info;
        }

    public void setInfo(String info) 
        {
        System.out.println("ScriptingComponentServerState : In setInfo - info = " + info);
        this.info = info;
        }

    @XmlTransient public String getCellOwner()
        {
        System.out.println("ScriptingComponentServerState : In getCellOwner - " + cellOwner);
        return cellOwner;
        }

    public void setCellOwner(String cellOwner)
        {
        System.out.println("ScriptingComponentServerState : In setCellOwner - " + cellOwner);
        this.cellOwner = cellOwner;
        }

    @XmlTransient public boolean getUseGlobalScripts()
        {
        System.out.println("ScriptingComponentServerState : In getUseGlobalScripts - " + useGlobalScripts);
        return useGlobalScripts;
        }

    public void setUseGlobalScripts(boolean useGlobalScripts)
        {
        System.out.println("ScriptingComponentServerState : In setUseGlobalScripts - " + useGlobalScripts);
        this.useGlobalScripts = useGlobalScripts;
        }

    @XmlTransient public String[] getEventNames() 
        {
        System.out.println("ScriptingComponentServerState : In getEventNames");
        return eventNames;
        }

    public void setEventNames(String[] EventNames) 
        {
        System.out.println("ScriptingComponentServerState : In setEventNames");
        this.eventNames = EventNames;
        }
    
    @XmlTransient public String[] getScriptType() 
        {
        System.out.println("ScriptingComponentServerState : In getScriptType");
        return eventScriptType;
        }

    public void setScriptType(String[] ScriptType) 
        {
        System.out.println("ScriptingComponentServerState : In setScriptType");
        this.eventScriptType = ScriptType;
        }

    @XmlTransient public Boolean[] getEventResource()
        {
        System.out.println("ScriptingComponentServerState : In getEventResource");
        return eventResource;
        }

    public void setEventResource(Boolean[] EventResource)
        {
        System.out.println("ScriptingComponentServerState : In setEventResource");
        this.eventResource = EventResource;
        }
}
