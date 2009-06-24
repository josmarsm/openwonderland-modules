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

package org.jdesktop.wonderland.modules.eventplayer.server.handler;

import org.jdesktop.wonderland.modules.eventplayer.server.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
    private long timestamp;
    
    public MessageHandler(ChangeReplayer changeReplayer) {
        super(changeReplayer);
    }
    
    @Override
    public void startTag(Attributes atts) {
        super.startTag(atts);
        //Get the timestamp from the attributes of the XML element
        String timestampString = atts.getValue("timestamp");
        timestamp = Long.parseLong(timestampString);
    }
    
    @Override
    public void endTag() {
        super.endTag();
        //Decode the string content of the XML element into a bytebuffer
        //Unpack the byte buffer into a message
        //tell the change replayer to play the message
        try {
            ByteBuffer byteBuffer = Base64_Decoder.decodeBufferToByteBuffer(buffer.toString());
            ReceivedMessage rMessage = MessagePacker.unpack(byteBuffer);
            changeReplayer.playMessage(rMessage, timestamp);
        } catch (PackerException ex) {
            logger.log(Level.SEVERE, "Failed to pack message", ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "IO Exception", ex);
        }

        
    }

}
