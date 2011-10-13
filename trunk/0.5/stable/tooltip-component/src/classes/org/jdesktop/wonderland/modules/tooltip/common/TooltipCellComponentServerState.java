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

package org.jdesktop.wonderland.modules.tooltip.common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
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
    @XmlElement(name = "text")
    private String text = null;

    // The timeout (in milliseconds) to hide the tooltip even if the mouse has
    // not moved. If -1 then no timeout.
    @XmlElement(name = "timeout")
    private int timeout = -1;

    /** Default constructor */
    public TooltipCellComponentServerState() {
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.tooltip.server" +
               ".TooltipCellComponentMO";
    }

    /**
     * Returns the tooltip text.
     *
     * @return The tooltip text
     */
    @XmlTransient
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

    /**
     * Returns the tooltip timeout to hide the tooltip, in milliseconds.
     *
     * @return The timeout in milliseconds
     */
    @XmlTransient
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the tooltip timeout to hide the tooltip, in milliseconds.
     *
     * @param timeout The timeout in milliseconds
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
