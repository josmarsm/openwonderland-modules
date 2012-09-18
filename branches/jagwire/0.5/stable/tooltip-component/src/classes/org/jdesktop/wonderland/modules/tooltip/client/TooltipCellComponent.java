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
package org.jdesktop.wonderland.modules.tooltip.client;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.tooltip.common.TooltipCellComponentClientState;

/**
 * Client-side Tooltip Cell Component.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class TooltipCellComponent extends CellComponent {

    // The text of the tooltip.
    private String text = null;

    // The timeout (in milliseconds) to hide the tooltip even if the mouse has
    // not moved. If -1 then no timeout.
    private int timeout = -1;

    /**
     * Constructor, takes the Cell associated with the Cell Component.
     *
     * @param cell The Cell associated with this component
     */
    public TooltipCellComponent(Cell cell) {
        super(cell);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
        text = ((TooltipCellComponentClientState) clientState).getText();
        timeout = ((TooltipCellComponentClientState) clientState).getTimeout();
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
     * Returns the tooltip timeout to hide the tooltip, in milliseconds.
     *
     * @return The timeout in milliseconds
     */
    public int getTimeout() {
        return timeout;
    }
}
