/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client;

import java.util.ArrayList;
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
   
   private ArrayList<EventBridgeSPI> bridges;
   private ArrayList<ReturnableScriptMethodSPI> returnables;
   private ArrayList<ScriptMethodSPI> voids;
   private ArrayList<CellFactorySPI> cellFactories;
   
   private static final Logger logger = Logger.getLogger(ScriptedObjectDataSource.class.getName());
   
   private boolean initialized = false;
   
   ScriptedObjectDataSource() {
       
   }

    public void initialize() {
        
        if(initialized)
            return;
        
        logger.warning("OBTAINING SCRIPTED OBJECT DATA!");
        ScannedClassLoader loader = LoginManager.getPrimary().getClassloader();

        bridges = listFromIterator(loader.getInstances(EventBridge.class, EventBridgeSPI.class));
        returnables = listFromIterator(loader.getInstances(ReturnableScriptMethod.class,
                ReturnableScriptMethodSPI.class));
        voids = listFromIterator(loader.getInstances(ScriptMethod.class, ScriptMethodSPI.class));
        cellFactories = listFromIterator(loader.getInstances(CellFactory.class, CellFactorySPI.class));
        
        initialized = true;
    }
   
    public ArrayList<EventBridgeSPI> getBridges() {
        return bridges;
    }

    public ArrayList<CellFactorySPI> getCellFactories() {
        return cellFactories;
    }

    public ArrayList<ReturnableScriptMethodSPI> getReturnables() {
        return returnables;
    }

    public ArrayList<ScriptMethodSPI> getVoids() {
        return voids;
    }
    
    private <T> ArrayList listFromIterator(Iterator<T> iter) {
        ArrayList<T> list = new ArrayList<T>();
        
        while(iter.hasNext()) {
            list.add(iter.next());
        }
        
        
        return list;
        
        
    }
   
}
