/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.eventplayer.server.handler;

import org.jdesktop.wonderland.modules.eventplayer.server.MessageReplayer;
import org.xml.sax.Attributes;

/**
 *
 * @author bh37721
 */
public class EndMessageHandler extends DefaultTagHandler {
    private long timestamp;

    public EndMessageHandler(MessageReplayer messageReplayer) {
        super(messageReplayer);
    }

    @Override
    public void startTag(Attributes atts) {
        super.startTag(atts);
        String timestampString = atts.getValue("timestamp");
        timestamp = Long.parseLong(timestampString);
    }

    @Override
    public void endTag() {
        super.endTag();
        messageReplayer.endChanges(timestamp);
    }
}
