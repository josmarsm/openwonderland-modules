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

package org.jdesktop.wonderland.modules.layout.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Server state for layout participant cell component.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="layout-participant-cell-component")
@ServerState
public class LayoutParticipantCellComponentServerState
        extends CellComponentServerState {

    // The index of the position this participant was added to the layout
    @XmlElement(name = "position")
    private int position = -1;

    /** Default constructor */
    public LayoutParticipantCellComponentServerState() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.layout.server.cell." +
                "LayoutParticipantCellComponentMO";
    }

    @XmlTransient
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
