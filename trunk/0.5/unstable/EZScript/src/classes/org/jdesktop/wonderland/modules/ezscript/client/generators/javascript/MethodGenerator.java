/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.generators.javascript;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import org.jdesktop.wonderland.modules.ezscript.client.PassthruClassInstantiator;
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

        bindings.put("this"+method.getFunctionName(), method);
        String scriptx  = "function " + method.getFunctionName()+"() {\n"
            + "\tvar args = java.lang.reflect.Array.newInstance(java.lang.Object, arguments.length);\n"
            + "\tfor(var i = 0; i < arguments.length; i++) {\n"
            + "\targs[i] = arguments[i];\n"
            + "\t}\n"

           // + "\targs = "+method.getFunctionName()+".arguments;\n"
            + "\tthis"+method.getFunctionName()+".setArguments(args);\n"
            + "\tthis"+method.getFunctionName()+".run();\n"
            +"}";
  

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
