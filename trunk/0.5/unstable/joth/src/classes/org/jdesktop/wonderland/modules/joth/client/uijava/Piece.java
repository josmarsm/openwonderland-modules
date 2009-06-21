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
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
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
    private static final float DISK_RADIUS = 0.15f;

    /** The height of a disk. */
    private static final float DISK_HEIGHT = 0.1f;

    /** The cylinder of the piece. */
    Cylinder cyl;

    private Board.Color color;

    public Piece (Board.Color color) {
        super("Node for a " + color + " piece");
        this.color = color;

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                setLocalTranslation(new Vector3f(0f, 0f, Z_OFFSET));
                cyl = new Cylinder(Piece.this.color + " Cylinder", 20, 20, DISK_RADIUS, DISK_HEIGHT, true);
                cyl.setModelBound(new BoundingBox());
                cyl.updateModelBound();
                attachChild(cyl);
                ClientContextJME.getWorldManager().addToUpdateList(Piece.this);
            }
        } , null);

        if (color == Board.Color.WHITE) {
            setColor(WHITE);
        } else {
            setColor(BLACK);
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setColor(final ColorRGBA color) {
        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                MaterialState ms = (MaterialState) cyl.getRenderState(RenderState.RS_MATERIAL);
                if (ms == null) {
                    ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
                    cyl.setRenderState(ms);
                }
                ms.setAmbient(new ColorRGBA(color));
                ms.setDiffuse(new ColorRGBA(color));
                ClientContextJME.getWorldManager().addToUpdateList(Piece.this);
            }
        }, null); 
    }

    public Board.Color getColor () {
        return color;
    }
}
