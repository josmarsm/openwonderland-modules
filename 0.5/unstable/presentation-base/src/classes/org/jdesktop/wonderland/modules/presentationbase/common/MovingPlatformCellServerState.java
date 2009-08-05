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

package org.jdesktop.wonderland.modules.presentationbase.common;

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


    public MovingPlatformCellServerState() {
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.presentationbase.server.MovingPlatformCellMO";
    }

    @XmlTransient public float getPlatformWidth() { return this.platformWidth; }
    public void setPlatformWidth(float platformWidth) {
        this.platformWidth = platformWidth;
    }

    @   XmlTransient public float getPlatformDepth() { return this.platformDepth; }
    public void setPlatformDepth(float platformDepth) {
        this.platformDepth = platformDepth;
    }

}
