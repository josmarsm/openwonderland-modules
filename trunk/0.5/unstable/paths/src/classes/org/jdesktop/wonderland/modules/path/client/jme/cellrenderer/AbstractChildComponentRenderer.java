package org.jdesktop.wonderland.modules.path.client.jme.cellrenderer;

import com.jme.scene.Node;
import java.util.logging.Logger;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.CollisionSystem;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.EntityComponent;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.CellRefComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.path.common.Disposable;

/**
 * This Abstract base class is intended to be a cut down version of the BasicRenderer class for use in rendering
 * child components of a Cell which are not cells in their own right.
 *
 * @author Carl Jokl
 */
public abstract class AbstractChildComponentRenderer implements ChildRenderer {

    protected static final Logger logger = Logger.getLogger(AbstractChildComponentRenderer.class.getName());

    protected Entity entity;    
    protected Node sceneRoot;
    protected CellRetriever containingCellRetriever;
    protected EventClassListener listener;

    private final Object entityLock = new Object();
    private boolean collisionEnabled = true;
    private boolean pickingEnabled = true;
    private CellRendererJME parentRenderer;


    protected AbstractChildComponentRenderer() {
       
    }

    protected AbstractChildComponentRenderer(CellRendererJME parentRenderer, CellRetriever containingCellRetriever) {
        this.parentRenderer = parentRenderer;
        this.containingCellRetriever = containingCellRetriever;
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
        this.containingCellRetriever = containedCellRetriever;
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
     * Create a new entity to represent the child component.
     *
     * @return An entity used to represent the child component.
     */
    protected Entity createEntity() {
        Entity childEntity = new Entity(String.format("%s_%s", getClass().getName(), getOwnerName()));
        sceneRoot = createSceneGraph(childEntity);
        addComponents(childEntity, sceneRoot);
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
     * Add the default components to the Entity to support such things as lighting and collision.
     *
     * @param entity The Entity which represents the child component.
     * @param childTopNode The root most JME node for the screen graph of the child component.
     */
    protected void addComponents(Entity entity, Node childTopNode) {
        if (childTopNode != null) {
            RenderComponent renderComponent = entity.getComponent(RenderComponent.class);
            if (renderComponent == null) {
                renderComponent = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(childTopNode);
                entity.addComponent(RenderComponent.class, renderComponent);
            }
            else {
                renderComponent.setSceneRoot(childTopNode);
            }
            //Setup collision.
            CollisionComponent collisionComponent = setupCollision(getCollisionSystem(), childTopNode);
            if (collisionComponent != null) {
                entity.addComponent(CollisionComponent.class, collisionComponent);
            }
            if (containingCellRetriever != null) {
                Cell containingCell = containingCellRetriever.getContainingCell();
                if (containingCell != null) {
                    entity.addComponent(CellRefComponent.class, new CellRefComponent(containingCell));
                }
            }
        }
        else {
            logger.warning("The top level node for rendering the child component was null!");
        }
    }

    /**
     * Setup collision for the specified child top node using the specified CollisionSystem.
     *
     * @param collisionSystem The CollisionSystem to use to setup collision for the child component.
     * @param childTopNode The top Node of the child screen graph.
     * @return The CollisionComponent which was created and setup for the child component.
     */
    protected CollisionComponent setupCollision(CollisionSystem collisionSystem, Node childTopNode) {
        CollisionComponent collisionComponent = null;
        if (collisionSystem instanceof JMECollisionSystem) {
            collisionComponent = ((JMECollisionSystem) collisionSystem).createCollisionComponent(childTopNode);
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
     * {@inheritDoc}
     */
    @Override
    public Node updateScene() {
        if (sceneRoot != null) {
            sceneRoot.removeFromParent();
            sceneRoot.detachAllChildren();
            sceneRoot = null;
        }
        if (entity != null) {
            sceneRoot = createSceneGraph(entity);
            RenderComponent renderComponent = entity.getComponent(RenderComponent.class);
            if (renderComponent == null) {
                renderComponent = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(sceneRoot);
                logger.warning("Creating render component as part of update!");
            }
            else {
                renderComponent.setSceneRoot(sceneRoot);
            }
        }
        else {
            logger.warning("No entity exists when updating scene!");
            getEntity();
        }
        return sceneRoot;
    }

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
     * {@inheritDoc}
     */
    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        Entity childEntity = getEntity();
        if (childEntity != null) {
           if (status == CellStatus.INACTIVE && !increasing && listener != null) {
                listener.removeFromEntity(childEntity);
                if (listener instanceof Disposable) {
                    ((Disposable) listener).dispose();
                }
                listener = null;
           }
           else if (status == CellStatus.RENDERING && increasing && listener == null) {
                listener = this.createEventListener();
                listener.addToEntity(entity);
           }
        }
    }

    /**
     * @return the collisionEnabled
     */
    public boolean isCollisionEnabled() {
        return collisionEnabled;
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
     * Adjust the collision system after a change to picking or collision
     */
    private void adjustCollisionSystem() {
        CollisionComponent collisionComponent = entity.getComponent(CollisionComponent.class);
        if (!collisionEnabled && !pickingEnabled && collisionComponent != null) {
            entity.removeComponent(CollisionComponent.class);
        }
        else {
            if(collisionComponent == null) {
               collisionComponent = setupCollision(getCollisionSystem(), sceneRoot);
               entity.addComponent(CollisionComponent.class, collisionComponent);
            }
            else {
                collisionComponent.setCollidable(collisionEnabled);
                collisionComponent.setPickable(pickingEnabled);
            }
        }
    }

    /**
     * Get the CollisionSystem which this child component should use.
     *
     * @return The CollisionSystem to use for this child component.
     */
    protected CollisionSystem getCollisionSystem() {
        WorldManager worldManager = ClientContextJME.getWorldManager();
        return worldManager.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
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
}
