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
 * $Revision$
 * $Date$
 * $State$
 */

package org.jdesktop.wonderland.modules.eventplayer.server.handler;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.eventplayer.server.ChangeReplayer;
import org.xml.sax.Attributes;

/**
 * A tag handler responsible for handling the XML element named "UnloadedCell"
 * @author Bernard Horan
 */
public class UnloadedCellHandler extends DefaultTagHandler {
    public UnloadedCellHandler(ChangeReplayer changeReplayer) {
        super(changeReplayer);
    }
    
    @Override
    public void startTag(Attributes atts) {
        super.startTag(atts);
        //Get the timestamp from the attributes of the XML element
        String timestampString = atts.getValue("timestamp");
        long timestamp = Long.parseLong(timestampString);
        //Get the cellID from the attributes of the XML element
        String cellIDString = atts.getValue("cellID");
        long cellID = Long.parseLong(cellIDString);
        //Tell the change replayer to unload the cell
        changeReplayer.unloadCell(new CellID(cellID), timestamp);
    }
    
}
