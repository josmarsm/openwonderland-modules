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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.ConfigInstance;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.WorldManager.ConfigLoadListener;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 * A World test application
 * 
 * @author Doug Twilleager
 */
public class ShaderTest extends ModelTest {
    private static final Logger LOGGER =
            Logger.getLogger(ModelTest.class.getName());
    
  
    private final List<String> avatars = new ArrayList<String>();
   
    public ShaderTest() {
    }

    @Override
    public void initialize(JSONObject config) {
        super.initialize(config);
        
        if (config.containsKey("avatar")) {
            avatars.add((String) config.get("avatar"));
        } else if (config.containsKey("models")) {
            JSONArray jModels = (JSONArray) config.get("models");
            for (int i = 0; i < jModels.size(); i++) {
                avatars.add((String) jModels.get(i));
            }
        }
    }

    @Override
    protected Entity loadModel(ModelRecord model) throws IOException {
        WorldManager wm = getWorldManager();
        if (wm.getRenderManager().supportsOpenGL20() == false) {
            LOGGER.warning("MTG files require OpenGL 2.0");
            return null;
        }
        
        RenderManager rm = wm.getRenderManager();
        
        String baseURL = model.getURL();
        baseURL = baseURL.substring(0, baseURL.lastIndexOf('/') + 1);
        baseURL = getAssetURL(baseURL).toExternalForm();
       
        String configBase = baseURL.substring(0, baseURL.length() - 1);
        wm.setConfigBaseURL(configBase);
        
        Node rootNode = new Node("mtg-root");
        rootNode.setLocalTranslation(model.getTranslation());
        rootNode.setLocalRotation(model.getRotation());
        rootNode.setLocalScale(model.getScale());
        
        Entity rootEntity = new Entity();
        
        rootEntity.addComponent(RenderComponent.class, 
                                rm.createRenderComponent(rootNode));
        
        URL modelURL = getAssetURL(model.getModel());
        
        wm.loadConfiguration(modelURL, new ConfigLoadListener() {

            public void configLoaded(ConfigInstance ci) {
//                System.out.println("Loaded: " + ci.getEntity());
            }
        });

        ConfigInstance ci[] = wm.getAllConfigInstances();
        for (int i = 0; i < ci.length; i++) {
            RenderComponent rc = ci[i].getEntity().getComponent(RenderComponent.class);
            if (rc != null) {
                rc.setAttachPoint(rootNode);
            }
            
            rootEntity.addEntity(ci[i].getEntity());
        }
        
        wm.addEntity(rootEntity);
        return rootEntity;
    }
}
