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
package org.jdesktop.wonderland.modules.joth.common.cell;

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellServerState;

/**
 * The WFS server state class for JothCellMO.
 * 
 * @author deronj
 */
@XmlRootElement(name="joth-cell")
@ServerState
public class JothCellServerState extends App2DCellServerState {
    
    /** Default constructor */
    public JothCellServerState() {}
    
    /** {@inheritDoc} */
    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.joth.server.cell.JothCellMO";
    }
}
