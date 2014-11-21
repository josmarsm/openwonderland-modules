/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.server;

/**
 *
 * @author nilang
 */
import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;
import org.jdesktop.wonderland.modules.standardsheet.common.AudioRecordingMessage;


/**
 * For classes that want to receive notices about new TextChatMessages.
 *
 * @author drew_harry
 */
public interface AudioRecordingMessageListener extends Serializable, ManagedObject {

    public void handleMessage(AudioRecordingMessage msg);
}

