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
package org.jdesktop.wonderland.modules.shape.client.jme.cellrenderer;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.shape.client.ShapeCell;

public class ShapeCellRenderer extends BasicRenderer {
    
    private Node node = null;
    
    public ShapeCellRenderer(Cell cell) {
        super(cell);
    }
    
    public void updateShape() {
        final String name = cell.getCellID().toString();
        final String shapeType = ((ShapeCell) cell).getShapeType();

        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                node.detachAllChildren();
                node.attachChild(getShapeMesh(name, shapeType));
                node.setModelBound(new BoundingBox());
                node.updateModelBound();

                ClientContextJME.getWorldManager().addToUpdateList(node);
            }
        });
    }
    
    private TriMesh getShapeMesh(String name, String shapeType) {        
        TriMesh mesh = null;
        if (shapeType != null && shapeType.equals("BOX") == true) {
            mesh = new Box(name, new Vector3f(), 2, 2, 2);
        }
        else if (shapeType != null && shapeType.equals("SPHERE") == true) {
            mesh = new Sphere(name, new Vector3f(), 25, 25, 2);
        }
        else {
            logger.warning("Unsupported Shape type " +cell.getLocalBounds().getClass().getName());
        }
        return mesh;
    }

    protected Node createSceneGraph(Entity entity) {
        String name = cell.getCellID().toString();
        String shapeType = ((ShapeCell)cell).getShapeType();

        TriMesh mesh = this.getShapeMesh(name, shapeType);
        if (mesh == null) {
          node = new Node();
          return node;
        }
        
        node = new Node();
        node.attachChild(mesh);
        node.setModelBound(new BoundingBox());
        node.updateModelBound();
        node.setName("Cell_"+cell.getCellID()+":"+cell.getName());

        String textureURI = ((ShapeCell)cell).getTextureURI();
        if (textureURI != null) {
            RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
            TextureState ts = (TextureState) rm.createRendererState(StateType.Texture);
            Texture t = null;
            try {
                URL url = getAssetURL(textureURI);
                t = TextureManager.loadTexture(url);
                t.setWrap(Texture.WrapMode.MirroredRepeat);
                t.setTranslation(new Vector3f());
                ts.setTexture(t);
                ts.setEnabled(true);
            } catch (MalformedURLException ex) {
                Logger.getLogger(ShapeCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
            node.setRenderState(ts);
        }
        return node;
    }
}
