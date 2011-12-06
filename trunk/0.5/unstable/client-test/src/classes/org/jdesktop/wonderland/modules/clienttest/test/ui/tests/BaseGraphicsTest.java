/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui.tests;

import com.jme.light.DirectionalLight;
import com.jme.light.LightNode;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.modules.clienttest.test.ui.GraphicsUtils;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestManager;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestResult;
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
                GraphicsUtils.getFrame().setTitle(getName());
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
                    
                    List<Entity> loaded = doLoad();
                    entities.addAll(loaded);
                    
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
                GraphicsUtils.getFrame().setFPS(fps);
            }
        });
    }
    
    protected void cleanup() {
        final Semaphore lock = new Semaphore(1);
        lock.drainPermits();
        
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
        float x = (float)(radius*Math.cos(Math.PI/6));
        float z = (float)(radius*Math.sin(Math.PI/6));
        globalLight1 = createLight(x, lheight, z);
        x = (float)(radius*Math.cos(5*Math.PI/6));
        z = (float)(radius*Math.sin(5*Math.PI/6));
        globalLight2 = createLight(x, lheight, z);
        x = (float)(radius*Math.cos(3*Math.PI/2));
        z = (float)(radius*Math.sin(3*Math.PI/2));
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
    
    protected WorldManager getWorldManager() {
        return wm;
    }
    
    protected static URL getAssetURL(String asset) throws IOException {
        return TestManager.getAssetURL(asset);
    }
}
