/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.Quaternion;
import com.jme.scene.Node;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.cell.AnotherMovableComponent;
/**
 * Usage: animateScale(cell, scale, seconds);
 */
/**
 *
 * @author JagWire
 */
@ScriptMethod
public class AnimateScaleMethod implements ScriptMethodSPI {

    private Cell cell;
    private float scale;
    private float seconds;
    private Semaphore lock;

    public String getFunctionName() {
       return "animateScale";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        scale = ((Double)args[1]).floatValue();
        seconds = ((Double)args[2]).floatValue();
        lock = new Semaphore(0);
    }

    public void run() {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                BasicRenderer r = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                Node n = r.getSceneRoot();
                if(r.getEntity().hasComponent(ScaleProcessor.class)) {
                    lock.release();
                    return;
                }
                r.getEntity().addComponent(ScaleProcessor.class, new ScaleProcessor("scale", n, scale));
            }
        });
        try {
            lock.acquire();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("animateScale finished...");
        }
    }

    public String getDescription() {
        return "usage: animateScale(cell, scale, seconds)\n\n"
                +"-animate a cell resizing by scale in the duration of seconds.\n"
                +"-the method blocks the executing thread until animation is finished.";

    }

    public String getCategory() {
        return "Object Movement";
    }

    public MovableComponent getMovable(Cell cell) {
        if(cell.getComponent(MovableComponent.class) != null) {
            return cell.getComponent(MovableComponent.class);
        }
        return null;
    }
    
    private AnotherMovableComponent amc;
    public AnotherMovableComponent getAnotherMovable(final Cell cell) {
        if (cell.getComponent(AnotherMovableComponent.class) != null) {
            return cell.getComponent(AnotherMovableComponent.class);
        }

        final Semaphore movableLock = new Semaphore(0);
        //<editor-fold defaultstate="collapsed" desc="legacy">
//        new Thread(new Runnable() {
//            
//            public void run() {
//                //try and add MovableComponent manually
//                String className = "org.jdesktop.wonderland.modules.ezscript.server.cell.AnotherMovableComponentMO";
//                CellServerComponentMessage cscm =
//                        CellServerComponentMessage.newAddMessage(
//                        cell.getCellID(), className);
//                logger.warning("Requesting AnotherMovableComponent...");
//                
//                ResponseMessage response = cell.sendCellMessageAndWait(cscm);
//                if (response instanceof ErrorMessage) {
//                    logger.log(Level.WARNING, "Unable to add movable component "
//                            + "for Cell " + cell.getName() + " with ID "
//                            + cell.getCellID(),
//                            ((ErrorMessage) response).getErrorCause());
//                    
//                    logger.warning("AnotherMovableComponent request failed!");
//                    movableLock.release();
//                    
//                } else {
//                    logger.warning("returning AnotherMovableComponent");
//                    movableLock.release();
//                    amc = cell.getComponent(AnotherMovableComponent.class);
//                }
//            }
//        }).start();
        //</editor-fold>

        try {
            //Logger.warning("Acquiring lock in getMovable()!");
            if(!cell.getStatus().equals(CellStatus.DISK)) {
                movableLock.acquire();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return amc;
        }
    }

    class ScaleProcessor extends ProcessorComponent {

        private WorldManager worldManager = null;
        private float degrees = 0.0f;
        private float scale = 0.0f;
        private Quaternion quaternion = new Quaternion();
        private Node target = null;
        private String name = null;
        int frameIndex = 0;
        private float currentScale; //scale
        private float inc;
        private boolean done = false;
        private CellTransform transform = null;
        public ScaleProcessor(String name, Node target, float scale) {
            this.worldManager = ClientContextJME.getWorldManager();
            this.target = target;
            this.scale = scale;
            this.name = name;
            setArmingCondition(new NewFrameCondition(this));

            transform = cell.getLocalTransform();
            currentScale = transform.getScaling();

            //currentScale = target.getLocalScale().length();
            //target.getLocalScale().length();

            // s = 5; scale = 12
            // 12 - 5 = 6
            // 6/(30 * 3) = 1/15
            // 5 += (1/15)
            //
            // s = 5; scale = 3
            // 3 - 5 = -2
            // -2/(30*3) = -1/30
            //5 += (-1/30)
            
            float scaleDifference = scale - currentScale;
            inc = scaleDifference/(30*seconds);
        }

            @Override
        public String toString() {
            return (name);
        }

        /**
         * The initialize method
         */
        public void initialize() {
        }

        /**
         * The Calculate method
         */
        public void compute(ProcessorArmingCollection collection) {
            if(frameIndex >= 30*seconds) {
                done = true;
                return;
            }

            currentScale += inc;
            frameIndex +=1;
        }

        public void commit(ProcessorArmingCollection collection) {
           if(done) {
                this.getEntity().removeComponent(ScaleProcessor.class);
                lock.release();
            }
           transform.setScaling(currentScale);
            getAnotherMovable(cell).localMoveRequest(transform);

        }
    }
}
