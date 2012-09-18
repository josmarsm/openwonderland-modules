/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.generators.javascript;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.generators.GeneratorSPI;

/**
 *
 * @author Ryan
 */
public class MethodGenerator implements GeneratorSPI {

    private final ScriptEngine engine;
    private final Bindings bindings;
    private ScriptMethodSPI method;

    public MethodGenerator(ScriptEngine engine, Bindings bindings) {
//        super(engine, bindings);
        this.engine = engine;
        this.bindings = bindings;
    }

    public void setActiveMethod(ScriptMethodSPI method) {
        this.method = method;
    }

    @Override
    public String generateScriptBinding() {

        bindings.put("I"+method.getFunctionName(), method);
//        String scriptx  = "var "+method.getFunctionName()+" = function() {\n"
//            + "\tvar args = java.lang.reflect.Array.newInstance(java.lang.Object, arguments.length);\n"
//            + "\tfor(var i = 0; i < arguments.length; i++) {\n"
//            + "\t\targs[i] = arguments[i];\n"
//            + "\t}\n"
//
//           // + "\targs = "+method.getFunctionName()+".arguments;\n"
//            + "\tI"+method.getFunctionName()+".setArguments(args);\n"
//            + "\tI"+method.getFunctionName()+".run();\n"
//            +"}";
        String IFace = "I"+method.getFunctionName();
        String scriptx = "var " + method.getFunctionName()+" = command_factory("+IFace+");";
        
        
        
        
        
        
//            System.out.println(scriptx);
//        bindings.put("Factory", PassthruClassInstantiator.INSTANCE);
//        String scriptx = "function " + method.getFunctionName() + "() {\n"
//                + "\timportClass("+method.getClass().getName()+");\n"
//                + "\tvar obj = Factory.instantiate(" + method.getClass().getName() + ");\n"
//                + "\tvar args = java.lang.reflect.Array.newInstance(java.lang.Object, arguments.length);\n"
//                + "\tfor(var i = 0; i < arguments.length; i++) {\n"
//                + "\t args[i] = arguments[i];\n"
//                + "\t}\n"
//                + "\tobj.setArguments(args);\n"
//                + "\tobj.run();"
//                + "}";
//
        return scriptx;
    }
}
