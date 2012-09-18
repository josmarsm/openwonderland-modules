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
package org.jdesktop.wonderland.modules.connectionsample.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.Message;

/**
 * A message sent to the ColorChangeConnectionHandler to change the
 * color of a particular cell.  The state of this message includes the
 * id of the cell to update and the color to change it to.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ChangeColorMessage extends Message {
    private CellID cellID;
    private int color;

    public ChangeColorMessage(CellID cellID, int color) {
        super();

        this.cellID = cellID;
        this.color = color;
    }

    public CellID getCellID() {
        return cellID;
    }

    public int getColor() {
        return color;
    }
}
