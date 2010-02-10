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
package org.jdesktop.wonderland.modules.poster.client;

import com.jme.bounding.BoundingBox;
import com.jme.scene.Node;
import java.awt.Image;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;

/**
 * An example of a cell renderer.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class PosterCellRenderer extends BasicRenderer {
    private static final Logger posterLogger = Logger.getLogger(PosterCellRenderer.class.getName());
    private Node node = null;

    public PosterCellRenderer(Cell cell) {
        super(cell);
    }

    public void updateText() {
        posterLogger.info("update text");
        node.detachAllChildren();
        node.attachChild(getPosterNode());
        node.setModelBound(new BoundingBox());
        node.updateModelBound();

        ClientContextJME.getWorldManager().addToUpdateList(node);
    }

     private PosterNode getPosterNode() {
         Image posterImage = ((PosterCell)cell).getPosterImage();
         posterLogger.info("poster image: " + posterImage);
         if (posterImage == null) {
              return null;
         }
        return new PosterNode(posterImage);
    }

    protected Node createSceneGraph(Entity entity) {
        /* Create the new labelNode for the shape */
        Node labelNode = this.getPosterNode();
        posterLogger.info("labelNode: " + labelNode);
        if (labelNode == null) {
          node = new Node();
          return node;
        }

        node = new Node();
        node.attachChild(labelNode);
        node.setModelBound(new BoundingBox());
        node.updateModelBound();
        node.setName("Cell_" + cell.getCellID() + ":" + cell.getName());
        return node;
    }
}
