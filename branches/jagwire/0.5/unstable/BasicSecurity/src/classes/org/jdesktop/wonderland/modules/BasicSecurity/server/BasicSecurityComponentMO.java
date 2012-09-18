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

package org.jdesktop.wonderland.modules.BasicSecurity.server;


import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;

/**
 * A basic security component managed object which automatically
 * assigns basic permissions to all cells. Specifically, members of the Users
 * group gets full access and members of the Guests group can only view cells.
 *
 * @author jagwire 
 */



/** As of December 20th, 2011, this class should be ignored. Security will now
 * be handled on the client.
 * 
 * @author JagWire
 */
public class BasicSecurityComponentMO extends CellComponentMO {
   
    public BasicSecurityComponentMO(CellMO cell) {
        super(cell);
//        SecurityComponentServerState state = new SecurityComponentServerState();
//        setServerState(state);

    }
//
//
//    @Override
//    public void setServerState(CellComponentServerState state) {
//        
//        super.setServerState(state);
//    }
//    
//    @Override
//    public String getClientClass() {
//        return "org.jdesktop.wonderland.modules.BasicSecurity"
//    }
//    

    @Override
    protected String getClientClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
