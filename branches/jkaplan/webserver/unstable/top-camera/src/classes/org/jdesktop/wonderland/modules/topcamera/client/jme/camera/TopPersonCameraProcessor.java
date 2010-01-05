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
package org.jdesktop.wonderland.modules.topcamera.client.jme.camera;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.jme.CameraController;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * A very simplistic top camera model.
 *
 * @author paulby
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class TopPersonCameraProcessor implements CameraController {

    private Quaternion rotation = new Quaternion();
    private Vector3f translation = new Vector3f();
    protected float cameraZoom = 0.2f;
    protected Vector3f offset = new Vector3f(0.0f, 10.0f, 0.0f);
    private boolean commitRequired = false;
    private Quaternion viewRot = new Quaternion();
    private Vector3f viewTranslation = new Vector3f();

    protected Vector3f cameraLook = new Vector3f(0.0f, -1.0f, 0.0f);
    private Vector3f yUp = new Vector3f(0.0f, 0.0f, 1.0f);

    private WorldManager wm;

    private CameraNode cameraNode;

    private EventClassFocusListener listener = null;

    private boolean enabled = false;

    private int mouseX = 0;
    private int mouseY = 0;
    private float elevation = 0f;

    private Vector3f avatarPos = new Vector3f();
    private Quaternion avatarRot = new Quaternion();
    private Vector3f cameraTranslation = new Vector3f();

    public TopPersonCameraProcessor() {
        wm = ClientContextJME.getWorldManager();
    }

    public void compute() {
    }

    public void commit() {
        if (commitRequired) {
            cameraNode.setLocalRotation(rotation);
            cameraNode.setLocalTranslation(translation);
            wm.addToUpdateList(cameraNode);
            commitRequired = false;
        }
    }

    @Override
    public void viewMoved(CellTransform worldTransform) {
        avatarPos = worldTransform.getTranslation(avatarPos);
        avatarRot = worldTransform.getRotation(avatarRot);
        update(avatarPos, avatarRot);
    }

    private void update(Vector3f tIn, Quaternion rIn) {
        translation.set(tIn);
        rotation.set(rIn);
        viewRot.set(rotation);
        viewTranslation.set(translation);

        Vector3f cameraTrans = rotation.mult(offset);
        cameraTrans.addLocal(cameraTranslation);
        translation.addLocal(cameraTrans);

        rotation.lookAt(rotation.mult(cameraLook), yUp);
        commitRequired = true;
    }

    public void setEnabled(boolean enabled, CameraNode cameraNode) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;

        // Called on the compute thread, therefore does not need to be synchronized
        this.cameraNode = cameraNode;
        if (enabled) {
            if (listener == null) {
                listener = new EventClassFocusListener() {

                    @Override
                    public Class[] eventClassesToConsume() {
                        return new Class[]{KeyEvent3D.class, MouseEvent3D.class};
                    }

                    @Override
                    public void commitEvent(Event event) {
                        if (event instanceof KeyEvent3D) {
                            KeyEvent key = (KeyEvent) ((KeyEvent3D) event).getAwtEvent();
                            if (key.getKeyCode() == KeyEvent.VK_EQUALS) {
                                offset.y += cameraZoom;
                                viewMoved(new CellTransform(viewRot, viewTranslation));
                            } else if (key.getKeyCode() == KeyEvent.VK_MINUS) {
                                offset.y -= cameraZoom;
                                viewMoved(new CellTransform(viewRot, viewTranslation));
                            }
                        } else if (event instanceof MouseEvent3D) {
                            MouseEvent mouse = (MouseEvent) ((MouseEvent3D) event).getAwtEvent();
                            if (mouse instanceof MouseWheelEvent) {
                                int clicks = ((MouseWheelEvent) mouse).getWheelRotation();
                                offset.y += cameraZoom * clicks;
                                viewMoved(new CellTransform(viewRot, viewTranslation));
                            } else if (mouse.isControlDown() && mouse.getID() == MouseEvent.MOUSE_DRAGGED) {
                                float diffz = (mouse.getY() - mouseY) * .04f;
                                float diffx =  (mouse.getX() - mouseX) * .04f;

                                cameraTranslation.x += diffx;
                                cameraTranslation.z += diffz;

                                mouseX = mouse.getX();
                                mouseY = mouse.getY();

                                viewMoved(new CellTransform(viewRot, viewTranslation));
                            } else if (mouse.isControlDown() && mouse.getButton() == MouseEvent.BUTTON3) {
                                offset = new Vector3f(0.0f, 10.0f, 0.0f);
                                cameraTranslation.zero();
                                viewMoved(new CellTransform(viewRot, viewTranslation));
                            }
                            else {
                                mouseX = mouse.getX();
                                mouseY = mouse.getY();
                            }
                        }
                    }
                };
            }
            ClientContextJME.getInputManager().addGlobalEventListener(listener);
        } else {
            ClientContextJME.getInputManager().removeGlobalEventListener(listener);
        }
    }
}
