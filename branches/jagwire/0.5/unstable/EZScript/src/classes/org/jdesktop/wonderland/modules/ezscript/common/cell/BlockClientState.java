/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.common.cell;

import com.jme.renderer.ColorRGBA;
import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 *
 * @author Ryan
 */
public class BlockClientState extends CellClientState {
    
    
    private ColorRGBA material;
    private String textureURL;
    
    public BlockClientState() {
        super();
    }

    public ColorRGBA getMaterial() {
        return material;
    }

    public void setMaterial(ColorRGBA material) {
        this.material = material;
    }

    public String getTextureURL() {
        return textureURL;
    }

    public void setTextureURL(String textureURL) {
        this.textureURL = textureURL;
    }
    
    
    
    
}
