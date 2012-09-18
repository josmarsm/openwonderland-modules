/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.common.cell;

import com.jme.renderer.ColorRGBA;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 *
 * @author Ryan
 */
@XmlRootElement(name="Block")
@ServerState
public class BlockServerState extends CellServerState {

    @XmlElement(name="material")
    public ColorRGBA material;
    
    @XmlElement(name="texture-url")
    public String textureURL;
    
    @Override
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.ezscript.server.cell.BlockCellMO";
    }

    @XmlTransient
    public ColorRGBA getMaterial() {
        return material;
    }

    public void setMaterial(ColorRGBA material) {
        this.material = material;
    }

    @XmlTransient
    public String getTextureURL() {
        return textureURL;
    }

    public void setTextureURL(String textureURL) {
        this.textureURL = textureURL;
    }
 
}
