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

import org.jdesktop.wonderland.modules.eventplayer.server.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import org.jdesktop.wonderland.common.messages.MessagePacker;
import org.jdesktop.wonderland.common.messages.MessagePacker.PackerException;
import org.jdesktop.wonderland.common.messages.MessagePacker.ReceivedMessage;
import org.xml.sax.Attributes;
import sun.misc.BASE64Decoder;


/**
 * A Tag Handler that handles XML elements named "Message".
 *
 * @author Bernard Horan
 */
public class MessageHandler extends DefaultTagHandler {
    private final static BASE64Decoder Base64_Decoder = new BASE64Decoder();
    private long timestamp;
    
    public MessageHandler(ChangeReplayer changeReplayer) {
        super(changeReplayer);
    }
    
    @Override
    public void startTag(Attributes atts, Semaphore semaphore) {
        super.startTag(atts, semaphore);
        //Get the timestamp from the attributes of the XML element
        String timestampString = atts.getValue("timestamp");
        timestamp = Long.parseLong(timestampString);
        logger.info("releasing semaphore");
       semaphore.release();
    
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
        //Decode the string content of the XML element into a bytebuffer
        //Unpack the byte buffer into a message
        //tell the change replayer to play the message
        try {
            ByteBuffer byteBuffer = Base64_Decoder.decodeBufferToByteBuffer(buffer.toString());
            ReceivedMessage rMessage = MessagePacker.unpack(byteBuffer);
            changeReplayer.playMessage(rMessage, timestamp, semaphore);
        } catch (PackerException ex) {
            logger.log(Level.SEVERE, "Failed to pack message", ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "IO Exception", ex);
        }

        
    }

}
