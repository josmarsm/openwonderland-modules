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
package org.jdesktop.wonderland.modules.proximitytest.client;

import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 * Cell renderer for proximity test
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class ProximityCellRenderer extends BasicRenderer {
    private Node node = null;

    private final Map<BVRecord, BoundsViewerEntity> boundsViewers =
            new HashMap<BVRecord, BoundsViewerEntity>();

    public ProximityCellRenderer(Cell cell) {
        super(cell);
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.VISIBLE && increasing == true) {
            CollisionComponent cc = entity.getComponent(CollisionComponent.class);
            cc.setCollidable(false);
        } else if (status == CellStatus.INACTIVE && increasing == false) {
            for (BoundsViewerEntity bve : boundsViewers.values()) {
                bve.dispose();
            }
            boundsViewers.clear();
        }
    }

    protected void updateRenderer(List<BoundingVolume> clientBounds,
                                  List<BoundingVolume> serverBounds)
    {
        for (BoundsViewerEntity bve : boundsViewers.values()) {
            bve.dispose();
        }
        boundsViewers.clear();

        int i = 0;
        for (BoundingVolume bv : clientBounds) {
            BoundsViewerEntity bve = new BoundsViewerEntity(cell);
            bve.showBounds(bv);
            boundsViewers.put(BVRecord.newClientRecord(i++), bve);
        }

        i = 0;
        for (BoundingVolume bv : serverBounds) {
            BoundsViewerEntity bve = new BoundsViewerEntity(cell);
            bve.showBounds(bv);
            boundsViewers.put(BVRecord.newServerRecord(i++), bve);
        }
    }

    protected void setSolid(boolean server, int index, boolean solid) {
        BVRecord bvr = server ?
            BVRecord.newServerRecord(index) :
            BVRecord.newClientRecord(index);

        BoundsViewerEntity bve = boundsViewers.get(bvr);
        if (bve != null) {
            bve.setSolid(solid);
        } else {
            System.out.println("No entity found for " + bvr);
        }
    }

    protected Node createSceneGraph(Entity entity) {

        /* Fetch the basic info about the cell */
        String name = cell.getCellID().toString();

        /* Create the new mesh for the shape */
        TriMesh mesh = new Box(name, new Vector3f(), 0.5f, 0.5f, 0.5f);

        /* Create the scene graph object and set its wireframe state */
        node = new Node();
        node.attachChild(mesh);
        node.setModelBound(new BoundingSphere());
        node.updateModelBound();
        node.setName("Cell_"+cell.getCellID()+":"+cell.getName());

        return node;
    }

    private static class BVRecord {
        enum Type { SERVER, CLIENT };

        private Type type;
        private int index;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BVRecord other = (BVRecord) obj;
            if (this.type != other.type) {
                return false;
            }
            if (this.index != other.index) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + this.type.hashCode();
            hash = 37 * hash + this.index;
            return hash;
        }


        public static BVRecord newClientRecord(int index) {
            BVRecord rec = new BVRecord();
            rec.type = Type.CLIENT;
            rec.index = index;

            return rec;
        }

        public static BVRecord newServerRecord(int index) {
            BVRecord rec = new BVRecord();
            rec.type = Type.SERVER;
            rec.index = index;

            return rec;
        }
    }
}
