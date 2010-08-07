/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.programmingdemo.client.jme.cellrenderer;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.CellRefComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.client.jme.input.MouseEnterExitEvent3D;
import org.jdesktop.wonderland.modules.programmingdemo.client.SortCell;

/**
 * Renderer for sorted spheres
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */

public class SortCellRenderer extends BasicRenderer {
    private Node node = null;
    private List<SortSphere> sorted;

    private float scale = 1f;
    private float spacing = 1f;
    private int rowSize = 8;
    private Color color = Color.RED;

    private Entity sortParent;

    public SortCellRenderer(Cell cell) {
        super(cell);

        node = new Node();
    }

    public void reset(final List<Sortable> values) {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                WorldManager wm = ClientContextJME.getWorldManager();

                if (sortParent == null) {
                    sortParent = new Entity();

                    // add a render component for the sort parent entity, and add it as
                    // a child of the root entity
                    Node sortParentNode = new Node();
                    RenderComponent rc = wm.getRenderManager().createRenderComponent(sortParentNode);
                    sortParent.addComponent(RenderComponent.class, rc);
                    entityAddChild(entity, sortParent);
                    wm.addToUpdateList(sortParentNode);
                } else {
                    // remove all existing children
                    while (sortParent.numEntities() > 0) {
                        Entity r = sortParent.getEntity(0);
                        sortParent.removeEntity(r);
                    }
                }

                // now create a sphere for each child
                sorted = new ArrayList<SortSphere>(values.size());
                int idx = 0;
                for (Sortable s : values) {
                    SortSphere sphere = new SortSphere(s.getValue(),
                                                       color,
                                                       s.getColorPercentage());
                    sphere.setLocalTranslation(getTranslationFor(idx++));
                    sphere.setLocalScale(SortCellRenderer.this.scale);
                    sorted.add(sphere);
                       
                    // create an entity so we can make it pickable
                    Entity e = createEntity(sphere);
                    entityAddChild(sortParent, e);

                    // add a hover listener
                    SortHoverListener listener = new SortHoverListener(s.getValue());
                    listener.setIndex(idx - 1);
                    listener.addToEntity(e);
                    sphere.setHoverListener(listener);

                    wm.addToUpdateList(sphere);
                }
              
                wm.addToUpdateList(node);
            }
        });
    }

    public void swap(final int idx1, final int idx2) {
        if (idx1 == idx2) {
            return;
        }

        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                WorldManager wm = ClientContextJME.getWorldManager();

                int minIdx = Math.min(idx1, idx2);
                int maxIdx = Math.max(idx1, idx2);

                // get the current values
                SortSphere s1 = sorted.get(minIdx);
                SortSphere s2 = sorted.get(maxIdx);

                // remove higher index first
                sorted.remove(maxIdx);
                sorted.remove(minIdx);

                // now add them back into the array in the new order
                sorted.add(minIdx, s2);
                sorted.add(maxIdx, s1);

                // and set their position
                s1.setLocalTranslation(getTranslationFor(maxIdx));
                s2.setLocalTranslation(getTranslationFor(minIdx));

                // update listener
                s1.getHoverListener().setIndex(maxIdx);
                s2.getHoverListener().setIndex(minIdx);

                wm.addToUpdateList(s1);
                wm.addToUpdateList(s2);
            }
        });
    }

    public void highlight(final int idx, final boolean highlighted) {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                WorldManager wm = ClientContextJME.getWorldManager();

                SortSphere sphere = sorted.get(idx);
                sphere.setHighlighted(highlighted);

                wm.addToUpdateList(sphere);
            }
        });
    }

    public void setSpacing(final float spacing) {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                SortCellRenderer.this.spacing = spacing;

                WorldManager wm = ClientContextJME.getWorldManager();
                int idx = 0;

                for (SortSphere sphere : sorted) {
                    sphere.setLocalTranslation(getTranslationFor(idx++));
                    wm.addToUpdateList(sphere);
                }
            }
        });
    }

    public void setScale(final float scale) {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                SortCellRenderer.this.scale = scale;

                WorldManager wm = ClientContextJME.getWorldManager();

                for (SortSphere sphere : sorted) {
                    sphere.setLocalScale(scale);
                    wm.addToUpdateList(sphere);
                }
            }
        });
    }

    public void setRowSize(final int rowSize) {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                SortCellRenderer.this.rowSize = rowSize;

                WorldManager wm = ClientContextJME.getWorldManager();
                int idx = 0;

                for (SortSphere sphere : sorted) {
                    sphere.setLocalTranslation(getTranslationFor(idx++));
                    wm.addToUpdateList(sphere);
                }
            }
        });
    }

    public void setColor(final Color color) {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                SortCellRenderer.this.color = color;

                WorldManager wm = ClientContextJME.getWorldManager();
                for (SortSphere sphere : sorted) {
                    sphere.setColor(color);
                    wm.addToUpdateList(sphere);
                }
            }
        });
    }

    private Entity createEntity(SortSphere sphere) {
        WorldManager wm = ClientContextJME.getWorldManager();

        // create the entity and root node
        Entity out = new Entity();
       
        // add a render component
        RenderComponent rc = wm.getRenderManager().createRenderComponent(sphere);
        rc.setLightingEnabled(true);
        out.addComponent(RenderComponent.class, rc);

        // and a collision component
        JMECollisionSystem collisionSystem =
                (JMECollisionSystem) wm.getCollisionManager().loadCollisionSystem(JMECollisionSystem.class);
        CollisionComponent cc = collisionSystem.createCollisionComponent(sphere);
        cc.setCollidable(false);
        cc.setPickable(true);
        out.addComponent(CollisionComponent.class, cc);

        // and a reference to the cell
        out.addComponent(CellRefComponent.class, new CellRefComponent(cell));
        return out;
    }

    private Vector3f getTranslationFor(int index) {
        int rsq = rowSize * rowSize;

        int xidx = index % rowSize;
        int yidx = (int) Math.floor((index % rsq) / rowSize);
        int zidx = (int) Math.floor(index / rsq);
        
        return new Vector3f(xidx * spacing, yidx * spacing, zidx * spacing * -1);
    }
    
    protected Node createSceneGraph(Entity entity) {
        setCollisionEnabled(false);

        node.setModelBound(new BoundingBox(Vector3f.ZERO, 100, 100, 100));
        node.updateModelBound();
        node.setName("Cell_" + cell.getCellID() + ":" + cell.getName());

        return node;
    }

    public interface Sortable {
        public String getValue();
        public float getColorPercentage();
    }

    class SortHoverListener extends EventClassListener {
        private final String value;
        private int index;

        SortHoverListener(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseEnterExitEvent3D.class };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void commitEvent(Event event) {
            MouseEnterExitEvent3D me = (MouseEnterExitEvent3D) event;
            if (me.isEnter()) {
                ((SortCell) cell).setTooltipText("Position: " + getIndex() +
                                                 " Value: " + getValue());
            } else {
                ((SortCell) cell).setTooltipText(null);
            }
        }
    }
}
