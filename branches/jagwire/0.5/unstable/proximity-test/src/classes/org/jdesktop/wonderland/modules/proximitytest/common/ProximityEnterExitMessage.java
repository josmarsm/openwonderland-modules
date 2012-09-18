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
package org.jdesktop.wonderland.modules.proximitytest.common;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Message to notify the client when it enters or exits server bounds
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ProximityEnterExitMessage extends CellMessage {
    private int index;
    private boolean enter;

    public ProximityEnterExitMessage(int index, boolean enter) {
        this.index = index;
        this.enter = enter;
    }

    public int getIndex() {
        return index;
    }

    public boolean isEnter() {
        return enter;
    }
}
