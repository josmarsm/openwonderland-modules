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
package org.jdesktop.wonderland.modules.layout.common;

import com.jme.bounding.BoundingBox;
import org.jdesktop.wonderland.modules.layout.api.common.*;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.common.cell.state.spi.CellServerStateSPI;

/**
 * Layout configuration specific to the table layout. Gives an M x N grid.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name = "table-layout-config")
@ServerState
public class TableLayoutConfig extends LayoutConfig implements CellServerStateSPI {

    // The bounding volume to use for the layout area
    // XXX Should this be JAXB annotated? XXX
    @XmlTransient
    private BoundingVolume boundingVolume = null;

    // The M x N grid size, where "M" is along the X axis and N is along the
    // Z axis.
    // XXX Should these be JAXB annotated? XXX
    private int m = 0;
    private int n = 0;

    /**
     * Default constructor
     */
    public TableLayoutConfig() {
        // XXX
        // Because we don't have bounding volume JAXB annotated yet
        // XXX
        boundingVolume = new BoundingBox(new Vector3f(), 4, 4, 4);
    }
    
    /**
     * Constructor, takes the bounding volume of the area, and the M x N grid
     * size
     * @param boundingVolume The area to use for layout
     * @param m The number of grid spaces along the X-axis
     * @param n The number of grid spaces along the Z-axis
     */
    public TableLayoutConfig(BoundingVolume boundingVolume, int m, int n) {
        this.boundingVolume = boundingVolume;
        this.m = m;
        this.n = n;
    }

    /**
     * {@inheritDoc}
     */
    public <T extends Layout> Class<T> getLayoutClass() {
        return (Class<T>)TableLayout.class;
    }

    /**
     * {@inheritDoc}
     */
    public BoundingVolume getBounds() {
        return boundingVolume;
    }

    public void setBoundings(BoundingVolume boundingVolume) {
        this.boundingVolume = boundingVolume;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }
}
