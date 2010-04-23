/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

package org.jdesktop.wonderland.modules.eventplayer.server.handler;

import java.util.concurrent.Semaphore;
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
    public void startTag(Attributes atts, Semaphore semaphore) {
        super.startTag(atts, semaphore);
        //Get the timestamp from the attributes of the XML element
        String timestampString = atts.getValue("timestamp");
        long timestamp = Long.parseLong(timestampString);
        //Get the cellID from the attributes of the XML element
        String cellIDString = atts.getValue("cellID");
        long cellID = Long.parseLong(cellIDString);
        //Tell the change replayer to unload the cell
        changeReplayer.unloadCell(new CellID(cellID), timestamp, semaphore);
    }

    @Override
    public void characters(char[] ch, int start, int length, Semaphore semaphore) {
        super.characters(ch, start, length, semaphore);
        logger.info("releasing semaphore");
       semaphore.release();
    }


    @Override
    public void endTag(Semaphore semaphore) {
       super.endTag(semaphore);
       logger.info("releasing semaphore");
       semaphore.release();
    }
    
}
