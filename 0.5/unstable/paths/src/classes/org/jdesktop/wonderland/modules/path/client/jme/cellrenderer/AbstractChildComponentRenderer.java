package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.CullState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.CollisionSystem;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.PostEventCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ComponentChangeListener;
import org.jdesktop.wonderland.client.cell.InteractionComponent;
import org.jdesktop.wonderland.client.cell.InteractionComponent.InteractionComponentListener;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.path.common.Disposable;

/**
 * This Abstract base class is intended to be a cut down version of the BasicRenderer class for use in rendering
 * child components of a Cell which are not cells in their own right.
 *
 * @author Carl Jokl
 */
public abstract class AbstractChildComponentRenderer implements ChildRenderer {

    protected static final Logger logger = Logger.getLogger(AbstractChildComponentRenderer.class.getName());
    private static final ZBufferState zbuf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
    static {
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
    }

     /**
     * Apply the transform to the JME node.
     * @param node The node to which to apply the transform.
     * @param transform The transform to be applied.
     */
    public static void applyTransform(Spatial node, CellTransform transform) {
        node.setLocalRotation(transform.getRotation(null));
        node.setLocalScale(transform.getScaling(null));
        node.setLocalTranslation(transform.getTranslation(null));
    }

    protected Entity entity;    
    protected Node sceneRoot;
    protected ChildMoveProcessor moveProcessor;
    protected EventClassListener listener;

    private CellStatus status = CellStatus.DISK;
    private final Object entityLock = new Object();
    private boolean collisionEnabled = true;
    private boolean pickingEnabled = true;
    private boolean lightingEnabled = true;
    private boolean backfaceCullingEnabled = true;
    private CellRendererJME parentRenderer;
    private final ChildCollisionListener childCollisionListener;


    protected AbstractChildComponentRenderer() {
        childCollisionListener = new ChildCollisionListener(this, null);
    }

    protected AbstractChildComponentRenderer(CellRendererJME parentRenderer, CellRetriever containedCellRetriever) {
        this.parentRenderer = parentRenderer;
        childCollisionListener = new ChildCollisionListener(this, containedCellRetriever);
    }

    /**
     * Set the parent renderer for this ChildRenderer.
     *
     * @param parentRenderer The CellRenderer which is the parent of this ChildRenderer.
     */
    @Override
    public void setParentRenderer(CellRendererJME parentRenderer) {
        this.parentRenderer = parentRenderer;
    }

    /**
     * Set the CellRetriever used to find the Cell in which the child component rendered by this ChildRenderer is contained.
     *
     * @param containedCellRetriever The CellRetriever used to find the Cell which contains the child component rendered by this ChildRenderer.
     */
    @Override
    public void setCellRetriever(CellRetriever containedCellRetriever) {
        childCollisionListener.setCellRetriever(containedCellRetriever);
    }

    /**
     * Whether the Child component that owns this renderer is set.
     *
     * @return True if the child component that owns this renderer is set.
     */
    protected abstract boolean isOwnerSet();

    /**
     * Whether this Child component uses a listener to listen for events.
     *
     * @return True if this Child component should listen for events.
     */
    protected abstract boolean isListeningChild();

    /**
     * If this ChildRenderer listens for events i.e. isListeningChild() returns true then
     * this method is used to create the appropriate event listener for this child component.
     *
     * @return The EventClassListener used to listen for events which take place on the rendered component or null
     *         if not supported.
     */
    protected abstract EventClassListener createEventListener();

    /**
     * Get the Entity instance for the parent of this child component.
     *
     * @return The entity instance if available of the parent component of the component which owns this renderer.
     */
    protected Entity getParentEntity() {
        return parentRenderer != null ? parentRenderer.getEntity() : null;
    }

    /**
     * Get the name of the owner of this renderer.
     *
     * @return The name of the owner of this renderer.
     */
    protected abstract String getOwnerName();

    /**
     * Get the current containedCell status.
     *
     * @return The current containedCell status.
     */
    @Override
    public CellStatus getStatus() {
        return status;
    }

    /**
     * Set the CellStatus to which the containedCell has been changed.
     *
     * @param status The new CellStatus of the containedCell containing this child component.
     * @param increasing Whether the change of status is from a lower to a higher state.
     */
    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        this.status = status;
        switch(status) {
            case ACTIVE :
                if (isOwnerSet()) {
                    if (increasing) {
                        Entity parentEntity = getParentEntity();
                        Entity thisEntity = getEntity();
                        if (thisEntity == null) {
                            logger.severe(String.format("Got null entity for %s!", getClass().getName()));
                            return;
                        }
                        if (parentEntity != null) {
                            parentEntity.addEntity(thisEntity);
                        }
                        else {
                            ClientContextJME.getWorldManager().addEntity(thisEntity);
                        }

                        // Figure out the correct parent entity for this cells entity.
                        if (parentEntity !=null && thisEntity !=null) {
                            RenderComponent parentRendComp = (RenderComponent) parentEntity.getComponent(RenderComponent.class);
                            RenderComponent thisRendComp = (RenderComponent) thisEntity.getComponent(RenderComponent.class);
                            if (parentRendComp!=null && parentRendComp.getSceneRoot()!=null && thisRendComp!=null) {
                                thisRendComp.setAttachPoint(parentRendComp.getSceneRoot());
                            }
                        }
                        // enable the collision listener
                        else if (listener == null && isListeningChild()) {
                            listener = createEventListener();
                        }
                        if (listener != null) {
                            listener.addToEntity(thisEntity);
                        }
                    }
                }
                else {
                    logger.info("Owner not set for item during status change!");
                }
            break;
            case INACTIVE :
                if (!increasing) {
                    //disable collision listening
                    try {
                        Entity child = getEntity();
                        Entity parent = child.getParent();
                        if (listener != null) {
                            if (child != null) {
                                listener.removeFromEntity(child);
                            }
                            listener = null;
                        }
                        if (parent != null) {
                            parent.removeEntity(child);
                        }
                        else {
                            ClientContextJME.getWorldManager().removeEntity(child);
                        }
                        cleanupSceneGraph(child);
                    } 
                    catch(Exception e) {
                        logger.log(Level.SEVERE, "Error while transitioning child renderer into inactive state!", e);
                    }

                }
            break;
        }
    }

    /**
     * Convenience method which attaches the child entity to the specified
     * parent AND sets the attach point of the child's RenderComponent to the
     * scene root of the parents RenderComponent
     *
     * @param parentEntity
     * @param child
     */
    public static void entityAddChild(Entity parentEntity, Entity child) {
        if (parentEntity != null && child != null) {
            RenderComponent parentRendComp = (RenderComponent) parentEntity.getComponent(RenderComponent.class);
            RenderComponent thisRendComp = (RenderComponent) child.getComponent(RenderComponent.class);
            if (parentRendComp !=null && parentRendComp.getSceneRoot() !=null && thisRendComp !=null) {
                thisRendComp.setAttachPoint(parentRendComp.getSceneRoot());
            }
            parentEntity.addEntity(child);
        }
    }

    /**
     * Create a new entity to represent the child component.
     *
     * @return An entity used to represent the child component.
     */
    protected Entity createEntity() {
        Entity childEntity = new Entity(String.format("%s_%s", getClass().getName(), getOwnerName()));
        sceneRoot = createSceneGraph(childEntity);
        addRenderState(sceneRoot);
        addDefaultComponents(childEntity, sceneRoot);
        return childEntity;
    }

    /**
     * Return the scene root, this is the node created by createSceneGraph.
     * The BasicRenderer also has a rootNode which contains the containedCell transform,
     * the rootNode is the parent of the scene root.
     * @return The SceneRoot node for this renderer.
     */
    @Override
    public Node getSceneRoot() {
        return sceneRoot;
    }

    /**
     * Add the default render state to the root node. Override this method
     * if you want to apply a different RenderState
     * @param node
     */
    protected void addRenderState(Node node) {
        node.setRenderState(zbuf);
    }

    /**
     * Add the default components to the Entity to support such things as lighting and collision.
     *
     * @param entity The Entity which represents the child component.
     * @param rootNode The root most JME node for the screen graph of the child component.
     */
    protected void addDefaultComponents(Entity entity, Node rootNode) {
        //Movement?
        if (rootNode != null) {
            rootNode.updateWorldBound();

            // Some subclasses (like the imi collada renderer) already add
            // a render component
            RenderComponent renderComponent = entity.getComponent(RenderComponent.class);
            if (renderComponent == null) {
                renderComponent = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
                entity.addComponent(RenderComponent.class, renderComponent);
            }
            else {
                renderComponent.setSceneRoot(rootNode);
            }
            //Setup collision.
            CollisionComponent collisionComponent = setupCollision(getDefaultCollisionSystem(), rootNode);
            if (collisionComponent != null) {
                entity.addComponent(CollisionComponent.class, collisionComponent);
            }
            // set initial lighting
            adjustLighting(entity);

//            PhysicsSystem jBulletPhysicsSystem = ClientContextJME.getPhysicsSystem(session.getSessionManager(), "Physics");
//            CollisionSystem jBulletCollisionSystem = ClientContextJME.getCollisionSystem(session.getSessionManager(), "Physics");
//            if (jBulletPhysicsSystem!=null) {
//                CollisionComponent jBulletCollisionComponent = setupPhysicsCollision(jBulletCollisionSystem, rootNode);
//                PhysicsComponent pc = setupPhysics(jBulletCollisionComponent, jBulletPhysicsSystem, rootNode);
//                entity.addComponent(JBulletCollisionComponent.class, jBulletCollisionComponent);
//                entity.addComponent(JBulletPhysicsComponent.class, pc);
//            }
        }
        else {
            logger.warning("**** Child Renderer - ROOT NODE WAS NULL !");
        }

    }

    /**
     * Setup collision
     *
     * @param collisionSystem
     * @param rootNode
     * @return
     */
    protected CollisionComponent setupCollision(CollisionSystem collisionSystem, Node rootNode) {
        CollisionComponent collisionComponent = null;
        if (collisionSystem instanceof JMECollisionSystem) {
            collisionComponent = ((JMECollisionSystem) collisionSystem).createCollisionComponent(rootNode);
            collisionComponent.setCollidable(collisionEnabled);
            collisionComponent.setPickable(pickingEnabled);
        }
        else if (collisionSystem == null) {
            logger.warning("The supplied CollisionSystem: with which to setup collision was null!");
        }
        else {
            logger.warning(String.format("Unsupported CollisionSystem: %s.", collisionSystem));
        }
        return collisionComponent;
    }


    /**
     * Create the scene graph. The node returned will have  default
     * components set to handle collision and rendering. The returned graph will
     * also automatically be positioned correctly with the cells transform. This
     * is achieved by adding the returned Node to a rootNode for this renderer which
     * automatically tracks the cells transform.
     * @return
     */
    protected abstract Node createSceneGraph(Entity entity);

    /**
     * Cleanup the scene graph, allowing resources to be gc'ed
     * TODO - should be abstract, but don't want to break compatability in 0.5 API
     *
     * @param entity
     */
    protected void cleanupSceneGraph(Entity entity) {
        if (sceneRoot != null) {
            sceneRoot.removeFromParent();
            sceneRoot.detachAllChildren();
            sceneRoot.clearControllers();
            sceneRoot = null;
        }
    }   

    /**
     * Return the entity for this basic renderer. The first time this
     * method is called the entity will be created using createEntity()
     * @return The current entity used to represent the child component.
     */
    @Override
    public Entity getEntity() {
        synchronized(entityLock) {
            logger.fine(String.format("Get Entity %s.", getClass().getName()));
            if (entity == null) {
                entity = createEntity();
            }
        }
        return entity;
    }

    /**
     * @return the collisionEnabled
     */
    public boolean isCollisionEnabled() {
        return collisionEnabled;
    }


    /**
     * Callback notifying the renderer that the cell transform has changed.
     * @param localTransform the new local transform of the cell
     */
    @Override
    public void cellTransformUpdate(CellTransform localTransform) {
        // The fast-path case is if the move processor already exists, in
        // which case, we move the cell
        if (moveProcessor != null) {
            moveProcessor.cellMoved(localTransform);
        }
        else {
            // Otherwise, the move processor is null so we will attempt to add it
            // but only if there is a movable component on the cell.
            if (getMovableComponent() != null && sceneRoot != null) {
                moveProcessor = new ChildMoveProcessor(ClientContextJME.getWorldManager(), sceneRoot);
                getEntity().addComponent(ChildMoveProcessor.class, moveProcessor);
                moveProcessor.cellMoved(localTransform);
            }
        }
    }

    /**
     * Given a URL, determine and return the full asset URL. This is a
     * convenience method that invokes methods on AssetUtils using the session
     * associated with the cell for this cell renderer
     *
     * @param uri The asset URI
     * @return A URL representing the URI.
     * @throws MalformedURLException Upon error forming the URL
     */
    protected URL getAssetURL(String uri) throws MalformedURLException {
        CellRetriever cellRetriever = childCollisionListener.getCellRetriever();
        if (cellRetriever != null) {
            Cell containingCell = cellRetriever.getContainingCell();
            if (containingCell != null) {
                return AssetUtils.getAssetURL(uri, containingCell);
            }
        }
        return null;
    }


    /**
     * Set whether collision is enabled for this child component.
     *
     * @param collisionEnabled the collisionEnabled to set
     */
    @Override
    public void setCollisionEnabled(boolean collisionEnabled) {
        if (this.collisionEnabled != collisionEnabled) {
            synchronized(entityLock) {
                this.collisionEnabled = collisionEnabled;
                if (entity != null) {
                    adjustCollisionSystem();
                }
            }
        }
    }

    /**
     * Set whether collision is enabled for this child component.
     *
     * @param pickingEnabled True if picking should be enabled for the child component.
     */
    @Override
    public void setPickingEnabled(boolean pickingEnabled) {
        if (this.pickingEnabled != pickingEnabled) {
            synchronized(entityLock) {
                this.pickingEnabled = pickingEnabled;
                if (entity != null) {
                    adjustCollisionSystem();
                }
            }
        }
    }

    /**
     * Set whether lighting should be enabled for this child component.
     *
     * @param lightingEnabled True if lighting should be enabled for this child component.
     */
    public void setLightingEnabled(final boolean lightingEnabled) {
        if (this.lightingEnabled == lightingEnabled) {
            this.lightingEnabled = lightingEnabled;
            if (entity != null) {
                adjustLighting(entity);
            }
        }
    }

    /**
     * Set whether back face culling is enabled.
     *
     * @param backfaceCullingEnabled If true the back faces of the geometry triangles will be culled.
     */
    public void setBackfaceCullingEnabled(boolean backfaceCullingEnabled) {
        if (this.backfaceCullingEnabled != backfaceCullingEnabled) {
            this.backfaceCullingEnabled = backfaceCullingEnabled;
            if (entity != null) {
                final RenderComponent renderComponent = entity.getComponent(RenderComponent.class);
                final CullState.Face face = backfaceCullingEnabled ? CullState.Face.Back : CullState.Face.None;
                if (renderComponent != null && renderComponent.getSceneRoot() != null) {
                    ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
                        @Override
                        public void update(Object arg0) {
                            TreeScan.findNode(renderComponent.getSceneRoot(), new ProcessNodeInterface() {
                                @Override
                                public boolean processNode(Spatial node) {
                                    CullState cullState = (CullState) node.getRenderState(RenderState.StateType.Cull);
                                    if (cullState == null) {
                                        Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
                                        cullState = renderer.createCullState();
                                        node.setRenderState(cullState);
                                    }
                                    cullState.setCullFace(face);
                                    return true;
                                }
                            });
                            ClientContextJME.getWorldManager().addToUpdateList(renderComponent.getSceneRoot());
                        }
                    }, null);
                }
            }
        }
    }

    /**
     * Adjust the collision system after a change to picking or collision
     */
    private void adjustCollisionSystem() {
        CollisionComponent collisionComponent = entity.getComponent(CollisionComponent.class);
        if (!collisionEnabled && !pickingEnabled && collisionComponent != null) {
            entity.removeComponent(CollisionComponent.class);
        }
        if(collisionComponent == null) {
           collisionComponent = setupCollision(getDefaultCollisionSystem(), sceneRoot);
           entity.addComponent(CollisionComponent.class, collisionComponent);
        }
        else {
            collisionComponent.setCollidable(collisionEnabled);
            collisionComponent.setPickable(pickingEnabled);
        }
    }

    /**
     * Get the default collision system if none already exists.
     */
    private CollisionSystem getDefaultCollisionSystem() {
        CellRetriever cellRetriever = childCollisionListener.getCellRetriever();
        if (cellRetriever != null) {
            Cell containingCell = cellRetriever.getContainingCell();
            if (containingCell != null) {
                WonderlandSession session = containingCell.getCellCache().getSession();
                return ClientContextJME.getCollisionSystem(session.getSessionManager(), "Default");
            }
        }
        return null;
    }

    /**
     * Get the default collision system if none already exists.
     */
    private MovableComponent getMovableComponent() {
        CellRetriever cellRetriever = childCollisionListener.getCellRetriever();
        if (cellRetriever != null) {
            Cell containingCell = cellRetriever.getContainingCell();
            if (containingCell != null) {
                return containingCell.getComponent(MovableComponent.class);
            }
        }
        return null;
    }

    /**
     * Adjust the lighting for the child component Entity.
     *
     * @param entity The child component Entity for which to adjust lighting.
     */
    private void adjustLighting(final Entity entity) {
        RenderComponent renderComponent = entity.getComponent(RenderComponent.class);
        renderComponent.setLightingEnabled(lightingEnabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (sceneRoot != null) {
            sceneRoot.detachAllChildren();
            sceneRoot.removeFromParent();
            sceneRoot = null;
        }
        if (entity != null) {
            while (entity.numEntities() > 0) {
                entity.removeEntity(entity.getEntity(0));
            }
            if (listener != null) {
                listener.removeFromEntity(entity);
            }
            entity = null;
        }
        if (listener instanceof Disposable) {
            ((Disposable) listener).dispose();
        }
        listener = null;
    }

    /**
     * An mtgame ProcessorCompoenent to process cell moves.
     */
    protected static class ChildMoveProcessor extends ProcessorComponent {

        private CellTransform cellTransform;
        private boolean dirty = false;
        private Node node;
        private WorldManager worldManager;
        private Vector3f translationVector = new Vector3f();
        private Vector3f scallingVector = new Vector3f();
        private Quaternion rotationQuaternion = new Quaternion();

        private final long postId = ClientContextJME.getWorldManager().allocateEvent();

        private PostEventCondition postCondition = new PostEventCondition(this, new long[] {postId});

        public ChildMoveProcessor(WorldManager worldManager, Node node) {
            this.node = node;
            this.worldManager = worldManager;
        }

        @Override
        public void compute(ProcessorArmingCollection arg0) {

        }

        @Override
        public void commit(ProcessorArmingCollection arg0) {
            // The dirty flag is important for Avatars, as we chain
            // the moveProcessor to the avatarcontrol which update per frame
            // This needs breaking out at some point in the future

            synchronized(this) {
                if (dirty) {
                    // System.err.println("BasicRenderer.cellMoved "+node.getLocalTranslation()+"  "+cellTransform.getTranslation(null));
                    node.setLocalTranslation(cellTransform.getTranslation(translationVector));
                    node.setLocalRotation(cellTransform.getRotation(rotationQuaternion));
                    node.setLocalScale(cellTransform.getScaling(scallingVector));
                    dirty = false;
                    worldManager.addToUpdateList(node);
                    // System.err.println("--------------------------------");
                }
            }
            // Clear the triggering events
            if (arg0.size() != 0) {
               PostEventCondition pec = (PostEventCondition)arg0.get(0);
               pec.getTriggerEvents();
            }
        }

        @Override
        public void initialize() {
            setArmingCondition(postCondition);
        }

        /**
         * Notify the MoveProcessor that the cell has moved
         *
         * @param transform cell transform in world coordinates
         */
        public void cellMoved(CellTransform transform) {
            synchronized(this) {
                this.cellTransform = transform;
                dirty = true;
//                System.err.println("CellMoved "+postId);
                ClientContextJME.getWorldManager().postEvent(postId);
            }
        }

        @Override
        protected void finalize() throws Throwable {
            ClientContextJME.getWorldManager().freeEvent(postId);
            super.finalize();
        }
    }

    private static class ChildCollisionListener implements ComponentChangeListener, InteractionComponentListener
    {
        private InteractionComponent interactionComponent;
        private ChildRenderer parent;
        private CellRetriever containigCellRetriever;

        /**
         * Create a new ChildCollisionListener with the specified parent.
         *
         * @param parent The ChildRenderer which is the parent of this ChildCollisionListener.
         * @param containingCellRetriever The object used to get hold of the cell which contains this child component.
         */
        public ChildCollisionListener(ChildRenderer parent, CellRetriever containingCellRetriever) {
            this.parent = parent;
            this.containigCellRetriever = containingCellRetriever;
        }

        /**
         * Set the CellRetriever used to find the Cell in which this child component resides.
         *
         * @param containingCellRetriever The CellRetriever to find the Cell in which this child component resides.
         */
        public void setCellRetriever(CellRetriever containingCellRetriever) {
            this.containigCellRetriever = containingCellRetriever;
        }

        /**
         * Get the CellRetriever used to find the Cell which contains the child component to be rendered.
         *
         * @return The CellRetriever (if set) used to get the Cell which contains this CellRetriever.
         */
        public CellRetriever getCellRetriever() {
            return containigCellRetriever;
        }

        /**
         * Enable the ChildCollisionListener.
         */
        public void enable() {
            logger.warning("Enabling collision listener!");
            Cell containedCell = containigCellRetriever != null ? containigCellRetriever.getContainingCell() : null;
            if (containedCell != null) {
                logger.warning("Contained cell is set while enabling collision listener!");
                containedCell.addComponentChangeListener(this);
                setInteractionComponent(containedCell.getComponent(InteractionComponent.class));
            }
        }

        /**
         * Disable the ChildCollisionListener.
         */
        public void disable() {
            logger.warning("Disabling collision listener!");
            Cell containedCell = containigCellRetriever != null ? containigCellRetriever.getContainingCell() : null;
            if (containedCell != null) {
                logger.warning("Contained cell is set while disabling collision listener!");
                containedCell.removeComponentChangeListener(this);
                setInteractionComponent(null);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void componentChanged(Cell cell, ChangeType type, CellComponent component) {
            logger.warning(String.format("Collision listener component changed: %s, change type: %s!", component.toString(), type.name()));
            if (component instanceof InteractionComponent) {
                switch (type) {
                    case ADDED:
                        setInteractionComponent((InteractionComponent) component);
                        break;
                    case REMOVED:
                        setInteractionComponent(null);
                        break;
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void collidableChanged(boolean collidable) {
            logger.warning(String.format("Collision listener collidable changed to: %s!", Boolean.toString(collidable)));
            parent.setCollisionEnabled(collidable);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void selectableChanged(boolean selectable) {
            // ignore
        }

        /**
         * Set the InteractionComponent of this CollisionListener.
         *
         * @param interactionComponent
         */
        private void setInteractionComponent(InteractionComponent interactionComponent) {
            if (this.interactionComponent != null) {
                this.interactionComponent.removeInteractionComponentListener(this);
            }
            this.interactionComponent = interactionComponent;
            if (interactionComponent != null) {
                logger.warning(String.format("Collision listener, interaction component set (non null) to: %s!", interactionComponent));
                interactionComponent.addInteractionComponentListener(this);
                parent.setCollisionEnabled(interactionComponent.isCollidable());
            }
            else {
                // if there is no interaction component, set collision to
                // the default, true
                logger.warning("Collision listener, interaction component set to null!");
                parent.setCollisionEnabled(true);
            }
        }

    }
}
