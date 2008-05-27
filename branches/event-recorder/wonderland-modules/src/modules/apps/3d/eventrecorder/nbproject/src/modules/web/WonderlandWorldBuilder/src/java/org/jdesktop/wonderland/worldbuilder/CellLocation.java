    /**
     * Project Looking Glass
     *
     * $RCSfile$
     *
     * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
     *
     * Redistributions in source code form must reproduce the above
     * copyright and this condition.
     *
     * The contents of this file are subject to the GNU General Public
     * License, Version 2 (the "License"); you may not use this file
     * except in compliance with the License. A copy of the License is
     * available at http://www.opensource.org/licenses/gpl-license.php.
     *
     * $Revision$
     * $Date$
     * $State$
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
