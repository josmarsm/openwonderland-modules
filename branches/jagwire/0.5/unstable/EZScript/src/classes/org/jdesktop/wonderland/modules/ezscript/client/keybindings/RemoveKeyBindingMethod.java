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
 * @author JagWire
 */
@ScriptMethod
public class RemoveKeyBindingMethod implements ScriptMethodSPI {

    private static final Logger LOGGER = Logger.getLogger(RemoveKeyBindingMethod.class.getName());
    private Map<Integer, Integer> bindings = null;
    private boolean fail = false;
    private String trigger;
    public String getFunctionName() {
        return "RemoveBinding";
    }

    public void setArguments(Object[] args) {
        if(!(args[0] instanceof Map)) {
            fail = true;
            return;
        }
        bindings = (Map)args[0];
        trigger = ((String)args[1]).toUpperCase();
    }

    public String getDescription() {
        return "";//throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getCategory() {
        return "KeyBinding";
    }

    public void run() {
        if(fail)
            return;

        if(!bindings.containsValue(KeyBindingUtils.getTriggerFromString(trigger))) {
            return;
        }
        // Fetch the current key bindings from the avatar and update the table
        // model. This assumes there is a primary view Cell.
        Cell cell = ClientContextJME.getViewManager().getPrimaryViewCell();
        if (cell == null) {
            LOGGER.warning("Unable to find primary view cell, is null.");
            return;
            //return new HashMap<Integer, Integer>();
        }
        CellRenderer renderer = cell.getCellRenderer(RendererType.RENDERER_JME);
        if (!(renderer instanceof AvatarImiJME)) {
            LOGGER.warning("View cell renderer is not of the property type: " +
                    renderer.getClass().getName());
            return;
            //return new HashMap<Integer, Integer>();
        }

        bindings.remove(getKeyFromInteger(KeyBindingUtils.getTriggerFromString(trigger)));
        WlAvatarCharacter avatar = ((AvatarImiJME)renderer).getAvatarCharacter();
        avatar.setKeyBindings(bindings);
    }

    public Object getKeyFromInteger(Integer in) {
        Integer key = null;
        if(!bindings.containsValue(in))
            return key;

        for(Integer i: bindings.keySet()) {
           if(bindings.get(i) == in) {
               key = i;
               break;
           }
        }
        return key;   
    }
}
