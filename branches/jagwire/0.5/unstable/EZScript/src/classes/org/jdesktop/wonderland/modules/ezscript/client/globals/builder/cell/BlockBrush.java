/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.globals.builder.cell;

import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import java.awt.Image;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeViewerEntity;

/**
 *
 * @author Ryan
 */
public class BlockBrush extends BasicRenderer {

    private BlockCell cell = null;
    private ColorRGBA material = null;
//    private Image textureImage;
    private String textureURL = null;
    
    public BlockBrush(BlockCell cell) {
        super(cell);
        
        this.cell = cell;
        this.material = cell.getMaterial();
        this.textureURL = cell.getTextureURL();
    }
    
    @Override
    protected Node createSceneGraph(Entity entity) {
        
        ShapeViewerEntity e = new ShapeViewerEntity("BOX");
        
        if(textureURL != null) {
            e.setTexture(textureURL);
        } else {
            e.setAppearance(material);
        }
        
        
        
        return e.getScenegraphWithoutCollision();
    }
    
}
