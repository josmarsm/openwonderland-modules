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
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class GetKeyBindingsMethod implements ReturnableScriptMethodSPI {

    private Map<Integer, Integer> bindings = null;
    private static final Logger LOGGER = Logger.getLogger(GetKeyBindingsMethod.class.getName());
    public String getDescription() {
        return "";//throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getFunctionName() {
        return "GetKeyBindings";
    }

    public String getCategory() {
        return "KeyBinding";
    }

    public void setArguments(Object[] args) {
        //if(args[0] instanceof Map)
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object returns() {
        return bindings;
    }

    public void run() {
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
        WlAvatarCharacter avatar = ((AvatarImiJME)renderer).getAvatarCharacter();
        bindings = avatar.getKeyBindings();
        //return avatar.getKeyBindings();
    }

}
