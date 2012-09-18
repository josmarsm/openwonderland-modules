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
public class DefaultFriendlyJavaErrorInfo implements FriendlyErrorInfoSPI {
    private final String name;

    public DefaultFriendlyJavaErrorInfo(String name) {
        this.name = name;
    }

    public String getSummary() {
        return "EZScript had a problem executing a script command you used for object: "+name+".";
    }

    public List<String> getSolutions() {
        List<String> solutions = new ArrayList<String>();

        solutions.add("Contact the author of the script command and send them the text from the details tab.");
        solutions.add("Refrain from using that scripting command until author fixes issue.");
        return solutions;
    }
}
