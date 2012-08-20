/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.globals.builder.brushes;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.PickDetails;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseMovedEvent3D;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeViewerEntity;
import org.jdesktop.wonderland.modules.ezscript.client.globals.Builder;
import org.jdesktop.wonderland.modules.ezscript.client.globals.builder.MouseFollowerListener;

import static org.jdesktop.wonderland.modules.ezscript.client.globals.Builder.*;

/**
 *
 * @author Ryan
 */
public class ShapeBrush extends MouseFollowerListener {

    private final ShapeViewerEntity entity;
    private final Semaphore lock = new Semaphore(0);
    private final boolean followsTheMouse;
    private static final Logger logger = Logger.getLogger(ShapeBrush.class.getName());

    public ShapeBrush(String shape, String color, boolean blend, boolean followsTheMouse) {
        this.followsTheMouse = followsTheMouse;

        entity = new ShapeViewerEntity(shape);

        entity.setAppearance(STRING_TO_COLORS.get(color.toLowerCase()));
        entity.setBlended(blend);
    }

    public void paintMesh(Vector3f position) {
        entity.showShape();

        if (followsTheMouse) {
            followTheMouse();
        } else {
            doNotFollowTheMouse(position);
        }
    }

    public ShapeViewerEntity getMesh() {
        return entity;
    }

    @Override
    public void mouseFollowerClicked(MouseButtonEvent3D event) {
        PickDetails details = event.getPickDetails();
        if (details != null) {
            entity.updateTransform(details.getPosition(), new Quaternion());
        }

        lock.release();
        InputManager.inputManager().removeGlobalEventListener(this);
    }

    @Override
    public void mouseFollowerMoved(MouseMovedEvent3D event) {
        PickDetails details = event.getPickDetails();
        if (details == null) {
            return;
        }


        entity.updateTransform(details.getPosition(), new Quaternion());
    }

    private void followTheMouse() {
        InputManager.inputManager().addGlobalEventListener(this);
        try {
            lock.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(Builder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doNotFollowTheMouse(Vector3f position) {
//            entity.updateTransform(position, new Quaternion());
        logger.warning("ADDING MINECRAFT MOUSE LISTENER!");

        entity.addEventListener(new EventClassListener() {
            @Override
            public Class[] eventClassesToConsume() {
                return new Class[]{MouseButtonEvent3D.class};
            }

            @Override
            public void commitEvent(Event event) {
                MouseButtonEvent3D e = (MouseButtonEvent3D) event;

                if (e.isClicked()) {

                    Vector3f position = e.getPickDetails().getPosition();
                    Vector3f normal = e.getPickDetails().getNormal();

                    AuxiliaryQuadBrush brush = new AuxiliaryQuadBrush();
                    ShapeViewerEntity quad = brush.getQuad();

                    logger.warning("PROCESSING ROTATION!");
                    Quaternion rotation = new Quaternion();
                    rotation.lookAt(normal, new Vector3f(0, 1, 0));
                    quad.updateTransform(position, rotation);
                }
            }
        });

//
//            InputManager.inputManager().addGlobalEventListener(new MinecraftMouseListener() {
//                @Override
//                public void mouseClickedOnEntity(Vector3f normal, Vector3f position) {
//                }
//
//                @Override
//                public void mouseMovedOnEntity(Vector3f normal, Vector3f position) {
//
//                    if (normal == null) {
//                        logger.warning("NORMAL IS NULL, NOT PROCESSING EVENT!");
//                        return;
//                    }
//
//                    if (position == null) {
//                        logger.warning("POSITION IS NULL, NOT PROCESSING EVENT!");
//                        return;
//                    }
//
//
//                    logger.warning("CREATING AUXILIARY BRUSH!");
//
//
//                    AuxiliaryQuadBrush brush = new AuxiliaryQuadBrush();
//                    ShapeViewerEntity quad = brush.getQuad();
//
//                    logger.warning("PROCESSING ROTATION!");
//                    Quaternion rotation = new Quaternion();
//                    rotation.lookAt(normal, new Vector3f(0, 1, 0));
//                    quad.updateTransform(position, rotation);
//                }
//            });
//

    }
}
