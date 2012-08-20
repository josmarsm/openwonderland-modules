/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.globals.builder.brushes;

import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellCache.CellCacheListener;
import org.jdesktop.wonderland.client.cell.CellManager;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.BoundingVolumeHint;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeViewerEntity;
import org.jdesktop.wonderland.modules.ezscript.client.cell.SmallCell;
import org.jdesktop.wonderland.modules.ezscript.client.cell.StaticCellRenderer;
import org.jdesktop.wonderland.modules.ezscript.client.globals.builder.MinecraftMouseListener;
import org.jdesktop.wonderland.modules.ezscript.common.cell.BlockServerState;
import org.jdesktop.wonderland.modules.ezscript.common.cell.iCellServerState;

/**
 *
 * @author Ryan
 */
public class CubeCellCreator {

    private static final Logger logger = Logger.getLogger(CubeCellCreator.class.getName());

    private static WonderlandSession session() {
        return LoginManager.getPrimary().getPrimarySession();
    }

    private static CellCache cache() {
        return ClientContextJME.getCellCache(session());
    }

    private static void WaitForRenderingStatusAndSetScenegraph(final SmallCell small,
            final String textureURL,
            final ColorRGBA color,
            final Vector3f position) {

        CellManager.getCellManager().addCellStatusChangeListener(new CellStatusChangeListener() {
            public void cellStatusChanged(Cell cell, CellStatus status) {
                if (status == CellStatus.RENDERING) {
                    CellRenderer cr = small.getRenderer();


                    if (cr != null) {



                        waitForChannelComponent(small);
                        //finally, move to the correct position

                        CellTransform transform = new CellTransform(new Quaternion(), position);
                        movable(small).localMoveRequest(transform);


                        final CellStatusChangeListener listenerRef = this;
                        new Thread(new Runnable() {
                            public void run() {
                                CellManager.getCellManager().removeCellStatusChangeListener(listenerRef);
                            }
                        }).start();
                    } else {
                        logger.warning("CELL RENDERER IS NULL!");
                        waitForCellRenderer(small);

                        CellTransform transform = new CellTransform(new Quaternion(), position);
                        movable(small).localMoveRequest(transform);


                        final CellStatusChangeListener listenerRef = this;
                        new Thread(new Runnable() {
                            public void run() {
                                CellManager.getCellManager().removeCellStatusChangeListener(listenerRef);
                            }
                            //problems!
                        }).start();
                
                    }
                }
            }

            

            private void waitForChannelComponent(SmallCell small) {

                while (small.getComponent(ChannelComponent.class) == null) {
                    try {
                        wait(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CubeCellCreator.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        continue;
                    }
                }
            }

            private void waitForCellRenderer(SmallCell small) {
                while(small.getRenderer() == null) {
                    try {
                        wait(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CubeCellCreator.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        continue;
                    }
                }
            }
        });


    }

    public static MovableComponent movable(Cell cell) {
        if (cell.getComponent(MovableComponent.class) != null) {
            return cell.getComponent(MovableComponent.class);
        }

        return null;
    }

    public static Cell create(final String textureURL,
            final ColorRGBA color,
            final Vector3f position) {

        Cell _cell = null;

        try {
            //create server state
            /*
             * Add position
             * Add Cell class name
             * Add renderer class name
             */

            logger.warning("BUILDING STATE");
//            iCellServerState state = new iCellServerState();
//            state.setCellClassName("org.jdesktop.wonderland.modules.ezscript.client.cell.SmallCell");
//            state.setRendererClassName("org.jdesktop.wonderland.modules.ezscript.client.cell.StaticCellRenderer");
//            state.setBoundingVolumeHint(new BoundingVolumeHint(true, new BoundingBox(position, 0.5f, 0.5f, 0.5f)));


            BlockServerState state = new BlockServerState();
            state.setMaterial(color);
            state.setTextureURL(textureURL);


            logger.warning("TELLING SERVER TO CREATE CELL");
            //tell the server to create the cell
            final CellID result = CellUtils.createCell(state);


            //get the cache so that we can add the cell listener below
            final CellCache cache = cache();

            logger.warning("ADDING CACHE LISTENER!");
            //add the cell listener
            cache.addCellCacheListener(new CellCacheListener() {
                public void cellLoaded(CellID cellID, Cell cell) {
                    if (cellID.equals(result)) {
//                        _cell = cache.getCell(cellID);
                        SmallCell small = (SmallCell) cell;

                        logger.warning("GETTING CELL RENDERER!");
                        //add the scenegraph to the renderer

                        WaitForRenderingStatusAndSetScenegraph(small, textureURL, color, position);


                        final CellCacheListener listenerRef = this;

                        new Thread(new Runnable() {
                            public void run() {
                                cache.removeCellCacheListener(listenerRef);
                            }
                        }).start();
                    }
                }

                public void cellLoadFailed(CellID cellID, String className, CellID parentCellID, Throwable cause) {
                }

                public void cellUnloaded(CellID cellID, Cell cell) {
                }
            });


            _cell = cache.getCell(result);


        } catch (CellCreationException ex) {
            Logger.getLogger(CubeCellCreator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return _cell;
        }


    }
}
