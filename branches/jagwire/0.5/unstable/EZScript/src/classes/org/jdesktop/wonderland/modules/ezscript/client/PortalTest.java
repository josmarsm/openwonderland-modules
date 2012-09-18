/*
 * Copyright (c) 2009, Sun Microsystems, Inc. All rights reserved.
 *
 *    Redistribution and use in source and binary forms, with or without
 *    modification, are permitted provided that the following conditions
 *    are met:
 *
 *  . Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  . Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  . Neither the name of Sun Microsystems, Inc., nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import org.jdesktop.mtgame.processor.EyeSelectionProcessor;
import org.jdesktop.mtgame.processor.MouseSelectionProcessor;
import org.jdesktop.mtgame.processor.SelectionProcessor;
import org.jdesktop.mtgame.processor.RotationProcessor;
import org.jdesktop.mtgame.processor.PostEventProcessor;
import org.jdesktop.mtgame.processor.OrbitCameraProcessor;
import org.jdesktop.mtgame.processor.FPSCameraProcessor;
import org.jdesktop.mtgame.shader.Shader;
import org.jdesktop.mtgame.*;
import com.jme.scene.Node;
import com.jme.scene.CameraNode;
import com.jme.scene.shape.AxisRods;
import com.jme.scene.state.ZBufferState;
import com.jme.light.PointLight;
import com.jme.light.DirectionalLight;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.LightState;
import com.jme.light.LightNode;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.CullState;
import com.jme.scene.shape.Teapot;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Quad;
import com.jme.scene.Geometry;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.scene.Line;
import com.jme.math.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.GridBagLayout;
import java.awt.FlowLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JLabel;
import javax.swing.JFileChooser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
//import com.jmex.model.collada.ColladaImporter;

import com.jme.scene.Skybox;
import com.jme.image.Texture;
import com.jme.scene.Spatial.TextureCombineMode;
import com.jme.scene.Spatial;
import com.jme.util.resource.ResourceLocator;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.geom.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import java.net.URL;
import java.net.MalformedURLException;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import org.jdesktop.mtgame.processor.AWTEventProcessorComponent;
import com.jme.scene.shape.Sphere;
import com.jme.intersection.TriangleCollisionResults;
import org.jdesktop.mtgame.shader.DiffuseNormalMap;
import com.jme.util.geom.TangentBinormalGenerator;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;

import java.awt.Toolkit;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Image;

import java.util.Random;


/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class PortalTest {
    /**
     * The WorldManager for this world
     */
    WorldManager wm = null;
    
    /**
     * The CameraNode
     */
    private CameraNode cameraNode = null;
    
    /**
     * The desired frame rate
     */
    private int desiredFrameRate = 60;
    
    /**
     * The width and height of our 3D window
     */
    private int width = 800;
    private int height = 600;
    private float aspect = 800.0f/600.0f;
    
    /**
     * Some options state variables
     */
    private boolean coordsOn = true;
    private boolean gridOn = false;
    
    /**
     * The width of the grid
     */
    private int gridWidth = 250;
    
    /**
     * The entity which represents the grid
     */
    private Entity grid = new Entity("Grid");
    
    /**
     * The Entity which represents the axis
     */
    private Entity axis = new Entity("Axis");
    
    /**
     * A list of the models we are looking at
     */
    private ArrayList models = new ArrayList();
        
    private Canvas canvas = null;
    private RenderBuffer rb = null;
    private PortalBufferController pbc = null;
    private Skybox skybox = null;
    private SwingFrame frame = null;
    private String loadfile = "/Users/runner/Desktop/OFFTWG/a_great_epyptian_temple/models/Palace.dae";
    private String urlpath = "file:/Users/runner/Desktop/models/images/";
    Portal p1 = null;
    Portal p2 = null;
    boolean warping = false;
    boolean animating = false;
    PortalAnimationProcessor animationProcessor = null;
    JBulletDynamicCollisionSystem jBcollisionSystem = null;
    JBulletPhysicsSystem jBphysicsSystem = null;
    Geometry roof = null;

    /**
     * The collision system - if we are tracking collision.
     */
    private JMECollisionSystem collisionSystem = null;
    
    public PortalTest(String[] args) {
        wm = new WorldManager("TestWorld");

        pbc = new PortalBufferController();
        wm.getRenderManager().setBufferController(pbc);
        
        processArgs(args);
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);

        collisionSystem = (JMECollisionSystem)wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
        jBcollisionSystem = (JBulletDynamicCollisionSystem)
                wm.getCollisionManager().loadCollisionSystem(JBulletDynamicCollisionSystem.class);
        jBphysicsSystem = (JBulletPhysicsSystem)
                wm.getPhysicsManager().loadPhysicsSystem(JBulletPhysicsSystem.class, jBcollisionSystem);

        //addLight(new Vector3f(1.0f, 1.0f, 1.0f));
        //addLight(new Vector3f(-1.0f, 1.0f, -1.0f));
        //addLight(new Vector3f(-1.0f, 1.0f, 1.0f));
        //addLight(new Vector3f(1.0f, -1.0f, -1.0f));
        setGlobalLights();
        
        createUI(wm);  
        createCameraEntity(wm);   
        createAxis();
        //wm.addEntity(axis);
        createCrossHairs(wm);
        createOuterBox(wm);
        createEntry("Entry 1", wm, new Vector3f(0.0f, 0.0f, -215.0f), new Quaternion());
        Quaternion rot = new Quaternion();
        rot.fromAngleAxis(-(float)(Math.PI/2.0), new Vector3f(0.0f, 1.0f, 0.0f));
        createEntry("Entry 2", wm, new Vector3f(215.0f, 0.0f, 0.0f), rot);
        createLedges();
        //createSkybox(wm);
        //frame.loadFile(loadfile, false);

        createSectorAndPortals(wm, pbc);
        //addPortalProcessors(wm);
        
    }

    public void setGlobalLights() {
        LightNode globalLight1 = new LightNode();
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 1.0f));
        light.setSpecular(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
        light.setAmbient(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
        light.setLocation(new Vector3f(0.0f, 400.0f, 0.0f));
        light.setEnabled(true);
        globalLight1.setLight(light);
        globalLight1.setLocalTranslation(0.0f, 0.0f, 0.0f);

        LightNode globalLight2 = new LightNode();
        light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 1.0f));
        light.setAmbient(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
        light.setSpecular(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
        light.setEnabled(true);
        globalLight2.setLight(light);
        globalLight2.setLocalTranslation(0.0f, -200.0f, -200.0f);
        wm.getRenderManager().addLight(globalLight1);
        //wm.getRenderManager().addLight(globalLight2);
    }
    
    private void createCameraEntity(WorldManager wm) {
        Node cameraSG = createCameraGraph(wm);
        
        // Add the camera
        Entity camera = new Entity("DefaultCamera");
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cameraNode, 
                width, height, 45.0f, aspect, 1.0f, 1000.0f, true);
        rb.setCameraComponent(cc);
        camera.addComponent(CameraComponent.class, cc);

        // Create the input listener and process for the camera
        int eventMask = InputManager.KEY_EVENTS | InputManager.MOUSE_EVENTS;
        AWTInputComponent cameraListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);
        PortalProcessor eventProcessor = new PortalProcessor(cameraListener, cameraNode, wm, camera);
        eventProcessor.setRunInRenderer(true);

        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        pcc.addProcessor(eventProcessor);
        camera.addComponent(ProcessorCollectionComponent.class, pcc);
        
        wm.addEntity(camera);
    }
    
    private Node createCameraGraph(WorldManager wm) {
        Node cameraSG = new Node("MyCamera SG");        
        cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(cameraNode);
        
        return (cameraSG);
    }

    private void createCrossHairs(WorldManager wm) {
        int crossWidth = width/10;
        int crossHeight = height/10;

        Vector3f[] points = new Vector3f[4];
        points[0] = new Vector3f(width/2 - crossWidth/2, height/2, 0.0f);
        points[1] = new Vector3f(width/2 + crossWidth/2, height/2, 0.0f);
        points[2] = new Vector3f(width/2, height/2 - crossHeight/2, 0.0f);
        points[3] = new Vector3f(width/2, height/2 + crossHeight/2, 0.0f);

        Node crossHairSG = new Node("CrossHair");
        Line crossHairLines = new Line("CrossHair", points, null, null, null);
        crossHairSG.attachChild(crossHairLines);

        RenderComponent rc = wm.getRenderManager().createRenderComponent(crossHairSG);
        rc.setOrtho(true);
        rc.setLightingEnabled(false);
        grid.addComponent(RenderComponent.class, rc);

        Entity e = new Entity("Cross Hair");
        e.addComponent(RenderComponent.class, rc);
        wm.addEntity(e);
    }

    private void createAxis() { 
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
            
        Node axisSG = new Node("Axis");
        AxisRods axisG = new AxisRods("Axis", true, 10.0f, 0.2f);
        axisSG.attachChild(axisG);
        axisSG.setRenderState(buf);
        
        RenderComponent rc = wm.getRenderManager().createRenderComponent(axisSG);
        rc.setLightingEnabled(false);
        axis.addComponent(RenderComponent.class, rc);
    }

    private void createOuterBox(WorldManager wm) {
        Entity e = new Entity("OuterBox");

        Vector3f v0 = new Vector3f(-250.0f,   0.0f,  250.0f);
        Vector3f v1 = new Vector3f( -50.0f, 500.0f,   50.0f);
        Vector3f v2 = new Vector3f(  50.0f, 500.0f,   50.0f);
        Vector3f v3 = new Vector3f( 250.0f,   0.0f,  250.0f);
        Vector3f v4 = new Vector3f(-250.0f,   0.0f, -250.0f);
        Vector3f v5 = new Vector3f( -50.0f, 500.0f,  -50.0f);
        Vector3f v6 = new Vector3f(  50.0f, 500.0f,  -50.0f);
        Vector3f v7 = new Vector3f( 250.0f,   0.0f, -250.0f);
        Vector3f vt = new Vector3f(   0.0f, 700.0f,    0.0f);

        Node n = new Node("OuterBox");
        float tc[] = new float[8];
        tc[0] = 0.0f; tc[1] = 20.0f; tc[2] = 20.0f; tc[3] = 20.0f;
        tc[4] = 20; tc[5] = 0.0f; tc[6] = 0.0f; tc[7] = 0.0f;
        n.attachChild(createWall("Floor", v0, v3, v7, v4, "terr_dirt_D.jpg", "terr_dirt_N.jpg", tc));
        //n.attachChild(createWall("Floor", v0, v3, v7, v4, "bldg_brick_002.png", "bldg_brick_N_002.png", tc));

        tc = new float[6];
        tc[0] = 0.0f; tc[1] = 0.0f; tc[2] = 5.0f; tc[3] = 0.0f; tc[4] = 2.5f; tc[5] = 5.0f;
        n.attachChild(createWall("Wall 1", v0, v4, vt, "bldg_brick_001.png", "bldg_brick_N.png", tc));
        n.attachChild(createWall("Wall 2", v4, v7, vt, "bldg_brick_001.png", "bldg_brick_N.png", tc));
        n.attachChild(createWall("Wall 3", v7, v3, vt, "bldg_brick_001.png", "bldg_brick_N.png", tc));
        n.attachChild(createWall("Wall 4", v3, v0, vt, "bldg_brick_001.png", "bldg_brick_N.png", tc));

        tc = new float[8];
        tc[0] = 0.0f; tc[1] = 1; tc[2] = 0.0f; tc[3] = 0.0f;
        tc[4] = 1; tc[5] = 0.0f; tc[6] = 1; tc[7] = 1;
        roof = createWall("Roof", v1, v5, v6, v2, "bldg_brick_001.png", "bldg_brick_N.png", tc);
        n.attachChild(roof);

        CullState cs = (CullState)wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
        cs.setCullFace(CullState.Face.None);
        n.getChild(5).setRenderState(cs);

        ZBufferState zbuf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        n.setRenderState(zbuf);

        RenderComponent rc = wm.getRenderManager().createRenderComponent(n);
        rc.setLightingEnabled(false);
        e.addComponent(RenderComponent.class, rc);

        JMECollisionComponent cc = collisionSystem.createCollisionComponent(n);
        e.addComponent(JMECollisionComponent.class, cc);

        wm.addEntity(e);
    }

    private Geometry createWall(String name, Vector3f v1, Vector3f v2, Vector3f v3, String textureName, String normalMap,
            float[] tc) {
        URL url = null;
        Texture texture = null;

        Vector3f n = new Vector3f();
        Vector3f vec1 = new Vector3f(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
        Vector3f vec2 = new Vector3f(v3.x - v2.x, v3.y - v2.y, v3.z - v2.z);
        vec2.cross(vec1, n);
        n.normalizeLocal();
        //System.out.println(name + " normal: " + n);

        Vector3f[] vdata = new Vector3f[3];
        vdata[0] = v1; vdata[1] = v2; vdata[2] = v3;
        FloatBuffer vbuf = BufferUtils.createFloatBuffer(vdata);

        int indicies[] = new int[] { 0, 1, 2 };
        IntBuffer ibuf = BufferUtils.createIntBuffer(indicies);

        Vector3f[] ndata = new Vector3f[3];
        ndata[0] = n; ndata[1] = n; ndata[2] = n;
        FloatBuffer nbuf = BufferUtils.createFloatBuffer(ndata);

        FloatBuffer tbuf = BufferUtils.createVector2Buffer(3);
        tbuf.put(tc);

        TriMesh geo = new TriMesh(name);
        geo.setVertexCount(3);
        geo.setVertexBuffer(vbuf);
        geo.setNormalBuffer(nbuf);
        geo.setTextureCoords(new TexCoords(tbuf));
        geo.setTriangleQuantity(1);
        geo.setIndexBuffer(ibuf);

        BoundingBox bbox = new BoundingBox();
        bbox.computeFromPoints(vbuf);
        geo.setModelBound(bbox);
        //createCollisionPlane(geo);

        //System.out.println("BBOX for " + name + ": " + bbox);

        TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
        try {
            url = new URL(urlpath + textureName);
            texture = TextureManager.loadTexture(url,
                    Texture.MinificationFilter.Trilinear,
                    Texture.MagnificationFilter.Bilinear);
            texture.setWrap(Texture.WrapMode.Repeat);
        } catch (MalformedURLException e) {
            System.out.println(e);
        }
        ts.setTexture(texture, 0);
        ts.setEnabled(true);

        if (normalMap != null) {
            try {
                url = new URL(urlpath + normalMap);
                texture = TextureManager.loadTexture(url,
                        Texture.MinificationFilter.Trilinear,
                        Texture.MagnificationFilter.Bilinear);
                texture.setWrap(Texture.WrapMode.Repeat);
            } catch (MalformedURLException e) {
                System.out.println(e);
            }
            ts.setTexture(texture, 1);
            TangentBinormalGenerator.generate(geo);
            PortalShader shader = new PortalShader(wm);
            shader.applyToGeometry(geo);
        }

        geo.setRenderState(ts);
        CullState cs = (CullState)wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
        cs.setCullFace(CullState.Face.Back);
        geo.setRenderState(cs);

        return (geo);
    }

    private Geometry createWall(String name, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, String textureName, String normalMap,
            float[] tc) {
        URL url = null;
        Texture texture = null;
        
        Vector3f n = new Vector3f();
        Vector3f vec1 = new Vector3f(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
        Vector3f vec2 = new Vector3f(v3.x - v2.x, v3.y - v2.y, v3.z - v2.z);
        vec2.cross(vec1, n);
        n.normalizeLocal();
        //System.out.println(name + " normal: " + n);
        
        Vector3f[] vdata = new Vector3f[4];
        vdata[0] = v1; vdata[1] = v2; vdata[2] = v3; vdata[3] = v4;
        FloatBuffer vbuf = BufferUtils.createFloatBuffer(vdata);

        int indicies[] = new int[] { 0, 1, 2, 2, 3, 0 };
        IntBuffer ibuf = BufferUtils.createIntBuffer(indicies);

        Vector3f[] ndata = new Vector3f[4];
        ndata[0] = n; ndata[1] = n; ndata[2] = n; ndata[3] = n;
        FloatBuffer nbuf = BufferUtils.createFloatBuffer(ndata);
        
        FloatBuffer tbuf = BufferUtils.createVector2Buffer(4);
        tbuf.put(tc);

        TriMesh geo = new TriMesh(name);
        geo.setVertexCount(4);
        geo.setVertexBuffer(vbuf);
        geo.setNormalBuffer(nbuf);
        geo.setTextureCoords(new TexCoords(tbuf));
        geo.setTriangleQuantity(2);
        geo.setIndexBuffer(ibuf);

        BoundingBox bbox = new BoundingBox();
        bbox.computeFromPoints(vbuf);
        geo.setModelBound(bbox);

        //System.out.println("BBOX for " + name + ": " + bbox);
        if (name.equals("Roof")) {
            createCollisionPlane(geo);
        }
        
        TextureState ts = (TextureState) wm.getRenderManager().createRendererState(RenderState.StateType.Texture);
        try {
            url = new URL(urlpath + textureName);
            texture = TextureManager.loadTexture(url,
                    Texture.MinificationFilter.Trilinear,
                    Texture.MagnificationFilter.Bilinear);
            texture.setWrap(Texture.WrapMode.Repeat);
        } catch (MalformedURLException e) {
            System.out.println(e);
        }
        ts.setTexture(texture, 0);
        ts.setEnabled(true);

        if (normalMap != null) {
            try {
                url = new URL(urlpath + normalMap);
                texture = TextureManager.loadTexture(url,
                        Texture.MinificationFilter.Trilinear,
                        Texture.MagnificationFilter.Bilinear);
                texture.setWrap(Texture.WrapMode.Repeat);
            } catch (MalformedURLException e) {
                System.out.println(e);
            }
            ts.setTexture(texture, 1);
            TangentBinormalGenerator.generate(geo);
            PortalShader shader = new PortalShader(wm);
            shader.applyToGeometry(geo);
        }

        geo.setRenderState(ts);
        CullState cs = (CullState)wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
        cs.setCullFace(CullState.Face.Back);
        geo.setRenderState(cs);

        return (geo);
    }

    private void createCollisionPlane(TriMesh mesh) {
        JBulletCollisionComponent cc = null;
        JBulletPhysicsComponent pc = null;
        Entity e = null;

        // Create the ground plane
        //CollisionShape groundShape = new StaticPlaneShape(new javax.vecmath.Vector3f(normal.x, normal.y, normal.z), d);
        cc = jBcollisionSystem.createCollisionComponent(mesh);
        //pc = jBphysicsSystem.createPhysicsComponent(cc);
        e = new Entity("Ground Plane");
        e.addComponent(JBulletCollisionComponent.class, cc);
        //e.addComponent(JBulletPhysicsComponent.class, pc);
        wm.addEntity(e);
    }

    private void createEntry(String name, WorldManager wm, Vector3f trans, Quaternion rot) {
        Vector3f v0 = new Vector3f(-10.0f,  0.0f,   0.0f);
        Vector3f v1 = new Vector3f(-10.0f, 25.0f,   0.0f);
        Vector3f v2 = new Vector3f( 10.0f, 25.0f,   0.0f);
        Vector3f v3 = new Vector3f( 10.0f,  0.0f,   0.0f);
        Vector3f v4 = new Vector3f(-10.0f,  0.0f, -40.0f);
        Vector3f v5 = new Vector3f(-10.0f, 25.0f, -40.0f);
        Vector3f v6 = new Vector3f( 10.0f, 25.0f, -40.0f);
        Vector3f v7 = new Vector3f( 10.0f,  0.0f, -40.0f);
        Vector3f v8 = new Vector3f(  0.0f, 30.0f,   0.0f);
        Vector3f v9 = new Vector3f(  0.0f, 30.0f, -40.0f);

        Entity e = new Entity(name + " entity");
        Node n = new Node(name + " node");
        float[] tc = new float[8];
        tc[0] = 0.0f; tc[1] = 0.0f; tc[2] = 2.0f; tc[3] = 0.0f;
        tc[4] = 2.0f; tc[5] = 2.0f; tc[6] = 0.0f; tc[7] = 2.0f;
        //n.attachChild(createWall(v0, v4, v7, v3, "bldg_brick_002.png"));
        n.attachChild(createWall(name + " floor", v4, v0, v1, v5, "bldg_brick_002.png", null, tc));
        //n.attachChild(createWall(v4, v5, v6, v7, "bldg_brick_002.png"));
        n.attachChild(createWall(name + " wall1", v3, v7, v6, v2, "bldg_brick_002.png", null, tc));
        n.attachChild(createWall(name + " wall2", v0, v3, v2, v1, "bldg_brick_002.png", null, tc));
        n.attachChild(createWall(name + " roof1", v5, v1, v8, v9, "bldg_brick_002.png", null, tc));
        n.attachChild(createWall(name + " roof2", v2, v6, v9, v8, "bldg_brick_002.png", null, tc));
        tc = new float[6];
        tc[0] = 0.0f; tc[1] = 0.0f; tc[2] = 2.0f; tc[3] = 0.0f; tc[4] = 1; tc[5] = 0.4f;
        n.attachChild(createWall(name + " gable", v1, v2, v8, "bldg_brick_002.png", null, tc));
        //n.attachChild(createWall(v6, v5, v9, "bldg_brick_002.png"));

        ZBufferState zbuf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        n.setRenderState(zbuf);

        n.setLocalRotation(rot.clone());
        n.setLocalTranslation(trans.x, trans.y, trans.z);

        RenderComponent rc = wm.getRenderManager().createRenderComponent(n);
        rc.setLightingEnabled(false);
        e.addComponent(RenderComponent.class, rc);

        JMECollisionComponent cc = collisionSystem.createCollisionComponent(n);
        e.addComponent(JMECollisionComponent.class, cc);

        wm.addEntity(e);
    }

    private void createLedges() {
        createLedge(new Vector3f(0.0f, 100.0f, 0.0f), 350.0f);
        createLedge(new Vector3f(0.0f, 200.0f, 0.0f), 275.0f);
        createLedge(new Vector3f(0.0f, 300.0f, 0.0f), 200.0f);
        createLedge(new Vector3f(0.0f, 400.0f, 0.0f), 125.0f);
    }

    private void createLedge(Vector3f center, float extent) {
        Vector3f lc = new Vector3f();
        float depth = 50.0f;
        float lHeight = 10.0f;
        float hExt = extent/2.0f;

        Vector3f trans = new Vector3f(center.x, center.y, center.z + hExt + depth/2.0f);
        Quaternion rot = new Quaternion();
        rot.fromAngleAxis((float)Math.toRadians(180.0), new Vector3f(0.0f, 1.0f, 0.0f));
        createShelf("Ledge1", lc, extent + 2.0f*depth, lHeight, depth, trans, rot);

        trans = new Vector3f(center.x, center.y, center.z - (hExt + depth/2.0f));
        rot = new Quaternion();
        createShelf("Ledge2", lc, extent + 2.0f*depth, lHeight, depth, trans, rot);

        trans = new Vector3f(center.x - (hExt + depth/2.0f), center.y, center.z);
        rot = new Quaternion();
        rot.fromAngleAxis((float)Math.toRadians(90.0), new Vector3f(0.0f, 1.0f, 0.0f));
        createShelf("Ledge3", lc, extent, lHeight, depth, trans, rot);

        trans = new Vector3f(center.x + (hExt + depth/2.0f), center.y, center.z);
        rot = new Quaternion();
        rot.fromAngleAxis((float)Math.toRadians(-90.0), new Vector3f(0.0f, 1.0f, 0.0f));
        createShelf("Ledge3", lc, extent, lHeight, depth, trans, rot);
    }

    private void createShelf(String name, Vector3f c, float xext, float yext, float zext, Vector3f trans, Quaternion rot) {
        float hxext = xext/2.0f;
        float hyext = yext/2.0f;
        float hzext = zext/2.0f;
        Vector3f v0 = new Vector3f (c.x - hxext, c.y - hyext, c.z + hzext);
        Vector3f v1 = new Vector3f (c.x - hxext, c.y - hyext, c.z - hzext);
        Vector3f v2 = new Vector3f (c.x - hxext, c.y + hyext, c.z - hzext);
        Vector3f v3 = new Vector3f (c.x - hxext, c.y + hyext, c.z + hzext);
        Vector3f v4 = new Vector3f (c.x + hxext, c.y - hyext, c.z + hzext);
        Vector3f v5 = new Vector3f (c.x + hxext, c.y - hyext, c.z - hzext);
        Vector3f v6 = new Vector3f (c.x + hxext, c.y + hyext, c.z - hzext);
        Vector3f v7 = new Vector3f (c.x + hxext, c.y + hyext, c.z + hzext);

        Entity e = new Entity(name + " entity");
        Node n = new Node(name + " node");
        float[] tc = new float[8];
        tc[0] = 0.0f; tc[1] = 0.0f; tc[2] = 20.0f; tc[3] = 0.0f;
        tc[4] = 20.0f; tc[5] = 7.0f; tc[6] = 0.0f; tc[7] = 7.0f;
        n.attachChild(createWall(name + " shelf", v1, v5, v4, v0, "terr_sidewalk_002.png", null, tc));
        n.attachChild(createWall(name + " shelf", v3, v7, v6, v2, "terr_sidewalk_002.png", null, tc));
        tc[0] = 0.0f; tc[1] = 0.0f; tc[2] = 15.0f; tc[3] = 0.0f;
        tc[4] = 15.0f; tc[5] = 1.0f; tc[6] = 0.0f; tc[7] = 1.0f;
        n.attachChild(createWall(name + " shelf", v0, v4, v7, v3, "terr_sidewalk_002.png", null, tc));

        ZBufferState zbuf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        n.setRenderState(zbuf);

        n.setLocalTranslation(trans.x, trans.y, trans.z);
        n.setLocalRotation(rot);
        

        RenderComponent rc = wm.getRenderManager().createRenderComponent(n);
        rc.setLightingEnabled(false);
        e.addComponent(RenderComponent.class, rc);

        JMECollisionComponent cc = collisionSystem.createCollisionComponent(n);
        e.addComponent(JMECollisionComponent.class, cc);

        wm.addEntity(e);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PortalTest worldBuilder = new PortalTest(args);
        
    }
    
    /**
     * Process any command line args
     */
    private void processArgs(String[] args) {
        for (int i=0; i<args.length;i++) {
            if (args[i].equals("-fps")) {
                desiredFrameRate = Integer.parseInt(args[i+1]);
                System.out.println("DesiredFrameRate: " + desiredFrameRate);
                i++;
            }
        }
    }
    
    /**
     * Create all of the Swing windows - and the 3D window
     */
    private void createUI(WorldManager wm) {             
        frame = new SwingFrame(wm);
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame
        frame.setVisible(true);
    }
    
    class SwingFrame extends JFrame implements FrameRateListener, ActionListener, ResourceLocator {

        JPanel contentPane;
        JPanel menuPanel = new JPanel();
        JPanel canvasPanel = new JPanel();
        JPanel optionsPanel = new JPanel();
        JPanel statusPanel = new JPanel();
        JLabel fpsLabel = new JLabel("FPS: ");
        
        JToggleButton coordButton = new JToggleButton("Coords", true);
        JToggleButton gridButton = new JToggleButton("Physics", true);
        JMenuItem loadItem = null;
        JMenuItem exitItem = null;
        JMenuItem createTeapotItem = null;

        String textureSubdir = "file:/Users/runner/Desktop/OFFTWG/a_great_epyptian_temple/images/";
        String textureSubdirName = "/Users/runner/Desktop/OFFTWG/a_great_epyptian_temple/images/";


        // Construct the frame
        public SwingFrame(WorldManager wm) {
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    dispose();
                    // TODO: Real cleanup
                    System.exit(0);
                }
            });

            contentPane = (JPanel) this.getContentPane();
            contentPane.setLayout(new BorderLayout());
            
            // The Menu Bar
            menuPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JMenuBar menuBar = new JMenuBar();
            
            // File Menu
            JMenu fileMenu = new JMenu("File");
            exitItem = new JMenuItem("Exit");
            exitItem.addActionListener(this);
            fileMenu.add(exitItem);
            menuBar.add(fileMenu);
                      
            menuPanel.add(menuBar);
            contentPane.add(menuPanel, BorderLayout.NORTH);
            
            // The Rendering Canvas
            rb = wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.ONSCREEN, width, height);
            ((OnscreenRenderBuffer)rb).setClearEnable(false);
            ((OnscreenRenderBuffer)rb).setSwapEnable(false);
            wm.getRenderManager().addRenderBuffer(rb);
            canvas = ((OnscreenRenderBuffer)rb).getCanvas();
            canvas.setVisible(true);
            canvas.setBounds(0, 0, width, height);
            wm.getRenderManager().setFrameRateListener(this, 100);
            canvasPanel.setLayout(new BorderLayout());
            canvasPanel.add(canvas);
            contentPane.add(canvasPanel, BorderLayout.CENTER);
            
            // The options panel
            //optionsPanel.setLayout(new GridBagLayout());
            
            //coordButton.addActionListener(this);
            //optionsPanel.add(coordButton);
          
            //gridButton.addActionListener(this);
            //optionsPanel.add(gridButton);
            
            //contentPane.add(optionsPanel, BorderLayout.WEST);
            
            // The status panel
            statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            statusPanel.add(fpsLabel);
            contentPane.add(statusPanel, BorderLayout.SOUTH);

            pack();
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, this);
        }
        
        /**
         * Listen for frame rate updates
         */
        public void currentFramerate(float framerate) {
            fpsLabel.setText("FPS: " + framerate);
        }

        void loadFile(String filename, boolean normalMap) {
            FileInputStream fileStream = null;
            ArrayList transpList = new ArrayList();

            System.out.println("You chose to open this file: " + filename);
            try {
                fileStream = new FileInputStream(filename);
            } catch (FileNotFoundException ex) {
                System.out.println(ex);
            }


            //Commented out by JagWire on 4/20/2011
            // Now load the model
//            ColladaImporter.load(fileStream, "Model");
//            Node model = ColladaImporter.getModel();
//            model.setLocalTranslation(-700.0f, -20.0f, 2000.0f);
//            Quaternion rot = new Quaternion();
//            rot.fromAngleAxis(-(float)(Math.PI/2.0), new Vector3f(1.0f, 0.0f, 0.0f));
//            model.setLocalRotation(rot.clone());
//            model.setLocalScale(0.4f);
////            transpList.clear();
////            parseModel(0, model, normalMap, transpList);
////            for (int i=0; i<transpList.size(); i++) {
////                TriMesh tm = (TriMesh) transpList.get(i);
////                Node n = org.jdesktop.mtgame.util.Geometry.explodeIntoSpatials(wm, tm);
////                Node p = tm.getParent();
////                tm.removeFromParent();;
////                p.attachChild(n);
////            }
//            addModel(model);
        }

        public URL locateResource(String resourceName) {
            URL url = null;

            System.out.println("Looking for: " + resourceName);
            try {
                if (resourceName.contains(textureSubdirName)) {
                    // We already resolved this one.
                    url = new URL("file:" + resourceName);
                } else {
                    url = new URL(textureSubdir + resourceName);
                }
                //System.out.println("TEXTURE: " + url);
            } catch (MalformedURLException e) {
                System.out.println(e);
            }

            return (url);
        }

        /**
         * Add a model to be visualized
         */
        private void addModel(Node model) {
            Node modelRoot = new Node("Model");
            
            
            ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
            buf.setEnabled(true);
            buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
            modelRoot.setRenderState(buf);
            
            System.out.println("Adding: " + model);
            modelRoot.attachChild(model);
            models.add(modelRoot);
            
            Entity e = new Entity("Model");
            RenderComponent sc = wm.getRenderManager().createRenderComponent(modelRoot);
            e.addComponent(RenderComponent.class, sc);
            wm.addEntity(e);              
        }
        
            

    
        /**
         * The method which gets the state change from the buttons
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == coordButton) {
                if (coordsOn) {
                    coordsOn = false;
                    wm.removeEntity(axis);
                    System.out.println("Turning Coordinates Off");
                } else {
                    coordsOn = true;
                    wm.addEntity(axis);
                    System.out.println("Turning Coordinates On");
                }
            }
            
            if (e.getSource() == gridButton) {
                if (gridOn) {
                    gridOn = false;
                    System.out.println("Turning Physics Off");
                } else {
                    gridOn = true;
                    System.out.println("Turning Physics On");
                }
                jBphysicsSystem.setStarted(gridOn);
            }
            
            if (e.getSource() == loadItem) {
                FileInputStream fileStream = null;
                JFileChooser chooser = new JFileChooser();
                int returnVal = chooser.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    System.out.println("You chose to open this file: " +
                            chooser.getSelectedFile().getName());
                    try {
                        fileStream = new FileInputStream(chooser.getSelectedFile());
                    } catch (FileNotFoundException ex) {
                        System.out.println(ex);
                    }


                    //commented out by JagWire on 4/20/2011
                    // Now load the model
                    //ColladaImporter.load(fileStream, "Model");
                    //Node model = ColladaImporter.getModel();
                    //addModel(model);
                }
            }
            
            if (e.getSource() == exitItem) {
                System.exit(1);
            }
            
            if (e.getSource() == createTeapotItem) {
                
            }
        }
    }
   
    void createSectorAndPortals(WorldManager wm, PortalBufferController pbc) {
        BoundingBox bbox = new BoundingBox(new Vector3f(), 1000.0f, 1000.0f, 1000.0f);
        Sector mySector = new Sector(bbox);

        ZBufferState zbuf = (ZBufferState)wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);

        Quad p1Quad = new Quad("P1 Quad", 12, 20);
        BoundingBox pbbox = new BoundingBox(new Vector3f(), 6.0f, 10.0f, 2.0f);
        p1Quad.setModelBound(pbbox);
        p1Quad.setRenderState(zbuf);
        Vector3f p1EnterLoc = new Vector3f(0.0f, 12.0f, -214.0f);
        Vector3f p1EnterDir = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f p1EnterUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f p1EnterLeft = new Vector3f(-1.0f, 0.0f, 0.0f);
        Vector3f p1ExitLoc = new Vector3f(214.0f, 12.0f, 0.0f);
        Vector3f p1ExitDir = new Vector3f(-1.0f, 0.0f, 0.0f);
        Vector3f p1ExitUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f p1ExitLeft = new Vector3f(0.0f, 0.0f, 1.0f);
        p1Quad.setLocalTranslation(p1EnterLoc.x, p1EnterLoc.y, p1EnterLoc.z);

        p1 = new Portal(p1Quad, p1EnterLoc, p1EnterDir, p1EnterUp,
                               p1EnterLeft, p1ExitLoc, p1ExitDir, p1ExitUp,
                               p1ExitLeft, mySector, mySector);

        Quad p2Quad = new Quad("P2 Quad", 12, 20);
        BoundingBox p2bbox = new BoundingBox(new Vector3f(), 6.0f, 10.0f, 2.0f);
        p2Quad.setModelBound(p2bbox);
        p2Quad.setRenderState(zbuf);
        Vector3f p2EnterLoc = new Vector3f(214.0f, 12.0f, 0.0f);
        Vector3f p2EnterDir = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f p2EnterUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f p2EnterLeft = new Vector3f(0.0f, 0.0f,-1.0f);
        Vector3f p2ExitLoc = new Vector3f(0.0f, 12.0f, -214.0f);
        Vector3f p2ExitDir = new Vector3f(0.0f, 0.0f, 1.0f);
        Vector3f p2ExitUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f p2ExitLeft = new Vector3f(1.0f, 0.0f, 0.0f);
        p2Quad.setLocalTranslation(p2EnterLoc.x, p2EnterLoc.y, p2EnterLoc.z);
        Quaternion rot = new Quaternion();
        rot.fromAngleAxis(-(float)(Math.PI/2.0), p2EnterUp);
        p2Quad.setLocalRotation(rot.clone());

        p2 = new Portal(p2Quad, p2EnterLoc, p2EnterDir, p2EnterUp,
                               p2EnterLeft, p2ExitLoc, p2ExitDir, p2ExitUp,
                               p2ExitLeft, mySector, mySector);
        mySector.addPortal(p1);
        mySector.addPortal(p2);
        pbc.addSector(mySector);

        wm.addToUpdateList(p1Quad);
        wm.addToUpdateList(p2Quad);

        Node n = new Node("Portals");
        n.attachChild(p1Quad);
        p1Quad.setGlowEnabled(true);
        p1Quad.setGlowColor(new ColorRGBA(0.0f, 0.0f, 1.0f, 0.5f));
        n.attachChild(p2Quad);
        p2Quad.setGlowEnabled(true);
        p2Quad.setGlowColor(new ColorRGBA(1.0f, 0.65f, 0.0f, 0.5f));
        RenderComponent rc = wm.getRenderManager().createRenderComponent(n);


        Entity qe = new Entity("");
        float[] times = new float[2];
        times[0] = 0.0f; times[1] = 0.75f;
        animationProcessor = new PortalAnimationProcessor(wm, qe, null, null, times);
        qe.addComponent(PortalAnimationProcessor.class, animationProcessor);
        qe.addComponent(RenderComponent.class, rc);
        wm.addEntity(qe);
    }

    public class PortalProcessor extends AWTEventProcessorComponent {

        /**
         * The arming conditions for this processor
         */
        private ProcessorArmingCollection collection = null;
        /**
         * First, some common variables
         */
        private int lastMouseX = -1;
        private int lastMouseY = -1;
        /**
         * The cumulative rotation in Y and X
         */
        private float rotY = 0.0f;
        private float rotX = 0.0f;
        /**
         * This scales each change in X and Y
         */
        private float scaleX = 0.7f;
        private float scaleY = 0.7f;
        private float walkInc = 0.5f;
        private float fallInc = 0.5f;

        /**
         * States for movement
         */
        private static final int STOPPED = 0;
        private static final int WALKING_FORWARD = 1;
        private static final int WALKING_BACK = 2;
        private static final int STRAFE_LEFT = 3;
        private static final int STRAFE_RIGHT = 4;
        /**
         * Our current state
         */
        private int state = STOPPED;
        /**
         * Our current position
         */
        private Vector3f position = new Vector3f(0.0f, 11.0f, -30.0f);
        private Vector3f potentialPos = new Vector3f(0.0f, 11.0f, -30.0f);

        /**
         * The Y Axis
         */
        private Vector3f yDir = new Vector3f(0.0f, 1.0f, 0.0f);

        /**
         * Our current forward direction
         */
        private Vector3f fwdDirection = new Vector3f(0.0f, 0.0f, 1.0f);
        private Vector3f rotatedFwdDirection = new Vector3f();
        private Vector3f lookDirection = new Vector3f();

        /**
         * Our current side direction
         */
        private Vector3f sideDirection = new Vector3f(1.0f, 0.0f, 0.0f);
        private Vector3f rotatedSideDirection = new Vector3f();

        /**
         * The quaternion for our rotations
         */
        private Quaternion quaternion = new Quaternion();
        /**
         * This is used to keep the direction rotated
         */
        private Matrix3f directionRotation = new Matrix3f();
        /**
         * The Node to modify
         */
        private Node target = null;

        /**
         * The light node for the camera
         */
        LightNode lightNode = null;
        
        /**
         * A Ray used for collision
         */
        private Ray ray = new Ray();
        private Ray heightRay = new Ray();

        /**
         * The WorldManager
         */
        private WorldManager worldManager = null;

        private Vector3f[] triData = new Vector3f[3];
        private Vector3f[] triData2 = new Vector3f[3];
        private Box avatar = null;
        private TriangleCollisionResults tcr = new TriangleCollisionResults();
        private float alpha = 0.5f;
        private float alphaInc = 0.01f;
        private Vector3f downVec = new Vector3f(0.0f, -1.0f, 0.0f);
        private BoundingBox triggerBox = new BoundingBox(new Vector3f(0.0f, 600.0f, 0.0f), 100.0f, 100.0f, 100.0f);
        private boolean removeRoof = false;

        /**
         * The default constructor
         */
        public PortalProcessor(AWTInputComponent listener, Node cameraNode,
                WorldManager wm, Entity myEntity) {
            super(listener);
            target = cameraNode;
            worldManager = wm;

            collection = new ProcessorArmingCollection(this);
            collection.addCondition(new AwtEventCondition(this));
            collection.addCondition(new NewFrameCondition(this));


            ray = new Ray();
            ray.origin = position;
            ray.direction = lookDirection;

            triData[0] = new Vector3f();
            triData[1] = new Vector3f();
            triData[2] = new Vector3f();
            triData2[0] = new Vector3f();
            triData2[1] = new Vector3f();
            triData2[2] = new Vector3f();

            avatar = new Box("Avatar", new Vector3f(), 1.0f, 1.0f, 1.0f);
            BoundingBox collisionBox = new BoundingBox(new Vector3f(), 1.0f, 1.0f, 1.0f);
            avatar.setModelBound(collisionBox);
            avatar.setLocalTranslation(potentialPos.x, potentialPos.y, potentialPos.z);
            avatar.setLocalRotation(quaternion.clone());
        }

        public void initialize() {
            setArmingCondition(collection);
        }

        public void compute(ProcessorArmingCollection collection) {
            Object[] events = getEvents();
            boolean updateRotations = false;

            for (int i = 0; i < events.length; i++) {
                if (events[i] instanceof MouseEvent) {
                    MouseEvent me = (MouseEvent) events[i];
                    if (me.getID() == MouseEvent.MOUSE_MOVED) {
                        processRotations(me);
                        updateRotations = true;
                    } else if (me.getID() == MouseEvent.MOUSE_CLICKED) {
                        if (me.getButton() == MouseEvent.BUTTON1) {
                            shootPortal(p1, p2);
                        } else if (me.getButton() == MouseEvent.BUTTON3) {
                            shootPortal(p2, p1);
                        }
                    }
                } else if (events[i] instanceof KeyEvent) {
                    KeyEvent ke = (KeyEvent) events[i];
                    processKeyEvent(ke);
                }
            }

            if (updateRotations) {
                updateRotations();
            }

            updatePosition();
            updatePortalAlpha();
        }

        private void updateRotations() {
            directionRotation.fromAngleAxis(rotY * (float) Math.PI / 180.0f, yDir);
            directionRotation.mult(fwdDirection, rotatedFwdDirection);
            directionRotation.mult(sideDirection, rotatedSideDirection);
            //System.out.println("Forward: " + rotatedFwdDirection);
            quaternion.fromAngles(rotX * (float) Math.PI / 180.0f, rotY * (float) Math.PI / 180.0f, 0.0f);
            quaternion.mult(fwdDirection, lookDirection);
        }

        private void processRotations(MouseEvent me) {
            int deltaX = 0;
            int deltaY = 0;
            int currentX = 0;
            int currentY = 0;
            currentX = me.getX();
            currentY = me.getY();

            if (lastMouseX == -1) {
                // First time through, just initialize
                lastMouseX = currentX;
                lastMouseY = currentY;
            } else {
                deltaX = currentX - lastMouseX;
                deltaY = currentY - lastMouseY;
                deltaX = -deltaX;

                rotY += (deltaX * scaleX);
                rotX += (deltaY * scaleY);
                if (rotX > 60.0f) {
                    rotX = 60.0f;
                } else if (rotX < -60.0f) {
                    rotX = -60.0f;
                }
                lastMouseX = currentX;
                lastMouseY = currentY;
            }
        }

        private void processKeyEvent(KeyEvent ke) {
            if (ke.getID() == KeyEvent.KEY_PRESSED) {
                if (ke.getKeyCode() == KeyEvent.VK_W) {
                    state = WALKING_FORWARD;
                }
                if (ke.getKeyCode() == KeyEvent.VK_S) {
                    state = WALKING_BACK;
                }
                if (ke.getKeyCode() == KeyEvent.VK_A) {
                    state = STRAFE_LEFT;
                }
                if (ke.getKeyCode() == KeyEvent.VK_D) {
                    state = STRAFE_RIGHT;
                }
                if (ke.getKeyCode() == KeyEvent.VK_EQUALS) {
                    System.out.println(position);
                }
            }
            if (ke.getID() == KeyEvent.KEY_RELEASED) {
                if (ke.getKeyCode() == KeyEvent.VK_W ||
                        ke.getKeyCode() == KeyEvent.VK_S ||
                        ke.getKeyCode() == KeyEvent.VK_A ||
                        ke.getKeyCode() == KeyEvent.VK_D) {
                    state = STOPPED;
                }

                if (ke.getKeyCode() == KeyEvent.VK_SPACE &&
                    triggerBox.contains(position)) {
                    removeRoof = true;
                    jBphysicsSystem.setStarted(true);
                }
            }
        }

        private void updatePosition() {
            switch (state) {
                case WALKING_FORWARD:
                    potentialPos.x = position.x + (walkInc * rotatedFwdDirection.x);
                    potentialPos.y = position.y + (walkInc * rotatedFwdDirection.y);
                    potentialPos.z = position.z + (walkInc * rotatedFwdDirection.z);
                    break;
                case WALKING_BACK:
                    potentialPos.x = position.x - (walkInc * rotatedFwdDirection.x);
                    potentialPos.y = position.y - (walkInc * rotatedFwdDirection.y);
                    potentialPos.z = position.z - (walkInc * rotatedFwdDirection.z);
                    break;
                case STRAFE_LEFT:
                    potentialPos.x = position.x + (walkInc * rotatedSideDirection.x);
                    potentialPos.y = position.y + (walkInc * rotatedSideDirection.y);
                    potentialPos.z = position.z + (walkInc * rotatedSideDirection.z);
                    break;
                case STRAFE_RIGHT:
                    potentialPos.x = position.x - (walkInc * rotatedSideDirection.x);
                    potentialPos.y = position.y - (walkInc * rotatedSideDirection.y);
                    potentialPos.z = position.z - (walkInc * rotatedSideDirection.z);
                    break;
            }

//            System.out.println("Avatar Bounds: " + avatar.getWorldBound());
            tcr.clear();
            boolean collision = false;
            avatar.setLocalTranslation(potentialPos.x, potentialPos.y, potentialPos.z);
            avatar.setLocalRotation(quaternion.clone());
            avatar.updateGeometricState(0, true);

            collisionSystem.findCollisions(avatar, tcr);
            for (int i=0; i<tcr.getNumber(); i++) {
                ArrayList<Integer> tris = tcr.getCollisionData(i).getSourceTris();
                if (tris.size() != 0) {
                    collision = true;
                    //TriMesh mesh = (TriMesh)tcr.getCollisionData(i).getSourceMesh();
                    //mesh.getTriangle(tris.get(0).intValue(), triData2);
                    //computeCollisionResponse(position, potentialPos, rotatedFwdDirection, triData2, walkInc);
                    break;
                }
            }
            checkGround(potentialPos);
            if (!collision || warping) {
                
                position.set(potentialPos);
            }

            if (p1 != null && p1.getGeometry().getWorldBound() != null &&
                p1.getGeometry().getWorldBound().contains(position)) {
                warpToPortal(p1);
                //System.out.println("Intersecting Portal 1");
            } else if (p2 != null && p2.getGeometry().getWorldBound() != null &&
                p2.getGeometry().getWorldBound().contains(position)) {
                warpToPortal(p2);
                //System.out.println("Intersecting Portal 2");
            } else {
                // I'm done
                warping = false;
            }
        }

        private void computeCollisionResponse(Vector3f curPos, Vector3f newPos, Vector3f curDir, Vector3f[] colTri, float inc) {
            Vector3f n = new Vector3f();
            Vector3f R = new Vector3f();
            Vector3f w = new Vector3f();
            Vector3f v1 = new Vector3f();
            Vector3f v2 = new Vector3f();
            Vector3f newDir = new Vector3f();
            Vector3f L = curDir.negate();

            colTri[0].subtract(colTri[1], v1);
            colTri[2].subtract(colTri[1], v2);
            v1.normalizeLocal();
            v2.normalizeLocal();
            v2.cross(v1, n);
            n.normalizeLocal();

            // R = 2(N*L)N-L
            float NL2 = L.dot(n) * 2.0f;
            R.scaleAdd(NL2, n, L.negate());

            //System.out.println("L: " + curDir);
            //System.out.println("N: " + n);
            //System.out.println("R: " + R);

            // Now compute the new direction vector
            Vector3f Po = new Vector3f();
            Vector3f Pp = new Vector3f();
            colTri[0].add(R, Po);
            colTri[0].subtract(Po, w);
            //w.negateLocal();
            float d = n.dot(w)/n.length();
            Pp.scaleAdd(d, n, Po);
            Pp.subtract(colTri[0], newDir);
            newDir.normalizeLocal();
            //System.out.println("newDir: " + newDir);

            newPos.x = curPos.x + R.x * inc;
            newPos.z = curPos.z + R.z * inc;
            newPos.y = curPos.y;
        }

        private void checkGround(Vector3f newPos) {
            float yDelta = 10.0f;
            float dy = fallInc;

            heightRay.origin = newPos.clone();
            heightRay.direction = downVec;
            //System.out.println(newPos);
            PickInfo pi = collisionSystem.pickAllWorldRay(heightRay, true, false);
            if (pi.size() != 0) {
                // Grab the first one
                PickDetails pd = pi.get(0);
                if (dy + yDelta > pd.getDistance()) {
                    dy = pd.getDistance() - yDelta;
                }

                newPos.y -= dy;
            } else {
                //System.out.println("NO GROUND!!!");
            }
        }

        private void updatePortalAlpha() {
            alpha += alphaInc;
            if (alpha > 0.8f) {
                alpha = 0.8f;
                alphaInc = -alphaInc;
            }

            if (alpha < 0.2f) {
                alpha = 0.2f;
                alphaInc = -alphaInc;
            }
        }

        /**
         * The commit methods
         */
        public void commit(ProcessorArmingCollection collection) {
            target.setLocalRotation(quaternion.clone());
            target.setLocalTranslation(position.x, position.y, position.z);
            //avatar.setLocalRotation(quaternion.clone());
            //avatar.setLocalTranslation(position.x, position.y, position.z);

            if (p1 != null && p1.getGeometry() != null) {
                ColorRGBA c1 = p1.getGeometry().getGlowColor();
                c1.a = alpha;
                p1.getGeometry().setGlowColor(c1);
                worldManager.addToUpdateList(p1.getGeometry());
            }

            if (p2 != null && p2.getGeometry() != null) {
                ColorRGBA c2 = p2.getGeometry().getGlowColor();
                c2.a = alpha;
                p2.getGeometry().setGlowColor(c2);
                worldManager.addToUpdateList(p2.getGeometry());
            }

            worldManager.addToUpdateList(target);
            if (removeRoof) {
                //roof.removeFromParent();
            }
            //worldManager.addToUpdateList(avatar);
        }

        private void warpToPortal(Portal p) {
            Vector3f zVec = new Vector3f(0.0f, 0.0f, 1.0f);
            if (warping || animating) {
                return;
            } else {
                warping = true;
            }

            Vector3f enterDir = new Vector3f();
            Vector3f exitDir = new Vector3f();

            // Change the position
            p.getEnterCoordinate(null, enterDir, null, null);
            p.getExitCoordinate(position, exitDir, null, null);

            double dx = Math.atan2(enterDir.z, enterDir.x) - Math.atan2(exitDir.z, exitDir.x);
            rotY += Math.toDegrees(dx);
            updateRotations();
        }

        private void shootPortal(Portal src, Portal dst) {
            
            //System.out.println("Shooting Ray: " + ray.origin + ", " + ray.direction);
            PickInfo pi = collisionSystem.pickAllWorldRay(ray, true, false);
            if (pi.size() != 0) {
                // Grab the first one
                PickDetails pd = pi.get(0);
                pd.getTriMesh().getTriangle(pd.getTriIndex(), triData);
                triData[0] = pd.getTriMesh().localToWorld(triData[0], null);
                triData[1] = pd.getTriMesh().localToWorld(triData[1], null);
                triData[2] = pd.getTriMesh().localToWorld(triData[2], null);
                calculateNewPortalCoordinate(src, triData, pd.getPosition(), dst);               
            }
        }

        private void calculateNewPortalCoordinate(Portal p, Vector3f[] dst, Vector3f nLoc, Portal pExit) {
            float heightClearance = 12.0f;
            Vector3f n = new Vector3f();
            Vector3f nUp = new Vector3f();
            Vector3f nLeft = new Vector3f();
            Vector3f v1 = new Vector3f();
            Vector3f v2 = new Vector3f();
            Vector3f pDir = new Vector3f();
            Vector3f pUp = new Vector3f(0.0f, 1.0f, 0.0f);
            Vector3f pLeft = new Vector3f();
            Vector3f w = new Vector3f();
            Vector3f cLoc = new Vector3f();
            Vector3f cDir = new Vector3f();
            Vector3f cUp = new Vector3f();
            Vector3f cLeft = new Vector3f();

            p.getEnterCoordinate(null, pDir, null, pLeft);
            p.getEnterCoordinate(cLoc, cDir, cUp, cLeft);

//            System.out.println("Current Coordinate System: ");
//            System.out.println("Dir: " + pDir);
//            System.out.println("Up: " + pUp);
//            System.out.println("Left: " + pLeft);

            // Create the new normal vector
            dst[0].subtract(dst[1], v1);
            dst[2].subtract(dst[1], v2);
            v1.normalizeLocal();
            v2.normalizeLocal();
            v2.cross(v1, n);
            n.normalizeLocal();

            // Now compute the new Up vector
            Vector3f Po = new Vector3f();
            Vector3f Pp = new Vector3f();
            dst[0].add(pUp, Po);
            dst[0].subtract(Po, w);
            //w.negateLocal();
            float d = n.dot(w)/n.length();
            Pp.scaleAdd(d, n, Po);
            Pp.subtract(dst[0], nUp);
            nUp.normalizeLocal();

            // Finally, get left by doing a cross
            Vector3f enterDir = n.negate();
            nUp.cross(n, nLeft);
            Vector3f enterLeft = nLeft.negate();

//            System.out.println("New Coordinate System: ");
//            System.out.println("Loc: " + nLoc);
//            System.out.println("Dir: " + n);
//            System.out.println("Up: " + nUp);
//            System.out.println("Left: " + nLeft);

            // Nudge the new location in the direction of the normal
            nLoc.scaleAdd(0.1f, n, nLoc);

            // Check if we are a certain height above a floor
            Ray heightRay = new Ray();
            heightRay.origin = nLoc;
            heightRay.direction = nUp.negate();
            PickInfo pi2 = collisionSystem.pickAllWorldRay(heightRay, true, false);
            if (pi2.size() != 0) {
                PickDetails pd2 = pi2.get(0);
                if (pd2.getDistance() < heightClearance) {
                    float nudge = heightClearance - pd2.getDistance();
                    nLoc.scaleAdd(nudge, nUp, nLoc);
                }
            }

            Quaternion endRot = new Quaternion();
            endRot.fromAxes(nLeft, nUp, n);

            animating = true;
            animationProcessor.reset(p, nLoc, endRot);
            animationProcessor.start();

            p.setEnterCoordinate(nLoc, enterDir, nUp, enterLeft);
            pExit.setExitCoordinate(nLoc, n, nUp, nLeft);
        }
    }

    public class PortalAnimationProcessor extends ProcessorComponent {

        /**
         * The arming conditions for this processor
         */
        private ProcessorArmingCollection collection = null;

        /**
         * States for movement
         */
        private static final int STOPPED = 0;
        private static final int RUNNING = 1;

        /**
         * Our current state
         */
        private int state = STOPPED;

        /**
         * The Portals to modify
         */
        private Portal p = null;

        /**
         * The WorldManager
         */
        private WorldManager worldManager = null;

        /**
         * The set of positions, directions, ups, and times
         */
        private Vector3f position = null;
        private Quaternion rotation = null;
        private float[] times = null;

        /**
         * The set of current variables
         */
        private float penetration = 0.0f;
        private long startTime = 0;
        private long totalTime = 0;

        private Object processLock = new Object();

        /**
         * The default constructor
         */
        public PortalAnimationProcessor(WorldManager wm, Entity myEntity, Vector3f positions,
                Quaternion rots, float[] times) {
            worldManager = wm;

            this.position = positions;
            this.rotation = rots;
            this.times = times;
            state = STOPPED;

            collection = new ProcessorArmingCollection(this);
            collection.addCondition(new NewFrameCondition(this));
        }

        public void initialize() {
            setArmingCondition(collection);
        }

        public void compute(ProcessorArmingCollection collection) {
            synchronized (processLock) {
            if (state == RUNNING) {
                totalTime = System.nanoTime() - startTime;
                float flTime = totalTime / 1000000000.0f;

                // Find the right interval
                int startIndex = 0;
                int endIndex = 0;

                for (int i = 0; i < times.length - 1; i++) {
                    if (flTime > times[i] && flTime < times[i + 1]) {
                        startIndex = i;
                        endIndex = i + 1;
                        break;
                    }
                }

                if (startIndex != endIndex) {
                    // Find the percentage
                    float timeSpread = times[endIndex] - times[startIndex];
                    float alpha = flTime - times[startIndex];
                    penetration = alpha / timeSpread;
                } else {
                    animating = false;
                }
            }
            }
        }

        public void start() {
            synchronized (processLock) {
                startTime = System.nanoTime();
                state = RUNNING;
            }
        }

        public void stop() {
            synchronized (processLock) {
                state = STOPPED;
            }
        }

        public void reset(Portal p, Vector3f newPosition, Quaternion newRotation) {
            synchronized (processLock) {
                state = STOPPED;
                this.p = p;
                this.position = newPosition;
                this.rotation = newRotation;
                penetration = 0.0f;
            }
        }

        /**
         * The commit methods
         */
        public void commit(ProcessorArmingCollection collection) {
            if (state == RUNNING && p != null) {
                p.getGeometry().setLocalTranslation(position.x, position.y, position.z);
                p.getGeometry().setLocalRotation(rotation.clone());
                p.getGeometry().setLocalScale(penetration);
                worldManager.addToUpdateList(p.getGeometry());
            }
        }
    }

    public class PortalShader extends Shader {

        /**
         * The vertex and fragment shader
         */
        private static final String vShader =
                //        "attribute vec3 tangent;" +
                //        "attribute vec3 binormal;" +
                "varying vec3 EyeDir;" +
                "varying vec3 LightDir;" +
                "void main(void)" +
                "{" +
                //"        vec3 n = normalize(gl_NormalMatrix * gl_Normal);" +
                //"        vec3 t = normalize(gl_NormalMatrix * gl_SecondaryColor.xyz);" +
                "        vec3 n = normalize(gl_Normal);" +
                "        vec3 t = normalize(gl_SecondaryColor.xyz);" +
                "        vec3 b = normalize(cross(n, t));" +
                "        gl_Position = ftransform();" +
                "        vec3 vVertex = vec3(gl_ModelViewMatrix * gl_Vertex);" +
                "        gl_TexCoord[0] = gl_MultiTexCoord0;" +
                "" +
                "        vec3 v;" +
                "        vec4 lloc = vec4(0.0, 75.0, 0.0, 1.0);" +
                "        vec3 mlloc = vec3(lloc); " +
                //"        vec3 tmpVec = normalize(mlloc - vVertex);" +
                "        vec3 tmpVec = normalize(mlloc - gl_Vertex.xyz);" +
                //"        vec3 tmpVec = normalize(lloc);" +
                //"        mat3 tMat = mat3(t, b, n);" +
                "        v.x = dot(tmpVec, t);" +
                "        v.y = dot(tmpVec, b);" +
                "        v.z = dot(tmpVec, n);" +
                //"        LightDir = normalize(tMat*lloc);" +
                "        LightDir = normalize(v);" +
                "" +
                //"        tmpVec = vVertex;" +
                //"        v.x = dot(tmpVec, t);" +
                //"        v.y = dot(tmpVec, b);" +
                //"        v.z = dot(tmpVec, n);" +
                //"        EyeDir = normalize(v);" +
                "}";
        private static final String fShader =
                "varying vec3 EyeDir;" +
                "varying vec3 LightDir;" +
                "uniform sampler2D DiffuseMapIndex;" +
                "uniform sampler2D NormalMapIndex;" +
                "vec3 FragLocalNormal;" +
                "vec3 finalColor;" +
                "vec3 diffuseColor;" +
                "vec3 specularColor;" +
                "float NdotL;" +
                "float spec;" +
                "vec3 reflectDir;" +
                "void main(void) { " +
                // Do some setup
                "        diffuseColor = texture2D(DiffuseMapIndex, gl_TexCoord[0].st).rgb;" +
                "        FragLocalNormal = normalize(texture2D(NormalMapIndex, gl_TexCoord[0].st).xyz * 2.0 - 1.0);" +
                //"        FragLocalNormal.y = -FragLocalNormal.y;" +
                //"        specularColor = vec3(1.0, 1.0, 1.0);" +
                //"        finalColor = gl_FrontMaterial.ambient.rgb * gl_LightSource[0].ambient.rgb;" +
                // Compute diffuse for light0
                //"        vec3 mlloc = gl_NormalMatrix * lloc; " +
                "        vec3 ld = vec3(0, 0, 1);" +
                "        NdotL = dot(FragLocalNormal, normalize(LightDir));" +
                //"        NdotL = dot(FragLocalNormal, ld);"  +
                "        finalColor = (diffuseColor*NdotL);" +
                //"        finalColor = vec3(NdotL);" +
                // Compte specular for light0
                //"        reflectDir = reflect(LightDir, FragLocalNormal);" +
                //"        spec = max(dot(EyeDir, reflectDir), 0.0);" +
                //"        spec = pow(spec, 32.0);" +
                //"        finalColor += (spec * specularColor * gl_LightSource[0].specular.rgb);" +
                // Final assignment
                "        gl_FragColor = vec4(finalColor, 1.0);" +
                "}";

        public PortalShader(WorldManager worldManager) {
            super(vShader, fShader);
            init(worldManager);
        }

        /**
         * This applies this shader to the given geometry
         */
        public void applyToGeometry(Geometry geo) {
//        shaderState.setAttributePointer("binormal", 3, false, 0, geo.getBinormalBuffer());
//        shaderState.setAttributePointer("tangent", 3, false, 0, geo.getTangentBuffer());
            shaderState.setUniform("DiffuseMapIndex", 0);
            shaderState.setUniform("NormalMapIndex", 1);
            geo.setRenderState(shaderState);
        }
    }
}
