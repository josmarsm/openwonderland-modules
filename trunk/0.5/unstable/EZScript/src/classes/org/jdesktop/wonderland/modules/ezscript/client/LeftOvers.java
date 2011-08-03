/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.ZBufferState;
import imi.scene.PTransform;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import org.jdesktop.mtgame.AwtEventCondition;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;

/**
 * This class is never invoked, it just acts as a container/graveyard of old
 * scripting parts
 * @author JagWire
 */
public abstract class LeftOvers {
        //for testing only... not currently used...
    private final String script = "function clickRun() { print(\"hello!\"); }"
                                + "function enterRun() { print(\"hello mouse enter!\"); }"
                                + "function exitRun() { print(\"hello mouse exit!\"); }"
                                + "function approachRun() { print(\"hello approach!\"); }"
                                + "function leaveRun() { print(\"hello leave!\");}"
                                + "ScriptContext.enableMouseEvents();"
                                + "ScriptContext.enableProximityEvents();"
                                + "ScriptContext.onClick(clickRun);"
                                + "ScriptContext.onMouseEnter(enterRun);"
                                + "ScriptContext.onMouseExit(exitRun);"
                                + "ScriptContext.onApproach(approachRun);"
                                + "ScriptContext.onLeave(leaveRun);"
                                + "var name = cell.getClass().getName();"
                                + "print(name);";

    /**
     *  EXPERIMENTAL - THIS WILL PROBABLY BE REMOVED FROM THIS CLASS IN THE NEAR
     *  FUTURE!
     */

    /**
     * As of February 18th, 2010 this code no longer works as expected though it
     * can still be used as a rough guide for implementing physics in Wonderland.
     *
     * It WILL be used to construct a scripted physics framework in Wonderland in
     * later revisions of EZScript, for now it will just be hosted here.
     */
    private CollisionConfiguration collisionConfiguration;
    private CollisionDispatcher collisionDispatcher;
    private AxisSweep3 overlappingPairCache;
    private SequentialImpulseConstraintSolver solver;
    private DiscreteDynamicsWorld world;
    private ObjectArrayList collisionShapes;
    private Map<CollisionObject, Node> bodiesToNodes;
    private ZBufferState zbuf;
    private PhysicsProcessor proc;
    private Queue<RigidBody> potentialBodies;
    private Node root;
    private RigidBody avatarBody;
    private Node avatarRoot;
    private WlAvatarCharacter avatarCharacter;

    public void enablePhysics() {
        potentialBodies = new LinkedList<RigidBody>();
        collisionConfiguration = new DefaultCollisionConfiguration();
        collisionDispatcher = new CollisionDispatcher(collisionConfiguration);
        collisionShapes = new ObjectArrayList();
        Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
        Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
        int maxProxies = 1024;

        overlappingPairCache = new AxisSweep3(worldAabbMin, worldAabbMax, maxProxies);
        solver = new SequentialImpulseConstraintSolver();

        world = new DiscreteDynamicsWorld(collisionDispatcher,
                                          overlappingPairCache,
                                          solver,
                                          collisionConfiguration);
  
        world.setGravity(new Vector3f(0, -2, 0));
        bodiesToNodes = new HashMap<CollisionObject, Node>();
        //TESTING
        float[] args = { 50, 0, 50};
        addRigidBody("Ground",  args);
        args[0] = 0.5f;
        //addRigidBody("Sphere",args);

        //avatar stuff - also experimental
        AvatarImiJME avatarRenderer = (AvatarImiJME)ClientContextJME.getViewManager().getPrimaryViewCell().getCellRenderer(RendererType.RENDERER_JME);
         avatarCharacter = avatarRenderer.getAvatarCharacter();
        avatarRoot = avatarCharacter.getJScene().getExternalKidsRoot();

        CollisionShape avatarShape = new BoxShape(new Vector3f(1, 1, 0.25f));
        collisionShapes.add(avatarShape);
        Transform avatarTransform = new Transform();
        avatarTransform.setIdentity();
        com.jme.math.Vector3f avatarPosition = avatarRoot.getLocalTranslation();
        avatarTransform.origin.set(avatarPosition.x,
                                   avatarPosition.y,
                                   avatarPosition.z);
        float mass = 10.0f;
        Vector3f localInertia = new Vector3f(0, 0 ,0);
        avatarShape.calculateLocalInertia(mass, localInertia);
        DefaultMotionState avatarMotionState = new DefaultMotionState(avatarTransform);
        RigidBodyConstructionInfo rigidAvatarInfo
                = new RigidBodyConstructionInfo(mass,
                                                avatarMotionState,
                                                avatarShape,
                                                localInertia);

        avatarBody =  new RigidBody(rigidAvatarInfo);
        avatarBody.setRestitution(0);
        avatarBody.setFriction(0);

        world.addRigidBody(avatarBody);
        bodiesToNodes.put(avatarBody, avatarRoot);
        //avatarRoot.addGeometricUpdateListener(this);
    }

    public void geometricDataChanged(Spatial spatial) {
        com.jme.math.Vector3f trans = spatial.getWorldTranslation();
        com.jme.math.Quaternion rotation = spatial.getWorldRotation();
        synchronized(world) {
            Transform t = new Transform();
            t.setIdentity();
            t.origin.x = trans.x;
            t.origin.y = trans.y;
            t.origin.z = trans.z;
            t.setRotation(new Quat4f(rotation.x,
                                     rotation.y,
                                     rotation.z,
                                     rotation.w));//f2, f3));
            avatarBody.setWorldTransform(t);

            //avatarBody.setWorldTransform(t);

        }
    }
    public void jump() {
        synchronized(world) {
            System.out.println("JUMP!");
            Vector3f up = new Vector3f(0, 1, 0);
            up.normalize();
            up.scale(20);

            avatarBody.applyCentralImpulse(up);
        }
    }
    public void fireSphere(final float radius) {


        Entity e = proc.getEntity();
        synchronized(world) {
            float[] array = { radius };
            root.attachChild(addRigidBody("Sphere", array));
        }
    }

    public Node addRigidBody(String shape, float[] floats) {
        if(shape.equals("Sphere")) {
            //create the physics shape
            CollisionShape ballShape = new SphereShape(floats[0]);
            //create the material
            MaterialState matState = (MaterialState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
            matState.setDiffuse(ColorRGBA.blue);
            //create the JME node
            Node ballNode = new Node();
            //ballNode.attachChild(new Sphere("sphere", 10, 10, floats[0]));
            ballNode.setRenderState(matState);
            collisionShapes.add(ballShape);

            //create Transform
            Transform ballTransform = new Transform();
            ballTransform.setIdentity();

            com.jme.math.Vector3f lookDirection = new com.jme.math.Vector3f();
            ClientContextJME.getViewManager().getCameraLookDirection(lookDirection);
            CellTransform transform = ClientContextJME.getViewManager().getPrimaryViewCell().getLocalTransform();
            com.jme.math.Vector3f avatarVector = transform.getTranslation(null);
            ballTransform.origin.set(new Vector3f(avatarVector.x,
                                                  avatarVector.y,
                                                  avatarVector.z));
            ballNode.setLocalTranslation(avatarVector.x,
                                         avatarVector.y+2f,
                                         avatarVector.z);
            float mass = 10.0f;
            //boolean isDynamic = true;
            Vector3f localInertia = new Vector3f(0, 0, 0);
            ballShape.calculateLocalInertia(mass, localInertia);
            DefaultMotionState ballMotionState = new DefaultMotionState(ballTransform);
            RigidBodyConstructionInfo rigidBallInfo = new RigidBodyConstructionInfo(mass,
                                                                                    ballMotionState,
                                                                                    ballShape,
                                                                                    localInertia);
            Vector3f impulse = new Vector3f(lookDirection.x, lookDirection.y, lookDirection.z);
            Vector3f velocity = impulse;
            impulse.normalize();
            impulse.scale(3);
            velocity.scale(8);
            RigidBody ballBody = new RigidBody(rigidBallInfo);
            ballBody.setRestitution(1);


            ballBody.applyCentralImpulse(impulse);
            ballBody.setLinearVelocity(velocity);

                //world.addRigidBody(ballBody);
            potentialBodies.add(ballBody);
            bodiesToNodes.put(ballBody, ballNode);
            return ballNode;

        } else if(shape.equals("Ground")) {
            //create the physics shape
            CollisionShape groundShape = new BoxShape(new Vector3f(floats[0], floats[1], floats[2]));

            //create the material so we can see it in wonderland
            MaterialState matState = (MaterialState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
            matState.setDiffuse(new ColorRGBA(0, 1, 0, 1));
            //create the JME scenegraph node
            Node groundNode = new Node();
            groundNode.attachChild(new Quad("Floor", floats[0], floats[2]));
            groundNode.setRenderState(matState);

            //add the physics shape to the physics collection
            collisionShapes.add(groundShape);

            //create the transform for the rigid body
            Transform groundTransform = new Transform();
            groundTransform.setIdentity();
            groundTransform.origin.set(new Vector3f(0.f, -3.0f, 0.f));

            //apply the same transforms to the JME scene as to the physics scene
            groundNode.setLocalTranslation(0, -1.0f, 0);
            Quaternion pitch90 = new Quaternion();
            pitch90.fromAngleAxis(-1*FastMath.PI/2, new com.jme.math.Vector3f(1, 0, 0));
            groundNode.setLocalRotation(pitch90);

            //floor has a mass of 0 so it doesn't move during collisions.
            float mass = 0f;
            boolean isDynamic = (mass != 0f);

            Vector3f localInertia = new Vector3f(0, 0, 0);
            if (isDynamic) {
                    groundShape.calculateLocalInertia(mass, localInertia);
            }

            // using motionstate is recommended, it provides interpolation
            // capabilities, and only synchronizes 'active' objects
            // create rigid body for physics scene.
            DefaultMotionState myMotionState = new DefaultMotionState(groundTransform);

            RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(
                                                            mass,
                                                            myMotionState,
                                                            groundShape,
                                                            localInertia);
            RigidBody groundBody = new RigidBody(rbInfo);
            groundBody.setRestitution(0.5f);

            // add the body to the dynamics world
            
            groundBody.setFriction(0.1f);
            world.addRigidBody(groundBody);
            bodiesToNodes.put(groundBody, groundNode);

            return groundNode;
        } else if(shape.equals("Box")) {
            return new Node();

        } else {
            return new Node();
        }
    }

    public void startSimulation(float seconds) {
         root = new Node();
        zbuf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.ZBuffer);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        root.setRenderState(zbuf);
        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(root);
        Entity e = new Entity("jbullet");
        e.addComponent(RenderComponent.class, rc);
        rc.setSceneRoot(root);


        for(Node n : bodiesToNodes.values()) {
            root.attachChild(n);

        }

        proc = new PhysicsProcessor("Physics", seconds);
        e.addComponent(PhysicsProcessor.class, proc);
        ClientContextJME.getWorldManager().addEntity(e);
        //world.debugDrawWorld();

    }
    class PhysicsProcessor extends ProcessorComponent {

        private String name;
        private float seconds;
        private int frameIndex = 0;
        public PhysicsProcessor(String name, float seconds) {
            this.name = name;
            this.seconds = seconds;
            setArmingCondition(new NewFrameCondition(this));
        }
        public void idle() {
            setArmingCondition(new AwtEventCondition(this));

        }

        public void rearm() {
            setArmingCondition(new NewFrameCondition(this));
        }
        @Override
        public void compute(ProcessorArmingCollection pac) {
            if(frameIndex > (30*seconds)) {

                ClientContextJME.getWorldManager().removeEntity(this.getEntity());
                this.getEntity().removeComponent(PhysicsProcessor.class);
                //avatarRoot.removeGeometricUpdateListener(getLocalInstance());
            }

            while(!potentialBodies.isEmpty()) {
                world.addRigidBody(potentialBodies.remove());
                //potentialBodies.remove();
            }
            synchronized(world) {
                world.stepSimulation(1/30f, 10);
            }
            frameIndex +=1;
        }

        @Override
        public void commit(ProcessorArmingCollection pac) {

                for(CollisionObject obj : world.getCollisionObjectArray()) {
                    RigidBody body = RigidBody.upcast(obj);
                    if (body != null && body.getMotionState() != null) {
                            Transform trans = new Transform();

                            body.getMotionState().getWorldTransform(trans);

                            Node n = bodiesToNodes.get(obj);
                            if(n.equals(avatarRoot)) {
                                //System.out.println("Commit processing avatarRoot!");
                                CellTransform previous = ClientContextJME.getViewManager().getPrimaryViewCell().getWorldTransform();
                                CellTransform transform = new CellTransform();
                                com.jme.math.Vector3f translation;
                                Quaternion rotation;
                                translation = new com.jme.math.Vector3f(trans.origin.x,
                                                                        trans.origin.y,
                                                                        trans.origin.z);
                                rotation = new Quaternion(trans.getRotation(new Quat4f()).x,
                                                          trans.getRotation(new Quat4f()).y,
                                                          trans.getRotation(new Quat4f()).z,
                                                          trans.getRotation(new Quat4f()).w);

                                transform.setTranslation(translation);
                                transform.setRotation(rotation);
                                avatarCharacter.getModelInst().setTransform(new PTransform(previous.getRotation(null), transform.getTranslation(null), 1.0f));

                            }

                            n.setLocalTranslation(trans.origin.x, trans.origin.y, trans.origin.z);

                            ClientContextJME.getWorldManager().addToUpdateList(n);
                           /* System.out.printf("world pos = %f,%f,%f\n", trans.origin.x,
                                            trans.origin.y, trans.origin.z);*/
                    }
                }
        }

        @Override
        public void initialize() {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}



