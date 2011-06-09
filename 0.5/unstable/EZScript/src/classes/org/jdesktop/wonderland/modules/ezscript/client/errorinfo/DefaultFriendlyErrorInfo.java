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
public class DefaultFriendlyErrorInfo implements FriendlyErrorInfoSPI {

    public String getSummary() {
        return "EZScript was unable to execute your script!";
    }

    public List<String> getSolutions() {
        List<String> solutions = new ArrayList<String>();
        solutions.add("Post the text from the details tab to the open wonderland forum.");
        return solutions;
    }

}
