/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.keybindings;

import imi.character.avatar.AvatarContext.TriggerNames;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;

/**
 *
 * @author JagWire
 */
public class KeyBindingUtils {

    private static final Logger LOGGER = Logger.getLogger(KeyBindingUtils.class.getName());
    private static final Map<String, Integer> stringToKeys = new HashMap<String, Integer>();
    private static final Map<String, TriggerNames> stringToTriggers = new HashMap<String, TriggerNames>();
    private static final String[] keys = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
                              "J", "K", "L", "M", "N", "O", "P", "Q", "R",
                              "S", "T", "U", "V", "W", "X", "Y", "Z", " "};

    private static String[] movementTriggers = { "UP", "LEFT", "RIGHT", "DOWN",
                                                 "JUMP", "TURN_LEFT", "TURN_RIGHT" };

    static {
        stringToTriggers.put("UP", TriggerNames.Move_Up);
        stringToTriggers.put("DOWN", TriggerNames.Move_Down);
        stringToTriggers.put("LEFT", TriggerNames.Move_Strafe_Left);
        stringToTriggers.put("RIGHT", TriggerNames.Move_Strafe_Right);
        stringToTriggers.put("JUMP", TriggerNames.Move_Up);
        stringToTriggers.put("TURN_LEFT", TriggerNames.Move_Left);
        stringToTriggers.put("TURN_RIGHT", TriggerNames.Move_Right);
        stringToTriggers.put("MOVE_FORWARD", TriggerNames.Move_Forward);
        stringToTriggers.put("MOVE_BACK", TriggerNames.Move_Back);

        stringToKeys.put("A", KeyEvent.VK_A);
        stringToKeys.put("B", KeyEvent.VK_B);
        stringToKeys.put("C", KeyEvent.VK_C);
        stringToKeys.put("D", KeyEvent.VK_D);
        stringToKeys.put("E", KeyEvent.VK_E);
        stringToKeys.put("F", KeyEvent.VK_F);
        stringToKeys.put("G", KeyEvent.VK_G);
        stringToKeys.put("H", KeyEvent.VK_H);
        stringToKeys.put("I", KeyEvent.VK_I);
        stringToKeys.put("J", KeyEvent.VK_J);
        stringToKeys.put("K", KeyEvent.VK_K);
        stringToKeys.put("L", KeyEvent.VK_L);
        stringToKeys.put("M", KeyEvent.VK_M);
        stringToKeys.put("N", KeyEvent.VK_N);
        stringToKeys.put("O", KeyEvent.VK_O);
        stringToKeys.put("P", KeyEvent.VK_P);
        stringToKeys.put("Q", KeyEvent.VK_Q);
        stringToKeys.put("R", KeyEvent.VK_R);
        stringToKeys.put("S", KeyEvent.VK_S);
        stringToKeys.put("T", KeyEvent.VK_T);
        stringToKeys.put("U", KeyEvent.VK_U);
        stringToKeys.put("V", KeyEvent.VK_V);
        stringToKeys.put("W", KeyEvent.VK_W);
        stringToKeys.put("X", KeyEvent.VK_X);
        stringToKeys.put("Y", KeyEvent.VK_Y);
        stringToKeys.put("Z", KeyEvent.VK_Z);
        stringToKeys.put(" ", KeyEvent.VK_SPACE);
    }

    private Map<Integer, Integer> getAvatarKeyBindings() {
        // Fetch the current key bindings from the avatar and update the table
        // model. This assumes there is a primary view Cell.
        Cell cell = ClientContextJME.getViewManager().getPrimaryViewCell();
        if (cell == null) {
            LOGGER.warning("Unable to find primary view cell, is null.");
            return new HashMap<Integer, Integer>();
        }
        CellRenderer renderer = cell.getCellRenderer(RendererType.RENDERER_JME);
        if (!(renderer instanceof AvatarImiJME)) {
            LOGGER.warning("View cell renderer is not of the property type: " +
                    renderer.getClass().getName());
            return new HashMap<Integer, Integer>();
        }
        WlAvatarCharacter avatar = ((AvatarImiJME)renderer).getAvatarCharacter();
        return avatar.getKeyBindings();
    }

    public static Integer getKeyFromString(String key) {
        if(stringToKeys.containsKey(key)) {
            return stringToKeys.get(key);
        }
        return null;
    }

    public static Integer getTriggerFromString(String trigger) {
        if(stringToTriggers.containsKey(trigger)) {
            return stringToTriggers.get(trigger).ordinal();
        }
        return null;
    }
}
