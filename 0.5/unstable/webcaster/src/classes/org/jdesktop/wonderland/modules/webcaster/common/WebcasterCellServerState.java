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

package org.jdesktop.wonderland.modules.webcaster.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * @author Christian O'Connell
 */
@XmlRootElement(name="webcaster-cell")
@ServerState
public class WebcasterCellServerState extends CellServerState
{
    public WebcasterCellServerState(){
    }

    @XmlElement(name="stream-id")
    private static String streamID = "0";

    public void setStreamID(String streamID){
        this.streamID = streamID;
    }

    @XmlTransient public String getStreamID() {
        setStreamID(Integer.toString(Integer.parseInt(streamID)+1));
        return streamID;
    }
    
    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.webcaster.server.WebcasterCellMO";
    }
}