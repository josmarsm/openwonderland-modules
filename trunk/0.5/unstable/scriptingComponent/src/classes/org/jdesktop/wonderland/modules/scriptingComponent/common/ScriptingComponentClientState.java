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

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * Client state for sample cell component
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ScriptingComponentClientState extends CellComponentClientState 
    {

    private String info;
    private String cellName;
    private String[] eventNames;
    private String[] eventScriptType;
    
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
    
    public String getCellName() 
        {
        System.out.println("ScriptingComponentClientState : In getCellName - cellName = " + cellName);
        return cellName;
        }

    public void setCellName(String cellName) 
        {
        System.out.println("ScriptingComponentClientState : In setCellName - cellName = " + cellName);
        this.cellName = cellName;
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
    }
