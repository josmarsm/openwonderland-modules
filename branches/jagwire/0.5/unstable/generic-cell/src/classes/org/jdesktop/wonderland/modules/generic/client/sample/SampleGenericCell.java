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
package org.jdesktop.wonderland.modules.generic.client.sample;

import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.generic.client.GenericCell;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

/**
 * An example Cell that uses the "generic" cell facility.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class SampleGenericCell extends GenericCell {

    // The listen for mouse events to update the state
    private MouseEventListener listener = null;

    // The Cell's renderer
    private SampleGenericCellRenderer cellRenderer = null;

    // The current state of the Cell
    private String currentShape = "BOX";

    // The listener for changes to the shared map
    private SharedMapListenerCli mapListener = null;

    /** Constructor, takes Cell's ID and Cache */
    public SampleGenericCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        listener = new MouseEventListener();
        mapListener = new MySharedMapListener();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        if (status == CellStatus.ACTIVE && increasing == true) {
            // Create the shared hash map and initialize the shape type
            // if it does not already exist.
            SharedMapCli sharedMap = sharedStateComp.get("Sample");
            SharedString shapeType = sharedMap.get("Shape", SharedString.class);
            if (shapeType == null) {
                shapeType = SharedString.valueOf("BOX");
                sharedMap.put("Shape", shapeType);
            }
            currentShape = shapeType.toString();
        }
        else if (status == CellStatus.RENDERING && increasing == true) {
            // Initialize the render with the current shape type
            cellRenderer.updateShape(currentShape);
            
            // Listen for mouse events to change the shape
            listener.addToEntity(cellRenderer.getEntity());

            // Listen for changes in the shape type from other clients
            SharedMapCli sharedMap = sharedStateComp.get("Sample");
            sharedMap.addSharedMapListener("Shape", mapListener);

        }
        else if (status == CellStatus.DISK && increasing == false) {
            // Stop listening for mouse events to change the shape
            listener.removeFromEntity(cellRenderer.getEntity());

            // Remove the listener for changes to the shared map
            SharedMapCli sharedMap = sharedStateComp.get("Sample");
            sharedMap.removeSharedMapListener("Shape", mapListener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            cellRenderer = new SampleGenericCellRenderer(this);
            return cellRenderer;
        }
        return super.createCellRenderer(rendererType);
    }

    /**
     * Listens to changes in the shared map and updates the shape type
     */
    class MySharedMapListener implements SharedMapListenerCli {
        public void propertyChanged(SharedMapEventCli event) {
            String newShapeType = event.getNewValue().toString();
            cellRenderer.updateShape(newShapeType);
        }
    }

    /**
     * Listens for mouse click events on Button 1 and updates the shape
     */
    class MouseEventListener extends EventClassListener {
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (mbe.isClicked() == false || mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }

            // Update the shape type
            currentShape = (currentShape.equals("BOX") == true) ? "SPHERE" : "BOX";
            cellRenderer.updateShape(currentShape);

            // Tell other clients of the change by updating the shape type in
            // the shared map
            SharedMapCli sharedMap = sharedStateComp.get("Sample");
            SharedString shapeType = SharedString.valueOf(currentShape);
            sharedMap.put("Shape", shapeType);
        }
    }
}
