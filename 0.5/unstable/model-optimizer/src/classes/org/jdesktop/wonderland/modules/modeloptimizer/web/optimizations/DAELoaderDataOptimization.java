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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.LoaderData;

/**
 *
 * @author jkaplan
 */
public class DAELoaderDataOptimization implements Optimization {    
    private static final Logger LOGGER =
            Logger.getLogger(DAELoaderDataOptimization.class.getName());
        
    public void initialize() {
    }
    
    public boolean optimize(ContentNode node, Map<String, Object> context) 
            throws ContentRepositoryException, IOException 
    {
        String name = node.getName().toLowerCase();
        if (!(node instanceof ContentResource) || 
            !(name.endsWith(".dae.gz.dep") || name.endsWith(".dae.dep"))) 
        {
            // not interested
            return false;
        }
        
        LOGGER.log(Level.WARNING, "DAE Loader optimization " + node.getName());
            
        final DeployedModel dep = (DeployedModel) context.get(LoadModelOptimization.DEPLOYED_MODEL);
        if (dep == null || dep.getLoaderData() == null) {
            return false;
        }
        
        LoaderData data = (LoaderData) dep.getLoaderData();
        
        // remove any texture with %2F -- these were added due to a bug in
        // the collada loader
        boolean removed = false;
        for (Iterator<Map.Entry<String, String>> i = data.getDeployedTextures().entrySet().iterator();
             i.hasNext();)
        {
            Map.Entry<String, String> e = i.next();
            if (e.getKey().contains("%2F") && e.getValue().contains("%2F")) {
                i.remove();
                removed = true;
            }
        }
        
        if (!removed) {
            return false;
        }
         
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
}
