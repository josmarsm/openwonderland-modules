/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.presentationbase.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveListener;
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveSource;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class MovingPlatformAvatarComponent extends CellComponent implements CellMoveListener {

    private static final Logger logger =
        Logger.getLogger(MovingPlatformAvatarComponent.class.getName());

    public MovingPlatformAvatarComponent(Cell cell) {
        super(cell);
        logger.warning("Constructed + attached a moving platform avatar component!");
    }

    public void addMotionListener(Cell movablePlatform) {
        MovableComponent mc = movablePlatform.getComponent(MovableComponent.class);

        if(mc!=null) {
            logger.warning("Adding a motion listener to a new cell: " + movablePlatform);
            mc.addServerCellMoveListener(this);
        }
    }

    public void removeMotionListener(Cell movablePlatform) {
        MovableComponent mc = movablePlatform.getComponent(MovableComponent.class);

        if(mc!=null) {
            logger.warning("Removing motion listener from an old cell: " + movablePlatform);
            mc.removeServerCellMoveListener(this);
        }

    }

    public void cellMoved(CellTransform transform, CellMoveSource source) {
        logger.warning("Got a cell moved event! New transform: " + transform);
    }
}
