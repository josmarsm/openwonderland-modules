/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.modeloptimizer.web.optimizations;

import com.jme.scene.Node;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.jmecolladaloader.client.JmeColladaLoaderFactory;
import org.jdesktop.wonderland.modules.kmzloader.client.KmzLoaderFactory;

/**
 *
 * @author jkaplan
 */
public class LoadModelOptimization implements Optimization {    
    private static final Logger LOGGER =
            Logger.getLogger(LoadModelOptimization.class.getName());
    
    public static final String DEPLOYED_MODEL = "DeployedModel";
    public static final String LOADED_MODEL = "LoadedModel";
    
    public void initialize() {
        // use a dummy display system to load data
        DisplaySystem.getDisplaySystem("dummy");
        
        // register loaders
        JmeColladaLoaderFactory jmec = new JmeColladaLoaderFactory();
        LoaderManager.getLoaderManager().registerLoader(jmec);
        LoaderManager.getLoaderManager().activateLoader(jmec);
        
        KmzLoaderFactory kmz = new KmzLoaderFactory();
        LoaderManager.getLoaderManager().registerLoader(kmz);
        LoaderManager.getLoaderManager().activateLoader(kmz);
    }
    
    public boolean optimize(ContentNode node, Map<String, Object> context) 
            throws ContentRepositoryException, IOException 
    {
        if (!(node instanceof ContentResource) || !node.getName().endsWith(".dep")) {
            // not interested
            return false;
        }
        
        // if it is a .dep file, go ahead and load the model, which will
        // force the LoaderData to get read
        try {
            LOGGER.log(Level.WARNING, "Loading " + node.getName());
            
            InputStream is = ((ContentResource) node).getInputStream();
            DeployedModel dep = DeployedModel.decode(is);            
            context.put(DEPLOYED_MODEL, dep);
            
            Entity e = new Entity("Load model");
            Node n = dep.getModelLoader().loadDeployedModel(dep, e);
            context.put(LOADED_MODEL, n);
        } catch (JAXBException je) {
            throw new IOException(je);
        } finally {
            // cleanup textures
            TextureManager.clearCache();
        }
        
        return true;
    }
}
