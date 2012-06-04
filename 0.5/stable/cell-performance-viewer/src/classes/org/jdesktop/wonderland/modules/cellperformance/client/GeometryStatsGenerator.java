/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.cellperformance.client;

import com.jme.image.Texture;
import com.jme.scene.Geometry;
import com.jme.scene.Spatial;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.StatisticsComponent;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellStatistics;
import org.jdesktop.wonderland.client.cell.CellStatistics.LongCellStat;
import org.jdesktop.wonderland.client.cell.CellStatistics.SizeCellStat;
import org.jdesktop.wonderland.client.cell.CellStatistics.TimeCellStat;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;

/**
 * Walk a cell's geometry and generate statistics about it.
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
class GeometryStatsGenerator {
    private static final Logger LOGGER =
            Logger.getLogger(GeometryStatsGenerator.class.getName());
    
    private final Cell cell;
    private final CountDownLatch done;

    private final LongCellStat entityCount;
    private final LongCellStat nodeCount;
    private final LongCellStat triangleCount;
    private final LongCellStat textureCount;

    private final SizeCellStat geometrySize;
    private final SizeCellStat textureSize;

    private final TimeCellStat renderTime;
    
    public GeometryStatsGenerator(Cell cell) {
        this.cell = cell;

        done = new CountDownLatch(1);

        entityCount = new LongCellStat("entitycount", "Entity Count");
        nodeCount = new LongCellStat("nodecount", "Node Count");
        triangleCount = new LongCellStat("trianglecount", "Triangle Count");
        textureCount = new LongCellStat("texturecount", "Texture Count");

        geometrySize = new SizeCellStat("geometrysize", "Geometry Size");
        textureSize = new SizeCellStat("texturesize", "Texture Size");

        renderTime = new TimeCellStat("rendertime", "Render Time", TimeUnit.NANOSECONDS);
    }
    
    public synchronized void generate() throws InterruptedException {
        // make sure we haven't already generated
        if (done.getCount() == 0) {
            return;
        }
        
        // add the worker that will do the actual work
        SceneWorker.addWorker(new StatWorker());
    }

    public void await() throws InterruptedException {
        done.await();
    }

    public boolean await(long time, TimeUnit unit) throws InterruptedException {
        return done.await(time, unit);
    }
        
    private class StatWorker implements WorkCommit, ProcessNodeInterface {
        private final Set<Spatial> processedNodes = new HashSet<Spatial>();
        private final Set<Integer> processedTextures = new HashSet<Integer>();

        //private final StringBuilder output = new StringBuilder();

        public void commit() {
            try {
                // find the renderer
                CellRendererJME renderer =
                        (CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                if (renderer == null) {
                    return;
                }

                // process the root entity
                processEntity(renderer.getEntity());
            
                // update the statistics
                CellStatistics stats = cell.getCellCache().getStatistics();
                stats.add(cell, entityCount);
                stats.add(cell, nodeCount);
                stats.add(cell, triangleCount);
                stats.add(cell, textureCount);

                stats.add(cell, geometrySize);
                stats.add(cell, textureSize);

                stats.add(cell, renderTime);
                
                //LOGGER.warning(output.toString());
            } finally {
                done.countDown();
            }
        }

        private void processEntity(Entity e) {
            //output.append("Process entity ").append(e.getName()).append("\n");
            
            entityCount.changeValue(1l);

            // figure out the most recent render time
            StatisticsComponent sc = e.getComponent(StatisticsComponent.class);
            if (sc != null) {
                renderTime.changeValue(sc.getLastRenderTime());
            }
            
            RenderComponent rc = e.getComponent(RenderComponent.class);
            if (rc != null) {
                TreeScan.findNode(rc.getSceneRoot(), this);

                // triangles are aggregated for all children of a node, so
                // only process nodes once
                triangleCount.changeValue((long) rc.getSceneRoot().getTriangleCount());
            }

            for (int i = 0; i < e.numEntities(); i++) {
                processEntity(e.getEntity(i));
            }
        }

        public boolean processNode(Spatial node) {
            if (processedNodes.contains(node)) {
                return false;
            }
            
            if (node instanceof RenderComponent.AttachPointNode) {
                return false;
            }

            //output.append("Process node ").append(node.getName()).append("\n");
            
            // count it
            nodeCount.changeValue(1l);

            // calculate buffer sizes
            if (node instanceof Geometry) {
                Geometry g = (Geometry) node;
                if (g.getColorBuffer() != null) {
                    geometrySize.changeValue((long) g.getColorBuffer().capacity());
                }
                if (g.getNormalBuffer() != null) {
                    geometrySize.changeValue((long) g.getNormalBuffer().capacity());
                }
                if (g.getVertexBuffer() != null) {
                    geometrySize.changeValue((long) g.getVertexBuffer().capacity());
                }
                if (g.getTangentBuffer() != null) {
                    geometrySize.changeValue((long) g.getTangentBuffer().capacity());
                }
                if (g.getBinormalBuffer() != null) {
                    geometrySize.changeValue((long) g.getBinormalBuffer().capacity());
                }
                if (g.getFogBuffer() != null) {
                    geometrySize.changeValue((long) g.getFogBuffer().capacity());
                }
            }

            // read texture information
            TextureState ts = (TextureState) node.getRenderState(StateType.Texture);
            if (ts != null) {
                for (int i = 0; i < ts.getNumberOfSetTextures(); i++) {
                    Texture t = ts.getTexture(i);

                    // make sure not to double count shared textures
                    if (!processedTextures.contains(t.getTextureId())) {
                        //output.append("Process texture ").append(t.getTextureId()).
                        //       append(" image size ").append(t.getMemoryReq()).append("\n");
                        
                        processedTextures.add(t.getTextureId());
                        textureCount.changeValue(1l);
                        textureSize.changeValue((long) t.getMemoryReq());
                    }
                }
            }

            // add to the list
            processedNodes.add(node);
            return true;
        }
    }

}
