/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.util.ArrayList;
import java.util.List;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class GetClasspathJars implements ReturnableScriptMethodSPI {

    private List<String> jars;
    public String getDescription() {
        return "Returns a list of all JAR names within the current classpath.";
    }

    public String getFunctionName() {
        return "GetClasspathJars";
    }

    public String getCategory() {
        return "utilities";
    }

    public void setArguments(Object[] args) {
        jars = new ArrayList<String>();
    }

    public Object returns() {
        return jars;
    }

    public void run() {
        String s = System.getProperty("java.class.path");
        System.out.println("OS name: " +System.getProperty("os.name"));
        System.out.println(System.getProperty("java.class.path"));
        
        if(System.getProperty("os.name").toUpperCase().contains("WIN")) {
            //we're in Windows land... just print out the string.
            System.out.println("In Windows...");
            
            jars.add(s);
            return;
        }
        for(String string: process(s)) {
            jars.add(string);
        }
        
    }
    
    public String[] process(String in) {
        return in.split(":");
        
    }
    
}
