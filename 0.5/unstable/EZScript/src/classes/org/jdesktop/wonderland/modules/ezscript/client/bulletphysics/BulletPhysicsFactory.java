/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.bulletphysics;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
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
import javax.vecmath.Vector3f;

/**
 *
 * @author JagWire
 */
public class BulletPhysicsFactory {
    
    

    
    public static DiscreteDynamicsWorld createPhysicsWorld(int minExtent, int maxExtent) {
                CollisionConfiguration configuration
                = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(configuration);
        Vector3f worldAabbMin = new Vector3f(-1*minExtent,
                                             -1*minExtent,
                                             -1*minExtent);
        
        Vector3f worldAabbMax = new Vector3f(1*maxExtent,
                                             1*maxExtent,
                                             1*maxExtent);
        
        int maxProxies = 1024;
        AxisSweep3 overlappingPairCache
                = new AxisSweep3(worldAabbMin, worldAabbMax, maxProxies);
        
        SequentialImpulseConstraintSolver solver
                = new SequentialImpulseConstraintSolver();
        
        return new DiscreteDynamicsWorld(dispatcher,
                                        overlappingPairCache,
                                        solver,
                                        configuration);
    }
    
    
    public static DiscreteDynamicsWorld createPhysicsWorld(int extent) {
        return createPhysicsWorld(extent, extent);
    }
    
    public static DiscreteDynamicsWorld createDefaultPhyiscsWorld() {
        return createPhysicsWorld(10000);                
    }
    
    public static CollisionShape createSphereShape(float radius) {
       return new SphereShape(radius);
    }
    
    public static CollisionShape createUnitSphere() {
        return createSphereShape(1);
    }
    
    public static CollisionShape createBoxShape(float length, float height, float depth) {
        return new BoxShape(new Vector3f(length/2f, height/2f, depth/2f));
    }
    
    public static CollisionShape createBoxShape(float extent) {
        return new BoxShape(new Vector3f(extent, extent, extent));
    }
    
    public static CollisionShape createUnitBox() {
        return createBoxShape(1, 1, 1);
    }
    
    public static RigidBody createRigidBody(float mass,
                                            Vector3f localInertia,
                                            CollisionShape shape,
                                            Transform transform,
                                            DefaultMotionState motionState) {
        
        //if the body is not static, we need to calculate this
        if(mass != 0f) {
            shape.calculateLocalInertia(mass, localInertia);
        }
        
        RigidBodyConstructionInfo info =
                new RigidBodyConstructionInfo(mass, motionState, shape, localInertia);
        return new RigidBody(info);
        
    }
    
    public static RigidBody createDefaultRigidBody(CollisionShape shape,
                                                   float mass,
                                                   Vector3f inertia,
                                                   Transform transform) {
        
       DefaultMotionState motionState = new DefaultMotionState(transform);
       
       return createRigidBody(mass, inertia, shape, transform, motionState);
       
    }
    
    public static RigidBody createRigidBody(float mass, CollisionShape shape) {
        Vector3f localInertia = new Vector3f(0f, 0f, 0f);
        
        //create the position, orientation, and size of the shape
        Transform shapeTransform = new Transform();               
                
        shapeTransform.setIdentity();
        shapeTransform.origin.set(new Vector3f(0f, 0f, 0f));
        
        DefaultMotionState motionState = new DefaultMotionState(shapeTransform);
        
        RigidBodyConstructionInfo info 
                = new RigidBodyConstructionInfo(mass,
                                                motionState,
                                                shape,
                                                localInertia);
        
        RigidBody body = new RigidBody(info);
        
        return body;
    }
    
    public static RigidBody createStaticRigidBody(CollisionShape shape) {
        return createRigidBody(0f, shape);
    }
    
    public static RigidBody createDefaultStaticRigidBody() {
        return createStaticRigidBody(createBoxShape(20, 0, 20));                       
    }
    
}
