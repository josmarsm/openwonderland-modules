/**
 * Open Wonderland
 *
 * Copyright (c) 2011-12, Open Wonderland Foundation, All Rights Reserved
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

package org.jdesktop.wonderland.modules.webcaster.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.phone.common.PhoneCellServerState;

/**
 * @author Christian O'Connell
 * @author Bernard Horan
 */
@XmlRootElement(name="webcaster-cell")
@ServerState
public class WebcasterCellServerState extends CellServerState {
    private static int INSTANCE_COUNTER = 1;
    private PhoneCellServerState phoneCellState;

    public WebcasterCellServerState() {
        super();
        streamID = INSTANCE_COUNTER++;
    }

    @XmlElement(name = "stream-id")
    private int streamID;

    @XmlTransient
    public int getStreamID() {
        return streamID;
    }

    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.webcaster.server.WebcasterCellMO";
    }

    @XmlElement(name = "phone-cell")
    public PhoneCellServerState getPhoneCellState() {
        return phoneCellState;
    }

    public void setPhoneCellState(PhoneCellServerState phoneCellState) {
        this.phoneCellState = phoneCellState;
    }
}
