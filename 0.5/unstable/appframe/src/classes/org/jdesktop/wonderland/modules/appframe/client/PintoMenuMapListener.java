/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.appframe.client;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import org.jdesktop.wonderland.modules.appframe.common.AppFramePinToMenu;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 *
 * @author nilang
 */
public class PintoMenuMapListener implements SharedMapListenerCli {

    AppFrame parentCell;
    HashMap<String, String> oldPinToMenu;
    HashMap<String, String> newPinToMenu;

    public PintoMenuMapListener(AppFrame parentCell) {
        this.parentCell = parentCell;
        oldPinToMenu = new HashMap<String, String>();
        newPinToMenu = new HashMap<String, String>();
        if (!parentCell.pinToMenuMap.isEmpty()) {
            Set<Entry<String, SharedData>> pinnedItems =
                    parentCell.pinToMenuMap.entrySet();
            if (!pinnedItems.isEmpty()) {
                for (Entry<String, SharedData> pinnedItem : pinnedItems) {
                    AppFramePinToMenu afPin = (AppFramePinToMenu) pinnedItem.getValue();
                    oldPinToMenu.put(afPin.getFileName(), afPin.getFileURL());
                }
            }
        }
    }

    public void propertyChanged(SharedMapEventCli smec) {
        try {
            if (!parentCell.pinToMenuMap.isEmpty()) {
                Set<Entry<String, SharedData>> pinnedItems =
                        parentCell.pinToMenuMap.entrySet();
                if (!pinnedItems.isEmpty()) {
                    newPinToMenu.clear();
                    for (Entry<String, SharedData> pinnedItem : pinnedItems) {
                        AppFramePinToMenu afPin = (AppFramePinToMenu) pinnedItem.getValue();
                        newPinToMenu.put(afPin.getFileName(), afPin.getFileURL());
                    }
                }
                if (parentCell.appFrameProperties != null) {
                    parentCell.appFrameProperties.newPinToMenu.clear();
                    parentCell.appFrameProperties.newPinToMenu.putAll(newPinToMenu);
                    parentCell.appFrameProperties.populatePinTOMenu();
                }
            }
        } catch (Exception ei) {
            ei.printStackTrace();
        }
    }
}
