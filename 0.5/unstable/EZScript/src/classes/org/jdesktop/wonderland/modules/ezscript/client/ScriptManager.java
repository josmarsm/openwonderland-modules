/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
public class ScriptManager {

    private ScriptEditorPanel scriptEditor;
    private JDialog dialog;
    private ScriptEditorPanel panel;
    private ScriptEngineManager engineManager;// = new ScriptEngineManager(LoginManager.getPrimary().getClassloader());
    private ScriptEngine scriptEngine = null;
    private Bindings scriptBindings = null;

    private static ScriptManager instance;

    public static ScriptManager getInstance() {
        if(instance == null) {
            instance = new ScriptManager();
        }
        return instance;
    }

    private ScriptManager() {
        dialog = new JDialog();
        panel = new ScriptEditorPanel(dialog);
        dialog.setTitle("Script Editor - Wonderland Client");
        //2. Optional: What happens when the frame closes?
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        //3. Create component and put them in the frame.
        dialog.setContentPane(panel);

        //4. Size the frame.
        dialog.pack();

        //Next, acquire the scripting magicry
        engineManager = new ScriptEngineManager(LoginManager.getPrimary().getClassloader());
        scriptEngine = engineManager.getEngineByName("JavaScript");
        scriptBindings = scriptEngine.createBindings();

        //Add the necessary script bindings
        scriptBindings.put("Client", ClientContextJME.getClientMain());
        
        
        //Load the methods into the library
        ScannedClassLoader loader = LoginManager.getPrimary().getClassloader();
        Iterator<ScriptMethodSPI> iter = loader.getInstances(ScriptMethod.class,
                                                        ScriptMethodSPI.class);
        //grab all global void methods
        while(iter.hasNext()) {
            final ScriptMethodSPI method = iter.next();
            addFunctionBinding(method);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    panel.addLibraryEntry(method);
                }
            });
        }

        //grab all returnablesa
        Iterator<ReturnableScriptMethodSPI> returnables
                            = loader.getInstances(ReturnableScriptMethod.class,
                                               ReturnableScriptMethodSPI.class);
        while(returnables.hasNext()) {
            ReturnableScriptMethodSPI method = returnables.next();
            addFunctionBinding(method);
            panel.addLibraryEntry(method);
        }
    }
    public void evaluate(String script) {
        try {
            scriptEngine.eval(script, scriptBindings);
        } catch (ScriptException ex) {
            ex.printStackTrace();
            //Logger.getLogger(ScriptManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void showScriptEditor() {
        dialog.setVisible(true);
    }

    public void addFunctionBinding(ScriptMethodSPI method) {
        scriptBindings.put("this"+method.getFunctionName(), method);
        String scriptx  = "function " + method.getFunctionName()+"() {\n"
            + "\tvar args = java.lang.reflect.Array.newInstance(java.lang.Object, arguments.length);\n"
            + "\tfor(var i = 0; i < arguments.length; i++) {\n"
            + "\targs[i] = arguments[i];\n"
            + "\t}\n"

           // + "\targs = "+method.getFunctionName()+".arguments;\n"
            + "\tthis"+method.getFunctionName()+".setArguments(args);\n"
            + "\tthis"+method.getFunctionName()+".run();\n"
            +"}";

        try {
            //System.out.println("evaluating script: \n"+scriptx);
            scriptEngine.eval(scriptx, scriptBindings);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public void addFunctionBinding(ReturnableScriptMethodSPI method) {
        scriptBindings.put("this"+method.getFunctionName(), method);
        String scriptx  = "function " + method.getFunctionName()+"() {\n"
            + "\tvar args = java.lang.reflect.Array.newInstance(java.lang.Object, arguments.length);\n"
            + "\tfor(var i = 0; i < arguments.length; i++) {\n"
            + "\t\targs[i] = arguments[i];\n"
            + "\t}\n"
            + "\tthis"+method.getFunctionName()+".setArguments(args);\n"
            + "\tthis"+method.getFunctionName()+".run();\n"

            + "\tvar tmp = this"+method.getFunctionName()+".returns();\n"
            + "\treturn tmp\n;"
            +"}";

        try {
           // System.out.println("evaluating script: \n"+scriptx);
            scriptEngine.eval(scriptx, scriptBindings);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
