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

package org.jdesktop.wonderland.modules.tooltip.common;

import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * Server state for Tooltip Cell Component.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name="tooltip-cell-component")
@ServerState
public class TooltipCellComponentServerState extends CellComponentServerState {

    // The text of the tooltip
    private String text = null;

    /** Default constructor */
    public TooltipCellComponentServerState() {
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.tooltip.server.TooltipCellComponentMO";
    }

    /**
     * Returns the tooltip text.
     *
     * @return The tooltip text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the tooltip text.
     *
     * @param text The tooltip text
     */
    public void setText(String text) {
        this.text = text;
    }
}
