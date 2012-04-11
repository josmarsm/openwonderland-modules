/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.instructortools.server;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.instructortools.common.*;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.comms.annotation.ClientHandler;

/**
 *
 * @author Ryan
 */
@ClientHandler
public class InstructorToolsClientHandler implements ClientConnectionHandler, Serializable {

    private static final Logger logger = Logger.getLogger(InstructorToolsClientHandler.class.getName());
    
    public ConnectionType getConnectionType() {
        return InstructorToolsConnectionType.CLIENT_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
       
    }

    public void clientConnected(WonderlandClientSender sender, WonderlandClientID clientID, Properties properties) {
        
    }

    public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, Message message) {
        
        if(message instanceof PullToMeMessage) {
            handlePullToMeMessage(sender, (PullToMeMessage)message);
        } else if(message instanceof ReconnectSoftphoneMessage) {         
            handleReconnectSoftphoneMessage(sender, (ReconnectSoftphoneMessage)message);
        } else if(message instanceof RequestScreenShotMessage) {
            handleRequestScreenShotMessage(sender, (RequestScreenShotMessage)message);
        } else if(message instanceof ScreenShotResponseMessage) { 
            handleScreenShotResponseMessage(sender, (ScreenShotResponseMessage)message);
        }  else if(message instanceof AudioRequestMessage) { 
            handleAudioRequestMessage(sender, (AudioRequestMessage)message);
        } else if(message instanceof AudioResponseMessage) { 
            handleAudioResponseMessage(sender, (AudioResponseMessage)message);
        } else if(message instanceof AudioChangeMessage) { 
            handleAudioChangeMessage(sender, (AudioChangeMessage)message);
            
        }else {
            sender.send(message);
        }
        
    }
    
    public void handlePullToMeMessage(WonderlandClientSender sender, PullToMeMessage message) {
        sendOnlyToTheseClients(sender, message.getSessionIDs(), message);
    }
    
    public void handleReconnectSoftphoneMessage(WonderlandClientSender sender, ReconnectSoftphoneMessage message) {
         sendOnlyToTheseClients(sender, message.getSessionIDs(), message);
    }
    
    public void handleRequestScreenShotMessage(WonderlandClientSender sender, RequestScreenShotMessage message) {
        sendOnlyToTheseClients(sender, message.getTargetSessionIDs(), message);
    }
    
    public void handleScreenShotResponseMessage(WonderlandClientSender sender, ScreenShotResponseMessage message) {
        Set<BigInteger> target = new LinkedHashSet<BigInteger>();
        
        target.add(message.getTarget());
        sendOnlyToTheseClients(sender, target, message);
    }

    public void handleAudioRequestMessage(WonderlandClientSender sender, AudioRequestMessage message) {
        sendOnlyToTheseClients(sender, message.getIDs(), message);
    }
    
    public void handleAudioResponseMessage(WonderlandClientSender sender, AudioResponseMessage message) {
        Set<BigInteger> target = new LinkedHashSet<BigInteger>();
        target.add(message.getTarget());        
        
        sendOnlyToTheseClients(sender, target, message);
    }
    
    public void handleAudioChangeMessage(WonderlandClientSender sender, AudioChangeMessage message) {
        logger.warning("HANDLING AUDIO CHANGE MESSAGE: \n"
                + "target: "+message.getTarget()+"\n"
                + "micVolume: "+message.getMicVolume()+"\n"
                + "speakerVolume: "+message.getSpeakerVolume());
        Set<BigInteger> target = new LinkedHashSet<BigInteger>();
        
        
        target.add(message.getTarget());
        sendOnlyToTheseClients(sender, target, message);
        
    }
    
    public void clientDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
        
    }
    
    private void sendOnlyToTheseClients(WonderlandClientSender sender, Set<BigInteger> IDs, Message message) {
         Set<WonderlandClientID> idsToSendMessageTo = new LinkedHashSet<WonderlandClientID>();
        CommsManager cm = WonderlandContext.getCommsManager();
        
        for(BigInteger sessionID: IDs) {
            WonderlandClientID id = cm.getWonderlandClientID(sessionID);
            idsToSendMessageTo.add(id);
        }
        
        
        sender.send(idsToSendMessageTo, message);
    }
    
    
}
