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
package org.jdesktop.wonderland.modules.sharedstatetest.client;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 * Empty cell that just has a script component, and launches some testing
 * code.
 */
public class SharedStateTestCell extends Cell {
    private Action launchAction;
    private SharedStateTestFrame frame;

    public SharedStateTestCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);

        launchAction = new AbstractAction("Test map") {
            public synchronized void actionPerformed(ActionEvent e) {
                if (frame == null) {
                    frame = new SharedStateTestFrame(SharedStateTestCell.this);
                }

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        frame.setVisible(true);
                        frame.toFront();
                    }
                });
            }
        };
    }
   
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        return null;
    }

    @Override
    public boolean setStatus(CellStatus status) {
        if (!super.setStatus(status)) {
            return false;
        }
        
        switch (status) {
            case ACTIVE:
                // add an item to the list
                JmeClientMain.getFrame().addToToolMenu(new JMenuItem(launchAction));
                break;
            case INACTIVE:
                // remove an item from the list
                // XXX TODO: remove XXX
                break;
        }
        
        return true;
    }
}
