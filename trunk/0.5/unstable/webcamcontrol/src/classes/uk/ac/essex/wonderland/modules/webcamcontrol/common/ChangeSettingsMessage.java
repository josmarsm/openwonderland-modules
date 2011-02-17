/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package uk.ac.essex.wonderland.modules.webcamcontrol.common;

import org.jdesktop.wonderland.common.messages.Message;

/**
 * A message sent to the WebcamConnectionHandler to change the
 * settings of a particular Webcam viewer.  The state of this message includes the
 * id of the cell to update and the contents to change it to.
 *
 * @author Bernard Horan
 */
public class ChangeSettingsMessage extends Message {
    private int cellID;
    private WebcamRecord record;

    public ChangeSettingsMessage(int cellID, WebcamRecord record) {
        super();

        this.cellID = cellID;
        this.record = record;
    }

    public int getCellID() {
        return cellID;
    }

    public WebcamRecord getRecord() {
        return record;
    }

}
