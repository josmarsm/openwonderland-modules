/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.simplephysics;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.jme.math.Quaternion;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author JagWire
 */
public enum SimplePhysicsManager {
    INSTANCE;
    
    //physics configuration variables
    private CollisionConfiguration configuration;
    private CollisionDispatcher dispatcher;
    private AxisSweep3 overlappingPairCache;
    private SequentialImpulseConstraintSolver solver;
    private DiscreteDynamicsWorld world;
    private ObjectArrayList collisionShapes;
    private SimplePhysicsProcessor processor;
    
    //manager variables
    private Map<CollisionObject, SimpleRigidBodyComponent> bodies;
    private Vector3f gravity;
    private boolean running = false;
    
    public void initialize() {
        configuration = new DefaultCollisionConfiguration();
        dispatcher = new CollisionDispatcher(configuration);
        collisionShapes = new ObjectArrayList();
        
        Vector3f worldAabbMin = new Vector3f(-250, -250, -250);
        Vector3f worldAabbMax = new Vector3f(250, 250, 250);
        gravity = new Vector3f(0, -2, 0);
        int maxProxies = 1024;
        
        
        overlappingPairCache = new AxisSweep3(worldAabbMin,
                                              worldAabbMax,
                                              maxProxies);
        
        solver = new SequentialImpulseConstraintSolver();
        
        world = new DiscreteDynamicsWorld( dispatcher,
                                           overlappingPairCache,
                                           solver,
                                           configuration);
        world.setGravity(gravity);
        
        bodies = new HashMap<CollisionObject, SimpleRigidBodyComponent>();
        
        
        
    }
    
    public SimpleRigidBodyComponent createRigidBody(String bodyType) {
        return null;
    }
    
    public void addRigidBody(RigidBody body) {
        synchronized(world) {
            world.addRigidBody(body);
        }
    }
    
    public void addRigidBody(RigidBody body, SimpleRigidBodyComponent component) {
        bodies.put(body, component);
        addRigidBody(body);
    }
    
    public void applyForce(SimpleRigidBodyComponent body) {
        
    }
    
    public void applyImpulse(SimpleRigidBodyComponent body) {
        
    }
    

    
    public void start() {
        
    }
    
    public void stop() {
        
    }
    
    public boolean isRunning() {
        return running;
    }
    
    class SimplePhysicsProcessor extends ProcessorComponent {

        private String name;
        private float seconds;
        private int frameIndex = 0;
        
        public SimplePhysicsProcessor(String name, float seconds) {
            this.name = name;
            this.seconds = seconds;
            
            setArmingCondition(new NewFrameCondition(this));
            
        }
        
        
        
        @Override
        public void compute(ProcessorArmingCollection pac) {
            if(frameIndex > (30*seconds)) {
                ClientContextJME.getWorldManager().removeEntity(this.getEntity());
                this.getEntity().removeComponent(SimplePhysicsProcessor.class);
            }
            
            synchronized(world) {
                world.stepSimulation(1/30f, 10);
            }
        }

        @Override
        public void commit(ProcessorArmingCollection pac) {
            for(CollisionObject o: world.getCollisionObjectArray()) {
                RigidBody body = RigidBody.upcast(o);
                if(body != null && body.getMotionState() != null) {
                    Transform t = new Transform();
                    CellTransform cellTransform = bodies.get(o).getCellTransform();
                    body.getMotionState().getWorldTransform(t);
                    cellTransform.setTranslation(new com.jme.math.Vector3f(t.origin.x,
                                                                           t.origin.y,
                                                                           t.origin.z));
                    Quat4f rotation = t.getRotation(null);
                    cellTransform.setRotation(new Quaternion(rotation.x,
                                                             rotation.y,
                                                             rotation.z,
                                                             rotation.w));
                    
                    //for smooth animation, this applies the transform locally,
                    //but does not get propogated to other clients
                    bodies.get(o).getMovable().localMoveRequest(cellTransform, false); 
                }
                
            }
        }

        @Override
        public void initialize() {
            //
        }
        
    }

    
    
}
