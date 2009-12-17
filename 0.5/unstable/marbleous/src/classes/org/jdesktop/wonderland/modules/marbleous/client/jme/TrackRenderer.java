/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.marbleous.client.jme;

import com.bulletphysics.dynamics.RigidBody;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import javax.swing.event.ListDataEvent;
import org.jdesktop.wonderland.client.input.Event;
import javax.swing.event.TableModelEvent;
import org.jdesktop.wonderland.modules.marbleous.common.TCBKeyFrame;
import com.jme.math.Matrix4f;
import com.jme.math.Quaternion;
import com.jme.math.Triangle;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Line;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.Geometry;
import com.jme.scene.Spatial;
import com.jme.scene.GeometricUpdateListener;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Extrusion;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.image.Texture;
import com.jmex.effects.particles.ParticleMesh;
import com.jmex.effects.particles.ParticleFactory;
import com.jme.util.TextureManager;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import java.awt.event.MouseEvent;
import com.jme.scene.state.GLSLShaderObjectsState;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelListener;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.CollisionSystem;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JBulletCollisionComponent;
import org.jdesktop.mtgame.JBulletCollisionComponent.InitializedListener;
import org.jdesktop.mtgame.JBulletDynamicCollisionSystem;
import org.jdesktop.mtgame.JBulletPhysicsComponent;
import org.jdesktop.mtgame.JBulletPhysicsSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.marbleous.client.cell.MarblePhysicsComponent;
import org.jdesktop.wonderland.modules.marbleous.client.cell.TrackCell;
import org.jdesktop.wonderland.modules.marbleous.common.Track;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SimulationStateMessage.SimulationState;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author paulby
 */
public class TrackRenderer extends BasicRenderer {

    private static Logger logger = Logger.getLogger(TrackRenderer.class.getName());


    public interface MarbleMouseEventListener {
        public void commitEvent (Entity marbleEntity, Event event);
    }

    private TriMesh trackMesh = null;
    private Node cellRoot = new Node("Marbleous");
    private final Node trackRoot = new Node("TrackRoot");

    // The marble entity and it's root node
    private Entity marbleEntity = null;
    private Particles particles = null;

    private LinkedList<MarbleMouseEventListener> marbleMouseListeners = 
        new LinkedList<MarbleMouseEventListener>();

    private SimulationState simState = null;

    public TrackRenderer(Cell cell) {
        super(cell);
    }

    public Entity getMarbleEntity () {
        return marbleEntity;
    }

    /**
     * Resets the marble to its initial position.
     */
    public void resetMarble() {
        // First remove the marble entity and recreate and add it
        getEntity().removeEntity(marbleEntity);

        Vector3f cellPos = new Vector3f(rootNode.getLocalTranslation());
        Quaternion cellRot = rootNode.getLocalRotation();

//            cellRot.multLocal(cellPos);

        Vector3f marblePos = ((TrackCell) cell).getTrack().getMarbleStartPosition();
        cellRot.multLocal(marblePos);
        marblePos.addLocal(cellPos);
        marbleEntity = createMarble(marblePos);
        entity.addEntity(marbleEntity);

        MarbleMouseListener mouseListener = new MarbleMouseListener();
        mouseListener.addToEntity(marbleEntity);
    }

    public void stopParticles() {
        if (particles != null) {
            particles.setEnable(false);
        }
    }

    @Override
    protected Node createSceneGraph(Entity entity) {
        if (false) {
            // Code for visualizing test splines
            SplineTest splineTest = new SplineTest();

            drawKnot(splineTest.interp, trackRoot);
            drawSpline(splineTest.interp, trackRoot);
        } else {
            trackRoot.setLocalTranslation(Vector3f.ZERO);
            trackRoot.setLocalRotation(new Quaternion());

            Track track = ((TrackCell) cell).getTrack();
            createTrackGraph(track, entity);
            
            // Listen for changes to the track and trigger a redraw
            ((TrackCell)cell).getTrackListModel().addListDataListener(new ListDataListener() {
                public void intervalAdded(ListDataEvent e) {
                    update();
                }

                public void intervalRemoved(ListDataEvent e) {
                    update();
                }

                public void contentsChanged(ListDataEvent e) {
                    update();
                }

                
            });
            ((TrackCell)cell).getKnotTableModel().addTableModelListener(new TableModelListener() {

                public void tableChanged(TableModelEvent arg0) {
                    logger.info("Table changed in renderer");
                    update();
                }
            });
        }



        cellRoot.attachChild(trackRoot);

        return cellRoot;
    }

    public void setSimulationState(SimulationState simulationState) {
        simState = simulationState;
        if (simState.equals(SimulationState.STOPPED))
            stopParticles();
    }
    
    @Override
    public void cellTransformUpdate(CellTransform t) {
        // Ignore cell transform
    }

    private void update() {
        logger.info("Updating track renderer");
        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

            public void update(Object arg0) {
                synchronized (trackRoot) {
                    createTrackGraph(((TrackCell) cell).getTrack(), entity);
                    ClientContextJME.getWorldManager().addToUpdateList(trackRoot);
                }
            }
        }, trackRoot);
    }

    private void createTrackGraph(Track track, Entity trackEntity) {
        trackRoot.detachAllChildren();
        trackEntity.removeComponent(JBulletCollisionComponent.class);
        
        Collection<TCBKeyFrame> keyFrames = track.buildTrack();
        RotPosScaleTCBSplinePath spline = new RotPosScaleTCBSplinePath(keyFrames.toArray(new TCBKeyFrame[keyFrames.size()]));

//        drawKnot(spline, trackRoot);
//        drawSpline(spline, trackRoot);

        trackMesh = createTrackMesh(spline);
        trackMesh.setLocalTranslation(Vector3f.ZERO);
        trackMesh.setLocalRotation(new Quaternion());

        trackMesh.setModelBound(new BoundingBox());
        trackMesh.updateModelBound();
        trackRoot.attachChild(trackMesh);

        JBulletDynamicCollisionSystem trackCollisionSystem = (JBulletDynamicCollisionSystem) ClientContextJME.getCollisionSystem(cell.getCellCache().getSession().getSessionManager(), "Physics");
        final JBulletCollisionComponent bcc = trackCollisionSystem.createCollisionComponent(trackMesh);

        // Set the friction on the track
        bcc.addInitializedListener(new InitializedListener() {
            public void componentInitialized() {
                bcc.getCollisionObject().setFriction(0f);
            }
        });

        trackEntity.addComponent(JBulletCollisionComponent.class, bcc);
    }

    /**
     * Override so we can add the mesh to the collision system instead of the node, which means we
     * do triangle collision instead of bounds
     * @param entity
     * @param rootNode
     */
    @Override
    protected void addDefaultComponents(Entity entity, Node rootNode) {
        if (cell.getComponent(MovableComponent.class)!=null) {
            if (rootNode==null) {
                logger.warning("Cell is movable, but has no root node !");
            } else {
                // The cell is movable so create a move processor
                moveProcessor = new MoveProcessor(ClientContextJME.getWorldManager(), rootNode);
                entity.addComponent(MoveProcessor.class, moveProcessor);
            }
        }

        if (rootNode!=null) {
            // Some subclasses (like the imi collada renderer) already add
            // a render component
            RenderComponent rc = entity.getComponent(RenderComponent.class);
            if (rc==null) {
                rc = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(rootNode);
                entity.addComponent(RenderComponent.class, rc);
            }

            WonderlandSession session = cell.getCellCache().getSession();
            CollisionSystem collisionSystem = ClientContextJME.getCollisionSystem(session.getSessionManager(), "Default");

            rootNode.updateWorldBound();

            CollisionComponent cc=null;

            cc = setupCollision(collisionSystem, rootNode);
            if (cc!=null) {
                entity.addComponent(CollisionComponent.class, cc);
            }

            Vector3f cellPos = new Vector3f(rootNode.getLocalTranslation());
            Quaternion cellRot = rootNode.getLocalRotation();

//            cellRot.multLocal(cellPos);

            Vector3f marblePos = ((TrackCell)cell).getTrack().getMarbleStartPosition();
            cellRot.multLocal(marblePos);
            marblePos.addLocal(cellPos);
            marbleEntity = createMarble(marblePos);
            entity.addEntity(marbleEntity);

            MarbleMouseListener mouseListener = new MarbleMouseListener();
            mouseListener.addToEntity(marbleEntity);
        } else {
            logger.warning("**** BASIC RENDERER - ROOT NODE WAS NULL !");
        }

    }

    private Entity createMarble(Vector3f initialPosition) {
        float size = 0.25f;
        Entity e = new Entity();
        Vector3f pos = new Vector3f(initialPosition);
        Node marbleRoot = new Node("marble-root");
        Sphere marble = new Sphere("marble", 10, 10, size);

        // Compute bounds, JME does not calculate them correctly
        Triangle[] tris = new Triangle[marble.getTriangleCount()];
        BoundingSphere bsphere = new BoundingSphere();
        bsphere.computeFromTris(marble.getMeshAsTriangles(tris), 0, tris.length);
        marble.setModelBound(bsphere);

        marbleRoot.attachChild(marble);
        pos.y += 2;
        marbleRoot.setLocalTranslation(pos);
        marbleRoot.setLocalRotation(new Quaternion());

        particles = new Particles(ClientContextJME.getWorldManager(), marbleRoot, e);

        RenderComponent renderComponent = ClientContextJME.getWorldManager().getRenderManager().createRenderComponent(marbleRoot);
        e.addComponent(RenderComponent.class, renderComponent);

        WonderlandSession session = cell.getCellCache().getSession();
        JBulletPhysicsSystem physicsSystem = ((TrackCell) cell).getPhysicsSystem();
        JBulletDynamicCollisionSystem collisionSystem = ((TrackCell) cell).getCollisionSystem();

        final JBulletCollisionComponent cc = collisionSystem.createCollisionComponent(marbleRoot);
        JBulletPhysicsComponent pc = physicsSystem.createPhysicsComponent(cc);
        pc.setMass(MarblePhysicsComponent.MASS);
        e.addComponent(JBulletCollisionComponent.class, cc);
        e.addComponent(JBulletPhysicsComponent.class, pc);

        // Set the marble's rigid body on the cell so that we can fetch it for
        // time-series stuff. We need to wait for the collision component to
        // be initialized first.
        cc.addInitializedListener(new InitializedListener() {
            public void componentInitialized() {
                if (cc.getCollisionObject() instanceof RigidBody) {
                    ((TrackCell) cell).setMarbleRigidBody(((RigidBody) cc.getCollisionObject()));
                }
            }
        });

        ZBufferState buf = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        marbleRoot.setRenderState(buf);

        ColorRGBA color = new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f);
        MaterialState matState = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.Material);
        matState.setDiffuse(color);
        marble.setRenderState(matState);
        MarbleShader shader = new MarbleShader();
        shader.applyToGeometry(marble);

        // Make marble pickable
        CollisionSystem pickCollisionSystem = ClientContextJME.getCollisionSystem(session.getSessionManager(), "Default");
        CollisionComponent pcc = setupCollision(pickCollisionSystem, marbleRoot);
        if (pcc!=null) {
            e.addComponent(CollisionComponent.class, pcc);
        }

        return e;
    }

    protected class MarbleMouseListener extends EventClassListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseEvent3D.class};
        }

        /**
         * {@inheritDoc}
         */
        @Override
            public void commitEvent(Event event) {
            synchronized (marbleMouseListeners) {
                for (MarbleMouseEventListener ml : marbleMouseListeners) {
                    ml.commitEvent(marbleEntity, event);
                }
            }
        }
    }

    public void addMarbleMouseListener (MarbleMouseEventListener listener) {
        synchronized (marbleMouseListeners) {
            marbleMouseListeners.add(listener);
        }
    }

    private void drawKnot(RotPosScaleTCBSplinePath spline, Node root) {
        int size = spline.getArrayLength();
        for (int i = 0; i < size; i++) {
            Node knot = new Node("keyframe-"+i);
            TCBKeyFrame key = spline.getKeyFrame(i);

            // Knot
            Box box = new Box("knot-" + i, key.position, 0.1f, 0.1f, 0.1f);
//            box.setModelBound(new BoundingBox(Vector3f.ZERO, 0.1f, 0.1f, 0.1f));
            knot.attachChild(box);

            Vector3f offset = new Vector3f(0,1,0);
            key.quat.multLocal(offset);
            offset.addLocal(key.position);
            // Rotation
            Sphere s = new Sphere("knot-up-"+i, offset, 10, 10, 0.1f);
//            s.setModelBound(new BoundingSphere(0.1f, Vector3f.ZERO));
            knot.attachChild(s);

            root.attachChild(knot);
        }

        ClientContextJME.getInputManager().addGlobalEventListener(new MouseKnotListener());
    }

    private void drawSpline(RotPosScaleTCBSplinePath spline, Node root) {
        float step = 0.01f;

        Matrix4f mat = new Matrix4f();
        spline.computeTransform(0, mat);
        root.attachChild(createBox(0.1f, mat));

        for (float s = step; s <= 1; s += step) {
            spline.computeTransform(s, mat);
            root.attachChild(createBox(0.1f, mat));
        }
    }

    private TriMesh createTrackMesh(RotPosScaleTCBSplinePath spline) {
        float step = 1f/spline.getArrayLength()/20;

        Matrix4f mat = new Matrix4f();
        Vector3f pos;
        ArrayList<Vector3f> path = new ArrayList<Vector3f>();
        ArrayList<Vector3f> upList = new ArrayList<Vector3f>();
        for (float s = 0; s <= 1; s += step) {
            spline.computeTransform(s, mat);
            pos = mat.mult(Vector3f.ZERO);
            path.add(pos);

            Vector3f up = new Vector3f(0,1,0);
            mat.rotateVect(up);
            upList.add(up);
        }

        Extrusion ext = new Extrusion("trackMesh", getExtrusionShape(ExtrusionShape.U_SHAPE), path, upList);
        TrackShader shader = new TrackShader();
        shader.applyToGeometry(ext);
        ext.setModelBound(new BoundingBox());
        ext.updateModelBound();

        return ext;
    }

    enum ExtrusionShape { V_SHAPE, U_SHAPE };

    private Line getExtrusionShape(ExtrusionShape shape) {
        Line extrusionShape = new Line();
        extrusionShape.setMode(Line.Mode.Connected);

        float[] points=null;
        float[] normals=null;

        switch(shape) {
            case V_SHAPE :
                points = new float[]{
                    -0.7f, 0.4f, 0f,
                    0, 0, 0,
                    0, 0, 0,
                    0.7f, 0.4f, 0
                };

                // TODO fix normals
                normals = new float[]{
                    0, 1, 0,
                    0, 1, 0,
                    0, 1, 0,
                    0, 1, 0
                };
            break;
            case U_SHAPE :
                int samples = 12;
                float radius = 0.4f;
                double step = Math.toRadians(180/samples);
                Vector3f tmp = new Vector3f();

                points = new float[3*samples*2];
                normals = new float[3*samples*2];

                double angle = -Math.PI/2;
                for(int i=0; i<samples*6; i+=6) {
                    points[i] = (float)Math.sin(angle)*radius;
                    points[i+1] = radius-(float)Math.cos(angle)*radius;
                    points[i+2] = 0f;
                    points[i+3] = (float)Math.sin(angle+step)*radius;
                    points[i+4] = radius-(float)Math.cos(angle+step)*radius;
                    points[i+5] = 0f;

                    tmp.set(-points[i], -points[i+1], 0f);
                    tmp.normalizeLocal();
                    normals[i] = tmp.x;
                    normals[i+1] = tmp.y;
                    normals[i+2] = tmp.z;
                    tmp.set(-points[i+3], -points[i+4], 0f);
                    tmp.normalizeLocal();
                    normals[i+3] = tmp.x;
                    normals[i+4] = tmp.y;
                    normals[i+5] = tmp.z;

                    angle+=step;
                }
                break;
        };

        extrusionShape.setVertexBuffer(FloatBuffer.wrap(points));
        extrusionShape.setNormalBuffer(FloatBuffer.wrap(normals));

        return extrusionShape;
    }

    private TriMesh createBox(float size, Matrix4f transform) {
        Box b = new Box("box", Vector3f.ZERO, size, size, size);

        b.setLocalTranslation(transform.toTranslationVector());
        b.setLocalRotation(transform.toRotationQuat());
        ColorRGBA color = new ColorRGBA(0.0f, 0.0f, 1.0f, 1.0f);
        MaterialState matState = (MaterialState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.Material);
        matState.setDiffuse(color);
        b.setRenderState(matState);

        return b;
    }

    private class MouseKnotListener extends EventClassListener {

        public MouseKnotListener() {
        }

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {

            if (event instanceof MouseButtonEvent3D) {
                MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
                if (buttonEvent.isPressed() && buttonEvent.getButton() ==
                        MouseButtonEvent3D.ButtonId.BUTTON1) {
                    MouseEvent awtButtonEvent = (MouseEvent) buttonEvent.getAwtEvent();
                    if (buttonEvent.getPickDetails()!=null) {
                        logger.warning(buttonEvent.getPickDetails().getTriMesh().getName());
                    }

//                    if (buttonEvent.getPickDetails().getTriMesh().getName().equals(sourceNodeName)) {
//                        System.err.println("BINGO !");
//                    }
                }
                return;
            }

        }
    }

    public class TrackShader implements RenderUpdater {
        GLSLShaderObjectsState shaderState = null;

        /**
         * The vertex and fragment shader
         */
        protected static final String vShader =
                "uniform float Scale;" +
                "varying vec3  MCposition;" +
                "void main(void)" +
                "{" +
                    "MCposition      = vec3(gl_Vertex) * Scale;" +
                    "gl_Position     = ftransform();" +
                "}";

        private static final String fShader =
                "varying vec3  MCposition;" +
                "uniform vec3 Color1;" +
                "uniform vec3 Color2;" +
                "void main(void) { " +
                    "vec4 noisevec   = noise4(MCposition);" +

                    "float intensity = abs(noisevec[0] - 0.25) +" +
                                      "abs(noisevec[1] - 0.125) +" +
                                      "abs(noisevec[2] - 0.0625) +" +
                                      "abs(noisevec[3] - 0.03125);" +
                    "intensity    = clamp(intensity, 0.0, 1.0);" +
                    "vec3 color    = mix(Color1, Color2, intensity);" +
                    "gl_FragColor  = vec4(color, 1.0);" +
                "}";

        public TrackShader() {
        }

        /**
         * This applies this shader to the given geometry
         */
        public void applyToGeometry(Geometry geo) {
            shaderState = (GLSLShaderObjectsState) ClientContextJME.getWorldManager().getRenderManager().
                createRendererState(RenderState.StateType.GLSLShaderObjects);
            shaderState.setUniform("Scale", 1.2f);
            shaderState.setUniform("Color1", 0.8f, 0.7f, 0.0f);
            shaderState.setUniform("Color2", 0.6f, 0.1f, 0.0f);
            geo.setRenderState(shaderState);
            ClientContextJME.getWorldManager().addRenderUpdater(this, this);
        }
        /**
         * This loads the shader
         */
        public void update(Object o) {
            shaderState.load(vShader, fShader);
        }
    }

    public class MarbleShader implements RenderUpdater {
        GLSLShaderObjectsState shaderState = null;

        /**
         * The vertex and fragment shader
         */
        protected static final String vShader =
                "uniform float Scale;" +
                "varying vec3  MCposition;" +
                "void main(void)" +
                "{" +
                    "MCposition      = vec3(gl_Vertex) * Scale;" +
                    "gl_Position     = ftransform();" +
                "}";

        private static final String fShader =
                "varying vec3  MCposition;" +
                "uniform vec3 Color1;" +
                "uniform vec3 Color2;" +
                "void main(void) { " +
                    "vec4 noisevec   = noise4(MCposition);" +

                    "float intensity = abs(noisevec[0] - 0.25) +" +
                                      "abs(noisevec[1] - 0.125) +" +
                                      "abs(noisevec[2] - 0.0625) +" +
                                      "abs(noisevec[3] - 0.03125);" +
                    "float sineval = sin(MCposition.y * 6.0 + intensity * 12.0) * 0.5 + 0.5;" +
                    "vec3 color    = mix(Color1, Color2, intensity);" +
                    "gl_FragColor  = vec4(color, 1.0);" +
                "}";

        public MarbleShader() {
        }

        /**
         * This applies this shader to the given geometry
         */
        public void applyToGeometry(Geometry geo) {
            shaderState = (GLSLShaderObjectsState) ClientContextJME.getWorldManager().getRenderManager().
                createRendererState(RenderState.StateType.GLSLShaderObjects);
            shaderState.setUniform("Scale", 5.0f);
            shaderState.setUniform("Color1", 0.0f, 0.0f, 1.0f);
            shaderState.setUniform("Color2", 1.0f, 1.0f, 1.0f);
            geo.setRenderState(shaderState);
            ClientContextJME.getWorldManager().addRenderUpdater(this, this);
        }
        /**
         * This loads the shader
         */
        public void update(Object o) {
            shaderState.load(vShader, fShader);
        }
    }

    public class Particles implements RenderUpdater, GeometricUpdateListener {

        private WorldManager wm = null;
        ParticleMesh pMesh = null;
        Node followNode = null;
        Vector3f trans = new Vector3f();
        private boolean enable = true;
        private Entity e = null;
        private Entity rootEntity = null;

        public Particles(WorldManager wm, Node followModel, Entity rootEntity) {
            this.wm = wm;
            URL url = null;
            followNode = followModel;
            trans = followNode.getWorldTranslation();
            this.rootEntity = rootEntity;

            BlendState as1 = (BlendState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.Blend);
            as1.setBlendEnabled(true);
            as1.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
            as1.setDestinationFunction(BlendState.DestinationFunction.One);
            as1.setTestEnabled(true);
            as1.setTestFunction(BlendState.TestFunction.GreaterThan);
            as1.setEnabled(true);
            as1.setEnabled(true);

            try {
                url = AssetUtils.getAssetURL("wla://marbleous/flaresmall.jpg", getCell());
            } catch (MalformedURLException ex) {
                logger.log(Level.SEVERE, "Failed to load flare", ex);
            }


            TextureState ts = (TextureState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.Texture);
            ts.setTexture(
                    TextureManager.loadTexture(
                    url,
                    Texture.MinificationFilter.Trilinear,
                    Texture.MagnificationFilter.Bilinear));
            ts.setEnabled(true);

            pMesh = ParticleFactory.buildParticles("particles", 300);
            pMesh.setEmissionDirection(new Vector3f(0, 1, 0));
            pMesh.setInitialVelocity(.006f);
            pMesh.setStartSize(2.5f);
            pMesh.setEndSize(.5f);
            pMesh.setMinimumLifeTime(1200f);
            pMesh.setMaximumLifeTime(1400f);
            pMesh.setStartColor(new ColorRGBA(1, 0, 0, 1.0f));
            pMesh.setEndColor(new ColorRGBA(0, 1, 0, 0));
            pMesh.setMaximumAngle((float) Math.toRadians(360));
            pMesh.getParticleController().setControlFlow(false);
            pMesh.setParticlesInWorldCoords(true);
            pMesh.warmUp(60);

            pMesh.setRenderState(ts);
            pMesh.setRenderState(as1);
            ZBufferState zstate = (ZBufferState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
            zstate.setEnabled(false);
            pMesh.setRenderState(zstate);
            pMesh.setModelBound(new BoundingSphere());
            pMesh.updateModelBound();
            pMesh.setLocalScale(0.5f);
            pMesh.setLocalTranslation(trans.x, trans.y, trans.z);

            followNode.addGeometricUpdateListener(this);
            wm.addRenderUpdater(this, pMesh);

            e = new Entity("Particles ");
            RenderComponent sc = wm.getRenderManager().createRenderComponent(pMesh);
            sc.setLightingEnabled(false);
            e.addComponent(RenderComponent.class, sc);
            rootEntity.addEntity(e);
            //wm.addEntity(e);
        }

        public void geometricDataChanged(Spatial spatial) {
            trans = spatial.getWorldTranslation();
            pMesh.setLocalTranslation(trans.x, trans.y, trans.z);
        }

        public void setEnable(boolean flag) {
            if (enable != flag) {
                enable = flag;
                if (enable) {
                    rootEntity.addEntity(e);
                } else {
                    rootEntity.removeEntity(e);
                }
            }
        }

        /**
         * This loads the shader
         */
        public void update(Object p) {
            wm.addToUpdateList((Node)p);
            wm.addRenderUpdater(this, p);
        }

        public Entity getEntity() {
            return (e);
        }
    }
}
