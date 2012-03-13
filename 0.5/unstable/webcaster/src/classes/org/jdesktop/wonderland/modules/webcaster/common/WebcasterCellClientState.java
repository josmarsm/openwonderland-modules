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

import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 * @author Christian O'Connell
 * @author Bernard Horan
 */
public class WebcasterCellClientState extends CellClientState
{
    private final boolean isWebcasting;
    private final int streamID;

    public WebcasterCellClientState(boolean isWebcasting, int streamID){
        super();
        this.isWebcasting = isWebcasting;
        this.streamID = streamID;
    }

    public boolean isWebcasting() {
        return isWebcasting;
    }

    public int getStreamID(){
        return streamID;
    }
}

