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

package org.jdesktop.wonderland.modules.joth.client;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Cylinder;
import com.jme.bounding.BoundingBox;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.jme.ClientContextJME;

/***************************************************************
 * Piece: A 3D disk which represents a game piece.
 * @author deronj@dev.java.net
 */
public class Piece extends Node {

    // TODO: note: for now, pieces have a single color.

    /** The distance of the center of the piece above the board. */
    private static final float Z_OFFSET = 0.2f;

    /** The radius of a disk. */
    private static final float DISK_RADIUS = 0.2f;

    /** The height of a disk. */
    private static final float DISK_HEIGHT = 0.1f;

    Board.Color color;

    public Piece (Board.Color color) {
        super("Node for a " + color + " piece");
        this.color = color;

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                setLocalTranslation(new Vector3f(0f, 0f, Z_OFFSET));
                Cylinder cyl = new Cylinder(Piece.this.color + " Cylinder", 10, 10, DISK_RADIUS, DISK_HEIGHT);
                cyl.setModelBound(new BoundingBox());
                cyl.updateModelBound();
                attachChild(cyl);
                ClientContextJME.getWorldManager().addToUpdateList(Piece.this);
            }
        } , null);
    }

    public Board.Color getColor () {
        return color;
    }
}
