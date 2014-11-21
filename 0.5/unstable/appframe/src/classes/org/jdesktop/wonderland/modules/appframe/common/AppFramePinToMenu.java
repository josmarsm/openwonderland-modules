/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.appframe.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 *
 * @author nilang
 */
@ServerState
@XmlRootElement(name = "AppFramePinToMenu")
public class AppFramePinToMenu extends SharedData implements Serializable {

    private String fileName;
    private String fileURL;

    public AppFramePinToMenu() {
    }

    public AppFramePinToMenu(String fileName, String fileURL) {
        this.fileName = fileName;
        this.fileURL = fileURL;
    }
    
    @XmlElement
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @XmlElement
    public String getFileURL() {
        return fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }
    
}
