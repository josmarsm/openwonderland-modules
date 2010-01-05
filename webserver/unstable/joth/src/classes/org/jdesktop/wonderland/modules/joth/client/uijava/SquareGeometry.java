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

package org.jdesktop.wonderland.modules.joth.client.uijava;

import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Quad;
import com.jme.bounding.BoundingBox;
import com.jme.scene.state.MaterialState;
import org.jdesktop.wonderland.modules.joth.client.utiljava.JothUtilJme;

/******************************************************************
 * SquareGeometry: The geometry for a board square (a square quad).
 * about the squares location in the board.
 * @author deronj@dev.java.net
 */

public class SquareGeometry extends Quad {

    public static final float WIDTH = 0.3f;
    public static final float HEIGHT = 0.3f;

    private static final ColorRGBA LIGHT_COLOR = new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f);
    private static final ColorRGBA DARK_COLOR = new ColorRGBA(0.0f, 0.9f, 0.0f, 1.0f);

    private JothUtilJme util;
    private MaterialState ms;

    public SquareGeometry (JothUtilJme util, int row, int col) {
        super("Quad for square " + row + "," + col, WIDTH, HEIGHT);
        this.util = util;

        ColorRGBA color;
        if ((row & 0x1) == (col & 0x1)) {
            color = LIGHT_COLOR;
        } else {
            color = DARK_COLOR;
        }
        setColor(color);
        
        util.doJmeOpAndWait(new Runnable() {
            public void run () {
                setModelBound(new BoundingBox());
                updateModelBound();
            }
        }, null);
    }

    public void setColor(final ColorRGBA color) {
        if (ms == null) {
            ms = util.spatialCreateMaterialState(this);
        }
        util.materialStateSetAmbientAndDiffuse(ms, color);
    }
}
