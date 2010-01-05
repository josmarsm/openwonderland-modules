/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

package org.jdesktop.wonderland.modules.chatzones.common;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.grouptextchat.common.GroupID;

public class ChatZonesCellChangeMessage extends CellMessage {
    public enum ChatZoneAction {
        JOINED,
        LEFT,
        LABEL
    }

    public String name;

    public String label;

    public ChatZoneAction action;

    public int numAvatarInZone = 0;

    public ChatZonesCellChangeMessage(ChatZoneAction action) {
        this.action = action;
    }
    
    public ChatZoneAction getAction() {
        return action;
    }

    public int getNumAvatarInZone() {
        return numAvatarInZone;
    }

    public void setNumAvatarInZone(int numAvatarInZone) {
        this.numAvatarInZone = numAvatarInZone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}