/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.client;

/**
 *
 * @author nilang
 */

import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.comms.SessionStatusListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;


@Plugin
public class AudioRecordingClientPlugin extends BaseClientPlugin
        implements SessionLifecycleListener, SessionStatusListener {

    /**
     * @inheritDoc()
     */
    @Override
    public void initialize(ServerSessionManager sessionManager) {
        // Listen for new primary sessions on this session manager
        sessionManager.addLifecycleListener(this);
        super.initialize(sessionManager);
    }

    @Override
    public void cleanup() {
        // Stop listening for the lifecycle changes
        getSessionManager().removeLifecycleListener(this);
        super.cleanup();
    }

    /**
     * @inheritDoc()
     */
    public void sessionCreated(WonderlandSession session) {
        // Do nothing.
    }

    /**
     * @inheritDoc()
     */
    public void primarySession(WonderlandSession session) {
        // Handle when a new primary session happens. Note that when there is
        // no primary session, the 'session' argument is null. In such a
        // case, we do nothing -- the case where the primary session becomes
        // disconnected is handled by the SessionStatusListener.
        if (session != null) {
            session.addSessionStatusListener(this);
            if (session.getStatus() == WonderlandSession.Status.CONNECTED) {
                connectClient(session);
            }
        }
    }

    /**
     * @inheritDoc()
     */
    public void sessionStatusChanged(WonderlandSession session, Status status) {
        switch (status) {
            case CONNECTED:
                connectClient(session);
                return;

            case DISCONNECTED:
                disconnectClient();
                return;
        }
    }

    /**
     * Connect the client.
     */
    private void connectClient(WonderlandSession session) {
        // Tell the Chat manager that there is a new primary session connected.
        AudioRecordingManager chatManager = AudioRecordingManager.getAudioRecordingManager();
        chatManager.register(session);
    }

    /**
     * Disconnect the client
     */
    private void disconnectClient() {
        // Tell the Chat manager that a primary session has disconnected
        AudioRecordingManager chatManager = AudioRecordingManager.getAudioRecordingManager();
        chatManager.unregister();
    }
}

