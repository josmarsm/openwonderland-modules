/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.presentationbase.client;

import com.jme.math.Vector3f;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class MovingPlatformAvatarComponent extends CellComponent implements TransformChangeListener {

    private static final Logger logger =
        Logger.getLogger(MovingPlatformAvatarComponent.class.getName());

    CellTransform currentPlatformTransform;
    CellTransform currentPlatformLocalAvatarTransform;

    public MovingPlatformAvatarComponent(Cell cell) {
        super(cell);
        logger.warning("Constructed + attached a moving platform avatar component!");
    }

    public void addMotionListener(Cell movablePlatform) {

            movablePlatform.addTransformChangeListener(this);

            // Grab the current position.
            currentPlatformTransform = movablePlatform.getWorldTransform().clone(null);

            // Also add a listener for the avatar's movement, so we can accurately keep
            // track of the avatar's position relative to the platform.
//            cell.addTransformChangeListener(this);
    }

    public void removeMotionListener(Cell movablePlatform) {

            logger.warning("Removing motion listener from an old cell: " + movablePlatform);
            movablePlatform.removeTransformChangeListener(this);
//            cell.removeTransformChangeListener(this);
    }

//    public void cellMoved(CellTransform transform, CellMoveSource source) {
//        logger.warning("Got a cell moved event! New transform: " + transform);
//    }
//
    public void transformChanged(Cell platformCell, ChangeSource source) {
        
//        if(cell.equals(this.cell)) {
//
////            // Then this is an update of the avatar position. Calculate the new
////            // platform-relative position.
////            currentPlatformLocalAvatarTransform = currentCellTransform.sub(cell.getWorldTransform());
////            logger.warning("update platform-local position: localPosition: " + currentPlatformLocalAvatarTransform);
//        } else {
//        // 2. Now look at our new position and add in the avatar's local position to figure
//        //    out where the avatar should be in the global transform post-cell-move.
//            CellTransform finalPosition = cell.getWorldTransform().mul(currentPlatformLocalAvatarTransform);
//            logger.warning("platform moved, updating avatar position to: " + finalPosition);

            logger.warning("oldPlatform: " + currentPlatformTransform.getTranslation(null) + "; newPlatform: " + platformCell.getWorldTransform().getTranslation(null));
            CellTransform avatarLocalToPlatform = transform(this.cell.getWorldTransform(), new CellTransform(null, null), currentPlatformTransform);

            logger.warning("avatarLocalToPlatform: " + avatarLocalToPlatform.getTranslation(null));
            
//            CellTransform avatarLocalToNewPlatform = transform(avatarLocalToPlatform, currentPlatformTransform, platformCell.getWorldTransform());
            CellTransform avatarLocalToNewPlatform = platformCell.getWorldTransform().mul(avatarLocalToPlatform);

            logger.warning("avatarLocalToNewPlatform: " + avatarLocalToNewPlatform.getTranslation(null));

//            CellTransform finalTransform = transform(avatarLocalToNewPlatform, platformCell.getWorldTransform(), new CellTransform(null, null));
            final CellTransform finalTransform = avatarLocalToNewPlatform;

            logger.warning("finalGlobalTransform: " + finalTransform.getTranslation(null));
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                ((AvatarCell)cell).triggerGoto(finalTransform.getTranslation(null), finalTransform.getRotation(null));
               }
            });
            
            currentPlatformTransform = platformCell.getWorldTransform();
//        }
    }

  /**
    * A utility routine to convert the given transform from world coordinates
    * to another reference system. Typically, the given transform is the
    * initial transform of the Cell in world coordinates and we want to
    * transform with respect to some Cell in the world.
    *
    */
   private static CellTransform transform(CellTransform transform,
           CellTransform fromReferenceSystem, CellTransform toReferenceSystem) {

       CellTransform newTransform = toReferenceSystem.clone(null);
       newTransform.invert();
       newTransform.mul(fromReferenceSystem);
       newTransform.mul(transform);
       return newTransform;
   }
}
