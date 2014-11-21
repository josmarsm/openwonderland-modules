/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.common;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * A chat message for a specific user. If the user is null or an empty string,
 * then it is meant for all users.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class AudioRecordingMessage extends Message {

    private String textMessage = null;
    private String fromUserName = null;
    private String toUserName = null;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private String fname =null;
    private Vector3f vector=null;
    private String contentrepos;

    /** Constructor */
    public AudioRecordingMessage(String msg, String fromUserName, String toUserName,boolean recording,boolean playing
            ,String fname,Vector3f vector,String contentrepos) {
        this.textMessage = msg;
        this.fromUserName = fromUserName;
        this.toUserName = toUserName;
        this.isPlaying = playing;
        this.isRecording = recording;
        this.fname = fname;
        this.vector = vector;
        this.contentrepos = contentrepos;
    }
    
    public AudioRecordingMessage(String msg, String fromUserName, String toUserName,boolean recording,boolean playing
            ,String fname,Vector3f vector) {
        this.textMessage = msg;
        this.fromUserName = fromUserName;
        this.toUserName = toUserName;
        this.isPlaying = playing;
        this.isRecording = recording;
        this.fname = fname;
        this.vector = vector;
    }
    
    public void setRecording(boolean recording) {
        this.isRecording = recording;
    }

    public boolean isRecording() {
        return isRecording;
    }
    
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
    
    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getFname() {
        return fname;
    }
    public void setContentRepos(String contentrepos) {
        this.contentrepos = contentrepos;
    }

    public String getContentRepos() {
        return contentrepos;
    }
    
    public void setVector(Vector3f vector) {
        this.vector = vector;
    }

    public Vector3f getVector() {
        return vector;
    }
    
    
    
    /**
     * Returns the name of the user from which the message came.
     *
     * @return A String user name
     */
    public String getFromUserName() {
        return fromUserName;
    }

    /**
     * Returns the name of the user to which the messages is sent. If meant
     * for everyone, this returns an empty string.
     *
     * @return A String user name
     */
    public String getToUserName() {
        return (toUserName != null) ? toUserName : "";
    }

    /**
     * Returns the text of the text chat message.
     *
     * @return A String text chat message
     */
    public String getTextMessage() {
        return textMessage;
    }
}
