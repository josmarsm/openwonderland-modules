/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.ezscript.client.loaders;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.EventBridgeSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.GlobalSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.IBindingsLoader;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.ScriptedObjectDataSource;
import org.jdesktop.wonderland.modules.ezscript.client.generators.GeneratedCellMethod;
import org.jdesktop.wonderland.modules.ezscript.client.generators.javascript.BridgeGenerator;
import org.jdesktop.wonderland.modules.ezscript.client.generators.javascript.GlobalsGenerator;
import org.jdesktop.wonderland.modules.ezscript.client.generators.javascript.MethodGenerator;
import org.jdesktop.wonderland.modules.ezscript.client.generators.javascript.ReturnableMethodGenerator;

/**
 *
 * @author Ryan
 * @author Abhishek Upadhyay
 */
public class OptimizedLoader implements IBindingsLoader {

    private ScriptEngine engine = null;
    private Bindings bindings = null;
    private final ScannedClassLoader classLoader;
    private final ScriptEngineManager manager;

    public OptimizedLoader() {
        classLoader = LoginManager.getPrimary().getClassloader();
        manager = new ScriptEngineManager(classLoader);
    }

    public void loadBindings() {
        engine = manager.getEngineByName("JavaScript");
        bindings = engine.createBindings();

        generateBindings(engine, bindings);


//        bindings.putAll(ScriptedObjectDataSource.INSTANCE.getCellBindings());
    }

    private ScriptedObjectDataSource dao() {
        return ScriptedObjectDataSource.INSTANCE;
        
    }
    
    
    public ScriptEngine getEngine() {
        return engine;
    }

    public Bindings getBindings() {
        return bindings;
    }

    private String generateCommandFactory(ScriptEngine engine, Bindings bindings) {

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



        bindScript(engine, void_factory, bindings);

        bindScript(engine, returnable_factory, bindings);

        return void_factory;

    }

    private synchronized Bindings generateBindings(ScriptEngine engine, Bindings bindings) {
//        Bindings bindings = engine.createBindings();

        generateCommandFactory(engine, bindings);



        generateVoidMethods(engine, bindings);
        generateNonVoidMethods(engine, bindings);
        generateCellFactories(engine, bindings);
//        generateBridges(bindings);
        generateGlobals(engine, bindings);
//        generateVirtuals(bindings);

        return bindings;
    }

    private void generateBridges(ScriptEngine engine,Bindings scriptBindings) {

        BridgeGenerator bridgeGenerator = new BridgeGenerator();

        for (EventBridgeSPI bridge : dao().getBridges()) {
            bridgeGenerator.setActiveBridge(bridge);
            bindScript(engine, bridgeGenerator.generateScriptBinding(),
                    scriptBindings);
            bridge.initialize(engine, scriptBindings);
        }
    }

    private void generateNonVoidMethods(ScriptEngine engine,Bindings scriptBindings) {
        //grab all returnablesa
        ReturnableMethodGenerator returnableGenerator = new ReturnableMethodGenerator(engine, scriptBindings);


        for (ReturnableScriptMethodSPI returnable : dao().getReturnables()) {
            returnableGenerator.setActiveMethod(returnable);

//            System.out.println("[EZSCRIPT] Generating function: " + returnable.getFunctionName());



            bindScript(engine, returnableGenerator.generateScriptBinding(),
                    scriptBindings);
        }
    }

    private void generateCellFactories(ScriptEngine engine,Bindings scriptBindings) {
        ReturnableMethodGenerator generator = new ReturnableMethodGenerator(engine, scriptBindings);

        for (CellFactorySPI factory : dao().getCellFactories()) {
            final ReturnableScriptMethodSPI returnable = new GeneratedCellMethod(factory);
            generator.setActiveMethod(returnable);

//            System.out.println("[EZSCRIPT] Generating function: " + returnable.getFunctionName());


            bindScript(engine, generator.generateScriptBinding(),
                    scriptBindings);

        }
    }

    private void generateVoidMethods(ScriptEngine engine,Bindings scriptBindings) {

        //grab all global void methods
        MethodGenerator methodGenerator = new MethodGenerator(engine, scriptBindings);

        for (final ScriptMethodSPI method : dao().getVoids()) {
            methodGenerator.setActiveMethod(method);
//            System.out.println("[EZSCRIPT] Generating function: " + method.getFunctionName());

            bindScript(engine, methodGenerator.generateScriptBinding(),
                    scriptBindings);

        }
    }

    private void generateGlobals(ScriptEngine engine,Bindings scriptBindings) {
        GlobalsGenerator globalGenerator = new GlobalsGenerator(engine, scriptBindings);
        for (final GlobalSPI global : dao().getGlobals()) {
            globalGenerator.setActiveGlobal(global);

//            System.out.println("[EZSCRIPT] Generating GLOBAL: " + global.getName());


            bindScript(engine, globalGenerator.generateScriptBinding(),
                    scriptBindings);
        }
    }

    private synchronized void bindScript(ScriptEngine engine, String script, Bindings bindings) {
        try {
            engine.eval(script, bindings);
        } catch (ScriptException ex) {
            Logger.getLogger(OptimizedLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
