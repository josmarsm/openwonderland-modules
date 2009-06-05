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
package org.jdesktop.wonderland.modules.xmpp_presence.server.service;

import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.impl.auth.IdentityImpl;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.impl.util.AbstractService;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.service.TransactionProxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.UserManager;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.modules.textchat.common.TextChatConnectionType;
import org.jdesktop.wonderland.modules.textchat.server.TextChatConnectionHandler;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

/**
 * An underlying service that manages XMPP connections for making user presence
 * information available.
 *
 * On startup the service logs in the specified account and waits
 * for events to trigger status updates. It can also act as a bridge for
 * text chat messages between people in the world and users talking to
 * the XMPP user that represents the server.
 *
 * This service has a number of important configuration options:
 * <ul>
 *  <li><i>wonderland.modules.xmpp-presence.server</i>: The XMPP server you want to connect to. (This service has only been tested with jabber.org. Other servers have different authentication schemes; to get them to connect, you may need to change the connection code. The <a href="http://www.igniterealtime.org/community/community/developers/smack">Smack Forums</a> have helped me with this in the past.)</li>
 *  <li><i>wonderland.modules.xmpp-presence.port</i>: The server port. Defaults to 5222.
 *  <li><i>wonderland.modules.xmpp-presence.account</i>: The login name for the specified server. Sometimes this includes the domain name, sometimes it doesn't (for instance jabber.org doesn't, gtalk does)</li>
 *  <li><i>wonderland.modules.xmpp-presence.password</i>: The password for the account specified in .login. Be aware that by default, this password is sent in clear text, so it should be a password unique to this service.</li>
 *  <li><i>wonderland.modules.xmpp-presence.domain-whitelist</i>: A comma separated list of domains from which the Service should accept access requests from. If this is empty, requests from all domain are accepted. Otherwise, users not from the specified domains will not be able to add this account to their buddy list. (NOT SUPPORTED YET.)</li>
 * </ul>
 *
 * These configuration options can be set in Wonderland web configuration tool. Go to Server Status, and click on the "edit" link next in the "Darkstar Server" row. From that interface, you can set these properties.
 *
 * @author drew
 */
public class XMPPPresenceService extends AbstractService implements ChatManagerListener {

    private static final LoggerWrapper logger =
            new LoggerWrapper(Logger.getLogger(XMPPPresenceService.class.getName()));
    /**
     * Reference to the actual XMPP connection, as provided by Smack.
     */
    private XMPPConnection conn;
    private UserManager userManager;
    private ComponentRegistry registry;

    // Keys for the properties file that specifies Service parameters.
    public static final String XMPP_SERVER_PROPERTY = "wonderland.modules.xmpp-presence.server";
    public static final String XMPP_SERVER_PORT_PROPERTY = "wonderland.modules.xmpp-presence.port";
    public static final String XMPP_ACCOUNT_PROPERTY = "wonderland.modules.xmpp-presence.account";
    public static final String XMPP_PASSWORD_PROPERTY = "wonderland.modules.xmpp-presence.password";
    public static final String XMPP_DOMAIN_WHITELIST_PROPERTY = "wonderland.modules.xmpp-presence.domain-whitelist";

    private boolean domainWhitelisting = false;
    private Vector<String> whitelistedDomains;

    private boolean validConfiguration = false;

    private HashMap<String, ConversationManager> conversationManagers = new HashMap<String, ConversationManager>();

    private TransactionProxy txnProxy;

    public XMPPPresenceService(Properties props,
            ComponentRegistry registry,
            TransactionProxy proxy) {

        super(props, registry, proxy, logger);

        // Grab all the configuration from the properties files.
        validConfiguration = true;

        txnProxy = proxy;

        String server;
        int port;
        String account = null;
        String password = null;

        logger.log(Level.INFO, "all keys: " + props.keySet());

        logger.log(Level.INFO, "login: " + props.getProperty("wonderland.modules.xmpp-presence.account"));

        if (props.containsKey(XMPP_SERVER_PROPERTY)) {
            server = props.getProperty(XMPP_SERVER_PROPERTY);
        } else {
            server = "jabber.org";
        }

        if (props.containsKey(XMPP_SERVER_PORT_PROPERTY)) {
            port = Integer.parseInt(props.getProperty(XMPP_SERVER_PORT_PROPERTY));
        } else {
            port = 5222;
        }

        if (props.containsKey(XMPP_ACCOUNT_PROPERTY)) {
            account = props.getProperty(XMPP_ACCOUNT_PROPERTY);
        } else {
            validConfiguration = false;
        }

        if (props.containsKey(XMPP_PASSWORD_PROPERTY)) {
            password = props.getProperty(XMPP_PASSWORD_PROPERTY);
        } else {
            validConfiguration = false;
        }

        if (props.containsKey(XMPP_DOMAIN_WHITELIST_PROPERTY)) {
            String domainList = props.getProperty(XMPP_DOMAIN_WHITELIST_PROPERTY);
            whitelistedDomains = new Vector<String>(Arrays.asList(domainList.split(",")));

            if (whitelistedDomains.size() > 0) {
                domainWhitelisting = true;
            } else {
                domainWhitelisting = false;
            }
        } else {
            domainWhitelisting = false;
        }

        logger.log(Level.INFO, account + " on " + server + ":" + port + " with pass '" + password + "'. whitelistedDomains: " + whitelistedDomains + " (valid configuration? " + validConfiguration + ")");

        if (!validConfiguration) {
            throw new RuntimeException("Credentials " + account + ":" + password + " are not valid. Both must be non-null. See the javadoc for instructions on how to set these properties.");
        }


        ConnectionConfiguration cc = new ConnectionConfiguration(server, port);

        conn = new XMPPConnection(cc);

        // TODO Think about making this whole thing async somehow? Will add
        //      a bunch of blocking time to startup otherwise.
        try {
            conn.connect();

            // This works around a bug in 3.1.0b, as described here: http://www.igniterealtime.org/community/thread/35976
            SASLAuthentication.supportSASLMechanism("PLAIN", 0);

            // Resource name is hardcoded right now, because I don't think it's that important, but it would be easy enough to make
            // it configurable here.
            conn.login(account, password, "wonderland");

            // TODO Figure out what the appropriate failure mode is here. I think
            //      the good article described it.
            if (conn.isAuthenticated()) {
                logger.log(Level.FINER, "Sucessfully connected to XMPP server");

                // Send a first presence packet to the XMPP server.
                Presence presence = new Presence(Presence.Type.unavailable);
                presence.setStatus("Initializing presence services...");
                conn.sendPacket(presence);

                // Set a listener for incoming chat messages.
                conn.getChatManager().addChatListener(this);

            } else {
                logger.log(Level.WARNING, "XMPP authentication failed.");
            }
        } catch (XMPPException ex) {
            logger.log(Level.SEVERE, "Exception connecting to XMPP server: " + ex);
        }

    }

    @Override
    protected void doReady() {
        logger.log(Level.INFO, "XMPP Presence Service Ready.");
    }

    @Override
    protected void doShutdown() {
        logger.log(Level.INFO, "XMPP Presence Service Shutdown.");

        conn.disconnect();
    }

    @Override
    protected void handleServiceVersionMismatch(Version arg0, Version arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // TODO add in a callback object.
    protected void doUpdateStatusMessage() {
        logger.log(Level.FINER, "Updating status message.");

        try {
            logger.log(Level.INFO, "About to schedule a task: " + transactionScheduler + "; proxy: " + txnProxy.getCurrentOwner());

            transactionScheduler.scheduleTask(new StatusUpdateKernelRunnable(), txnProxy.getCurrentOwner());
        } catch (Exception e) {
            // Not sure what to do here yet. Want some way to say that it has gone wrong. TODO
        }
    }

    /**
     * Triggered when someone opens a chat with the wonderland XMPP user. Called by Smack.
     * 
     * @param chat
     * @param createdLocally
     */
    public void chatCreated(Chat chat, boolean createdLocally) {

        // This is a bit counter-intuitive. The docs make it seem like the mappaing from
        // "chat" to conversation is 1:1, ie when you send the first message to someone
        // it creates a new "chat" object, and that chat object persists for the rest
        // of the conversation between you and the conversation-starter. This isn't true,
        // though. It's up to the remote client to decide what constitutes a chat, because
        // this chatCreated method fires whenever there's a new threadID, and threadIDs
        // seem to be created at the whim of either client. As a result, we have to
        // be a little more savvy about conversations here and keep track of who we're
        // talking to.

        try {

            logger.log(Level.INFO, "Chat opened by: " + chat.getParticipant() + " threadID: " + chat.getThreadID());

            ConversationManager cm = conversationManagers.get(chat.getParticipant());

            // If we already have a conversation manager for this person, use it. Otherwise,
            // make a new one and put it in the map.
            //
            // (there might be a bug here where this gets persisted across conversations, so the message
            //  only gets sent on your first convo EVER with the server. Need to find a way to plug into
            //  conversation-closed events.)
            if (cm == null) {
                cm = new ConversationManager();
                conversationManagers.put(chat.getParticipant(), cm);

                // Send welcome message.
                chat.sendMessage("Hi! Any messages you type to me will be sent in-world. Any responses from in-world avatars that start with an '@' will be sent back to you here.");
            }


            // set this delegate class as a listener to messages from this chat conversation (to make it easier to manage chat states)
            chat.addMessageListener(cm);

        } catch (XMPPException ex) {
            logger.log(Level.WARNING, "Error sending welcome chat message to xmpp user: " + ex.getMessage());
        }
    }

    protected class ConversationManager implements MessageListener {

        public void processMessage(Chat chat, Message message) {
            // Be aware that this seems to trigger for typing events too. This is a bit of a tricky matter,
            // and for the purposes of this module we're just going to ignore anything that's null.
            //
            // (getting typing messages would be interesting, but seems to be substantially trickier...
            //  you need to register a packet listener that can find non-standard XMPP XML tags
            //  that represent the typing state, because it's technically an XMPP extension.)
            if (message.getBody() == null) {
                return;
            }

            logger.log(Level.FINER, chat.getParticipant() + ": " + message.getBody() + " (threadID: " + chat.getThreadID() + ")");

            try {
            // Now spin off a task in a transaction to handle the message sending.
            // Spent a while trying to get the txnProxy object to cough up the right identity object, but it turns out
            // that only works if you're already in a transaction. I'm not, here, so we just have to make up our own
            // Identity object. As far as I can tell, it's used only for accounting purposes, so as long as it's clear,
            // there are no procedural implications to making your own up on the spot. 
            transactionScheduler.scheduleTask(new ProcessMessageKernelRunnable(chat, message), new IdentityImpl("XMPPConversationManager"));
            } catch (Exception e)
            {
                logger.log(Level.WARNING, "BAD", e);
            }
        }
    }

    protected class StatusUpdateKernelRunnable implements KernelRunnable {

        /**
         * The name of the webserver class users. This is a bad hack that stands in for not
         * having a good way to differentiate between different kinds of users right now.
         * At the moment, webservers are the only non-user Users that are often logged
         * in, so for the the sake of having a clean and meaningful list of users in
         * this context, we just throw it out by excluding users with this name.
         */
        private static final String WEBSERVER_NAME = "webserver";

        public StatusUpdateKernelRunnable() {
        }

        public String getBaseTaskType() {
            return "XMPPPresenceService.updateStatusMessageTask";
        }

        public void run() {
            UserManager manager = WonderlandContext.getUserManager();

            String userList = "";
            int validUserCount = 0;

            // Now loop through the list of users to grab their names.
            boolean first = true;
            for (ManagedReference userRef : manager.getAllUsers()) {
                UserMO user = (UserMO) userRef.get();

                // See comment for this constant about why I do this now,
                // and why I would like to find a cleaner way to manage it.
                if (user.getUsername().equals(WEBSERVER_NAME)) {
                    continue;
                }

                if (!first) {
                    userList += ", ";
                }

                userList += user.getUsername();

                validUserCount++;

                first = false;
            }


            String statusMessage = validUserCount + " users in-world";
            if (validUserCount > 0) {
                statusMessage += ": ";
            }

            statusMessage += userList;

            // Pack up the packet and send it out.
            Presence presence = new Presence(Presence.Type.available);
            presence.setStatus(statusMessage);
            conn.sendPacket(presence);

            logger.log(Level.FINEST, "XMPP presence packet sent.");

        }
    }

    /**
     * Encapsulates the process of bridging messages from XMPP into the Wonderland
     * text-chat system. Because we're interacting with the CommsManager and TextChat
     * handlers, this needs to happen in a transaction.
     */
    protected class ProcessMessageKernelRunnable implements KernelRunnable {

        private String participant;
        private String threadID;
        private String msgBody;

        protected ProcessMessageKernelRunnable(Chat c, Message msg) {
            // Grab these to avoid having references to the Smack/XMPP
            // objects. Those references might cause problems with
            // Darkstar.
            participant = c.getParticipant();
            threadID = c.getThreadID();
            msgBody = msg.getBody();
        }

        public String getBaseTaskType() {
            return "XMPPPresenceService.handleMessageKernelTask";
        }

        public void run() throws Exception {
            // Send the message in-world.
            ClientConnectionHandler handler = null;
            CommsManager cm = WonderlandContext.getCommsManager();
            handler = cm.getClientHandler(TextChatConnectionType.CLIENT_TYPE);
            
            if (handler == null) {
                logger.log(Level.WARNING, "Could not send XMPP chat message in-world because TextChatClientHandler could not be found.");
                return;
            }
            TextChatConnectionHandler tcch = (TextChatConnectionHandler) handler;
            tcch.sendGlobalMessage(participant, msgBody);
        }
    }
}