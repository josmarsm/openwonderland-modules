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
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
@XmlRootElement(name="presentation-cell")
@ServerState
public class PresentationCellServerState extends CellServerState {


    // Transient because CellID isn't jaxb serializable, and we only
    // need this field for the initial construction of the cell.
    @XmlTransient
    private CellID slidesCellID;

    @XmlElement(name="initialized")
    private boolean initialized;

    @XmlElement(name="cur-slide")
    private int curSlide;
    
    public PresentationCellServerState() {
    }

    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.presentationbase.server.PresentationCellMO";
    }

    @XmlTransient public CellID getSlidesCellID() { return this.slidesCellID; }
    public void setSlidesCellID(CellID cellID) {
        this.slidesCellID = cellID;
    }

    @XmlTransient public boolean isInitialized() { return this.initialized; }
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @XmlTransient public int getCurSlide() { return this.curSlide; }
    public void setCurslide(int curSlide) {
        this.curSlide = curSlide;
    }
}
