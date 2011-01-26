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
package org.jdesktop.wonderland.modules.poster.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellServerState;

/**
 * The cell server state for the PosterCell.<br>
 * Adapted from the Cell server state for the "generic" Cell originally written<br>
 * by Jordan Slott <jslott@dev.java.net>
 *
 * @author Bernard Horan
 */
@XmlRootElement(name="postercell")
// bind all non-static, non-transient fields
// to XML unless annotated with @XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@ServerState
public class PosterCellServerState extends App2DCellServerState {
    /** The user's preferred width of the poll window. */
    @XmlElement(name="preferredWidth")
    public int preferredWidth = 300;

    /** The user's preferred height of the poll window. */
    @XmlElement(name="preferredHeight")
    public int preferredHeight = 300;

    public PosterCellServerState() {
        super();
    }

    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.poster.server.PosterCellMO";
    }

    @XmlTransient public int getPreferredWidth () {
        return preferredWidth;
    }

    public void setPreferredWidth (int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    @XmlTransient public int getPreferredHeight () {
        return preferredHeight;
    }

    public void setPreferredHeight (int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    /**
     * Returns a string representation of this class.
     *
     * @return The server state information as a string.
     */
    @Override
    public String toString() {
        return super.toString() + " [PosterCellServerState]: " +
	    "preferredWidth=" + preferredWidth + "," +
	    "preferredHeight=" + preferredHeight + "," +
	    "pixelScaleX=" + pixelScaleX + "," +
	    "pixelScaleY=" + pixelScaleY;
    }

}
