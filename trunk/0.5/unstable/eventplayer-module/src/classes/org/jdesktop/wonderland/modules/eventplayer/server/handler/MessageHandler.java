/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.xml.sax.Attributes;
import sun.misc.BASE64Decoder;


/**
 *
 * @author bh37721
 */
public class MessageHandler extends DefaultTagHandler {
    private final static BASE64Decoder Base64_Decoder = new BASE64Decoder();
    private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
    private long timestamp;
    
    public MessageHandler(MessageReplayer messageReplayer) {
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
        try {
            ByteBuffer byteBuffer = Base64_Decoder.decodeBufferToByteBuffer(buffer.toString());
            ReceivedMessage rMessage = MessagePacker.unpack(byteBuffer);
            messageReplayer.playMessage(rMessage, timestamp);
        } catch (PackerException ex) {
            Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }

}
