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
package org.jdesktop.wonderland.modules.marbleous.common.cell.messages;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Message giving the time currently selected on the slider.
 *
 * @author jslott
 */
public class SelectedSampleMessage extends CellMessage {

    // The time (in seconds) currently selected in the slider
    private float selectedTime = 0.0f;

    public SelectedSampleMessage(float selectedTime) {
        this.selectedTime = selectedTime;
    }

    public float getSelectedTime() {
        return selectedTime;
    }
}
