/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.eventplayer.server.handler;

import org.jdesktop.wonderland.modules.eventplayer.server.*;
import org.xml.sax.Attributes;

/**
 *
 * @author bh37721
 */
public class WonderlandChangesHandler extends DefaultTagHandler {
    public WonderlandChangesHandler(MessageReplayer messageReplayer) {
        super(messageReplayer);
    }
    
    @Override
    public void startTag(Attributes atts) {
        super.startTag(atts);
        String timestampString = atts.getValue("timestamp");
        long timestamp = Long.parseLong(timestampString);
        messageReplayer.startChanges(timestamp);
    }
    
}
