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

import com.jme.image.Texture;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.LoaderData;
import org.jdesktop.wonderland.modules.kmzloader.client.KmzLoaderFactory;

/**
 *
 * @author jkaplan
 */
public class KMZLoaderDataOptimization implements Optimization {    
    private static final Logger LOGGER =
            Logger.getLogger(KMZLoaderDataOptimization.class.getName());
    
    public void initialize() {
    }
    
    public boolean optimize(ContentNode node, Map<String, Object> context) 
            throws ContentRepositoryException, IOException 
    {
        String name = node.getName().toLowerCase();
        if (!(node instanceof ContentResource) || !name.endsWith(".kmz.dep")) {
            // not interested
            return false;
        }
        
        LOGGER.log(Level.WARNING, "KMZ Loader optimization " + node.getName());
            
        final DeployedModel dep = (DeployedModel) context.get(LoadModelOptimization.DEPLOYED_MODEL);
        if (dep == null || dep.getLoaderData() == null) {
            return false;
        }

        final LoaderData data = (LoaderData) dep.getLoaderData();
        if (!data.getDeployedTextures().isEmpty()) {
            // already optimized
            return false;
        }
        
        Node model = (Node) context.get(LoadModelOptimization.LOADED_MODEL);
        if (model == null) {
            return false;
        }
        
        TreeScan.findNode(model, new ProcessNodeInterface() {
            public boolean processNode(Spatial node) {
                TextureState ts = (TextureState) node.getRenderState(StateType.Texture);
                if (ts != null) {
                    processTextures(ts, dep, data);
                }
                
                return true;
            }
        });
         
        // now write the updated loader data
        String depFile = node.getName();
        String loaderFile = depFile.replaceAll(".dep$", ".ldr");
        if (loaderFile.equals(depFile)) {
            // make sure we don't overwrite the wrong file
            return false;
        }
        
        ContentResource loaderNode = (ContentResource) node.getParent().getChild(loaderFile);
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            data.encode(baos);
            loaderNode.put(baos.toByteArray());
        } catch (JAXBException je) {
            throw new IOException(je);
        }
        
        return true;
    }
    
    private void processTextures(TextureState ts, DeployedModel dep,
                                 LoaderData data)
    {
        for (int i = 0; i < ts.getNumberOfSetTextures(); i++) {
            Texture t = ts.getTexture(i);
            String urlStr = t.getImageLocation();
            
            // find the relative path between the model and this texture
            String relativePath = KmzLoaderFactory.getRelativePath(dep.getModelURL(),
                                                                   urlStr);
            
            // idx is the index of the first non-overlap character
            data.getDeployedTextures().put(relativePath, relativePath);
        }
    }
}
