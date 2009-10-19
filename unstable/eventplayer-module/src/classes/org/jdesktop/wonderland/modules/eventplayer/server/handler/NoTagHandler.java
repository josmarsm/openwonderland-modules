/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.eventplayer.server.handler;

import java.util.concurrent.Semaphore;
import org.jdesktop.wonderland.modules.eventplayer.server.ChangeReplayer;
import org.xml.sax.Attributes;

/**
 *
 * @author bh37721
 */
public class NoTagHandler extends DefaultTagHandler {
    public NoTagHandler(ChangeReplayer changeReplayer) {
            super(changeReplayer);
        }

    public void startTag(Attributes atts, Semaphore semaphore) {
        super.startTag(atts, semaphore);
       logger.info("releasing semaphore");
       semaphore.release();
    }

    public void characters(char[] ch, int start, int length, Semaphore semaphore) {
        super.characters(ch, start, length, semaphore);
        logger.info("releasing semaphore");
       semaphore.release();
    }


    public void endTag(Semaphore semaphore) {
       super.endTag(semaphore);
       logger.info("releasing semaphore");
       semaphore.release();
    }
}
