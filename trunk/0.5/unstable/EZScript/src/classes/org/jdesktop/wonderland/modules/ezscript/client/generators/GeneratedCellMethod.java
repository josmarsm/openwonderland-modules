/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.generators;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.ezscript.client.EZScriptComponentFactory;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;

/**
 *
 * @author JagWire
 */


public class GeneratedCellMethod implements ReturnableScriptMethodSPI {

    private CellFactorySPI factory = null;
    private String givenCellName = null;
    private CellID givenCellID = null;
    private CellID generatedCellID = null;
    private static final Logger logger = Logger.getLogger(GeneratedCellMethod.class.getName());
    
//    private Semaphore lock = new Semaphore(0);
    
    public GeneratedCellMethod(CellFactorySPI factory) {
        this.factory = factory;
    }
    
    public String getDescription() {
        
        if(factory.getDisplayName() == null) {
            return "Create "+factory.getClass().getName().replace("class", "").replace(".","_")+" cell.\n"
                + "-- usage: var c = "+getFunctionName()+"();\n"
                + "-- automatically adds EZScriptComponent to cell.";
        }
        
        return "Create "+factory.getDisplayName()+" cell.\n"
                + "-- usage: var c = "+getFunctionName()+"();\n"
                + "-- automatically adds EZScriptComponent to cell.";
    }

    public String getFunctionName() {
        if(factory.getDisplayName() == null) {
            return factory.getClass().getName().replace("class", "")
                                               .replace(".", "_");
        }
        return factory.getDisplayName().replace(' ', '_')
                                       .replace('(','_')
                                       .replace(')', '_');
    }

    public String getCategory() {
        return "Insert Object";
    }

    public void setArguments(Object[] args) {
//        throw new UnsupportedOperationException("Not supported yet.");
        givenCellName = null;
        givenCellID = null;
        generatedCellID = null;

        if (args.length == 0) {
            return;
        }

        if (args.length >= 1) {

            if (!(args[0] instanceof String)) {
                givenCellID = (CellID) args[0];
            } else {
                givenCellName = (String) args[0];
            }

        }

        if (args.length > 1) {
            givenCellName = (String) args[1];
        }

    }

    public Object returns() {
        CellCache cellCache = null;

        if (generatedCellID == null) {
            logger.warning("RESULTING CELLID IS NULL, RETURNING NULL!");
            return null;
        }

        //get wonderland session
        WonderlandSession session = LoginManager.getPrimary().getPrimarySession();

        //use session to get cell cache
        cellCache = ClientContextJME.getCellCache(session);

        //use cell cache to get cell from resultingCellID


        return cellCache.getCell(generatedCellID);

    }

    public void run() {

        CellServerState state = factory.getDefaultCellServerState(null);

        EZScriptComponentFactory ezFactory = new EZScriptComponentFactory();
        if (givenCellName != null) {
            state.setName(givenCellName);
        }

        state.addComponentServerState(ezFactory.getDefaultCellComponentServerState());

        try {
            if (givenCellID == null) {
                generatedCellID = CellUtils.createCell(state);
            } else {
                generatedCellID = CellUtils.createCell(state, givenCellID);
            }


        } catch (CellCreationException ex) {
            Logger.getLogger(GeneratedCellMethod.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
//            lock.release();
        }

    }
    
    
}
