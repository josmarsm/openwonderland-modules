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

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Cylinder;
import com.jme.bounding.BoundingBox;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.MaterialState;
import org.jdesktop.wonderland.modules.joth.client.utiljava.JothUtilJme;
import org.jdesktop.wonderland.modules.joth.client.gamejava.Board;

/***************************************************************
 * Piece: A 3D disk which represents a game piece.
 * @author deronj@dev.java.net
 */
public class Piece extends Node {

    // TODO: note: for now pieces are all one color or the other.
    public static final ColorRGBA BLACK = new ColorRGBA(0f, 0f, 0f, 1f);
    public static final ColorRGBA WHITE = new ColorRGBA(1f, 1f, 1f, 1f);

    /** The distance of the center of the piece above the board. */
    private static final float Z_OFFSET = 0.1f;

    /** The radius of a disk. */
    private static final float DISK_RADIUS = 0.12f;

    /** The height of a disk. */
    private static final float DISK_HEIGHT = 0.1f;

    /** The geometry of the piece. */
    private Cylinder cyl;

    /** The color of this piece. */
    private Board.Color color;

    private JothUtilJme util;
    private MaterialState ms;

    public Piece (Board.Color color, JothUtilJme util) {
        super("Node for a " + color + " piece");
        this.color = color;
        this.util = util;

        util.doJmeOpAndWait(new Runnable() {
            public void run () {
                cyl = new Cylinder(Piece.this.color + " Cylinder", 20, 20, DISK_RADIUS, DISK_HEIGHT, true);
                cyl.setModelBound(new BoundingBox());
                cyl.updateModelBound();
            }
        }, null);

        util.attachChild(this, cyl);
        util.setLocalTranslation(this, new Vector3f(0f, 0f, Z_OFFSET));

        if (color == Board.Color.WHITE) {
            setColor(WHITE);
        } else {
            setColor(BLACK);
        }
    }

    /**
     * {@inheritDoc}
     */
    private synchronized void setColor(final ColorRGBA color) {
        if (ms == null) {;
            ms = util.spatialCreateMaterialState(cyl);
        }
        util.materialStateSetAmbientAndDiffuse(ms, color);
    }

    public Board.Color getColor () {
        return color;
    }
}
