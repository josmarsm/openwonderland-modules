/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.content.spi.ContentImporterSPI;

/**
 *
 * @author JagWire
 */
public class ScriptImporter implements ContentImporterSPI {

    public String[] getExtensions() {
        return new String[] { "ez" };
    }

    public String importFile(File file, String extension) {
        //throw new UnsupportedOperationException("Not supported yet.");
        return importFile(file, extension, false);
    }

    public String importFile(File file, String extension, boolean createCell) {
        try {
            //throw new UnsupportedOperationException("Not supported yet.");
            ScriptManager.getInstance().evaluate(retrieveScript(file));
        } catch (IOException ex) {
            Logger.getLogger(ScriptImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new String("");
    }

    private String retrieveScript(File scriptFile) throws IOException {
        String script = new String();
        //so the script definitely exists...
        FileInputStream in = new FileInputStream(scriptFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String line;
        while ((line = br.readLine()) != null) {
            script += "\n" + line;

        }
        br.close();
        return script;
    }

}
