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

package org.jdesktop.wonderland.modules.movierecorder.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Class to represent the server state of the movie recorder.
 * @author Bernard Horan
 */
@XmlRootElement(name="movierecorder-cell")
// bind all non-static, non-transient fields
// to XML unless annotated with @XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@ServerState
public class MovieRecorderCellServerState extends CellServerState implements Serializable {
    @XmlAttribute(required=true)
    private boolean isRecording;

    public MovieRecorderCellServerState() {
        super();
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.movierecorder.server.MovieRecorderCellMO";
    }

    public void setRecording(boolean b) {
        isRecording = b;
    }

    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" isRecording=");
        builder.append(isRecording);
        return builder.toString();
    }
}
