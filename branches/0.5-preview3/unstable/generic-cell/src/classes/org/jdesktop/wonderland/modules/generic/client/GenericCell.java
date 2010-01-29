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
package org.jdesktop.wonderland.modules.generic.client;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;

/**
 * A "generic" client-side Cell that works with a developer-defined client-side
 * Cell. This Cell is meant to be the most simple implementation of a Cell, so
 * that developers do not need to develop any server-side facilities or the
 * communication mechanism (client and server state) necessary for the Cell
 * infrastructure.
 * <p>
 * This class adds a "shared state" component which can be used by the client-
 * side Cell code to synchronize the state of the Cell across clients.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class GenericCell extends Cell {

    // The "shared state" Cell component
    @UsesCellComponent protected SharedStateComponent sharedStateComp;

    /** Default constructor */
    public GenericCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
}
