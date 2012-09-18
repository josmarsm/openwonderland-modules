/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.generators.javascript;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.generators.GeneratorSPI;

/**
 *
 * @author JagWire
 */
public class ReturnableMethodGenerator implements GeneratorSPI {
    private ReturnableScriptMethodSPI method;
    private final Bindings bindings;
    private final ScriptEngine scriptEngine;

    public ReturnableMethodGenerator(ScriptEngine scriptEngine, Bindings bindings) {
        this.scriptEngine = scriptEngine;
        this.bindings = bindings;
    }
    
    public void setActiveMethod(ReturnableScriptMethodSPI method) {
        this.method = method;
    }
    
        
    public String generateScriptBinding() {
        bindings.put("I"+method.getFunctionName(), method);
        
//        String scriptx  = "var "+method.getFunctionName()+" = function() {\n"
//            + "\tvar args = java.lang.reflect.Array.newInstance(java.lang.Object, arguments.length);\n"
//            + "\tfor(var i = 0; i < arguments.length; i++) {\n"
//            + "\t\targs[i] = arguments[i];\n"
//            + "\t}\n"
//            + "\tI"+method.getFunctionName()+".setArguments(args);\n"
//            + "\tI"+method.getFunctionName()+".run();\n"
//
//            + "\tvar tmp = I"+method.getFunctionName()+".returns();\n"
//            + "\treturn tmp;\n"
//            +"}";

        String iface = "I"+method.getFunctionName();
        String scriptx = "var "+method.getFunctionName()+" = returnable_factory("+iface+")";
        
        
        
//        System.out.println(scriptx);
        return scriptx;
    }
    
    
//    public String generateScriptBinding() {
//        bindings.put("Factory", PassthruClassInstantiator.INSTANCE);;
//        String scriptx = "function "+method.getFunctionName()+"() {\n"
//                + "\timportClass("+method.getClass().getName()+");\n"
//                + "\tvar obj = Factory.instantiate("+method.getClass().getName()+");\n"
//                + "\tvar args = java.lang.reflect.Array.newInstance(java.lang.Object, arguments.length);\n"
//                + "\tfor(var i = 0; i < arguments.length; i++) {\n"
//                + "\t args[i] = arguments[i];\n"
//                + "\t}\n"
//                + "\tobj.setArguments(args);\n"
//                + "\tobj.run();"
//                + "\tvar tmp = obj.returns();\n"
//                + "\treturn tmp\n" 
//                + "}";
//        
//        return scriptx;
//    }
    
}
