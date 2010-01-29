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

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import com.sun.sgs.impl.sharedutil.LoggerWrapper;
import java.io.Serializable;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.server.UserListener;
import org.jdesktop.wonderland.server.UserMO;
import org.jdesktop.wonderland.server.UserManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * Provides simple hooks for starting and stopping presence updating over XMPP.
 * When presence updating starts, the status message of the XMPP account
 * representing this server will update whenever a user logs in or out, such
 * that the message represents the current number of logged in users and their
 * names. 
 *
 * Right now, the manager (and its matching service) aren't designed to abstract out
 * the notion of XMPP connections. One nice future way to take this might be to
 * support multiple connections so that Cells can request XMPP conversations with
 * external people, or so each room could have its own presence account, etc.
 * @author drew
 */
public class XMPPPresenceManager {

    XMPPPresenceService service;
    private static final LoggerWrapper logger =
            new LoggerWrapper(Logger.getLogger(XMPPPresenceManager.class.getName()));
    private PresenceUserListener listener;

    public XMPPPresenceManager(XMPPPresenceService xmppPS) {
        service = xmppPS;

    }

    /**
     * Start updating the presence information. Tells the PresenceManager to plug into
     * the UserListener system so it can be notified of user join/leave events,
     * which it takes as signals to update the presence information.
     */
    public void startPresenceUpdating() {
        if (!service.isValidConfiguration()) {
            logger.log(Level.WARNING, "Tried to start presence updating, but XMPP configuration was not valid. Make sure to set an account name and password.");
            return;
        }

        // Start a recurring task that updates the status messages.

        // lets try using the UserManager notification mechanism for deciding when a good time to update would be.
        UserManager manager = UserManager.getUserManager();

        listener = new PresenceUserListener();
        // Using this inner class as an indirection to avoid serialization problems.
        manager.addUserListener(listener);

        // Kick off a task that periodically cleans out expired XMPP connections
        // (ie connections from which we haven't seen an event in a while)
        // no need to run this that often; being off by a few seconds is no big deal.
        AppContext.getTaskManager().schedulePeriodicTask(new ExpireConnectionsTask(), 0, 3000);
    }

    /**
     * Stops presence updating by removing the listener on login/logout events.
     */
    public void stopPresenceUpdating() {
        UserManager manager = UserManager.getUserManager();
        manager.removeUserListener(listener);
    }

    /**
     * Trigger a one-time presence update. 
     */
    public void updatePresence() {
        if (!service.isValidConfiguration()) {
            logger.log(Level.WARNING, "Tried to update presence information, but XMPP configuration was not valid. Make sure to set an account name and password.");
            return;
        }

        service.doUpdateStatusMessage();
    }

    public void userLoggedOutEvent(WonderlandClientID clientID) {
        // both these methods do the same thing — all we care about is that we run
        // the update here, not what kind of event it was. We're not going to
        // maintain our own user list her based on join/leave events.

        AppContext.getTaskManager().scheduleTask(new StatusMessageUpdateTask());
    }

    public void userLoggedInEvent(WonderlandClientID clientID) {
        AppContext.getTaskManager().scheduleTask(new StatusMessageUpdateTask());
    }

    public void sendMessageToConnectedXMPPClients(String message) {
    }

    /**
     * Container class for listening for user events. We need this
     * because of managed object issues - this class can be serialized
     * easily because it maintains no reference to the Manager or
     * Service objects. 
     */
    public static class PresenceUserListener implements UserListener {

        public void userLoggedOut(WonderlandClientID clientID,
                ManagedReference<UserMO> userRef,
                ManagedReference<Queue<Task>> logoutTasksRef) {

            XMPPPresenceManager manager = AppContext.getManager(XMPPPresenceManager.class);
            manager.userLoggedOutEvent(clientID);
        }

        public void userLoggedIn(WonderlandClientID clientID,
                ManagedReference<UserMO> userRef) {

            XMPPPresenceManager manager = AppContext.getManager(XMPPPresenceManager.class);
            manager.userLoggedInEvent(clientID);
        }
    }

    public void textMessageReceived(String fromUserName, String message) {

        service.checkForExpiredConnections();

        if (message.charAt(0) == '@') {
            service.sendMessageToConnectedXMPPClients(fromUserName + ": " + message.substring(1));
        }
    }

    public void checkForExpiredConnections() {
        service.checkForExpiredConnections();
    }

    private static class ExpireConnectionsTask implements Task, Serializable {

        public void run() {
            XMPPPresenceManager manager = AppContext.getManager(XMPPPresenceManager.class);
            manager.checkForExpiredConnections();
        }
    }

    private static class StatusMessageUpdateTask implements Task, Serializable {

        public void run() {
            XMPPPresenceManager manager = AppContext.getManager(XMPPPresenceManager.class);
            manager.updatePresence();
        }
    }
}
