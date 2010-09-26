/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.scriptingPoster.client;

import com.jme.bounding.BoundingBox;
import com.jme.scene.Node;
import java.awt.Image;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;

/**
 * The renderer for the PosterCell. <br>
 * Adapted from the ShapeCell originally written by Jordan Slott <jslott@dev.java.net>
 * 
 * @author Bernard Horan
 */
public class ScriptingPosterCellRenderer extends BasicRenderer {
    private static final Logger posterLogger = Logger.getLogger(ScriptingPosterCellRenderer.class.getName());
    private Node node = null;

    public ScriptingPosterCellRenderer(Cell cell) {
        super(cell);
    }

    public void updateNode() {
        //posterLogger.info("update node");
        node.detachAllChildren();
        node.attachChild(getPosterNode());
        node.setModelBound(new BoundingBox());
        node.updateModelBound();

        ClientContextJME.getWorldManager().addToUpdateList(node);
    }

     private ScriptingPosterNode getPosterNode() {
         Image posterImage = ((ScriptingPosterCell)cell).getPosterImage();
         //posterLogger.info("poster image: " + posterImage);
         if (posterImage == null) {
              return null;
         }

        return new ScriptingPosterNode(posterImage, ((ScriptingPosterCell)cell).getBillboardMode());
    }

    protected Node createSceneGraph(Entity entity) {
        /* Create the new poster node for the image */
        Node posterNode = this.getPosterNode();
        //posterLogger.info("posterNode: " + posterNode);
        if (posterNode == null) {
          node = new Node();
          return node;
        }

        node = new Node();
        node.attachChild(posterNode);
        node.setModelBound(new BoundingBox());
        node.updateModelBound();
        node.setName("Cell_" + cell.getCellID() + ":" + cell.getName());
        return node;
    }
}
