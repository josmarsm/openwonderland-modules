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
package org.jdesktop.wonderland.modules.apptest.client.cell;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.apptest.common.cell.AppTestCellComponentClientState;

/**
 * Client-side app test cell component. This is attached to all client cells launched
 * by the app test. It registers the client cell with the app test client cell
 * so the launched app can be taken down at the appropriate time.
 *
 * NOTE: what is unique about this component is that it is not attached
 * to the app test cell. Instead, it is attached to the cells that the 
 * app test *launches*.
 *
 * @author deronj@dev.java.net
 */
public class AppTestCellComponent extends CellComponent {

    private static Logger logger = Logger.getLogger(AppTestCellComponent.class.getName());
    private CellID appTestCellID;
    private String displayName;

    public AppTestCellComponent(Cell cell) {
        super(cell);
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
        appTestCellID = ((AppTestCellComponentClientState)clientState).getAppTestCellID();
        displayName  = ((AppTestCellComponentClientState)clientState).getDisplayName();
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.ACTIVE && increasing == true) {
            AppTestCell appTestCell = (AppTestCell) cell.getCellCache().getCell(appTestCellID);
            if (appTestCell == null) {
                logger.severe("Cell launched by app test: Cannot register with app test cell " +
                              appTestCellID);
                return;
            }
            if (!(cell instanceof App2DCell)) {
                logger.severe("Cell launched by app test is not an App2D. CellID = " + 
                              cell.getCellID());
                return;
            }
            appTestCell.registerLaunchedCell(displayName, cell);
        }
    }
}

