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

import javax.xml.bind.annotation.XmlRootElement;
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

    private String info;

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

    public String getInfo() 
        {
        System.out.println("ScriptingComponentServerState : In getInfo - info = " + info);
        return info;
        }

    public void setInfo(String info) 
        {
        System.out.println("ScriptingComponentServerState : In setInfo - info = " + info);
        this.info = info;
        }
}
