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

import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import com.jme.bounding.BoundingBox;

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

    private Node parentNode;

    public SquareGeometry (final Node parentNode, int row, int col) {
        super("Quad for square " + row + "," + col, WIDTH, HEIGHT);
        this.parentNode = parentNode;

        ColorRGBA color;
        if ((row ^ col) == 0) {
            color = LIGHT_COLOR;
        } else {
            color = DARK_COLOR;
        }
        setColor(color);

        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                setModelBound(new BoundingBox());
                updateModelBound();
                ClientContextJME.getWorldManager().addToUpdateList(parentNode);
            }
        }, null); 
    }

    public void setColor(final ColorRGBA color) {
        ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {
            public void update(Object arg0) {
                MaterialState ms = (MaterialState) getRenderState(RenderState.RS_MATERIAL);
                if (ms == null) {
                    ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
                    setRenderState(ms);
                }
                ms.setAmbient(new ColorRGBA(color));
                ms.setDiffuse(new ColorRGBA(color));
                ClientContextJME.getWorldManager().addToUpdateList(parentNode);
            }
        }, null); 
    }

    public ColorRGBA getColor() {
        MaterialState ms = null;
        ms = (MaterialState) getRenderState(RenderState.RS_MATERIAL);
        if (ms == null) {
            return new ColorRGBA(1f, 1f, 1f, 1f);
        } else {
            return ms.getDiffuse();
        }
    }
}
