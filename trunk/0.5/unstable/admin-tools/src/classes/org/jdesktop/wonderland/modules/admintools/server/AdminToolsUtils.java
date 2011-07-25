/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.admintools.server;

import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.util.ScalableHashSet;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.admintools.common.AdminToolsConnectionType;
import org.jdesktop.wonderland.modules.admintools.common.BroadcastMessage;
import org.jdesktop.wonderland.modules.admintools.common.DisconnectMessage;
import org.jdesktop.wonderland.modules.admintools.common.MuteMessage;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
import org.jdesktop.wonderland.modules.presencemanager.server.PresenceManagerSrv;
import org.jdesktop.wonderland.modules.presencemanager.server.PresenceManagerSrvFactory;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.CommsManager;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Utility methods used by admin tools
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 * @author Bernard Horan <bernard@ellesley.com>
 */
public class AdminToolsUtils {
    private static final Logger LOGGER =
            Logger.getLogger(AdminToolsUtils.class.getName());
    private static final String DISCONNECT_LISTENERS_BINDING = "AdminToolsUtils.DisconnectListenerBinding";

    static void handleDisconnect(WonderlandClientSender sender,
                                 DisconnectMessage disconnect)
    {
        LOGGER.warning("Handle disconnect request for " +
                       disconnect.getSessionID());

        CommsManager cm = WonderlandContext.getCommsManager();
        WonderlandClientID remote =
                cm.getWonderlandClientID(disconnect.getSessionID());
        WonderlandClientSender noticeSender =
                cm.getSender(AdminToolsConnectionType.CONNECTION_TYPE);

        if (remote != null) {
            // notify the client
            noticeSender.send(remote, disconnect);

            // schedule a task to happen shortly. This should guarantee that the
            // preceding message gets sent before the actual disconnect happens
            AppContext.getTaskManager().scheduleTask(
                    new DisconnectTask(remote.getSession()), 2000);
            notifyDisconnectListeners(remote);

        } else {
            LOGGER.warning("No clientID found for " + disconnect.getSessionID());
        }
    }

    static void handleMute(WonderlandClientSender sender,
                           MuteMessage mute)
    {
        LOGGER.warning("Handle mute request for " +
                       mute.getSessionID());

        PresenceManagerSrv pm = PresenceManagerSrvFactory.getInstance();
        PresenceInfo pi = pm.getPresenceInfo(mute.getSessionID());

        CommsManager cm = WonderlandContext.getCommsManager();
        WonderlandClientID remote =
                cm.getWonderlandClientID(mute.getSessionID());
        WonderlandClientSender noticeSender =
                cm.getSender(AdminToolsConnectionType.CONNECTION_TYPE);

        if (remote != null && pi != null && pi.getCallID() != null) {
            VoiceManager vm = AppContext.getManager(VoiceManager.class);
            Call call = vm.getCall(pi.getCallID());
            if (call != null) {
                try {
                    call.mute(true);

                    noticeSender.send(remote, mute);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Unable to mute call " +
                               pi.getCallID(), ex);
                }
            } else {
                LOGGER.warning("Unable to find call for callID " +
                               pi.getCallID());
            }
        } else {
            LOGGER.warning("Unable to find presence info for session " +
                           mute.getSessionID());
        }
    }

    static void handleBroadcast(WonderlandClientSender sender,
                                BroadcastMessage broadcast)
    {
        CommsManager cm = WonderlandContext.getCommsManager();
        WonderlandClientSender noticeSender =
                cm.getSender(AdminToolsConnectionType.CONNECTION_TYPE);

        noticeSender.send(broadcast);
    }


    /**
     * Add a forced disconnection listener
     * @param listener an implementation of ForceDisconnetListener
     */
    @SuppressWarnings("unchecked")
    public static void addDisconnectListener(ForceDisconnectListener listener) {
        ScalableHashSet<ForceDisconnectListener> disconnectListeners;
        //Do this lazily
        try {
            disconnectListeners = (ScalableHashSet<ForceDisconnectListener>) AppContext.getDataManager().getBindingForUpdate(DISCONNECT_LISTENERS_BINDING);
        } catch (NameNotBoundException nnbe) {
            disconnectListeners = createDisconnectListenersSet();
        }
        disconnectListeners.add(listener);
    }

    /**
     * Remove a forced disconnection listener
     * @param listener an implementation of ForceDisconnetListener
     */
    public static void removeDisconnectListener(ForceDisconnectListener listener) {
        @SuppressWarnings("unchecked")
        ScalableHashSet<ForceDisconnectListener> disconnectListeners = (ScalableHashSet<ForceDisconnectListener>) AppContext.getDataManager().getBindingForUpdate(DISCONNECT_LISTENERS_BINDING);
        disconnectListeners.remove(listener);
    }

    private static ScalableHashSet<ForceDisconnectListener> createDisconnectListenersSet() {
        ScalableHashSet<ForceDisconnectListener> disconnectListeners = new ScalableHashSet<ForceDisconnectListener>();
        AppContext.getDataManager().setBinding(DISCONNECT_LISTENERS_BINDING, disconnectListeners);
        return disconnectListeners;
    }

    @SuppressWarnings("unchecked")
    private static void notifyDisconnectListeners(WonderlandClientID remote) {
        ScalableHashSet<ForceDisconnectListener> disconnectListeners;
        try {
            disconnectListeners = (ScalableHashSet<ForceDisconnectListener>) AppContext.getDataManager().getBindingForUpdate(DISCONNECT_LISTENERS_BINDING);
        } catch (NameNotBoundException nnbe) {
            LOGGER.warning("No disconnect listeners");
            return;
        }
        for (ForceDisconnectListener forceDisconnectListener : disconnectListeners) {
            forceDisconnectListener.disconnect(remote);
        }
    }

    /**
     * An interface that represents a listener that is called when a user is forcibly disconnected
     */
    public static interface ForceDisconnectListener extends Serializable {
        /**
         * Notification that a wonderland client has been forcibly disconnected
         * @param remote the wonderland client id that represents the client that has been forcibly disconnected
         */
        public void disconnect(WonderlandClientID remote);
    }

    private static class DisconnectTask implements Task, Serializable {
        private final ManagedReference<ClientSession> sessionRef;

        public DisconnectTask(ClientSession session) {
            sessionRef = AppContext.getDataManager().createReference(session);
        }

        public void run() throws Exception {
            AppContext.getDataManager().removeObject(sessionRef.get());
        }
    }
}
