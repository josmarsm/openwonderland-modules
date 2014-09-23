/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.ezscript.client.loaders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.jdesktop.wonderland.modules.ezscript.client.SPI.IBindingsLoader;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.ScriptedObjectDataSource;
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
 * @author Abhishek Upadhyay
 */
public class SerialLoader implements IBindingsLoader {

    private ScriptEngineManager manager = null;
    private ScriptEngine engine = null;
    private Bindings bindings = null;
    private ScannedClassLoader classLoader = null;

    public SerialLoader() {
        classLoader = LoginManager.getPrimary().getClassloader();
        manager = new ScriptEngineManager(classLoader);
    }

    public void loadBindings() {
        engine = manager.getEngineByName("JavaScript");
        bindings = engine.createBindings();

        //TODO: go through all the various bindings and put them in our bindings
        //object


//        List<EventBridgeSPI> bridges = listFromIterator(instances(classLoader,
//                EventBridge.class,
//                EventBridgeSPI.class));


        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                List<ScriptMethodSPI> voids = listFromIterator(instances(classLoader,
                        ScriptMethod.class,
                        ScriptMethodSPI.class));
                List<ReturnableScriptMethodSPI> nonvoids = listFromIterator(
                        instances(classLoader,
                        ReturnableScriptMethod.class,
                        ReturnableScriptMethodSPI.class));
                List<CellFactorySPI> cellFactories = listFromIterator(
                        instances(classLoader,
                        CellFactory.class,
                        CellFactorySPI.class));
                List<GlobalSPI> globals = listFromIterator(instances(classLoader,
                        Global.class,
                        GlobalSPI.class));

                generateCommandFactory(bindings);
                generateVoidMethods(bindings, voids);
                generateNonVoidMethods(bindings, nonvoids);
                generateCellFactories(bindings, cellFactories);
                generateGlobals(bindings, globals);
            }
        });






    }

    public ScriptEngine getEngine() {
        return engine;
    }

    public Bindings getBindings() {
        return bindings;
    }

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

    private void generateBridges(Bindings scriptBindings, List<EventBridgeSPI> bridges) {

        BridgeGenerator bridgeGenerator = new BridgeGenerator();


        for (EventBridgeSPI bridge : bridges) {
            bridgeGenerator.setActiveBridge(bridge);
            bindScript(bridgeGenerator.generateScriptBinding(),
                    scriptBindings);
            bridge.initialize(engine, scriptBindings);
        }
    }

    private void generateNonVoidMethods(Bindings scriptBindings,
            List<ReturnableScriptMethodSPI> returnables) {
        //grab all returnablesa
        ReturnableMethodGenerator returnableGenerator = new ReturnableMethodGenerator(engine, scriptBindings);


        for (ReturnableScriptMethodSPI returnable : returnables) {
            returnableGenerator.setActiveMethod(returnable);

//            System.out.println("[EZSCRIPT] Generating function: " + returnable.getFunctionName());



            bindScript(returnableGenerator.generateScriptBinding(),
                    scriptBindings);
        }
    }

    private void generateCellFactories(Bindings scriptBindings, List<CellFactorySPI> factories) {
        ReturnableMethodGenerator generator = new ReturnableMethodGenerator(engine, scriptBindings);

        for (CellFactorySPI factory : factories) {
            final ReturnableScriptMethodSPI returnable = new GeneratedCellMethod(factory);
            generator.setActiveMethod(returnable);

//            /System.out.println("[EZSCRIPT] Generating function: " + returnable.getFunctionName());


            bindScript(generator.generateScriptBinding(),
                    scriptBindings);

        }
    }

    private void generateVoidMethods(Bindings scriptBindings, List<ScriptMethodSPI> voids) {

        //grab all global void methods
        MethodGenerator methodGenerator = new MethodGenerator(engine, scriptBindings);

        for (final ScriptMethodSPI method : voids) {
            methodGenerator.setActiveMethod(method);
//            System.out.println("[EZSCRIPT] Generating function: " + method.getFunctionName());

            bindScript(methodGenerator.generateScriptBinding(),
                    scriptBindings);

        }
    }

    private void generateGlobals(Bindings scriptBindings, List<GlobalSPI> globals) {
        GlobalsGenerator globalGenerator = new GlobalsGenerator(engine, scriptBindings);
        for (final GlobalSPI global : globals) {
            globalGenerator.setActiveGlobal(global);

//            System.out.println("[EZSCRIPT] Generating GLOBAL: " + global.getName());


            bindScript(globalGenerator.generateScriptBinding(),
                    scriptBindings);
        }
    }

//    private synchronized Bindings generateBindings() {
//        Bindings bindings = engine.createBindings();
//
//        generateCommandFactory(bindings);
//
//
//
//        generateVoidMethods(bindings);
//        generateNonVoidMethods(bindings);
//        generateCellFactories(bindings);
////        generateBridges(bindings);
//        generateGlobals(bindings);
////        generateVirtuals(bindings);
//
//        return bindings;
//    }
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

    private <T> Iterator<T> instances(ScannedClassLoader loader, Class annotation, Class spi) {
        return loader.getInstances(annotation, spi);
    }
}
