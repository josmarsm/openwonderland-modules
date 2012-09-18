/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.bulletphysics;

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
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseMovedEvent3D;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
import org.jdesktop.wonderland.modules.ezscript.client.EZScriptComponentFactory;
import org.jdesktop.wonderland.modules.ezscript.client.ScriptManager;
import org.jdesktop.wonderland.modules.ezscript.client.cell.CommonCellFactory;
import org.jdesktop.wonderland.modules.ezscript.client.methods.CreateEmptyCellMethod;
import org.jdesktop.wonderland.modules.ezscript.common.cell.CommonCellServerState;

/**
 *
 * @author JagWire
 */
public enum SimplePhysicsManager implements MouseMovedListener {
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
    private Entity processorEntity = null;
    private PhysicsControlPanel controlPanel = null;
    private HUDComponent hudComponent = null;
    private static final Logger logger = Logger.getLogger(SimplePhysicsManager.class.getName());
    private IndependentBoundsViewerEntity boundsView;
    
    //HAX
    private Semaphore lock = new Semaphore(0);
    private com.jme.math.Vector3f mousePosition;
    
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
    
    public BoundingVolumeHint createBounds(String type)  {
//        if(bodies == null || bodies.isEmpty()) {
//            type = "PLANE";
//        }
        
        if(type.toUpperCase().equals("BOX") 
            || type.toUpperCase().equals("PLANE")) {
            BoundingBox box = new BoundingBox(new com.jme.math.Vector3f(0,0.5f,0),
                                              0.5f,
                                              0.5f,
                                              0.5f);
            
            if(type.toUpperCase().equals("PLANE")) {
                box.xExtent = 250.0f;
                box.yExtent = 0.01f;
                box.zExtent = 250.0f;
                box.setCenter(new com.jme.math.Vector3f(0, 0.01f, 0));
            }
            
            boundsView = new IndependentBoundsViewerEntity();
            boundsView.showBounds(box);
            MousePickEventListener l = new MousePickEventListener();
            l.addListener(this);
            
            InputManager.inputManager().addGlobalEventListener(l);
            
            /** BLOCK */
            l.acquireLock();
            InputManager.inputManager().removeGlobalEventListener(l);
            return new BoundingVolumeHint(false, box);
            
            
        } else if(type.toUpperCase().equals("SPHERE")) {
            BoundingSphere sphere = new BoundingSphere(1, new com.jme.math.Vector3f(0,1,0));
            boundsView = new IndependentBoundsViewerEntity();
            boundsView.showBounds(sphere);
            MousePickEventListener l = new MousePickEventListener();
            l.addListener(this);
            
            InputManager.inputManager().addGlobalEventListener(l);
            
            /** BLOCK */
            l.acquireLock();
            InputManager.inputManager().removeGlobalEventListener(l);
            return new BoundingVolumeHint(false, sphere);
        }
        return null;
    }
    
    public SimpleRigidBodyComponent createRigidBody(final String bodyType, boolean notused) {
        if(bodies == null) {
            initialize();
        }
        
        //Let the user create the bounds
        logger.warning("Creating bounds hint...");
        BoundingVolumeHint hint = createBounds(bodyType);
        String name = "RigidBody-"+bodies.size();
        CommonCellFactory factory = new CommonCellFactory();
        
        CommonCellServerState state = factory.getDefaultCellServerState(null);
        state.setBoundingVolumeHint(hint);
        state.setName(name);
        
        //
        
        return null;
    }
    
    /**
     * Create a rigid body given an explicit body type: BOX or SPHERE
     * @param bodyType "BOX" or "SPHERE"
     * 
     * @return The component and cell attached.
     */
    public SimpleRigidBodyComponent createRigidBody(final String bodyType) {

        if(bodies == null) {
            initialize();
        }
        
        String name = "RigidBody-"+bodies.size();
        CommonCellFactory factory = new CommonCellFactory();
        CommonCellServerState state = factory.getDefaultCellServerState(null);
                
        EZScriptComponentFactory ezFactory = new EZScriptComponentFactory();
        SimpleRigidBodyFactory rbFactory = new SimpleRigidBodyFactory();
        state.setName(name);
        
        //attach EZScript to the common cell
        state.addComponentServerState(ezFactory.getDefaultCellComponentServerState());
        
        //attach SimpleRigidBodyComponent to the common cell
        state.addComponentServerState(rbFactory.getDefaultCellComponentServerState());

        try {
            // literally create the cell
            logger.warning("waiting on cell creation...");
            CellUtils.createCell(state);            
            // wait until we can access the cell 
            
            while(ScriptManager.getInstance().getCellID(name) == null) { 
                Thread.currentThread().wait(500);
            }
                //spin wheels
            logger.warning("...cell creation finished.");
            //aquire the CellID in order to grab the cell.
            CellID id = ScriptManager.getInstance().getCellID(name);
            
            //acquire the WonderlandSession in order to get the cell cache.
            WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
            
            //use the CellID and WonderlandSession to get our cell.
            Cell cell = ClientContextJME.getCellCache(session).getCell(id);
            
            //get the client-side SimpleRigidBodyComponent from the cell
            SimpleRigidBodyComponent srbc = cell.getComponent(SimpleRigidBodyComponent.class);
            
            if(srbc == null) {
                logger.warning("UNABLE TO CREATE RIGID BODY!");
                return null;
            }
            //create listener to wait for cell to get past ACTIVE so we can
            //set our shape type effectively.            
            CellStatusChangeListener l = new CellStatusChangeListener() { 
                public void cellStatusChanged(Cell cell, CellStatus status) {
                    if(status == status.RENDERING) {
                        //return from listener and allow us to remove it. 

                        try {
                            //wait just a bit so our component has time to 
                            //catch up.
                            Thread.currentThread().wait(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(SimplePhysicsManager.class.getName()).log(Level.SEVERE, null, ex);
                        } finally {
                            lock.release();
                            logger.warning("Lock released!");   
                        }
                    }
                }
            };
            
            //apply the listener
            cell.addStatusChangeListener(l);
            
            //wait for lock to be released, signaling cell has reached the right
            //status.
            lock.acquire();
            
            //set the shape type, either a "BOX" or "SPHERE";
            srbc.setShapeType(bodyType);

            //add our rigid body to the physics world.
            addRigidBody(srbc.getRigidBody(), srbc);
            logger.log(Level.WARNING, "{0} CREATED!", name);
            
            return srbc;
            
        } catch (CellCreationException ex) {
            Logger.getLogger(CreateEmptyCellMethod.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch(InterruptedException ie) {
          logger.warning(ie.getMessage());
            return null;
        }               
    }
    /**
     * *Internal* add the body object to the jbullet world structure.
     * @param body 
     */
    private void addRigidBody(RigidBody body) {
        synchronized(world) {
            world.addRigidBody(body);
        }
    }
    
    /**
     * Associate the body with the component and add the body to the jbullet 
     * world structure
     * 
     * @param body
     * @param component 
     */
    public void addRigidBody(RigidBody body, SimpleRigidBodyComponent component) {
        bodies.put(body, component);
        addRigidBody(body);
    }
    
    /**
     * Apply a constant, central force in the direction and with the magnitude of the 
     * force argument to the rigidbody associated with the component argument.
     */
    public void applyForce(SimpleRigidBodyComponent component, Vector3f force) {
        component.getRigidBody().applyCentralForce(force);
        
    }
    
    /**
     * Apply a central impulse force in the direction and with the magnitude of
     * the force argument to the rigidbody associated with the component argument.
     * 
     * @param component
     * @param impulse 
     */
    public void applyImpulse(SimpleRigidBodyComponent component, Vector3f impulse) {
        component.getRigidBody().applyCentralImpulse(impulse);
    }
    
    /**
     * Start the jbullet engine.
     */
    public void start() {
        if(running)
            return;        
      
        processorEntity = new Entity();
        processorEntity.addComponent(SimplePhysicsProcessor.class, new SimplePhysicsProcessor("physics", 300));
        
        running = true;
        ClientContextJME.getWorldManager().addEntity(processorEntity);
        logger.warning("starting physics engine!");
    }
    
    /**
     * Stop the jbullet engine.
     */
    public void stop() {
        if(!running)
            return;
        
        running = false;
        processorEntity.removeComponent(SimplePhysicsProcessor.class);
        ClientContextJME.getWorldManager().removeEntity(processorEntity);
        logger.warning("stopping physics engine!");
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public void showControlPanel() {
        //if the panel hasn't been created yet, create it.
        if (controlPanel == null) {
            controlPanel = new PhysicsControlPanel();
            SwingUtilities.invokeLater(new Runnable() {
                //create the hud component also, add it to the HUD
                public void run() {
                    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                    hudComponent = mainHUD.createComponent(controlPanel);

                    hudComponent.setDecoratable(true);
                    hudComponent.setPreferredLocation(Layout.NORTH);
                    mainHUD.addComponent(hudComponent);
                    controlPanel.setHudComponent(hudComponent);
                    hudComponent.setVisible(true);
                }
            });
        }
        //if the panel already exists, skip creating it and show it on the HUD.
        SwingUtilities.invokeLater(new Runnable() { 
            public void run() {
                hudComponent.setVisible(true);
            }
        });
                
    }
    
    public boolean bodiesExist() {
        if(bodies == null) {
            return false;
        }
        
        return bodies.isEmpty();
    }
    
    public void mouseMoved(com.jme.math.Vector3f position) {
        if(boundsView == null)
            return;
        
        Quaternion q = boundsView.getRootNode().getLocalRotation();
        boundsView.updateTransform(position, q);
    }
    
    class MousePickEventListener extends EventClassListener {
        private Semaphore lock = new Semaphore(0);
        private com.jme.math.Vector3f position = null;
        private final List<MouseMovedListener> listeners = new ArrayList<MouseMovedListener>();
        private boolean moved = false;
        /**
         * 
         * @param lock should always have 0 contracts when it's passed.
         */


        
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseMovedEvent3D.class, MouseButtonEvent3D.class };                        
        }
        
        @Override
        public void commitEvent(Event event) {
            
            if (event instanceof MouseMovedEvent3D) {
                MouseMovedEvent3D me = (MouseMovedEvent3D) event;
                if (me.getPickDetails() == null) {
                    logger.warning("No pick details!");
                    return;
                }
                logger.warning("Fire MouseMovedEvent!");
                fireMouseMovedEvent(me.getPickDetails().getPosition());
                moved = true;
                
            } else if(event instanceof MouseButtonEvent3D) {
                if(!moved)
                    return;
                
                MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
                if(mbe.isClicked()) {
                    lock.release();                    
                    return;
                }
            }          
        }
        
        /**
         * Use to block thread until user clicks mouse.
         */
        public void acquireLock() {
            try {
                lock.acquire();
                logger.warning("Lock acquired!");
            } catch (InterruptedException ex) {
                Logger.getLogger(SimplePhysicsManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        public void addListener(MouseMovedListener listener) {
            synchronized(listeners) {
                listeners.add(listener);
            }
        }
        
        public void removeListener(MouseMovedListener listener) {
            synchronized(listeners) {
                listeners.remove(listener);
            }
        }
        
        public void fireMouseMovedEvent(com.jme.math.Vector3f position) {
            synchronized(listeners) {
                for(MouseMovedListener listener: listeners) {
                    listener.mouseMoved(position);
                }
            }
        }
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
