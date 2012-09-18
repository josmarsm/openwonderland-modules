/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.bulletphysics;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Quaternion;
import java.util.logging.Logger;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.cell.TransformChangeListener.ChangeSource;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.ezscript.client.cell.AnotherMovableComponent;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedFloat;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

/**
 *
 * @author JagWire
 */
public class SimpleRigidBodyComponent extends CellComponent {
    
    //physics stuph
    private String shapeType = "BOX";
    private CollisionShape shape = null;
    private Transform shapeTransform = null;
    private float mass = 10.0f;
    private Vector3f inertia = new Vector3f(0, 0, 0);
    private DefaultMotionState motionState = null;
    private RigidBodyConstructionInfo info = null;
    private RigidBody rigidBody = null;
    
    //shared-state component stuph
    @UsesCellComponent
    private SharedStateComponent stateComponent;
    private SharedMapCli stateMap = null;
    
    
    //AnotherMovableComponent stuph
    @UsesCellComponent
    private AnotherMovableComponent movableComponent;
    
    
    //HAX
    private boolean ready = false;
    private static final Logger logger = Logger.getLogger(SimpleRigidBodyComponent.class.getName());
    public SimpleRigidBodyComponent(Cell cell) {
        super(cell);
       
        
        initialize();
        
    }
    
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        
        switch (status) {
            case ACTIVE:
                if (increasing) {
                    new Thread(new Runnable() {

                        public void run() {                            
                            logger.warning("before map acquisition.");
                            stateMap = stateComponent.get("physics-state");
                            logger.warning("after map acquisition.");
                            handleStates();
                            ready = true;
                        }
                    }).start();

                    cell.addTransformChangeListener(new TransformChangeListener() {

                        public void transformChanged(Cell cell, ChangeSource source) {
                            synchronized(rigidBody) {
                                shapeTransform.origin.x = cell.getLocalTransform().getTranslation(null).x;
                                shapeTransform.origin.y = cell.getLocalTransform().getTranslation(null).y;
                                shapeTransform.origin.z = cell.getLocalTransform().getTranslation(null).z;
                                
                                Quaternion q = cell.getLocalTransform().getRotation(null);
                                shapeTransform.setRotation(new Quat4f(q.toAngles(null)));
                                
                            }
                        }
                    });
                }
                break;

            case DISK:
                
                break;
        }
    }
    
    public void handleStates() {
        if(stateMap.containsKey("type")) {
            shapeType = ((SharedString)stateMap.get("type")).getValue();
        }
        
        if(stateMap.containsKey("mass")) {
            mass = ((SharedFloat)stateMap.get("mass")).getValue();
        }
    }
    
    private void initialize() {
        
        //first initialize collision object
        float[] extents = new float[3];
        if(cell.getLocalBounds() instanceof BoundingBox) {
            extents[0] = ((BoundingBox)cell.getLocalBounds()).xExtent/2f;
            extents[1] = ((BoundingBox)cell.getLocalBounds()).yExtent/2f;
            extents[2] = ((BoundingBox)cell.getLocalBounds()).zExtent/2f;
           
        } else if(cell.getLocalBounds() instanceof BoundingSphere) {
            extents[0] = ((BoundingSphere)cell.getLocalBounds()).radius;
        } else {
            logger.warning("Bounds not available!");
            extents = new float[] { 1, 1, 1 };
        }
        
        if(shapeType.toUpperCase().equals("BOX")) {
            shape = new BoxShape(new Vector3f(extents));
        } else if(shapeType.toUpperCase().equals("SPHERE")) {
            shape = new SphereShape(extents[0]);
        } else {
            logger.warning("Shape type unsupported, defaulting to unit cube.");
            shape = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
        }
        
        //next initialize the shape's transform
        CellTransform cellTransform = cell.getLocalTransform();
        shapeTransform = new Transform();
        shapeTransform.setIdentity();
        shapeTransform.origin.set(cellTransform.getTranslation(null).x,
                                  cellTransform.getTranslation(null).y,
                                  cellTransform.getTranslation(null).z);//mass, mass);
        
        //next let's calculate inertia
        shape.calculateLocalInertia(mass, inertia);
        
        //intialize motion state and config info
        motionState = new DefaultMotionState(shapeTransform);
        info = new RigidBodyConstructionInfo(mass,
                                             motionState,
                                             shape,
                                             inertia);
        
        //finally, let's create the rigid body
        rigidBody = new RigidBody(info);
    }
    
    public CellTransform getCellTransform() {
        return cell.getLocalTransform();
    }
    
    public AnotherMovableComponent getMovable() {
        return movableComponent;
    }
    
    //netbeans generated
    //<editor-fold defaultstate="collapsed" desc="getters and setters">
    public Vector3f getInertia() {
        return inertia;
    }
    
    public void setInertia(Vector3f inertia) {
        this.inertia = inertia;
    }
    
    public RigidBodyConstructionInfo getInfo() {
        return info;
    }
    
    public void setInfo(RigidBodyConstructionInfo info) {
        this.info = info;
    }
    
    public float getMass() {
        return mass;
    }
    
    public void setMass(float mass) {
        this.mass = mass;
        stateMap.put("mass", SharedFloat.valueOf(mass));
    }
    
    public DefaultMotionState getMotionState() {
        return motionState;
    }
    
    public void setMotionState(DefaultMotionState motionState) {
        this.motionState = motionState;
    }
    
    public RigidBody getRigidBody() {
        return rigidBody;
    }
    
    public void setRigidBody(RigidBody rigidBody) {
        this.rigidBody = rigidBody;
    }
    
    public CollisionShape getShape() {
        return shape;
    }
    
    public void setShape(CollisionShape shape) {
        this.shape = shape;
    }
    
    public Transform getShapeTransform() {
        return shapeTransform;
    }
    
    public void setShapeTransform(Transform shapeTransform) {
        this.shapeTransform = shapeTransform;
    }
    
    public String getShapeType() {
        return shapeType;
    }
    
    public void setShapeType(String shapeType) {
        this.shapeType = shapeType;
        
        stateMap.put("type", SharedString.valueOf(shapeType));
    }
    
    //</editor-fold>

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
    
}
