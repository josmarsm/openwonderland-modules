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
package org.jdesktop.wonderland.modules.timeline.common.audio;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;

/**
 * The component server state
 * @author jprovino
 */
@XmlRootElement(name="timeline-audio-component")
@ServerState
public class TimelineAudioComponentServerState extends CellComponentServerState {

    //@XmlElement(name="groupId")
    //private String groupId = "";

    //@XmlElements({
    //	@XmlElement(name="treatment")
        //})
    //private String[] treatments = new String[0];

    public TimelineAudioComponentServerState() {
    }

    //public void setGroupId(String groupId) {
    //	this.groupId = groupId;
    //}

    //@XmlTransient
    //public String getGroupId() {
    //	return groupId;
    //}

    //public void setTreatments(String[] treatments) {
    //	this.treatments = treatments;
    //}

    //@XmlTransient
    //public String[] getTreatments() {
    //	return treatments;
    //}

    public String getServerComponentClassName() {
	return "org.jdesktop.wonderland.modules.timeline.server.audio.TimelineAudioComponentMO";
    }

}
