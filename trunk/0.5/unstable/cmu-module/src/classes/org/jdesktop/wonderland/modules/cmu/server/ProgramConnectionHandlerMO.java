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

package org.jdesktop.wonderland.modules.cmu.server;

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.NameNotBoundException;
import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.cmu.common.ProgramConnectionType;
import org.jdesktop.wonderland.server.WonderlandContext;

/**
 *
 * @author kevin
 */
public class ProgramConnectionHandlerMO implements ManagedObject, Serializable {

    private static final String HANDLER_MO_NAME = "__CMU_PROGRAM_CONNECTION_HANDLER";
    private final ProgramConnectionHandler connectionHandler;

    public ProgramConnectionHandlerMO() {
        //TODO: Decide which of these methods is better.

        /*
        connectionHandler = new ProgramConnectionHandler();
        WonderlandContext.getCommsManager().registerClientHandler(connectionHandler);
         * */

        connectionHandler = (ProgramConnectionHandler) WonderlandContext.getCommsManager().getClientHandler(ProgramConnectionType.TYPE);
        assert connectionHandler != null;
    }

    static public ProgramConnectionHandlerMO getInstance() {
        try {
            return (ProgramConnectionHandlerMO) AppContext.getDataManager().getBinding(HANDLER_MO_NAME);
        } catch (NameNotBoundException ex) {
            // If no object is registered yet, create one and register it.
            ProgramConnectionHandlerMO retVal = new ProgramConnectionHandlerMO();
            AppContext.getDataManager().setBinding(HANDLER_MO_NAME, retVal);
            return retVal;
        }
    }

    static public void createProgram(CellID cellID, String uri) {
        getInstance().connectionHandler.createProgram(cellID, uri);
    }

}
