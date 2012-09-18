package org.jdesktop.wonderland.modules.ezscript.client.methods;

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
//import com.jme.math.Vector3f;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.renderer.ColorRGBA;

import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.ZBufferState;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Vector3f;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class JBulletDemoMethod implements ScriptMethodSPI {

    private CollisionConfiguration collisionConfiguration;
    private CollisionDispatcher collisionDispatcher;
    private AxisSweep3 overlappingPairCache;
    private SequentialImpulseConstraintSolver solver;
    private DiscreteDynamicsWorld world;
    private ObjectArrayList collisionShapes;
    private Map<CollisionObject, Node> bodiesToNodes;
    private Cell cell;
    private float seconds;
    private ZBufferState zbuf;
    public String getFunctionName() {
        return "jbulletdemo";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        seconds = ((Double)args[1]).floatValue();

        zbuf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

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

        world.setGravity(new Vector3f(0, -10, 0));

        bodiesToNodes = new HashMap<CollisionObject, Node>();



        CollisionShape groundShape = new BoxShape(new Vector3f(50, 0, 50));
        //
        MaterialState matState = (MaterialState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
        matState.setDiffuse(new ColorRGBA(0, 1, 0, 1));
        //
        Node groundNode = new Node();
        groundNode.attachChild(new Quad("Floor", 100, 100));
        groundNode.setRenderState(matState);
        
        collisionShapes.add(groundShape);
        //bodiessToNodes.put(groundShape, groundNode);

        Transform groundTransform = new Transform();

        groundTransform.setIdentity();

        groundTransform.origin.set(new Vector3f(0.f, -5.0f, 0.f));
        groundNode.setLocalTranslation(0, -5.0f, 0);
        Quaternion pitch90 = new Quaternion();
        pitch90.fromAngleAxis(-1*FastMath.PI/2, new com.jme.math.Vector3f(1, 0, 0));
        groundNode.setLocalRotation(pitch90);
        float mass = 0f;

        // rigidbody is dynamic if and only if mass is non zero,
        // otherwise static
        boolean isDynamic = (mass != 0f);

        Vector3f localInertia = new Vector3f(0, 0, 0);
        if (isDynamic) {
                groundShape.calculateLocalInertia(mass, localInertia);
        }

        // using motionstate is recommended, it provides interpolation
        // capabilities, and only synchronizes 'active' objects
        DefaultMotionState myMotionState = new DefaultMotionState(groundTransform);

        RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(
                                                        mass,
                                                        myMotionState,
                                                        groundShape,
                                                        localInertia);
        RigidBody groundBody = new RigidBody(rbInfo);

        // add the body to the dynamics world
        world.addRigidBody(groundBody);
        bodiesToNodes.put(groundBody, groundNode);


        MaterialState sphereState = (MaterialState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(StateType.Material);
        sphereState.setDiffuse(new ColorRGBA(1, 0, 0, 1));

        CollisionShape sphereShape = new SphereShape(3);
        Node sphereNode = new Node();
        sphereNode.attachChild(new Sphere("sphere",10, 10, 3));
        sphereNode.setRenderState(sphereState);
        collisionShapes.add(sphereShape);

        //bodiesToNodes.put(sphereShape, sphereNode);
        // Create Dynamic Objects
        Transform startTransform = new Transform();
        startTransform.setIdentity();

        float mass1 = 20f;

        // rigidbody is dynamic if and only if mass is non zero,
        // otherwise static
        boolean isDynamic1 = (mass1 != 0f);

        Vector3f localInertia1 = new Vector3f(0, 0, 0);
        if (isDynamic1) {
            sphereShape.calculateLocalInertia(mass1, localInertia1);
        }

        startTransform.origin.set(new Vector3f(0, 50, 0));
        sphereNode.setLocalTranslation(0, 50, 0);
        //sphereNode.setLocalRotation(pitch90);

        // using motionstate is recommended, it provides
        // interpolation capabilities, and only synchronizes
        // 'active' objects
        DefaultMotionState myMotionState1 = new DefaultMotionState(startTransform);

        RigidBodyConstructionInfo rbInfo1 = new RigidBodyConstructionInfo(
                                                            mass1,
                                                            myMotionState1,
                                                            sphereShape,
                                                            localInertia1);
        RigidBody sphereBody = new RigidBody(rbInfo1);
        //sphereBody.setActivationState(RigidBody.ISLAND_SLEEPING);
        world.addRigidBody(sphereBody);
        bodiesToNodes.put(sphereBody, sphereNode);
        sphereBody.setRestitution(1);
        groundBody.setRestitution(0.5f);
        
        //Vector3f impulse = new Vector3f(0, -4.9f, 0);
        //mpulse.scale(5);
        
        //sphereBody.setActivationState(CollisionObject.ISLAND_SLEEPING);
        //sphereBody.applyCentralImpulse(impulse);
        //sphereBody.applyCentralForce(impulse);
	
    }

    public void run() {
        BasicRenderer r = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        //Entity e = r.getEntity();

        Node root = new Node();
        root.setRenderState(zbuf);
        RenderComponent rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(root);
        Entity e = new Entity("jbullet");
        e.addComponent(RenderComponent.class, rc);
        rc.setSceneRoot(root);


        for(Node n : bodiesToNodes.values()) {
            root.attachChild(n);
        }
        e.addComponent(JBulletProcessor.class, new JBulletProcessor("JBullet"));
        ClientContextJME.getWorldManager().addEntity(e);

    }

    public String getDescription() {
        return "usage: jbulletdemo()";
    }

    public String getCategory() {
        return "simulation";
    }

    class JBulletProcessor extends ProcessorComponent {

        /**
         * A name
         */
        private String name = null;
        /**
         * The constructor
         */
        int frameIndex = 0;

        public JBulletProcessor(String name) {
            this.name = name;
            setArmingCondition(new NewFrameCondition(this));
        }

        @Override
        public String toString() {
            return (name);
        }

        /**
         * The initialize method
         */
        public void initialize() {
            //setArmingCondition(new NewFrameCondition(this));
        }

        /**
         * The Calculate method
         */
        public void compute(ProcessorArmingCollection collection) {
            if (frameIndex > (30 * seconds)) {

                ClientContextJME.getWorldManager().removeEntity(this.getEntity());
                this.getEntity().removeComponent(JBulletProcessor.class);
            }
            world.stepSimulation(1 / 60f, 10);

            frameIndex += 1;

        }

        /**
         * The commit method
         */
        public void commit(ProcessorArmingCollection collection) {

            for (CollisionObject obj : world.getCollisionObjectArray()) {
                RigidBody body = RigidBody.upcast(obj);
                if (body != null && body.getMotionState() != null) {
                    Transform trans = new Transform();
                    body.getMotionState().getWorldTransform(trans);
                    Node n = bodiesToNodes.get(obj);
                    n.setLocalTranslation(trans.origin.x, trans.origin.y, trans.origin.z);

                    ClientContextJME.getWorldManager().addToUpdateList(n);
                    /*
                     * System.out.printf("world pos = %f,%f,%f\n",
                     * trans.origin.x, trans.origin.y, trans.origin.z);
                     */
                }
            }
        }
    }

}
