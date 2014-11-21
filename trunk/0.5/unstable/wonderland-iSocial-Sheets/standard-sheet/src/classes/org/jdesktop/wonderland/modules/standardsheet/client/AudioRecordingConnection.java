/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.client;

import com.jme.math.Vector3f;
import java.util.HashSet;
import java.util.Set;

import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.standardsheet.common.AudioRecordingConnectionType;
import org.jdesktop.wonderland.modules.standardsheet.common.AudioRecordingMessage;


/**
 *
 * @author nilang
 */
public class AudioRecordingConnection extends BaseConnection {

    /**
     * @inheritDoc()
     */
    
    public ConnectionType getConnectionType() {
        return AudioRecordingConnectionType.CLIENT_TYPE;
    }

    /**
     * Sends a text chat message from a user to a user. If the "to" user name
     * is null or an empty string, the message is sent to all users.
     *
     * @param message The String text message to send
     * @param from The user name the message is from
     * @param to The user name the message is to
     */
    public void sendTextMessage(String message, String from, String to,boolean recording,boolean playing,String fname,Vector3f vector,String contentrepos) {
        
        super.send(new AudioRecordingMessage(message, from, to,recording,playing,fname,vector,contentrepos));
    }

    /**
     * @inheritDoc()
     */
    public void handleMessage(Message message) {
        if (message instanceof AudioRecordingMessage) {
            String text = ((AudioRecordingMessage) message).getTextMessage();
            String from = ((AudioRecordingMessage) message).getFromUserName();
            String to = ((AudioRecordingMessage) message).getToUserName();
//            synchronized (listeners) {
//                for (AudioRecordingListener listener : listeners) {
//                    listener.textMessage(text, from, to);
//                }
//            }
        }
    }

    private Set<AudioRecordingListener> listeners = new HashSet();

    /**
     * Adds a new listener for text chat messages. If the listener is already
     * present, this method does nothing.
     *
     * @param listener The listener to add
     */
    public void addTextChatListener(AudioRecordingListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener for text chat messages. If the listener is not
     * present, this method does nothing.
     *
     * @param listener The listener to remove
     */
    public void removeTextChatListener(AudioRecordingListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Listener for text chat messages
     */
    public interface AudioRecordingListener {
        /**
         * A text message has been received by the client, given the user name
         * the message is from and the user name the message is to (empty string
         * if for everyone.
         *
         * @param message The String text message
         * @param from The String user name from which the message came
         * @param to The String user name to which the message is intended
         */
       
    }
}

