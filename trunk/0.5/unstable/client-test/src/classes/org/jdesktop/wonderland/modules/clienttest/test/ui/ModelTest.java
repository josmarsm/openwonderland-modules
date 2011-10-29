package org.jdesktop.wonderland.modules.clienttest.test.ui;

import com.jme.scene.Node;
import com.jme.scene.CameraNode;
import com.jme.scene.state.ZBufferState;
import com.jme.light.DirectionalLight;
import com.jme.renderer.ColorRGBA;
import com.jme.light.LightNode;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.CullState;
import com.jme.util.resource.ResourceLocator;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.model.collada.ThreadSafeColladaImporter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class ModelTest extends BaseTest implements FrameRateListener {
    private static final Logger LOGGER =
            Logger.getLogger(ModelTest.class.getName());
    
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
     
    private String name;
    private URL reference;
    
    private final List<ModelRecord> models = new ArrayList<ModelRecord>();
    private final List<LightNode> lights = new ArrayList<LightNode>();
    private Entity camera;
    private final List<Entity> modelEntities = new ArrayList<Entity>();
    
    public ModelTest() {
    }

    @Override
    public void initialize(JSONObject config) {
        super.initialize(config);
        
        name = (String) config.get("name");
        
        try {
            reference = getAssetURL(((String) config.get("reference")));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Unable to load reference image", ex);
        }
        
        if (config.containsKey("model")) {
            models.add(parseModel(config));
        } else if (config.containsKey("models")) {
            JSONArray jModels = (JSONArray) config.get("models");
            for (int i = 0; i < jModels.size(); i++) {
                models.add(parseModel((JSONObject) jModels.get(i)));
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }
    
    public TestResult run() {
        GraphicsUtils.getFrame().reset();
        
        wm = GraphicsUtils.getWorldManager();
        wm.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        wm.getRenderManager().setFrameRateListener(this, 100);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GraphicsUtils.getFrame().setReferenceImage(new ImageIcon(reference));
                GraphicsUtils.getFrame().setVisible(true);
            }
        });
        
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
                    
                    for (ModelRecord model : models) {
                        loadModel(model);
                    }
                    
                    GraphicsUtils.getFrame().setLoaded();
                    
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
            if (!result.get()) {
                return TestResult.FAIL;
            }
            
            boolean answer = GraphicsUtils.getFrame().waitForAnswer();
            if (answer) {
                return TestResult.PASS;
            } else {
                return TestResult.FAIL;
            }
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
    
    public void currentFramerate(final float fps) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GraphicsUtils.getFrame().setFPS(fps);
            }
        });
    }
    
    protected ModelRecord parseModel(JSONObject config) {
        String URL = (String) config.get("url");
        String model = (String) config.get("model");
        
        Vector3f location = new Vector3f();
        if (config.containsKey("location")) {
            JSONArray locArr = (JSONArray) config.get("location");
            location.set(readFloat(locArr.get(0)),
                         readFloat(locArr.get(1)),
                         readFloat(locArr.get(2)));
        }
        
        Quaternion rotation = new Quaternion();
        if (config.containsKey("rotation")) {
            JSONArray rotArr = (JSONArray) config.get("rotation");
            rotation.fromAngles(readFloat(rotArr.get(0)) * FastMath.DEG_TO_RAD, 
                                readFloat(rotArr.get(1)) * FastMath.DEG_TO_RAD,
                                readFloat(rotArr.get(2)) * FastMath.DEG_TO_RAD);
        }
        
        float scale = 1.0f;
        if (config.containsKey("scale")) {
            scale = readFloat(config.get("scale"));
        }
        
        boolean lighting = true;
        if (config.containsKey("lighting")) {
            lighting = readBoolean(config.get("lighting"));
        }
        
        return new ModelRecord(URL, model, location, rotation, scale, lighting);
    }
    
    protected void cleanup() {
        wm.getRenderManager().setFrameRateListener(null, 100);
        
        cleanupLights();
        cleanupModels();
        cleanupCameraEntity();
    }
    
    protected void createLights() {
        LightNode globalLight1 = null;
        LightNode globalLight2 = null;
        LightNode globalLight3 = null;

        float radius = 75.0f;
        float lheight = 30.0f;
        float x = (float)(radius*Math.cos(Math.PI/6));
        float z = (float)(radius*Math.sin(Math.PI/6));
        globalLight1 = createLight(x, lheight, z);
        x = (float)(radius*Math.cos(5*Math.PI/6));
        z = (float)(radius*Math.sin(5*Math.PI/6));
        globalLight2 = createLight(x, lheight, z);
        x = (float)(radius*Math.cos(3*Math.PI/2));
        z = (float)(radius*Math.sin(3*Math.PI/2));
        globalLight3 = createLight(x, lheight, z);

        wm.getRenderManager().addLight(globalLight1);
        lights.add(globalLight1);
        
        wm.getRenderManager().addLight(globalLight2);
        lights.add(globalLight2);
        
        wm.getRenderManager().addLight(globalLight3);
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
        light.setDiffuse(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setAmbient(new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
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
        
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cameraNode, 
                width, height, 45.0f, aspect, 1.0f, 1000.0f, true);
        GraphicsUtils.getRenderBuffer().setCameraComponent(cc);
        camera.addComponent(CameraComponent.class, cc);

        wm.addEntity(camera);
    }
    
    protected void cleanupCameraEntity() {
        GraphicsUtils.getRenderBuffer().setCameraComponent(null);
        wm.removeEntity(camera);
    }
    
    protected void cleanupModels() {
        for (Entity e : modelEntities) {
            wm.removeEntity(e);
        }
        modelEntities.clear();
    }
    
    protected void loadModel(ModelRecord model) 
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
                LOGGER.warning("Resolve: " + string);
                
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
        
        addModel(importer.getModel(), model);
    }
    
    /**
     * Add a model to be visualized
     */
    protected void addModel(Node model, ModelRecord record) 
    {
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
        
        modelEntities.add(e);
    }
    
    protected static URL getAssetURL(String asset) throws IOException {
        return AssetUtils.getAssetURL(asset, getServerNameAndPort());
    }
    
    private static float readFloat(Object object) {
        if (object instanceof Number) {
            return ((Number) object).floatValue();
        } else if (object instanceof String) {
            return Float.parseFloat((String) object);
        } else {
            throw new IllegalArgumentException("Can't convert " + object + 
                                               " to float.");
        }
    }
    
    private static boolean readBoolean(Object object) {
        if (object instanceof Boolean) {
            return (Boolean) object;
        } else if (object instanceof String) {
            return Boolean.parseBoolean((String) object);
        } else {
            throw new IllegalArgumentException("Can't convert " + object +
                                               " to boolean.");
        }
    }
    
    private static String getServerNameAndPort() throws MalformedURLException {
        URL serverURL = new URL(System.getProperty("wonderland.server.url"));
        return serverURL.getHost() + ":" + serverURL.getPort();
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
