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
package uk.ac.essex.wonderland.modules.webcamcontrol.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Record to represent a Webcam Viewer
 * @author Bernard Horan
 */
@XmlRootElement
public class WebcamRecord implements Serializable {

    @XmlElement
    private int cellID;
    @XmlElement
    private String cellName;
    private String cameraURI;
    private String cameraUsername;
    private String cameraPassword;
    private String cameraState;
    private List<WebcamAction> actions;

    public WebcamRecord(int cellID, String cellName, String cameraURI, String cameraUsername, String cameraPassword, String cameraState) {
        this.cellID = cellID;
        this.cellName = cellName;
        this.cameraURI = cameraURI;
        this.cameraUsername = cameraUsername;
        this.cameraPassword = cameraPassword;
        this.cameraState = cameraState;
        this.actions = new ArrayList<WebcamAction>();
    }

    public WebcamRecord() {
        
    }

    public int getCellID() {
        return cellID;
    }

    public String getCellName() {
        return cellName;
    }

    public void addAction(WebcamAction action) {
        actions.add(action);
    }

    public List<WebcamAction> getActions() {
        return actions;
    }

    public String getCameraURI() {
        return cameraURI;
    }

    public String getCameraUsername() {
        return cameraUsername;
    }

    public String getCameraPassword() {
        return cameraPassword;
    }

    public String getCameraObscuredPassword() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cameraPassword.length(); i++) {
            builder.append('*');
        }
        return builder.toString();
    }

    public String getCameraState() {
        return cameraState;
    }

    /**
     * Test equality of webcam records
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof WebcamRecord) {
            return cellID == ((WebcamRecord)o).cellID;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + this.cellID;
        return hash;
    }

    public void setCameraURI(String cameraURI) {
        this.cameraURI = cameraURI;
    }

    public void setCameraUsername(String cameraUsername) {
        this.cameraUsername = cameraUsername;
    }

    public void setCameraPassword(String cameraPassword) {
        this.cameraPassword = cameraPassword;
    }

    public void setCameraState(String cameraState) {
        this.cameraState = cameraState;
    }
}
