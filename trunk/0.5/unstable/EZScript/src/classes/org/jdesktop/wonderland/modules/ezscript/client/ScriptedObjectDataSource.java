/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.EventBridgeSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.GlobalSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.EventBridge;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.Global;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.generators.GeneratedCellMethod;
import org.jdesktop.wonderland.modules.ezscript.client.generators.javascript.BridgeGenerator;
import org.jdesktop.wonderland.modules.ezscript.client.generators.javascript.GlobalsGenerator;
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
   private ArrayList<GlobalSPI> globals;
   
   private ScriptEngineManager manager = null;
   private ScriptEngine engine = null;
   private Bindings clientBindings = null;
   private Bindings cellBindings = null;
   
   private static final Logger logger = Logger.getLogger(ScriptedObjectDataSource.class.getName());
   
   private boolean initialized = false;
   
   ScriptedObjectDataSource() {
       
   }

    public void initialize() {
        
        if(initialized)
            return;
        
        
        ScannedClassLoader loader = LoginManager.getPrimary().getClassloader();

        bridges = listFromIterator(instances(loader, EventBridge.class, EventBridgeSPI.class));
        
//        bridges = listFromIterator(loader.getInstances(EventBridge.class, EventBridgeSPI.class));
        returnables = listFromIterator(instances(loader,
                                                ReturnableScriptMethod.class,
                                                ReturnableScriptMethodSPI.class));
//        returnables = listFromIterator(loader.getInstances(ReturnableScriptMethod.class,
//                ReturnableScriptMethodSPI.class));
        voids = listFromIterator(instances(loader,
                                            ScriptMethod.class,
                                            ScriptMethodSPI.class));
//        voids = listFromIterator(loader.getInstances(ScriptMethod.class, ScriptMethodSPI.class));
        cellFactories = listFromIterator(instances(loader, CellFactory.class, CellFactorySPI.class));
//        cellFactories = listFromIterator(loader.getInstances(CellFactory.class, CellFactorySPI.class));
        
        globals = listFromIterator(instances(loader, Global.class, GlobalSPI.class));
        
        logger.warning("OBTAINED SCRIPTED OBJECT DATA!");
        
        manager = new ScriptEngineManager(LoginManager.getPrimary().getClassloader());
        engine = manager.getEngineByName("JavaScript");
        clientBindings = engine.createBindings();
        cellBindings = engine.createBindings();
        
        clientBindings.putAll(generateBindings());
        logger.warning("CLIENT BINDINGS SIZE: "+clientBindings.size());
        cellBindings.putAll(cloneBindings(clientBindings));
        logger.warning("CELL BINDINGS SIZE: "+cellBindings.size());
        
        
        initialized = true;
    }
   
    private <T> Iterator<T> instances(ScannedClassLoader loader, Class annotation, Class spi) {
       return loader.getInstances(annotation, spi);
    }
    
    
    public Bindings getClientBindings() {
        Bindings bindings = engine.createBindings();
        bindings.putAll(clientBindings);
        
        return bindings;
    }
    
    public Bindings getCellBindings() {
        Bindings bindings = engine.createBindings();
        bindings.putAll(cellBindings);
        
        return bindings;
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
    
    private void bindScript(String script, Bindings bindings) {
        try {
            engine.eval(script, bindings);
        } catch (ScriptException ex) {
            Logger.getLogger(ScriptedObjectDataSource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public ScriptEngine getClientScriptEngine() {
        return engine;
    }
    private Bindings cloneBindings(Bindings bs) {
        Bindings bindings = engine.createBindings();
        bindings.putAll(bs);
        
        return bindings;
    }
    
    private Bindings generateBindings() {
        Bindings bindings = engine.createBindings();
        
        generateVoidMethods(bindings);
        generateNonVoidMethods(bindings);
        generateCellFactories(bindings);
        generateBridges(bindings);
        generateGlobals(bindings);
        
        return bindings;
    }
    
    private void generateBridges(Bindings scriptBindings) {

        BridgeGenerator bridgeGenerator = new BridgeGenerator();
        
        for(EventBridgeSPI bridge:  getBridges()) {
            bridgeGenerator.setActiveBridge(bridge);
            bindScript(bridgeGenerator.generateScriptBinding(),
                       scriptBindings);
            bridge.initialize(engine, scriptBindings);
        }
    }
    
    private void generateNonVoidMethods(Bindings scriptBindings) {
        //grab all returnablesa
        ReturnableMethodGenerator returnableGenerator
                = new ReturnableMethodGenerator(engine, scriptBindings);

        
        for(final ReturnableScriptMethodSPI returnable:  getReturnables()) {
            returnableGenerator.setActiveMethod(returnable);
            bindScript(returnableGenerator.generateScriptBinding(),
                       scriptBindings);
        }
    }
    
    private void generateCellFactories(Bindings scriptBindings) {                
        ReturnableMethodGenerator generator
                = new ReturnableMethodGenerator(engine, scriptBindings);
        
        for( CellFactorySPI factory:  getCellFactories()) {
            final ReturnableScriptMethodSPI returnable = new GeneratedCellMethod(factory);
            generator.setActiveMethod(returnable);
            bindScript(generator.generateScriptBinding(),
                       scriptBindings);

        }
    }
    
    private void generateVoidMethods(Bindings scriptBindings) {
        
        //grab all global void methods
        MethodGenerator methodGenerator
                = new MethodGenerator(engine, scriptBindings);
        
        for(final ScriptMethodSPI method:  getVoids()) {
            methodGenerator.setActiveMethod(method);
            bindScript(methodGenerator.generateScriptBinding(),
                      scriptBindings);

        }
    }

    private void generateGlobals(Bindings scriptBindings) {
        GlobalsGenerator globalGenerator
                = new GlobalsGenerator(engine, scriptBindings);
        for(final GlobalSPI global: getGlobals()) {
            globalGenerator.setActiveGlobal(global);
            bindScript(globalGenerator.generateScriptBinding(),
                        scriptBindings);
        }
    }

    private ArrayList<GlobalSPI> getGlobals() {
        return globals;
    }
   
}
