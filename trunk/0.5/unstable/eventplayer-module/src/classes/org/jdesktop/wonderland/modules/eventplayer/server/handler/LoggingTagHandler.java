/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.eventplayer.server.handler;

import org.jdesktop.wonderland.modules.eventplayer.server.*;
import java.util.logging.Logger;
import org.xml.sax.Attributes;

/**
 *
 * @author bh37721
 */
public class LoggingTagHandler implements TagHandler {
    private static final Logger logger = Logger.getLogger(LoggingTagHandler.class.getName());

    public LoggingTagHandler(EventPlayer eventPlayer) {
        
    }

    public void startTag(Attributes atts) {
        logger.info(atts.toString());
    }

    public void characters(char[] ch, int start, int length) {
        logger.info(ch.toString());
    }

    public void endTag() {
        logger.info("endTag");
    }

}
