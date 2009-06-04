/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.xmpp_presence.server.service;

import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import com.sun.sgs.impl.util.AbstractService;
import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.kernel.KernelRunnable;
import com.sun.sgs.service.TransactionProxy;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.UserManager;
import org.jdesktop.wonderland.server.WonderlandContext;
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

    // TODO decide if this really needs to be transient (probably not)
    private XMPPConnection conn;
    private UserManager userManager;

//    private final TaskScheduler taskScheduler;
    private ComponentRegistry registry;

    public static final String XMPP_SERVER_PROPERTY = "wonderland.modules.xmpp-presence.server";
    public static final String XMPP_SERVER_PORT_PROPERTY = "wonderland.modules.xmpp-presence.port";
    public static final String XMPP_ACCOUNT_PROPERTY = "wonderland.modules.xmpp-presence.account";
    public static final String XMPP_PASSWORD_PROPERTY = "wonderland.modules.xmpp-presence.password";
    public static final String XMPP_DOMAIN_WHITELIST_PROPERTY = "wonderland.modules.xmpp-presence.domain-whitelist";


    private boolean domainWhitelisting = false;
    private Vector<String> whitelistedDomains;

    private boolean validConfiguration = false;

    public XMPPPresenceService(Properties props,
            ComponentRegistry registry,
            TransactionProxy proxy) {

        super(props, registry, proxy, logger);

        // Grab all the configuration from the properties files.
        validConfiguration = true;

        String server;
        int port;
        String account = null;
        String password = null;

        logger.log(Level.INFO, "all keys: " + props.keySet());

        logger.log(Level.INFO, "login: " + props.getProperty("wonderland.modules.xmpp-presence.account"));

        if(props.containsKey(XMPP_SERVER_PROPERTY))
            server = props.getProperty(XMPP_SERVER_PROPERTY);
        else
            server = "jabber.org";

        if(props.containsKey(XMPP_SERVER_PORT_PROPERTY))
            port = Integer.parseInt(props.getProperty(XMPP_SERVER_PORT_PROPERTY));
        else
            port = 5222;

        if(props.containsKey(XMPP_ACCOUNT_PROPERTY))
            account = props.getProperty(XMPP_ACCOUNT_PROPERTY);
        else
            validConfiguration = false;

        if(props.containsKey(XMPP_PASSWORD_PROPERTY))
            password = props.getProperty(XMPP_PASSWORD_PROPERTY);
        else
            validConfiguration = false;

        if(props.containsKey(XMPP_DOMAIN_WHITELIST_PROPERTY)) {
            String domainList = props.getProperty(XMPP_DOMAIN_WHITELIST_PROPERTY);
            whitelistedDomains = new Vector<String>(Arrays.asList(domainList.split(",")));
            
            if(whitelistedDomains.size() > 0)
                domainWhitelisting = true;
            else
                domainWhitelisting = false;
        }
        else {
            domainWhitelisting = false;
        }

        logger.log(Level.INFO, account + " on " + server + ":" + port + " with pass '" + password + "'. whitelistedDomains: " + whitelistedDomains + " (valid configuration? " + validConfiguration +")");

        if(!validConfiguration)
            throw new RuntimeException("Credentials " + account + ":" + password + " are not valid. Both must be non-null. See the javadoc for instructions on how to set these properties.");


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
    protected void doReady(){
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

            transactionScheduler.scheduleTask(new StatusUpdateKernelRunnable(), txnProxy.getCurrentOwner());
        } catch (Exception e) {
            // Not sure what to do here yet. Want some way to say that it has gone wrong. TODO
        }
    }

    /**
     * Triggered when someone opens a chat with the wonderland XMPP user.
     * @param chat
     * @param createdLocally
     */
    public void chatCreated(Chat chat, boolean createdLocally) {
        logger.log(Level.INFO, "Chat opened by: " + chat.getParticipant());

        // set this delegate class as a listener to messages from this chat conversation (to make it easier to manage chat states)
        chat.addMessageListener(new ConversationManager());
    }

    protected class ConversationManager implements MessageListener {

        public void processMessage(Chat chat, Message message) {
            // Be aware that this seems to trigger for typing events too. This is a bit of a tricky matter,
            // and for the purposes of this module we're just going to ignore anything that's null.
            
            logger.log(Level.INFO, chat.getParticipant() + ": " + message.getBody() + " <- " + message.getType());

        }

    }

    protected class StatusUpdateKernelRunnable implements KernelRunnable {

        UserManager manager;

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
            manager = WonderlandContext.getUserManager();

            String userList = "";
            int validUserCount = 0;

            // Now loop through the list of users to grab their names.
            boolean first = true;
            for (ManagedReference userRef : manager.getAllUsers()) {
                UserMO user = (UserMO) userRef.get();

                // See comment for this constant about why I do this now,
                // and why I would like to find a cleaner way to manage it.
                if(user.getUsername().equals(WEBSERVER_NAME))
                        continue;

                if (!first) {
                    userList += ", ";
                }

                userList += user.getUsername();

                validUserCount++;

                first = false;
            }

            
            String statusMessage = validUserCount + " users in-world";
            if(validUserCount > 0)
                statusMessage += ": ";

            // Pack up the packet and send it out.
            Presence presence = new Presence(Presence.Type.available);
            presence.setStatus(statusMessage);
            conn.sendPacket(presence);
            
            logger.log(Level.FINEST, "XMPP presence packet sent.");

        }
    }

}