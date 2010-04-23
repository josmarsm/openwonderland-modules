/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

package org.jdesktop.wonderland.modules.pdfpresentation.common;

import com.jme.math.Vector3f;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
@XmlRootElement(name="moving-platform-cell")
@ServerState
public class MovingPlatformCellServerState extends CellServerState {

    @XmlElement(name="platform-width")
    private float platformWidth;

    @XmlElement(name="platform-depth")
    private float platformDepth;

//    @XmlElement(name="translation")
//    private Vector3f translation;

    public MovingPlatformCellServerState() {
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.pdfpresentation.server.MovingPlatformCellMO";
    }

    @XmlTransient public float getPlatformWidth() { return this.platformWidth; }
    public void setPlatformWidth(float platformWidth) {
        this.platformWidth = platformWidth;
    }

    @   XmlTransient public float getPlatformDepth() { return this.platformDepth; }
    public void setPlatformDepth(float platformDepth) {
        this.platformDepth = platformDepth;
    }

//    @XmlTransient public Vector3f getTranslation() { return this.translation; }
//    public void setTranslation(Vector3f translation) {
//        this.translation = translation;
//    }

}
