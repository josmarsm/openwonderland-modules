/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.worldbuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author jkaplan
 */
@XmlRootElement(name="size")
public class CellDimension implements Cloneable {
    private int width;
    private int height;
    
    public CellDimension() {
    }
    
    public CellDimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @XmlElement
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @XmlElement
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }   
    
    @Override
    public CellDimension clone() {
        return new CellDimension(getWidth(), getHeight());
    }
    
    @Override
    public String toString() {
        return "(" + getWidth() + " x " + getHeight() + ")";
    }
}
