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
public class CellLocation implements Cloneable {
    private int x;
    private int y;
    
    public CellLocation() {
    }
    
    public CellLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @XmlElement
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @XmlElement
    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }   
    
    @Override
    public CellLocation clone() {
        return new CellLocation(getX(), getY());
    }
    
    @Override
    public String toString() {
        return "(" + getX() + ", " + getY() + ")";
    }
}
