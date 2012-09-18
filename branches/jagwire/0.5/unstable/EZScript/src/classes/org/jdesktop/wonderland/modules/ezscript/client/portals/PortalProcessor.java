/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.portals;

//import com.bulletphysics.collision.broadphase.Dbvt.Node;
import com.jme.bounding.BoundingBox;
import com.jme.entity.Entity;
import com.jme.intersection.TriangleCollisionResults;
import com.jme.light.LightNode;
import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.math.Ray;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import org.jdesktop.mtgame.AWTInputComponent;
import org.jdesktop.mtgame.AwtEventCondition;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.PickDetails;
import org.jdesktop.mtgame.PickInfo;
import org.jdesktop.mtgame.Portal;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.AWTEventProcessorComponent;

/**
 *
 * @author JagWire
 */

    public class PortalProcessor extends AWTEventProcessorComponent {

        /**
         * The arming conditions for this processor
         */
        private ProcessorArmingCollection collection = null;
        /**
         * First, some common variables
         */
        private int lastMouseX = -1;
        private int lastMouseY = -1;
        /**
         * The cumulative rotation in Y and X
         */
        private float rotY = 0.0f;
        private float rotX = 0.0f;
        /**
         * This scales each change in X and Y
         */
        private float scaleX = 0.7f;
        private float scaleY = 0.7f;
        private float walkInc = 0.5f;
        private float fallInc = 0.5f;

        /**
         * States for movement
         */
        private static final int STOPPED = 0;
        private static final int WALKING_FORWARD = 1;
        private static final int WALKING_BACK = 2;
        private static final int STRAFE_LEFT = 3;
        private static final int STRAFE_RIGHT = 4;
        /**
         * Our current state
         */
        private int state = STOPPED;
        /**
         * Our current position
         */
        private Vector3f position = new Vector3f(0.0f, 11.0f, -30.0f);
        private Vector3f potentialPos = new Vector3f(0.0f, 11.0f, -30.0f);

        /**
         * The Y Axis
         */
        private Vector3f yDir = new Vector3f(0.0f, 1.0f, 0.0f);

        /**
         * Our current forward direction
         */
        private Vector3f fwdDirection = new Vector3f(0.0f, 0.0f, 1.0f);
        private Vector3f rotatedFwdDirection = new Vector3f();
        private Vector3f lookDirection = new Vector3f();

        /**
         * Our current side direction
         */
        private Vector3f sideDirection = new Vector3f(1.0f, 0.0f, 0.0f);
        private Vector3f rotatedSideDirection = new Vector3f();

        /**
         * The quaternion for our rotations
         */
        private Quaternion quaternion = new Quaternion();
        /**
         * This is used to keep the direction rotated
         */
        private Matrix3f directionRotation = new Matrix3f();
        /**
         * The Node to modify
         */
        private Node target = null;


        /**
         * A Ray used for collision
         */
        private Ray ray = new Ray();
        private Ray heightRay = new Ray();
        private WorldManager worldManager = null;

        private Vector3f[] triData = new Vector3f[3];
        private Vector3f[] triData2 = new Vector3f[3];
        private Box avatar = null;
        private TriangleCollisionResults tcr = new TriangleCollisionResults();
        private float alpha = 0.5f;
        private float alphaInc = 0.01f;
        private Vector3f downVec = new Vector3f(0.0f, -1.0f, 0.0f);
        private BoundingBox triggerBox = new BoundingBox(new Vector3f(0.0f, 600.0f, 0.0f), 100.0f, 100.0f, 100.0f);
        private boolean removeRoof = false;


        //these are dependencies, they need to accounted for during use.
        private JMECollisionSystem collisionSystem;
        private boolean warping = false;
        private boolean animating = false;
        private Portal p1;
        private Portal p2;
        private PortalAnimationProcessor animationProcessor;
        
        /**
         * The default constructor
         */
        public PortalProcessor(AWTInputComponent listener, Node cameraNode,
                WorldManager wm, Entity myEntity) {
            super(listener);
            target = cameraNode;
            worldManager = wm;

            collection = new ProcessorArmingCollection(this);
            collection.addCondition(new AwtEventCondition(this));
            collection.addCondition(new NewFrameCondition(this));

            
            ray = new Ray();
            ray.origin = position;
            ray.direction = lookDirection;

            triData[0] = new Vector3f();
            triData[1] = new Vector3f();
            triData[2] = new Vector3f();
            triData2[0] = new Vector3f();
            triData2[1] = new Vector3f();
            triData2[2] = new Vector3f();

            avatar = new Box("Avatar", new Vector3f(), 1.0f, 1.0f, 1.0f);
            BoundingBox collisionBox = new BoundingBox(new Vector3f(), 1.0f, 1.0f, 1.0f);
            avatar.setModelBound(collisionBox);
            avatar.setLocalTranslation(potentialPos.x, potentialPos.y, potentialPos.z);
            avatar.setLocalRotation(quaternion.clone());
        }

        public void initialize() {
            setArmingCondition(collection);
        }

        public void compute(ProcessorArmingCollection collection) {
            Object[] events = getEvents();
            boolean updateRotations = false;

//// <editor-fold defaultstate="collapsed" desc="legacy mouse-look code">
//            for (int i = 0; i < events.length; i++) {
//                if (events[i] instanceof MouseEvent) {
//                    MouseEvent me = (MouseEvent) events[i];
//                    if (me.getID() == MouseEvent.MOUSE_MOVED) {
//                        processRotations(me);
//                        updateRotations = true;
//                    } else if (me.getID() == MouseEvent.MOUSE_CLICKED) {
//                        if (me.getButton() == MouseEvent.BUTTON1) {
//                            shootPortal(p1, p2);
//                        } else if (me.getButton() == MouseEvent.BUTTON3) {
//                            shootPortal(p2, p1);
//                        }
//                    }
//                } else if (events[i] instanceof KeyEvent) {
//                    KeyEvent ke = (KeyEvent) events[i];
//                    processKeyEvent(ke);
//                }
//            }// </editor-fold>

            if (updateRotations) {
                updateRotations();
            }

            updatePosition();
            updatePortalAlpha();
        }

        private void updateRotations() {
            directionRotation.fromAngleAxis(rotY * (float) Math.PI / 180.0f, yDir);
            directionRotation.mult(fwdDirection, rotatedFwdDirection);
            directionRotation.mult(sideDirection, rotatedSideDirection);
            //System.out.println("Forward: " + rotatedFwdDirection);
            quaternion.fromAngles(rotX * (float) Math.PI / 180.0f, rotY * (float) Math.PI / 180.0f, 0.0f);
            quaternion.mult(fwdDirection, lookDirection);
        }



        private void updatePosition() {
            switch (state) {
                case WALKING_FORWARD:
                    potentialPos.x = position.x + (walkInc * rotatedFwdDirection.x);
                    potentialPos.y = position.y + (walkInc * rotatedFwdDirection.y);
                    potentialPos.z = position.z + (walkInc * rotatedFwdDirection.z);
                    break;
                case WALKING_BACK:
                    potentialPos.x = position.x - (walkInc * rotatedFwdDirection.x);
                    potentialPos.y = position.y - (walkInc * rotatedFwdDirection.y);
                    potentialPos.z = position.z - (walkInc * rotatedFwdDirection.z);
                    break;
                case STRAFE_LEFT:
                    potentialPos.x = position.x + (walkInc * rotatedSideDirection.x);
                    potentialPos.y = position.y + (walkInc * rotatedSideDirection.y);
                    potentialPos.z = position.z + (walkInc * rotatedSideDirection.z);
                    break;
                case STRAFE_RIGHT:
                    potentialPos.x = position.x - (walkInc * rotatedSideDirection.x);
                    potentialPos.y = position.y - (walkInc * rotatedSideDirection.y);
                    potentialPos.z = position.z - (walkInc * rotatedSideDirection.z);
                    break;
            }

//            System.out.println("Avatar Bounds: " + avatar.getWorldBound());
            tcr.clear();
            boolean collision = false;
            avatar.setLocalTranslation(potentialPos.x, potentialPos.y, potentialPos.z);
            avatar.setLocalRotation(quaternion.clone());
            avatar.updateGeometricState(0, true);

            collisionSystem.findCollisions(avatar, tcr);
            for (int i=0; i<tcr.getNumber(); i++) {
                ArrayList<Integer> tris = tcr.getCollisionData(i).getSourceTris();
                if (tris.size() != 0) {
                    collision = true;
                    //TriMesh mesh = (TriMesh)tcr.getCollisionData(i).getSourceMesh();
                    //mesh.getTriangle(tris.get(0).intValue(), triData2);
                    //computeCollisionResponse(position, potentialPos, rotatedFwdDirection, triData2, walkInc);
                    break;
                }
            }
            checkGround(potentialPos);
            if (!collision || warping) {

                position.set(potentialPos);
            }

            if (p1 != null && p1.getGeometry().getWorldBound() != null &&
                p1.getGeometry().getWorldBound().contains(position)) {
                warpToPortal(p1);
                //System.out.println("Intersecting Portal 1");
            } else if (p2 != null && p2.getGeometry().getWorldBound() != null &&
                p2.getGeometry().getWorldBound().contains(position)) {
                warpToPortal(p2);
                //System.out.println("Intersecting Portal 2");
            } else {
                // I'm done
                warping = false;
            }
        }



        private void checkGround(Vector3f newPos) {
            float yDelta = 10.0f;
            float dy = fallInc;

            heightRay.origin = newPos.clone();
            heightRay.direction = downVec;
            //System.out.println(newPos);
            PickInfo pi = collisionSystem.pickAllWorldRay(heightRay, true, false);
            if (pi.size() != 0) {
                // Grab the first one
                PickDetails pd = pi.get(0);
                if (dy + yDelta > pd.getDistance()) {
                    dy = pd.getDistance() - yDelta;
                }

                newPos.y -= dy;
            } else {
                //System.out.println("NO GROUND!!!");
            }
        }

        private void updatePortalAlpha() {
            alpha += alphaInc;
            if (alpha > 0.8f) {
                alpha = 0.8f;
                alphaInc = -alphaInc;
            }

            if (alpha < 0.2f) {
                alpha = 0.2f;
                alphaInc = -alphaInc;
            }
        }

        /**
         * The commit methods
         */
        public void commit(ProcessorArmingCollection collection) {
            target.setLocalRotation(quaternion.clone());
            target.setLocalTranslation(position.x, position.y, position.z);
            //avatar.setLocalRotation(quaternion.clone());
            //avatar.setLocalTranslation(position.x, position.y, position.z);

            if (p1 != null && p1.getGeometry() != null) {
                ColorRGBA c1 = p1.getGeometry().getGlowColor();
                c1.a = alpha;
                p1.getGeometry().setGlowColor(c1);
                worldManager.addToUpdateList(p1.getGeometry());
            }

            if (p2 != null && p2.getGeometry() != null) {
                ColorRGBA c2 = p2.getGeometry().getGlowColor();
                c2.a = alpha;
                p2.getGeometry().setGlowColor(c2);
                worldManager.addToUpdateList(p2.getGeometry());
            }

            worldManager.addToUpdateList(target);
            if (removeRoof) {
                //roof.removeFromParent();
            }
            //worldManager.addToUpdateList(avatar);
        }

        private void warpToPortal(Portal p) {
            Vector3f zVec = new Vector3f(0.0f, 0.0f, 1.0f);
            if (warping || animating) {
                return;
            } else {
                warping = true;
            }

            Vector3f enterDir = new Vector3f();
            Vector3f exitDir = new Vector3f();

            // Change the position
            p.getEnterCoordinate(null, enterDir, null, null);
            p.getExitCoordinate(position, exitDir, null, null);

            double dx = Math.atan2(enterDir.z, enterDir.x) - Math.atan2(exitDir.z, exitDir.x);
            rotY += Math.toDegrees(dx);
            updateRotations();
        }

 

        
    }

