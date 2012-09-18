/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.keybindings;

import java.util.Map;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author Jagwire
 */
@ScriptMethod
public class SetKeyBindingMethod implements ScriptMethodSPI {

    Map<Integer, Integer> bindings = null;
    String trigger;
    String key;
    private boolean fail = false;
    private static final Logger LOGGER = Logger.getLogger(SetKeyBindingMethod.class.getName());
    public String getFunctionName() {
        return "SetKeyBinding";
    }

    public void setArguments(Object[] args) {
        if(!(args[0] instanceof Map)) {
            fail = true;
            return;
        }
        bindings = (Map)args[0];
        trigger = ((String)args[1]).toUpperCase();
        key = ((String)args[2]).toUpperCase();       

        Integer keyCode = new Integer(KeyBindingUtils.getKeyFromString(key));
        Integer triggerCode = new Integer(KeyBindingUtils.getTriggerFromString(trigger));
        bindings.put(keyCode, triggerCode);
    }

    public String getDescription() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return "Set a key binding to a movement.";
    }

    public String getCategory() {
        return "KeyBinding";

    }

    public void run() {
        if(fail)
            return;

        // Fetch the current key bindings from the avatar and update the table
        // model. This assumes there is a primary view Cell.
        Cell cell = ClientContextJME.getViewManager().getPrimaryViewCell();
        if (cell == null) {
            LOGGER.warning("Unable to find primary view cell, is null.");
            return;
        }
        CellRenderer renderer = cell.getCellRenderer(RendererType.RENDERER_JME);
        if (!(renderer instanceof AvatarImiJME)) {
            LOGGER.warning("View cell renderer is not of the property type: " +
                    renderer.getClass().getName());
            return;
        }
        WlAvatarCharacter avatar = ((AvatarImiJME)renderer).getAvatarCharacter();
        avatar.setKeyBindings(bindings);
        //return avatar.getKeyBindings();

    }

}
