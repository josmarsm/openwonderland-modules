/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.eventplayer.server;

import java.util.HashMap;
import java.util.Map;
import org.jdesktop.wonderland.common.messages.MessagePacker.ReceivedMessage;
import org.jdesktop.wonderland.modules.eventplayer.server.handler.MessageHandler;
import org.jdesktop.wonderland.modules.eventplayer.server.handler.WonderlandChangesHandler;

/**
 *
 * @author bh37721
 */
public abstract class MessageReplayer {
    /*a map of handlers to play the messages
     * the messages are recorded as XML elements, each handler class is responsible
     * for handling each element tag
     */

    private static final Map<String, Class> handlerMap = new HashMap<String, Class>();
    /* create the map of handlers
     * at the moment only two kinds
     */


    static {
        handlerMap.put("Wonderland_Changes", WonderlandChangesHandler.class);
        handlerMap.put("Message", MessageHandler.class);
    }

    public Class getTagHandlerClass(String elementName) {
        return handlerMap.get(elementName);
    }

    public abstract void playMessage(ReceivedMessage rMessage, long timestamp);

    public abstract void startChanges(long timestamp);
}
