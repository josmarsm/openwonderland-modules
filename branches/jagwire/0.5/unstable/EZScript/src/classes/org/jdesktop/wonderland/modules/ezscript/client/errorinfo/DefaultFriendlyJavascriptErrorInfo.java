/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.errorinfo;

import java.util.ArrayList;
import java.util.List;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.FriendlyErrorInfoSPI;

/**
 *
 * @author JagWire
 */
public class DefaultFriendlyJavascriptErrorInfo implements FriendlyErrorInfoSPI {
    private final String name;

    public DefaultFriendlyJavascriptErrorInfo(String name) {
        this.name = name;
    }

    public String getSummary() {
        return "EZScript had trouble running the javascript you entered for object: "+name+".";
    }

    public List<String> getSolutions() {
        List<String> solutions = new ArrayList<String>();

        solutions.add("Double check capitalization of variables, function names, and script commands.");
        solutions.add("Double check for missing semicolons.");
        solutions.add("Double check for missing curly brackets.");

        return solutions;
    }

}
