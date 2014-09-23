/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
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
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.EventBridgeSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.GlobalSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.*;
import org.jdesktop.wonderland.modules.ezscript.client.generators.GeneratedCellMethod;
import org.jdesktop.wonderland.modules.ezscript.client.generators.javascript.*;

/**
 *
 * @author JagWire
 * @author Abhishek Upadhyay
 */
public enum ScriptedObjectDataSource {

    INSTANCE;
    private ArrayList<EventBridgeSPI> bridges;
    private ArrayList<ReturnableScriptMethodSPI> returnables;
    private ArrayList<ScriptMethodSPI> voids;
    private ArrayList<CellFactorySPI> cellFactories;
    private ArrayList<GlobalSPI> globals;
//   private ArrayList<VirtualObjectFactorySPI> virtuals;
    private ScriptEngineManager manager = null;
    private ScriptEngine engine = null;
    private Bindings clientBindings = null;
    private Bindings cellBindings = null;
    private static final Logger logger = Logger.getLogger(ScriptedObjectDataSource.class.getName());
    private boolean initialized = false;

    private ScriptedObjectDataSource() {
        initialize();

    }

    private synchronized void initialize() {

//        System.out.println("[ScriptedObjectDataSource] INSIDE INITIALIZE!");
//        if(initialized)
//            return;

//        initialized = true;

        ScannedClassLoader loader = LoginManager.getPrimary().getClassloader();

//        logger.warning("RETRIEVING BRIDGES!");
        bridges = listFromIterator(instances(loader, EventBridge.class, EventBridgeSPI.class));

//        logger.warning("RETRIEVING RETURNABLES!");
        returnables = listFromIterator(instances(loader,
                ReturnableScriptMethod.class,
                ReturnableScriptMethodSPI.class));
//        logger.warning("RETRIEVING VOIDS!");
        voids = listFromIterator(instances(loader,
                ScriptMethod.class,
                ScriptMethodSPI.class));
//        logger.warning("RETRIVING CELL FACTORIES!");
        cellFactories = listFromIterator(instances(loader, CellFactory.class, CellFactorySPI.class));

//        logger.warning("RETRIEVING GLOBALS!");
        globals = listFromIterator(instances(loader, Global.class, GlobalSPI.class));

//        logger.warning("OBTAINED SCRIPTED OBJECT DATA!");

        manager = new ScriptEngineManager(LoginManager.getPrimary().getClassloader());
        engine = manager.getEngineByName("JavaScript");
        clientBindings = engine.createBindings();
        cellBindings = engine.createBindings();

        clientBindings.putAll(generateBindings());
//        logger.warning("CLIENT BINDINGS SIZE: "+clientBindings.size());
        cellBindings.putAll(cloneBindings(clientBindings));
//        logger.warning("CELL BINDINGS SIZE: "+cellBindings.size());



    }

    private <T> Iterator<T> instances(ScannedClassLoader loader, Class annotation, Class spi) {
        return loader.getInstances(annotation, spi);
    }

    public synchronized Bindings getClientBindings() {
        Bindings bindings = engine.createBindings();
        bindings.putAll(clientBindings);

        return bindings;
    }

    public synchronized Bindings getCellBindings() {
        Bindings bindings = engine.createBindings();
        bindings.putAll(cellBindings);

        return bindings;
    }

    public synchronized ArrayList<EventBridgeSPI> getBridges() {
        return bridges;
    }

    public synchronized ArrayList<CellFactorySPI> getCellFactories() {
        return cellFactories;
    }

    public synchronized ArrayList<ReturnableScriptMethodSPI> getReturnables() {
        return returnables;
    }

    public synchronized ArrayList<ScriptMethodSPI> getVoids() {
        return voids;
    }

    private <T> ArrayList listFromIterator(Iterator<T> iter) {
        ArrayList<T> list = new ArrayList<T>();

        while (iter.hasNext()) {
            list.add(iter.next());
        }

        return list;
    }

    private synchronized void bindScript(String script, Bindings bindings) {
        try {
            engine.eval(script, bindings);
        } catch (ScriptException ex) {
            Logger.getLogger(ScriptedObjectDataSource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ScriptEngine getClientScriptEngine() {
        return engine;
    }

    private synchronized Bindings cloneBindings(Bindings bs) {
        Bindings bindings = engine.createBindings();
        bindings.putAll(bs);

        return bindings;
    }

    private synchronized Bindings generateBindings() {
        Bindings bindings = engine.createBindings();

        generateCommandFactory(bindings);
        
        
        
        generateVoidMethods(bindings);
        generateNonVoidMethods(bindings);
        generateCellFactories(bindings);
//        generateBridges(bindings);
        generateGlobals(bindings);
//        generateVirtuals(bindings);

        return bindings;
    }

    private void generateBridges(Bindings scriptBindings) {

        BridgeGenerator bridgeGenerator = new BridgeGenerator();

        for (EventBridgeSPI bridge : getBridges()) {
            bridgeGenerator.setActiveBridge(bridge);
            bindScript(bridgeGenerator.generateScriptBinding(),
                    scriptBindings);
            bridge.initialize(engine, scriptBindings);
        }
    }

    private void generateNonVoidMethods(Bindings scriptBindings) {
        //grab all returnablesa
        ReturnableMethodGenerator returnableGenerator = new ReturnableMethodGenerator(engine, scriptBindings);


        for (ReturnableScriptMethodSPI returnable : getReturnables()) {
            returnableGenerator.setActiveMethod(returnable);
            
//            System.out.println("[EZSCRIPT] Generating function: "+returnable.getFunctionName());

            
            
            bindScript(returnableGenerator.generateScriptBinding(),
                    scriptBindings);
        }
    }

    private void generateCellFactories(Bindings scriptBindings) {
        ReturnableMethodGenerator generator = new ReturnableMethodGenerator(engine, scriptBindings);

        for (CellFactorySPI factory : getCellFactories()) {
            final ReturnableScriptMethodSPI returnable = new GeneratedCellMethod(factory);
            generator.setActiveMethod(returnable);
            
//            System.out.println("[EZSCRIPT] Generating function: "+returnable.getFunctionName());

            
            bindScript(generator.generateScriptBinding(),
                    scriptBindings);

        }
    }

    private void generateVoidMethods(Bindings scriptBindings) {

        //grab all global void methods
        MethodGenerator methodGenerator = new MethodGenerator(engine, scriptBindings);

        for (final ScriptMethodSPI method : getVoids()) {
            methodGenerator.setActiveMethod(method);
//            System.out.println("[EZSCRIPT] Generating function: "+method.getFunctionName());

            bindScript(methodGenerator.generateScriptBinding(),
                    scriptBindings);

        }
    }

    private void generateGlobals(Bindings scriptBindings) {
        GlobalsGenerator globalGenerator = new GlobalsGenerator(engine, scriptBindings);
        for (final GlobalSPI global : getGlobals()) {
            globalGenerator.setActiveGlobal(global);
            
//            System.out.println("[EZSCRIPT] Generating GLOBAL: "+global.getName());

            
            bindScript(globalGenerator.generateScriptBinding(),
                    scriptBindings);
        }
    }

    public synchronized ArrayList<GlobalSPI> getGlobals() {
        return globals;
    }
//    private void generateVirtuals(Bindings bindings) {
//        VirtualObjectGenerator generator 
//                = new VirtualObjectGenerator(engine, bindings);
//        
//        for(final VirtualObjectFactorySPI v: getVirtuals()) {
//            generator.setActiveVirtualObject(v);
//            bindScript(generator.generateScriptBinding(), bindings);
//        }
//    }
//    private ArrayList<VirtualObjectFactorySPI> getVirtuals() {
//        return virtuals;
//    }
    
    private String generateCommandFactory(Bindings bindings) {
        
        /**
         * The rhino javascript engine is replaced by nashorn javascript engine from java8
         * It has some different behavior. So we have changed some script here.
         */
        String firstArg = "";
        if(engine.getClass().getName().equals("jdk.nashorn.api.scripting.NashornScriptEngine")) {
            firstArg = "java.lang.Object.class";
        } else {
            firstArg = "java.lang.Object";
        }
        
        String void_factory = "function command_factory(iface) {\n"
                + "\t return function() {\n"
                + "\t\t var args = java.lang.reflect.Array.newInstance("+firstArg+", arguments.length);\n"
                + "\t\t for(var i = 0; i < arguments.length; i++) {\n"
                + "\t\t\t if(isNaN(parseFloat(arguments[i]))) {args[i] = arguments[i];} \n"
                + "\t\t\t else { args[i] = parseFloat(arguments[i]); }\n"
                + "\t\t }\n"
                + "\t\t iface.setArguments(args);\n"
                + "\t\t iface.run();\n"
                + "\t }\n"
                + "}\n";
        
//        System.out.println(void_factory);
        
        String returnable_factory = "function returnable_factory(iface) {\n"
                + "\t return function() {\n"
                + "\t\t var args = java.lang.reflect.Array.newInstance("+firstArg+", arguments.length);\n"
                + "\t\t for(var i = 0; i < arguments.length; i++) {\n"
                + "\t\t\t if(isNaN(parseFloat(arguments[i]))) {args[i] = arguments[i];} \n"
                + "\t\t\t else { args[i] = parseFloat(arguments[i]); }\n"
                + "\t\t }\n"
                + "\t\t iface.setArguments(args);\n"
                + "\t\t iface.run();\n"
                + "\t\t return iface.returns();\n"
                + "\t }\n"
                + "}\n";
        
        
        
        bindScript(void_factory, bindings);
        
        bindScript(returnable_factory, bindings);
        
        return void_factory;
        
    }
}
