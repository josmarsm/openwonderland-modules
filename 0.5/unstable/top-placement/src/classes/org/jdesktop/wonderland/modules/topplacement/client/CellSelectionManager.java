/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.topplacement.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.scenemanager.SceneManager;
import org.jdesktop.wonderland.client.scenemanager.event.SelectionEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 *
 * @author jkaplan
 */
public class CellSelectionManager extends EventClassListener {
    private static Logger logger = 
            Logger.getLogger(CellSelectionManager.class.getName());

    private final Map<Cell, CellSelectionEntity> selected =
            new LinkedHashMap<Cell, CellSelectionEntity>();
    private CellSelectionEntity hover;

    public void register() {
        SceneManager.getSceneManager().addSceneListener(this);
    }

    public void unregister() {
        SceneManager.getSceneManager().removeSceneListener(this);

        // clear selection on the commit thread, so we don't have to worry
        // about synchronization
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                SceneManager.getSceneManager().clearSelection();
                setHover(null);
                
                // we won't get the message about clearing the selection,
                // so make sure to manually remove all selection we have
                // made
                deselectAll();
            }
        });
    }

    public synchronized boolean areCellsSelected() {
        return !selected.isEmpty();
    }

    public synchronized Set<Cell> getSelectedCells() {
        // return a copy
        return new LinkedHashSet<Cell>(selected.keySet());
    }

    @Override
    public Class[] eventClassesToConsume() {
        return new Class[]{ SelectionEvent.class };
    }

    @Override
    public void commitEvent(Event event) {
        Set<Cell> addList = new LinkedHashSet<Cell>();
        Set<Cell> removeList = new LinkedHashSet<Cell>(selected.keySet());

        // sync up our view of selected entities with the scene manager's
        for (Entity e : SceneManager.getSceneManager().getSelectedEntities()) {
            Cell cell = SceneManager.getCellForEntity(e);
            if (cell != null) {
                addList.add(cell);
            }
        }

        // remove all cells that should be there from the list of current cells.
        // The remaining cells are the cells that need to be removed.
        removeList.removeAll(addList);
        for (Cell cell : removeList) {
            deselect(cell);
        }

        // remove all cells that were previously added from the list of cells to
        // add.  This gives just the new cells.
        addList.removeAll(selected.keySet());
        for (Cell cell : addList) {
            select(cell);
        }
    }

    public synchronized void showDrag(Vector3f delta) {
        for (CellSelectionEntity e : selected.values()) {
            e.showDrag(delta);
        }
    }

    public synchronized void showRotation(Quaternion rotation) {
        for (CellSelectionEntity e : selected.values()) {
            e.showRotaton(rotation);
        }
    }

    public synchronized void resetAll() {
        for (CellSelectionEntity e : selected.values()) {
            e.reset();
        }
    }

    private synchronized void setHover(Cell cell) {
        // remove existing hover
        if (hover != null) {
            hover.dispose();
            hover = null;
        }

        // add the new hover
        if (cell != null) {
            hover = new CellSelectionEntity(cell);
            hover.setColor(ColorRGBA.lightGray);
            hover.setVisible(true);
            hover.showBounds(cell.getLocalBounds());
        }
    }

    private synchronized void deselectAll() {
        for (Cell cell : selected.keySet().toArray(new Cell[selected.size()])) {
            deselect(cell);
        }
    }

    // must be called on the commit thread while holding the lock on
    // this class
    private void select(Cell cell) {
        CellSelectionEntity cse = new CellSelectionEntity(cell);
        cse.showBounds(cell.getLocalBounds());
        cse.setVisible(true);

        // this is a good time to make sure the cell has a movable component,
        // so that we know we can move it later
        checkMovableComponent(cell);

        selected.put(cell, cse);
    }

    // must be called on the commit thread while holding the lock on
    // this class
    private void deselect(Cell cell) {
        CellSelectionEntity cse = selected.remove(cell);
        cse.dispose();
    }

    private void checkMovableComponent(final Cell cell) {
        // First try to add the movable component. The presence of the
        // movable component will determine when we actually add the
        // affordance to the scene graph. We do this in a separate thread
        // because adding the movable component is a synchronous call (it
        // waits for a response message. That would block the thread calling
        // the setStatus() method. This won't work since this thread cannot
        // be blocked when adding the component.
        new Thread() {
            @Override
            public void run() {
                MovableComponent mc = cell.getComponent(MovableComponent.class);
                if (mc == null) {
                    addMovableComponent(cell);
                }
            }
        }.start();
    }

    /**
     * Adds the movable component, assumes it does not already exist.
     */
    private void addMovableComponent(Cell cell) {

        // Go ahead and try to add the affordance. If we cannot, then log an
        // error and return.
        CellID cellID = cell.getCellID();
        String className = "org.jdesktop.wonderland.server.cell.MovableComponentMO";
        CellServerComponentMessage cscm =
                CellServerComponentMessage.newAddMessage(cellID, className);
        ResponseMessage response = cell.sendCellMessageAndWait(cscm);
        if (response instanceof ErrorMessage) {
            logger.warning("Unable to add movable component for Cell" +
                    cell.getName() + " with ID " + cell.getCellID());
        }
    }
}
