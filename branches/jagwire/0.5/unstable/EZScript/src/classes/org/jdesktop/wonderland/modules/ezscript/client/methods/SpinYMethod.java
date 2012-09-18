/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.FastMath;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.processors.SpinYProcessor;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class SpinYMethod implements ScriptMethodSPI {
//
//    private Cell cell;
//    private float rotations;
//    private float time; //in seconds
//    private Semaphore lock;
    private static final Logger logger = Logger.getLogger(SpinYMethod.class.getName());

    public String getFunctionName() {
        return "SpinY";
    }

    public void setArguments(Object[] args) {

        new SpinYInvoker((Cell) args[0],
                ((Double) args[1]).floatValue(),
                ((Double) args[2]).floatValue()).invoke();
//        cell = (Cell) args[0];
//        rotations = ((Double) args[1]).floatValue();
//        time = ((Double) args[2]).floatValue();
//        lock = new Semaphore(0);
    }

    public String getDescription() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return "Spin a cell on the y-axis, like a top.\n"
                + "-- usage: SpinY(cell, <rotations>, <seconds>);";
    }

    public String getCategory() {
        return "Object Movement";
    }

    public void run() {
    }

//        SceneWorker.addWorker(new WorkCommit() {
//
//            public void commit() {
//
//                BasicRenderer r = (BasicRenderer) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
//
//                logger.warning("CHECKING " + cell.getName() + "FOR SPIN-Y PROCESSOR...");
//                if (r.getEntity().hasComponent(SpinYProcessor.class)) {
//                    logger.warning(cell.getName() + " ALREADY HAS SPIN-Y PROCESSOR - RELEASING LOCK!");
//                    lock.release();
//
//                    return;
//                }
//
//                logger.warning("ADDING SPIN-Y PROCESSOR TO " + cell.getName());
//                r.getEntity().addComponent(SpinYProcessor.class,
//                        new SpinYProcessor("Spin",
//                        cell,
//                        time,
//                        (rotations * FastMath.PI * 2) / (30.0f * time),
//                        lock));
//            }
//        });
//        try {
//            lock.acquire();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            System.out.println("SpinY finished...");
//        }
//    }
    private static class SpinYInvoker {

        private final float time;
        private final Cell cell;
        private final float rotations;

        public SpinYInvoker(Cell cell, float rotations, float time) {
            this.cell = cell;
            this.rotations = rotations;
            this.time = time;
        }

        public void invoke() {
            final Semaphore lock = new Semaphore(0);
            SceneWorker.addWorker(new WorkCommit() {

                public void commit() {
                    BasicRenderer r = (BasicRenderer) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);

                    logger.warning("CHECKING " + cell.getName() + "FOR SPIN-Y PROCESSOR...");
                    if (r.getEntity().hasComponent(SpinYProcessor.class)) {
                        logger.warning(cell.getName() + " ALREADY HAS SPIN-Y PROCESSOR - RELEASING LOCK!");
                        lock.release();

                        return;
                    }

                    logger.warning("ADDING SPIN-Y PROCESSOR TO " + cell.getName());
                    r.getEntity().addComponent(SpinYProcessor.class,
                            new SpinYProcessor("Spin",
                            cell,
                            time,
                            (rotations * FastMath.PI * 2) / (30.0f * time),
                            lock));
                }
            });

            try {
                lock.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                logger.warning(cell.getName() + " SpinY finished!");
            }
        }
    }
}
