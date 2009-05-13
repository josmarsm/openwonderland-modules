/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.eventplayer.server;

import org.xml.sax.Attributes;

/**
 *
 * @author bh37721
 */
public class DefaultTagHandler implements TagHandler {
    protected StringBuffer buffer;
    protected EventPlayerImpl eventPlayer;
    
    public DefaultTagHandler(EventPlayerImpl eventPlayer) {
        this.eventPlayer = eventPlayer;
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
