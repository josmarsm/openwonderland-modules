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


import org.jdesktop.wonderland.common.cell.ComponentLookupClass;
import org.jdesktop.wonderland.common.cell.security.ModifyAction;
import org.jdesktop.wonderland.common.cell.security.ViewAction;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.security.common.ActionDTO;
import org.jdesktop.wonderland.modules.security.common.CellPermissions;
import org.jdesktop.wonderland.modules.security.common.Permission;
import org.jdesktop.wonderland.modules.security.common.Principal;
import org.jdesktop.wonderland.modules.security.common.SecurityComponentServerState;
import org.jdesktop.wonderland.modules.security.server.SecurityComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;

/**
 * A basic security component managed object which automatically
 * assigns basic permissions to all cells. Specifically, members of the Users
 * group gets full access and members of the Guests group can only view cells.
 *
 * @author jagwire 
 */
@ComponentLookupClass(SecurityComponentMO.class)
public class BasicSecurityComponentMO extends SecurityComponentMO {
   
    public BasicSecurityComponentMO(CellMO cell) {
        super(cell);
        SecurityComponentServerState state = new SecurityComponentServerState();
        setServerState(state);

    }


    @Override
    public void setServerState(CellComponentServerState state) {

        SecurityComponentServerState scss = (SecurityComponentServerState) state;
        CellPermissions perms = scss.getPermissions();

        System.out.println("*** Inside BasicSecurityComponentMO ***");

        Principal v = new Principal("visitors", Principal.Type.EVERYBODY);

        //allow all users to view cells
        ActionDTO view = new ActionDTO(new ViewAction());
        perms.getPermissions().add(new Permission(v,
                                                  view,
                                                  Permission.Access.GRANT));
        //deny all visitors to modify cells
        ActionDTO modify = new ActionDTO(new ModifyAction());
        perms.getPermissions().add(new Permission(v,
                                                  modify,
                                                  Permission.Access.DENY));

        
        scss.setPermissions(perms);

        super.setServerState(state);
    }
}
