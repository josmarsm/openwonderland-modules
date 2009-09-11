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
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.xmpp_presence.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.ResourceUnavailableException;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.textchat.common.TextChatConnectionType;
import org.jdesktop.wonderland.modules.textchat.common.TextChatMessage;
import org.jdesktop.wonderland.modules.textchat.server.TextChatConnectionHandler;
import org.jdesktop.wonderland.modules.textchat.server.TextChatMessageListener;
import org.jdesktop.wonderland.modules.xmpp_presence.server.service.XMPPPresenceManager;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;

/**
 * Does the startup work for the XMPPPresenceService. Its two main jobs are
 * registering the service as a listener for text chat messages and kicking
 * off the presenceUpdating task.
 * 
 * @author drew
 */
@Plugin
public class XMPPPresenceServerPlugin implements ServerPlugin {

    private static final Logger logger =
            Logger.getLogger(XMPPPresenceServerPlugin.class.getName());
    public static final String CHAT_MESSAGE_LISTENER_BINDING = "CHAT_MESSAGE_LISTENER";

    public void initialize() {

        XMPPPresenceManager manager = AppContext.getManager(XMPPPresenceManager.class);

        AppContext.getTaskManager().scheduleTask(new ChatRegistrationTask());

        manager.startPresenceUpdating();

        logger.info("XMPP Presence Server Plugin initialized.");

    }

    protected static class WonderlandChatListener implements TextChatMessageListener {

        public void handleMessage(TextChatMessage message) {

            // Send it out over XMPP. (Should this kick off it's own KernelRunnable? Maybe?
            // I'm torn: on the one hand, it's good that whatever a textmessagelistener does
            // occurs in the same transaction as sending that message to clients. On the other,
            // I'm worried about network actions every time a text chat message is sent.

            // Send it out to everyone who has a current open conversation.
            XMPPPresenceManager manager = AppContext.getManager(XMPPPresenceManager.class);

            manager.textMessageReceived(message.getFromUserName(), message.getTextMessage());
        }
    }

    /**
     * A task that handles the registartion of a callback object with the TextChatConnectionHandler.
     *
     * Checks to see if such a listener already exists in the DataManager. If it does, then
     * do nothing. Otherwise, create a new one and register it appropriately.
     * 
     */
    protected static class ChatRegistrationTask implements Task, Serializable {

        public void run() {
            TextChatMessageListener listener;
            try {
                listener = (TextChatMessageListener) AppContext.getDataManager().getBinding(CHAT_MESSAGE_LISTENER_BINDING);
                logger.log(Level.FINER, "Found an existing listener object. No need to make a new one.");
            } catch (NameNotBoundException e) {
                logger.log(Level.FINER, "Listener not yet set. About to schedule listener registration.");
                // Sign up for in-world messages.
                CommsManager cm = WonderlandContext.getCommsManager();
                TextChatConnectionHandler tcch = (TextChatConnectionHandler) cm.getClientHandler(TextChatConnectionType.CLIENT_TYPE);

                if (tcch == null) {
                    throw new ResourceUnavailableException("TextChat module was not yet available. Retrying...");
                }
                
                listener = new WonderlandChatListener();
                tcch.addTextChatMessageListener(listener);

                // Now add this into the data manager, since we think we're officially registered.
                logger.log(Level.FINER, "Registered TextChatMessageListener. About to put it in the data manager...");
                AppContext.getDataManager().setBinding(CHAT_MESSAGE_LISTENER_BINDING, listener);
            }

        }
    }
}