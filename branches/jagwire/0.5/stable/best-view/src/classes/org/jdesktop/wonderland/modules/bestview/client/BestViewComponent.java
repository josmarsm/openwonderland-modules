/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.bestview.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import java.awt.event.MouseWheelEvent;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.jme.CameraController;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseWheelEvent3D;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * Cell component for best view
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class BestViewComponent extends CellComponent
    implements ContextMenuActionListener
{
    private static final Logger LOGGER =
            Logger.getLogger(BestViewComponent.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org.jdesktop.wonderland.modules.bestview.client.Bundle");

    // import the context menu component
    @UsesCellComponent
    private ContextMenuComponent contextMenu;

    // context menu item factory
    private final ContextMenuFactorySPI ctxMenuFactory;

    public BestViewComponent(Cell cell) {
        super (cell);

        // create the context menu item and register it
        final ContextMenuItem item =
                new SimpleContextMenuItem(BUNDLE.getString("Best_View"), this);
        ctxMenuFactory = new ContextMenuFactorySPI() {
           public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                return new ContextMenuItem[] { item };
            }
        };
    }

    /**
     * Configure the component based on the client state that was
     * passed in.
     */
    @Override
    public void setClientState(CellComponentClientState clientState) {
        // out component's state
        // ((BestViewClientState) clientState).getXXX();

        // allow the superclass to do any configuration necessary
        super.setClientState(clientState);
    }

    /**
     * Set the status of the component
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        if (status == CellStatus.ACTIVE && increasing) {
            // add context menu item
            contextMenu.addContextMenuFactory(ctxMenuFactory);
        } else if (status == CellStatus.INACTIVE && !increasing) {
            // remove context menu item
            contextMenu.removeContextMenuFactory(ctxMenuFactory);
        }

        super.setStatus(status, increasing);
    }

    /**
     * Called when the context menu is clicked
     */
    public void actionPerformed(ContextMenuItemEvent event) {
        CellTransform xform = BestViewUtils.getBestView(cell);

        CameraController prev = ViewManager.getViewManager().getCameraController();
        CellTransform viewLocation = ViewManager.getViewManager().getPrimaryViewCell().getWorldTransform();

        CameraController camera = new BestViewCameraController(xform.getTranslation(null),
                                                               xform.getRotation(null),
                                                               prev, viewLocation);
        ClientContextJME.getViewManager().setCameraController(camera);
    }

    
    /**
     * A camera controller that puts the camera at the best view position,
     * and allows zooming in or out from there.
     */
    class BestViewCameraController extends EventClassFocusListener 
            implements CameraController
    {
        private final Vector3f location;
        private final Quaternion look;
        private final CameraController prevCamera;
        private final CellTransform viewLocation;

        private boolean needsUpdate = true;
        private CameraNode cameraNode;
        private float zoom = 0;

        public BestViewCameraController(Vector3f location, Quaternion look,
                                        CameraController prevCamera,
                                        CellTransform viewLocation)
        {
            this.location = location;
            this.look = look;
            this.prevCamera = prevCamera;
            this.viewLocation = viewLocation;
        }
        
        public void setEnabled(boolean enabled, CameraNode cameraNode) {
            if (enabled) {
                this.cameraNode = cameraNode;
                ClientContextJME.getInputManager().addGlobalEventListener(this);
            } else {
                this.cameraNode = null;
                ClientContextJME.getInputManager().removeGlobalEventListener(this);
            }
        }

        @Override
        public Class[] eventClassesToConsume () {
            return new Class[] { MouseWheelEvent3D.class };
        }

        @Override
        public void commitEvent (Event event) {
            MouseWheelEvent me = (MouseWheelEvent) ((MouseEvent3D) event).getAwtEvent();
            int clicks = me.getWheelRotation();
            zoom -= clicks * 0.2f;
            needsUpdate = true;
        }

        public void compute() {
        }

        public void commit() {
            if (cameraNode != null && needsUpdate) {
                Vector3f loc = location.clone();
                Vector3f z = look.mult(new Vector3f(0, 0, zoom));
                loc.addLocal(z);

                cameraNode.setLocalTranslation(loc);
                cameraNode.setLocalRotation(look);
                ClientContextJME.getWorldManager().addToUpdateList(cameraNode);
                needsUpdate = false;
            }
        }

        public void viewMoved(CellTransform worldTransform) {
            // if the avatar moves, go back to the original camera
            if (!viewLocation.equals(worldTransform)) {
                ClientContextJME.getViewManager().setCameraController(prevCamera);
            }
        }
    }
}
