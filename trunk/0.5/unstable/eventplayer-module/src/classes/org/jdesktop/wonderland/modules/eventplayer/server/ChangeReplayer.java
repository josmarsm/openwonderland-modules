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
package org.jdesktop.wonderland.modules.eventplayer.server;

import java.util.HashMap;
import java.util.Map;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.MessagePacker.ReceivedMessage;
import org.jdesktop.wonderland.modules.eventplayer.server.handler.EndMessageHandler;
import org.jdesktop.wonderland.modules.eventplayer.server.handler.LoadedCellHandler;
import org.jdesktop.wonderland.modules.eventplayer.server.handler.MessageHandler;
import org.jdesktop.wonderland.modules.eventplayer.server.handler.TagHandler;
import org.jdesktop.wonderland.modules.eventplayer.server.handler.UnloadedCellHandler;
import org.jdesktop.wonderland.modules.eventplayer.server.handler.WonderlandChangesHandler;

/**
 * An abstract class responsible for replaying changes parsed via a SAX parser from an XML document
 * @author Bernard Horan
 */
public abstract class ChangeReplayer {
    /*a map of handlers to play the changes
     * the changes are recorded as XML elements, each handler class is responsible
     * for handling each element tag
     */

    private static final Map<String, Class> handlerMap = new HashMap<String, Class>();
    /* create the map of handlers
     */


    static {
        handlerMap.put("Wonderland_Changes", WonderlandChangesHandler.class);
        handlerMap.put("Message", MessageHandler.class);
        handlerMap.put("EndMessage", EndMessageHandler.class);
        handlerMap.put("LoadedCell", LoadedCellHandler.class);
        handlerMap.put("UnloadedCell", UnloadedCellHandler.class);
    }

    /**
     * I have reached the end of the changes
     * @param timestamp the timestamp when the changes finished playing
     */
    public abstract void endChanges(long timestamp);
    

    /**
     * Return an implementation of TagHandler that is responsible for handling
     * XML elements named by the argument
     * @param elementName the name of the XML element for which a handler is required
     * @return an implementation of TagHandler
     */
    public Class<TagHandler> getTagHandlerClass(String elementName) {
        return handlerMap.get(elementName);
    }

    /**
     * Load the cell described by the setupInfo at the time given by the timestamp
     * @param setupInfo an XML representation of the cellserverstate of a cell
     * @param timestamp the timestamp at which the cell was loaded
     */
    public abstract void loadCell(String setupInfo, long timestamp);

    /**
     * Play a message
     * @param rMessage the message to be played
     * @param timestamp the timestamp at which it is to be played
     */
    public abstract void playMessage(ReceivedMessage rMessage, long timestamp);

    /**
     * Indiates the timestamp at which the changes begin playing<br>
     * Used for computing when to replay later messages
     * @param timestamp a timestamp to indicate the start of the message playback
     */
    public abstract void startChanges(long timestamp);

    /**
     * Unload the cell identified by the cellID at the time given by the timestamp
     * @param cellID the id of the cell to be unloaded
     * @param timestamp the timestamnp at which the cell was unloaded
     */
    public abstract void unloadCell(CellID cellID, long timestamp);
    

    
}
