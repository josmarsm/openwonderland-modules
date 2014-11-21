/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.appframe.common;

import java.io.Serializable;
import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;


/**
 *
 * @author nilang
 */
@ServerState
@XmlRootElement(name = "AppFrameApp")
public class AppFrameApp extends SharedData implements Serializable {

    private Date created;
    private String state;
    private String createdBy;
    private Date lastUsed;
    private String contentURI;
    
    @XmlElement
    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }

// required constructor for JAXB
    public AppFrameApp() {
    }

    public AppFrameApp(String state, Date created, String createdBy,Date lastUsed,String contentURI) {
        
        this.state = state;
        this.created = created;
        this.createdBy = createdBy;
        this.lastUsed=lastUsed;
        this.contentURI = contentURI;
    }

    @XmlElement
    public String getState() {
        if(!state.contains("CDATA")) {
            state = "<![CDATA["+state+"]]>";
        }
        return state;
    }

    public void setState(String state) {
        if(!state.contains("CDATA")) {
            state = "<![CDATA["+state+"]]>";
        }
        this.state = state;
    }

    @XmlElement
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;

    }

    @XmlElement
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @XmlElement
    public String getContentURI() {
        return contentURI;
    }

    public void setContentURI(String contentURI) {
        this.contentURI = contentURI;
    }
    
    
}
