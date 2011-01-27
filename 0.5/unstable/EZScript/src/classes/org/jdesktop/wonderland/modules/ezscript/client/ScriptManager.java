/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import javax.swing.JDialog;
import org.jdesktop.wonderland.client.input.InputManager;

/**
 *
 * @author JagWire
 */
public class ScriptManager {

    private ScriptEditorPanel scriptEditor;
    private JDialog dialog;
    

    private static ScriptManager instance;

    public static ScriptManager getInstance() {
        if(instance == null) {
            instance = new ScriptManager();
        }
        return instance;
    }

    private ScriptManager() {
        dialog = new JDialog();

        dialog.setTitle("Script Editor - Wonderland Client");
        //2. Optional: What happens when the frame closes?
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        //3. Create component and put them in the frame.
        dialog.setContentPane(new ScriptEditorPanel( dialog));

        //4. Size the frame.
        dialog.pack();
        
    }
    public void showScriptEditor() {
        dialog.setVisible(true);
    }
}
