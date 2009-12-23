/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.topplacement.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseDraggedEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.jme.utils.ScenegraphUtils;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author jkaplan
 */
public class CellDragManager extends EventClassFocusListener {
    private static final Logger logger =
            Logger.getLogger(CellDragManager.class.getName());

    private CellSelectionManager selection;
    private boolean dragging = false;
    private boolean rotating = false;
    private boolean armed = false;

    private Vector3f dragStartWorld;
    private Vector3f dragEndWorld;
    private Quaternion dragEndRotation;
    private Point dragStartScreen;

    public CellDragManager(CellSelectionManager selection) {
        this.selection = selection;
    }

    public void register() {
        ClientContextJME.getInputManager().addGlobalEventListener(this);
    }

    public void unregister() {
        ClientContextJME.getInputManager().removeGlobalEventListener(this);
        dragging = false;
        rotating = false;
    }

    @Override
    public Class[] eventClassesToConsume() {
        return new Class[]{ MouseEvent3D.class, MouseDraggedEvent3D.class };
    }

    @Override
    public void commitEvent(Event event) {
        MouseEvent3D m3d = (MouseEvent3D) event;
        MouseEvent mouse = (MouseEvent) m3d.getAwtEvent();

        if (mouse.getID() == MouseEvent.MOUSE_PRESSED &&
                mouse.getButton() == MouseEvent.BUTTON1 &&
                selection.areCellsSelected())
        {
            // record the position of the start of a
            // potential drag
            dragStartWorld = m3d.getIntersectionPointWorld();
            dragStartScreen = new Point(mouse.getX(), mouse.getY());

            // we are now potentially ready for a drag or rotate
            armed = true;
        } else if (mouse.getID() == MouseEvent.MOUSE_RELEASED) {
            // we are no longer ready for a drag or rotate to start
            armed = false;

            if (rotating) {
                endRotate();
                rotating = false;
            }
            
            if (dragging) {
                endDrag();
                dragging = false;
            }
        } else if (mouse.getID() == MouseEvent.MOUSE_DRAGGED) {
            if (dragging) {
                doDrag(m3d);
            } else if (rotating) {
                doRotate(m3d);
            } else if (armed && mouse.isAltDown()) {
                rotating = true;
                doRotate(m3d);
            } else if (armed) {
                dragging = true;
                doDrag(m3d);
            }
        }
    }

    private void doDrag(MouseEvent3D m3d) {
        // caculate the world position we have dragged to
        dragEndWorld = ((MouseDraggedEvent3D) m3d).
            getDragVectorWorld(dragStartWorld, dragStartScreen, null);
        selection.showDrag(dragEndWorld);
    }

    private void endDrag() {
        // drag finished
        selection.resetAll();

        // move the cells
        for (Cell cell : selection.getSelectedCells()) {
            moveCell(cell, dragEndWorld, null);
        }
    }

    private void doRotate(MouseEvent3D m3d) {
        MouseEvent mouse = (MouseEvent) m3d.getAwtEvent();

        // calculate the signed angle between the current point and the
        // start
        double angle = Math.atan2(dragStartScreen.y - mouse.getY(),
                                  dragStartScreen.x - mouse.getX());

        dragEndRotation = new Quaternion();
        dragEndRotation.fromAngleAxis((float) angle, new Vector3f(0, -1f, 0));
        selection.showRotation(dragEndRotation);
    }

    private void endRotate() {
        // rotate finished
        selection.resetAll();

        // rotate the cells
        for (Cell cell : selection.getSelectedCells()) {
            moveCell(cell, null, dragEndRotation);
        }
    }

    private void moveCell(Cell cell, Vector3f deltaPosition, 
                          Quaternion deltaRotation)
    {
        // note that the movable component is added by the selection manager,
        // so we can assume it is available
        MovableComponent movableComp = cell.getComponent(MovableComponent.class);

        // Get the cell's world transform
        CellTransform cellWorldTransform = cell.getWorldTransform();

        // Calculate the goal position by applying the deltas to position
        // and rotation
        CellTransform target = cellWorldTransform.clone(null);

        if (deltaPosition != null) {
            Vector3f targetPosition = cellWorldTransform.getTranslation(null);
            targetPosition = targetPosition.add(deltaPosition);
            target.setTranslation(targetPosition);
        }

        if (deltaRotation != null) {
            Quaternion targetRotation = cellWorldTransform.getRotation(null);
            targetRotation = targetRotation.mult(deltaRotation);
            target.setRotation(targetRotation);
        }

        // Find the parent transform, if any
        Cell parent = cell.getParent();
        if (parent != null) {
            target = ScenegraphUtils.computeChildTransform(parent.getWorldTransform(),
                                                           target);
        }

        movableComp.localMoveRequest(target);
    }
}
