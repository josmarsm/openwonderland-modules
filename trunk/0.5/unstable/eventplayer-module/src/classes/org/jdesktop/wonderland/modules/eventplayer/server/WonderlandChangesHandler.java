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
public class WonderlandChangesHandler extends DefaultTagHandler {
    
    public WonderlandChangesHandler(EventPlayerImpl eventPlayer) {
        super(eventPlayer);
    }
    
    @Override
    public void startTag(Attributes atts) {
        super.startTag(atts);
        eventPlayer.startChanges();
    }
    
}
