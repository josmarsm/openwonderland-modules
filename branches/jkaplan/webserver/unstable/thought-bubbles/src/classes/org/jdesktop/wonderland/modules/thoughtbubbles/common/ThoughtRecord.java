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

package org.jdesktop.wonderland.modules.thoughtbubbles.common;

import com.jme.math.Vector3f;
import java.io.Serializable;
import java.util.Date;

/**
 * Stores all the info for representing a single thought. Representation is shared
 * between the client and server for consistency's sake.
 * 
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class ThoughtRecord implements Serializable {
    private String text;
    private boolean isQuestion;
    private String fromUser;
    private Date timestamp;

    private float posX, posY, posZ;

    public ThoughtRecord() {
        
    }

    public ThoughtRecord(String text, boolean isQuestion, String fromUser, Date timestamp, Vector3f pos) {
        this.text = text;
        this.isQuestion = isQuestion;
        this.fromUser = fromUser;
        this.timestamp = timestamp;
        this.posX = pos.x;
        this.posY = pos.y;
        this.posZ = pos.z;
    }

    public String getFromUser() {
        return fromUser;
    }

    public boolean isIsQuestion() {
        return isQuestion;
    }

    public float getX() {
        return this.posX;
    }
    
    public float getY() {
        return this.posY;
    }
    
    public float getZ() { 
        return this.posZ;
    }

    public String getText() {
        return text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public void setIsQuestion(boolean isQuestion) {
        this.isQuestion = isQuestion;
    }

    public void setPos(Vector3f pos) {
        this.posX = pos.x;
        this.posY = pos.y;
        this.posZ = pos.z;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        return "ThoughtRecord: " + fromUser + ": " + this.text + " (" + timestamp + ")";
    }
}
