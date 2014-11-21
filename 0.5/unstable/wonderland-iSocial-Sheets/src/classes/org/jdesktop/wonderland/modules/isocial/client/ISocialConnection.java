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
package org.jdesktop.wonderland.modules.isocial.client;

import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.isocial.common.CurrentInstanceMessage;
import org.jdesktop.wonderland.modules.isocial.common.ISocialConnectionType;
import org.jdesktop.wonderland.modules.isocial.common.ResultMessage;
import org.jdesktop.wonderland.modules.isocial.common.RoleRequestMessage;
import org.jdesktop.wonderland.modules.isocial.common.RoleResponseMessage;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;

/**
 * Connection for receiving sheet updates from the server
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class ISocialConnection extends BaseConnection {
    private static final Logger LOGGER =
            Logger.getLogger(ISocialConnection.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org.jdesktop.wonderland.modules.isocial.client.Bundle");

    private final Set<ISocialConnectionListener> listeners =
            new CopyOnWriteArraySet<ISocialConnectionListener>();

    public ConnectionType getConnectionType() {
        return ISocialConnectionType.CONNECTION_TYPE;
    }

    /**
     * Add a listener that will be notified of changes to instances
     * or results
     * @param listener the listener to add
     */
    public void addListener(ISocialConnectionListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener
     * @param listener the listener to remove
     */
    public void removeListener(ISocialConnectionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Determine the user's base role. If the result is Role.GUIDE, it only
     * means the user is eligible to be a guide, not that they are actually
     * a guide in the current instance.
     *
     * @return the user's base role
     */
    public Role getBaseRole() {
        try {
            RoleResponseMessage rrm = (RoleResponseMessage)
                    sendAndWait(new RoleRequestMessage());
            return rrm.getRole();
        } catch (InterruptedException ex) {
            LOGGER.log(Level.WARNING, "getBaseRole() interrupted", ex);
            return null;
        }
    }

    /**
     * Fire instance changed event
     * @param instanceId the new instance's id
     */
    protected void fireInstanceChanged(String instanceId) {
        for (ISocialConnectionListener listener : listeners) {
            listener.instanceChanged(instanceId);
        }
    }

    /**
     * Fire result added event
     * @param resultId the new result's id
     */
    protected void fireResultAdded(String resultId) {
        for (ISocialConnectionListener listener : listeners) {
            listener.resultAdded(resultId);
        }
    }

    /**
     * Fire result updated event
     * @param resultId the new result's id
     */
    protected void fireResultUpdated(String resultId) {
        for (ISocialConnectionListener listener : listeners) {
            listener.resultUpdated(resultId);
        }
    }

    @Override
    public void handleMessage(Message message) {
        if (message instanceof ResultMessage) {
            String resultId = ((ResultMessage) message).getResultId();
            switch (((ResultMessage) message).getType()) {
                case ADDED:
                    fireResultAdded(resultId);
                    break;
                case UPDATED:
                    fireResultUpdated(resultId);
                    break;
            }
        } else if (message instanceof CurrentInstanceMessage) {
            fireInstanceChanged(((CurrentInstanceMessage) message).getInstanceId());
        }
    }

    /**
     * Listener that is notified of changes to the iSocial results
     */
    public interface ISocialConnectionListener {
        /**
         * Notification that the current instance has changed
         * @param instanceId the new instance ID
         */
        public void instanceChanged(String instanceId);

        /**
         * Notification that a result has been added
         * @param resultId the id of the new result
         */
        public void resultAdded(String resultId);

        /**
         * Notification that a result has been updated
         * @param resultId the id of the updated result
         */
        public void resultUpdated(String resultId);
    }
}
