/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.modules.ezscript.client.EZScriptComponent;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.ScriptManager;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class IncludeMethod implements ScriptMethodSPI {

    private EZScriptComponent context;
    private boolean fail = false;
    private String filename = "";
    private boolean isGlobal = false;
    //private ScriptManager manager = null;

    public String getFunctionName() {
        return "Include";
    }

    public void setArguments(Object[] args) {
        //throw new UnsupportedOperationException("Not supported yet.");
        if(!(args[0] instanceof EZScriptComponent)) {
           isGlobal = true;
           //ScriptManager.getInstance().evaluate(filename);
        } else {
            context = ((EZScriptComponent)args[0]);
        }

        filename = (String)args[1];
    }

    public String getDescription() {
        return "Loads and evaluates a script file saved on disk.\n" +
                "-- usage: Include(Context, 'MyScript.ez');\n" +
                "-- usage: Include('MyScript.ez');\n" +
                "-- extension does not matter.\n" +
                "-- method looks within ~/.wonderland/0.5-dev/scripts/" +
                "-- specifying a context loads the script file per-cell.\n" +
                "-- not specifying a context, loads the script file for the client, outside the cell's scope.";
    }

    public String getCategory() {
        return "utilities";
    }

    public void run() {
        if(fail)
            return;

        try {
            String script = retrieveStartupScript();
            if (!isGlobal) {
                context.evaluateScript(script);
            } else {
                ScriptManager.getInstance().evaluate(script);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }        
    }

    public String retrieveStartupScript() throws IOException {
        File dir = ClientContext.getUserDirectory("scripts");
        String script = new String();

        File startup = new File(dir, filename);
        //check to see if the file is there
        if (!startup.exists()) {
            //if not...
            //fail gracefully
            return "";
        }

        //so the script definitely exists...
        FileInputStream in = new FileInputStream(startup);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String line;
        while ((line = br.readLine()) != null) {
            script += "\n" + line;

        }
        br.close();
        return script;
    }




}
