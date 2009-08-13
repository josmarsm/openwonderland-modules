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
import javax.swing.event.ListDataEvent;
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
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Extrusion;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
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
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.EventListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.marbleous.client.cell.MarblePhysicsComponent;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.modules.marbleous.client.cell.TrackCell;
import org.jdesktop.wonderland.modules.marbleous.common.Track;

/**
 *
 * @author paulby
 */
public class TrackRenderer extends BasicRenderer {

    public interface MarbleMouseEventListener {
        public void commitEvent (Entity marbleEntity, Event event);
    }

    private TriMesh trackMesh = null;
    private Node cellRoot = new Node("Marbleous");
    private final Node trackRoot = new Node("TrackRoot");

    private Entity marbleEntity;

    private LinkedList<MarbleMouseEventListener> marbleMouseListeners = 
        new LinkedList<MarbleMouseEventListener>();

    public TrackRenderer(Cell cell) {
        super(cell);
    }

    public Entity getMarbleEntity () {
        return marbleEntity;
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
            createTrackGraph(track);
            
            marbleEntity = createMarble(track.getMarbleStartPosition());
            entity.addEntity(marbleEntity);

            MarbleMouseListener mouseListener = new MarbleMouseListener();
            mouseListener.addToEntity(marbleEntity);

            ((TrackCell)cell).getTrackListModel().addListDataListener(new ListDataListener() {
                public void intervalAdded(ListDataEvent e) {
                    System.err.println("Added "+e);
                    update();
                }

                public void intervalRemoved(ListDataEvent e) {
                    System.err.println("Removed "+e);
                    update();
                }

                public void contentsChanged(ListDataEvent e) {
                    System.err.println("Changed "+e);
                    update();
                }

                
            });
            ((TrackCell)cell).getKnotTableModel().addTableModelListener(new TableModelListener() {

                public void tableChanged(TableModelEvent arg0) {
                    System.out.println("Table changed in renderer");
                    update();
                }
            });
        }



        cellRoot.attachChild(trackRoot);

        return cellRoot;
    }

    private void update() {
        System.out.println("Updating track renderer");
        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

            public void update(Object arg0) {
                synchronized (trackRoot) {
                    createTrackGraph(((TrackCell) cell).getTrack());
                    ClientContextJME.getWorldManager().addToUpdateList(trackRoot);
                }
            }
        }, trackRoot);
    }

    private void createTrackGraph(Track track) {
        Collection<TCBKeyFrame> keyFrames = track.buildTrack();
        RotPosScaleTCBSplinePath spline = new RotPosScaleTCBSplinePath(keyFrames.toArray(new TCBKeyFrame[keyFrames.size()]));
//            drawKnot(spline, ret);
        //drawSpline(spline, ret);
        trackMesh = createTrackMesh(spline);
        trackMesh.setLocalTranslation(Vector3f.ZERO);
        trackMesh.setLocalRotation(new Quaternion());

//            trackMesh = new Dome("", 25, 25, 10f);
        
//        trackMesh = new Quad("", 100, 100);
//        trackMesh.setLocalTranslation(Vector3f.ZERO);
//        Quaternion rot = new Quaternion();
//        rot.fromAngleAxis((float)Math.PI/2, new Vector3f(1,0,0));
//        trackMesh.setLocalRotation(rot);


        trackMesh.setModelBound(new BoundingBox());
        trackMesh.updateModelBound();
        trackRoot.detachAllChildren();
        trackRoot.attachChild(trackMesh);

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

            JBulletDynamicCollisionSystem trackCollisionSystem = (JBulletDynamicCollisionSystem) ClientContextJME.getCollisionSystem(cell.getCellCache().getSession().getSessionManager(), "Physics");

            entity.addComponent(JBulletCollisionComponent.class, trackCollisionSystem.createCollisionComponent(trackMesh));
        } else {
            logger.warning("**** BASIC RENDERER - ROOT NODE WAS NULL !");
        }

    }

    private Entity createMarble(Vector3f initialPosition) {
        float size = 0.25f;
        Entity e = new Entity();
        Node marbleRoot = new Node("marble-root");
        Sphere marble = new Sphere("marble", 10, 10, size);
        Triangle[] tris = new Triangle[marble.getTriangleCount()];

        // Compute bounds, JME does not calculate them correctly
        BoundingBox bsphere = new BoundingBox();
        bsphere.computeFromTris(marble.getMeshAsTriangles(tris), 0, tris.length);
        marble.setModelBound(bsphere);
        marbleRoot.attachChild(marble);
        initialPosition.y += 4;
        initialPosition.z -= size;      // HACK assumes initial track orientation
        marbleRoot.setLocalTranslation(initialPosition);

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
            TCBKeyFrame key = spline.getKeyFrame(i);
            Box box = new Box("knot-" + i, key.position, 0.5f, 0.5f, 0.5f);
            root.attachChild(box);
        }
    }

    private void drawSpline(RotPosScaleTCBSplinePath spline, Node root) {
        float step = 0.01f;

        Matrix4f mat = new Matrix4f();
        spline.computeTransform(0, mat);
        root.attachChild(createBox(0.1f, mat));

        for (float s = step; s <= 1; s += 0.01f) {
            spline.computeTransform(s, mat);
            root.attachChild(createBox(0.1f, mat));
        }
    }

    private TriMesh createTrackMesh(RotPosScaleTCBSplinePath spline) {
        float step = 0.01f;

        Line extrusionShape = new Line();
        extrusionShape.setMode(Line.Mode.Connected);
        float[] points = new float[]{
            -1, 0.5f, 0f,
            0, 0, 0,
            0, 0, 0,
            1, 0.5f, 0
        };

        // TODO fix normals
        float[] normals = new float[]{
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0
        };

        extrusionShape.setVertexBuffer(FloatBuffer.wrap(points));
        extrusionShape.setNormalBuffer(FloatBuffer.wrap(normals));

        Matrix4f mat = new Matrix4f();
        Vector3f pos;
        ArrayList<Vector3f> path = new ArrayList();
        for (float s = 0; s <= 1; s += 0.01f) {
            spline.computeTransform(s, mat);
            pos = mat.mult(Vector3f.ZERO);
            path.add(pos);
        }

        Extrusion ext = new Extrusion(extrusionShape, path, new Vector3f(0, 1, 0));
        TrackShader shader = new TrackShader();
        shader.applyToGeometry(ext);
        ext.setModelBound(new BoundingBox());
        ext.updateModelBound();

        return ext;
    }

    private TriMesh createBox(float size, Matrix4f transform) {
        Box b = new Box(null, Vector3f.ZERO, size, size, size);

        b.setLocalTranslation(transform.toTranslationVector());
        b.setLocalRotation(transform.toRotationQuat());

        return b;
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
}
