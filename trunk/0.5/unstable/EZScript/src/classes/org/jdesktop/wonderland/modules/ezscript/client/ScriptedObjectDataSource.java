/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client;

import java.util.Iterator;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.EventBridgeSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.EventBridge;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.generators.GeneratedCellMethod;
import org.jdesktop.wonderland.modules.ezscript.client.generators.javascript.BridgeGenerator;
import org.jdesktop.wonderland.modules.ezscript.client.generators.javascript.MethodGenerator;
import org.jdesktop.wonderland.modules.ezscript.client.generators.javascript.ReturnableMethodGenerator;

/**
 *
 * @author JagWire
 */
public enum ScriptedObjectDataSource {
   INSTANCE;
   
   private Iterator<EventBridgeSPI> bridges;
   private Iterator<ReturnableScriptMethodSPI> returnables;
   private Iterator<ScriptMethodSPI> voids;
   private Iterator<CellFactorySPI> cellFactories;
   
   private static final Logger logger = Logger.getLogger(ScriptedObjectDataSource.class.getName());
   
   private boolean initialized = false;
   
   ScriptedObjectDataSource() {
       
   }

    public void initialize() {
        
        if(initialized)
            return;
        
        logger.warning("OBTAINING SCRIPTED OBJECT DATA!");
        ScannedClassLoader loader = LoginManager.getPrimary().getClassloader();

        bridges = loader.getInstances(EventBridge.class, EventBridgeSPI.class);
        returnables = loader.getInstances(ReturnableScriptMethod.class,
                ReturnableScriptMethodSPI.class);
        voids = loader.getInstances(ScriptMethod.class, ScriptMethodSPI.class);
        cellFactories = loader.getInstances(CellFactory.class, CellFactorySPI.class);
        
        initialized = true;
    }
   
    public Iterator<EventBridgeSPI> getBridges() {
        return bridges;
    }

    public Iterator<CellFactorySPI> getCellFactories() {
        return cellFactories;
    }

    public Iterator<ReturnableScriptMethodSPI> getReturnables() {
        return returnables;
    }

    public Iterator<ScriptMethodSPI> getVoids() {
        return voids;
    }
   
}
