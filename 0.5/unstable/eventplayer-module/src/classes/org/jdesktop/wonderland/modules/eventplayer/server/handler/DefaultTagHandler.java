/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.eventplayer.server.handler;

import org.jdesktop.wonderland.modules.eventplayer.server.EventPlayer;
import org.jdesktop.wonderland.modules.eventplayer.server.MessageReplayer;
import org.xml.sax.Attributes;

/**
 *
 * @author bh37721
 */
public class DefaultTagHandler implements TagHandler {
    protected StringBuffer buffer;
    protected MessageReplayer messageReplayer;
    
    public DefaultTagHandler(MessageReplayer messageReplayer) {
        this.messageReplayer = messageReplayer;
    }

    public void startTag(Attributes atts) {
        buffer = new StringBuffer();
    }

    public void characters(char[] ch, int start, int length) {
        buffer.append(ch, start, length);
    }

    public void endTag() {
        
    }

}
