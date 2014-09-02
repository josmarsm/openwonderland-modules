/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui.tests;

import com.jme.light.DirectionalLight;
import com.jme.light.LightNode;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.state.CullState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.resource.ResourceLocator;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.model.collada.ThreadSafeColladaImporter;
import java.awt.EventQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.modules.clienttest.test.ui.GraphicsUtils;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestManager;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestResult;
import static org.jdesktop.wonderland.modules.clienttest.test.ui.tests.ModelTest.getAssetURL;
import org.json.simple.JSONObject;

/**
 *
 * @author jkaplan
 */
public abstract class BaseGraphicsTest extends BaseTest
    implements FrameRateListener 
{
    private static final Logger LOGGER =
            Logger.getLogger(BaseGraphicsTest.class.getName());

    /**
     * The WorldManager for this world
     */
    private WorldManager wm = null;

    /**
     * The CameraNode
     */
    private CameraNode cameraNode = null;

    /**
     * The desired frame rate
     */
    private int desiredFrameRate = 60;

    private String name;
    private URL reference;

    private Entity camera;
    private final List<LightNode> lights = new ArrayList<LightNode>();
    private final List<Entity> entities = new ArrayList<Entity>();

    @Override
    public void initialize(JSONObject config) {
        super.initialize(config);

        name = (String) config.get("name");

        try {
            reference = getAssetURL(((String) config.get("reference")));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Unable to load reference image", ex);
        }

        // hangle mtgame and awt threads so that the tests run properly
        GraphicsUtils.getCanvas().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                GraphicsUtils.getWorldManager().getRenderManager().setRunning(false);
                // ty to acquire synchronization semaphore
                try {
                    GraphicsUtils.getWorldManager().getRenderManager().getSynchronizer().acquire();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                final int width = e.getComponent().getWidth();
                final int height = e.getComponent().getHeight();
                GraphicsUtils.getCanvas().setBounds(0, 0, width, height);

                GraphicsUtils.getWorldManager().getRenderManager().getSynchronizer().release();
                GraphicsUtils.getWorldManager().getRenderManager().setRunning(true);
            }
        });
    }

    @Override
    public String getName() {
        return name;
    }
    
    public TestResult run() {

        // run dummy test, so that all tests work properly
        if (GraphicsUtils.getFlg() == 0) {
            dummy();
        }

        GraphicsUtils.getFrame().reset();

        wm = GraphicsUtils.getWorldManager();
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        wm.getRenderManager().setFrameRateListener(this, 100);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GraphicsUtils.getFrame().setTitle(getName());
                GraphicsUtils.getFrame().setReferenceImage(new ImageIcon(reference));
                GraphicsUtils.getFrame().setVisible(true);
            }
        });

        GraphicsUtils.getWorldManager().getRenderManager().getSynchronizer().release();
        GraphicsUtils.getWorldManager().getRenderManager().setRunning(true);

        // create the camera immediately
        createCameraEntity();

        // add content to frame
        final AtomicBoolean result = new AtomicBoolean(false);
        final Semaphore lock = new Semaphore(1);
        lock.drainPermits();

        // add other components on the renderer commit thread
        GraphicsUtils.workCommit(new WorkCommit() {
            public void commit() {
                try {
                    createLights();

                    List<Entity> loaded = doLoad();
                    entities.addAll(loaded);
                    result.set(true);
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, "Error loading model", ioe);
                } finally {
                    lock.release();
                }
            }
        });

        try {
            lock.acquire();

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {

                    GraphicsUtils.getWorldManager().getRenderManager().setRunning(false);
                    // ty to acquire synchronization semaphore
                    try {
                        GraphicsUtils.getWorldManager().getRenderManager().getSynchronizer().acquire();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                    GraphicsUtils.getFrame().setLoaded();
                    GraphicsUtils.getWorldManager().getRenderManager().getSynchronizer().release();
                    GraphicsUtils.getWorldManager().getRenderManager().setRunning(true);
                }
            });
            if (!result.get()) {
                return TestResult.FAIL;
            }

            return GraphicsUtils.getFrame().waitForAnswer();
        } catch (InterruptedException ie) {
        } finally {
            cleanup();

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GraphicsUtils.getFrame().setVisible(false);
                }
            });
        }

        return TestResult.FAIL;
    }

    protected abstract List<Entity> doLoad() throws IOException;

    protected void cleanupEntities() {
        for (Entity e : entities) {
            wm.removeEntity(e);
        }
        entities.clear();
    }

    public void currentFramerate(final float fps) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GraphicsUtils.getWorldManager().getRenderManager().setRunning(false);
                // ty to acquire synchronization semaphore
                try {
                    GraphicsUtils.getWorldManager().getRenderManager().getSynchronizer().acquire();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                GraphicsUtils.getFrame().setFPS(fps);
                GraphicsUtils.getWorldManager().getRenderManager().getSynchronizer().release();
                GraphicsUtils.getWorldManager().getRenderManager().setRunning(true);
            }
        });
    }

    protected void cleanup() {
        final Semaphore lock = new Semaphore(1);
        lock.drainPermits();
        GraphicsUtils.getWorldManager().getRenderManager().getSynchronizer().release();
        GraphicsUtils.getWorldManager().getRenderManager().setRunning(true);
        GraphicsUtils.workCommit(new WorkCommit() {
            public void commit() {
                try {
                    wm.getRenderManager().setFrameRateListener(null, 100);

                    cleanupLights();
                    cleanupCameraEntity();
                    cleanupEntities();
                } finally {
                    lock.release();
                }
            }
        });

        try {
            lock.acquire();
            GraphicsUtils.getWorldManager().getRenderManager().setRunning(false);
            // ty to acquire synchronization semaphore
            try {
                GraphicsUtils.getWorldManager().getRenderManager().getSynchronizer().acquire();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } catch (InterruptedException ie) {
            // ignore
        }
    }

    protected void createLights() {
        LightNode globalLight1 = null;
        LightNode globalLight2 = null;
        LightNode globalLight3 = null;

        float radius = 75.0f;
        float lheight = 30.0f;
        float x = (float) (radius * Math.cos(Math.PI / 6));
        float z = (float) (radius * Math.sin(Math.PI / 6));
        globalLight1 = createLight(x, lheight, z);
        x = (float) (radius * Math.cos(5 * Math.PI / 6));
        z = (float) (radius * Math.sin(5 * Math.PI / 6));
        globalLight2 = createLight(x, lheight, z);
        x = (float) (radius * Math.cos(3 * Math.PI / 2));
        z = (float) (radius * Math.sin(3 * Math.PI / 2));
        globalLight3 = createLight(x, lheight, z);

        wm.getRenderManager().addLight(globalLight3);
        lights.add(globalLight1);

        wm.getRenderManager().addLight(globalLight2);
        lights.add(globalLight2);

        wm.getRenderManager().addLight(globalLight1);
        lights.add(globalLight3);
    }

    protected void cleanupLights() {
        for (LightNode light : lights) {
            wm.getRenderManager().removeLight(light);
        }
        lights.clear();
    }

    private LightNode createLight(float x, float y, float z) {
        LightNode lightNode = new LightNode();
        DirectionalLight light = new DirectionalLight();
        light.setDiffuse(new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f));
        light.setAmbient(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
        light.setSpecular(new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f));
        light.setEnabled(true);
        lightNode.setLight(light);
        lightNode.setLocalTranslation(x, y, z);
        light.setDirection(new Vector3f(-x, -y, -z));
        return (lightNode);
    }

    protected void createCameraEntity() {
        Node cameraSG = new Node("MyCamera SG");
        cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(cameraNode);

        // Add the camera
        camera = new Entity("DefaultCamera");

        int width = GraphicsUtils.getCanvas().getWidth();
        int height = GraphicsUtils.getCanvas().getHeight();
        float aspect = (float) width / (float) height;

        CameraComponent cc = GraphicsUtils.getWorldManager().getRenderManager().createCameraComponent(cameraSG, cameraNode,
                width, height, 45.0f, aspect, 1.0f, 1000.0f, true);
        GraphicsUtils.getRenderBuffer().setCameraComponent(cc);
        camera.addComponent(CameraComponent.class, cc);

        GraphicsUtils.getWorldManager().addEntity(camera);
    }

    protected void cleanupCameraEntity() {
        GraphicsUtils.getRenderBuffer().setCameraComponent(null);
        wm.removeEntity(camera);
    }

    protected WorldManager getWorldManager() {
        return wm;
    }

    protected static URL getAssetURL(String asset) throws IOException {
        return TestManager.getAssetURL(asset);
    }
    
    // for dummy test
    protected Entity loadModel(ModelRecord model) 
            throws IOException 
    {
        URL assetURL = getAssetURL(model.getURL());
        
        final File tmpDir = File.createTempFile("model", "tmp");
        tmpDir.delete();
        tmpDir.mkdir();
        
        byte[] buffer = new byte[16384];
        
        ZipInputStream zis = new ZipInputStream(assetURL.openStream());
        ZipEntry ze = null;
        while ((ze = zis.getNextEntry()) != null) {
            if (!ze.isDirectory()) {
                File extractFile = new File(tmpDir, ze.getName());
                
                // create the parent directory if necessary
                File parentDir = extractFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                FileOutputStream os = new FileOutputStream(extractFile);
                int read;
                while ((read = zis.read(buffer, 0, buffer.length)) > 0) {
                    os.write(buffer, 0, read);
                }
                os.close();
            }
        }
        
        File modelFile = new File(tmpDir, model.getModel());
        final File modelDir = modelFile.getParentFile();
        
        ResourceLocator locator = new ResourceLocator() {
            public URL locateResource(String string) {
                LOGGER.log(Level.INFO, "Resolve: {0}", string);
                
                try {
                    File file = new File(string);
                    if (file.isAbsolute()) {
                        return file.toURI().toURL();
                    }
                    
                    return new File(modelDir, string).toURI().toURL();
                } catch (MalformedURLException ex) {
                    LOGGER.log(Level.WARNING, "Error creating URL for " + 
                               string, ex);
                    return null;
                }
            }
        };
        ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, locator);
        
        ThreadSafeColladaImporter importer = new ThreadSafeColladaImporter("model");
        importer.load(new FileInputStream(modelFile));
     
        // create a copy of the model before making any changes
        model = model.clone();
        
        float unitMeter = importer.getInstance().getUnitMeter();
        model.setScale(model.getScale() * unitMeter);
        
        String upAxis = importer.getInstance().getUpAxis();
        if (upAxis.equals("Z_UP")) {
            Quaternion up = new Quaternion(new float[] {-(float)Math.PI/2, 0f, 0f});
            model.getRotation().multLocal(up);
        } else if (upAxis.equals("X_UP")) {
            Quaternion up = new Quaternion(new float[] {0f, 0f, (float)Math.PI/2});
            model.getRotation().multLocal(up);
        } // Y_UP is the Wonderland default

        importer.cleanUp();
        ResourceLocatorTool.removeResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, locator);
        
        return addModel(importer.getModel(), model);
    }
    
    /**
     * Add a model to be visualized
     */
    protected Entity addModel(Node model, ModelRecord record) 
    {
        WorldManager wm = getWorldManager();
        Node modelRoot = new Node("Model");

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        modelRoot.setRenderState(buf);
        //modelRoot.setLocalScale(1.0f);

        CullState culls = (CullState) wm.getRenderManager().createRendererState(RenderState.StateType.Cull);
        culls.setCullFace(CullState.Face.None);
        modelRoot.setRenderState(culls);

        modelRoot.setLocalTranslation(record.getTranslation());
        modelRoot.setLocalRotation(record.getRotation());
        modelRoot.setLocalScale(record.getScale());
        
        //System.out.println("Adding: " + model);
        modelRoot.attachChild(model);
        
        Entity e = new Entity("Model");
        RenderComponent sc = wm.getRenderManager().createRenderComponent(modelRoot);
        sc.setLightingEnabled(record.isLighting());
        
        e.addComponent(RenderComponent.class, sc);
        wm.addEntity(e); 
        
        return e;
    }

    private void dummy() {
        GraphicsUtils.getFrame().reset();

        wm = GraphicsUtils.getWorldManager();
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        wm.getRenderManager().setFrameRateListener(this, 100);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GraphicsUtils.getFrame().setTitle(getName());
                GraphicsUtils.getFrame().setReferenceImage(new ImageIcon(reference));
                GraphicsUtils.getFrame().setVisible(true);
            }
        });

        GraphicsUtils.getWorldManager().getRenderManager().getSynchronizer().release();
        GraphicsUtils.getWorldManager().getRenderManager().setRunning(true);

        // create the camera immediately
        createCameraEntity();

        // add content to frame
        final AtomicBoolean result = new AtomicBoolean(false);
        final Semaphore lock = new Semaphore(1);
        lock.drainPermits();

        // add other components on the renderer commit thread
        GraphicsUtils.workCommit(new WorkCommit() {
            public void commit() {
                try {
                    createLights();
                    float[] angles = new float[3];
                    angles[0] = 0;
                    angles[1] = 180;
                    angles[2] = 0;
                    ModelRecord rec = new ModelRecord("wla://client-test/Ingrid.zip"
                            , "ingrid.dae", new Vector3f(0, (float) -0.5, 8), 
                            new Quaternion(angles), 0.005f, false);
                    loadModel(rec);
//                    List<Entity> loaded = doLoad();

//                    entities.addAll(loaded);
                    GraphicsUtils.setFlg(1);
                } catch (Exception ioe) {
                    LOGGER.log(Level.WARNING, "Error loading model", ioe);
                } finally {
                    lock.release();
                }
            }
        });

        try {
            lock.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(BaseGraphicsTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        GraphicsUtils.getWorldManager().getRenderManager().setRunning(false);
        // ty to acquire synchronization semaphore
        try {
            GraphicsUtils.getWorldManager().getRenderManager().getSynchronizer().acquire();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }
    
    protected class ModelRecord implements Cloneable {
        private final String url;
        private final String model;
        private Vector3f translation;
        private Quaternion rotation;
        private float scale;
        private boolean lighting;
                
        public ModelRecord(String url, String model, Vector3f translation, 
                           Quaternion rotation, float scale, boolean lighting)
        {
            this.url = url;
            this.model = model;
            this.translation = translation;
            this.rotation = rotation;
            this.scale = scale;
            this.lighting = lighting;
        }
        
        protected ModelRecord(ModelRecord copy) {
            this.url = copy.getURL();
            this.model = copy.getModel();
            this.translation = new Vector3f(copy.getTranslation());
            this.rotation = new Quaternion(copy.getRotation());
            this.scale = copy.getScale();
            this.lighting = copy.isLighting();
        }

        public String getURL() {
            return url;
        }
        
        public String getModel() {
            return model;
        }

        public Vector3f getTranslation() {
            return translation;
        }

        public void setTranslation(Vector3f translation) {
            this.translation = translation;
        }
        
        public Quaternion getRotation() {
            return rotation;
        }

        public void setRotation(Quaternion rotation) {
            this.rotation = rotation;
        }

        public float getScale() {
            return scale;
        }

        public void setScale(float scale) {
            this.scale = scale;
        }
        
        public boolean isLighting() {
            return lighting;
        }

        public void setLighting(boolean lighting) {
            this.lighting = lighting;
        }

        public ModelRecord clone() {
            return new ModelRecord(this);
        }
    }
}
