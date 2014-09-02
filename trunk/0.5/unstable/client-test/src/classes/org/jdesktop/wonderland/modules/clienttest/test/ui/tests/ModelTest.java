/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.clienttest.test.ui.tests;

import com.jme.scene.Node;
import com.jme.scene.state.ZBufferState;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class ModelTest extends BaseGraphicsTest {
    private static final Logger LOGGER =
            Logger.getLogger(ModelTest.class.getName());
    
    private final List<ModelRecord> models = new ArrayList<ModelRecord>();
    
    @Override
    public void initialize(JSONObject config) {
        super.initialize(config);
        
        if (config.containsKey("model")) {
            models.add(parseModel(config));
        } else if (config.containsKey("models")) {
            JSONArray jModels = (JSONArray) config.get("models");
            for (int i = 0; i < jModels.size(); i++) {
                models.add(parseModel((JSONObject) jModels.get(i)));
            }
        }
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

    @Override
    protected List<Entity> doLoad() throws IOException {
        List<Entity> loaded = new ArrayList<Entity>();
        
        for (ModelRecord record : models) {
            loaded.add(loadModel(record));
        }
        
        return loaded;
    }
    
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
        URL serverURL = new URL(System.getProperty("jnlp.wonderland.server.url"));
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
