/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.TransformChangeListener.ChangeSource;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.cell.AnotherMovableComponent;

/**
 * Usage: animateMove(cell, x, y, z, time);
 */
/**
 *
 * @author JagWire
 * @author Abhishek Upadhyay
 */
@ExperimentalAPI
@ScriptMethod
public class AnimateMoveMethod implements ScriptMethodSPI {

    private Cell cell;
    private float x;
    private float y;
    private float z;
    private float seconds;
    private Semaphore lock;
    
    private static Logger logger = Logger.getLogger(AnimateMoveMethod.class.getName());
    
    public String getFunctionName() {
        return "animateMove";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];      
        x = ((Double)args[1]).floatValue();// + node.getLocalTranslation().x;
        y = ((Double)args[2]).floatValue();// + node.getLocalTranslation().y;
        z = ((Double)args[3]).floatValue();// + node.getLocalTranslation().z;
        
        seconds = ((Double)args[4]).floatValue();
        lock = new Semaphore(0);

    }

    public void run() {

        SceneWorker.addWorker(new WorkCommit() {        
            public void commit() {
                BasicRenderer r = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);

                if(r.getEntity().hasComponent(TranslationProcessor.class)) {
                    lock.release();
                    return;
                }
                
                r.getEntity().addComponent(TranslationProcessor.class,
                                           new TranslationProcessor("trans",0));
            }
        });

        //Block until animation is finished
        try {
            lock.acquire();            
        } catch(InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("animateMove finished!");
        }
    }

    public String getDescription() {
        return "usage: animateMove(cell, x, y, z, seconds);\n\n"
                +"-animate a cell moving to the x,y,z coordinates in the duration of seconds."
                +"-moves relative to the current position\n" +
                "-Because this function uses network synchronization automatically, it is STRONGLY encouraged that this function be used in conjunction to local callbacks.\n" +
                " For example: Context.onClick(function_name, true); This will ensure that the callback\n";

    }

    public String getCategory() {
        return "Object Movement";
    }

    public MovableComponent getMovable(Cell cell) {
        if (cell.getComponent(MovableComponent.class) != null) {
            return cell.getComponent(MovableComponent.class);
        }


        //try and add AnotherMovableComponent manually
        String className = "org.jdesktop.wonderland.server.cell.MovableComponentMO";
        CellServerComponentMessage cscm =
                CellServerComponentMessage.newAddMessage(
                                                   cell.getCellID(), className);

        ResponseMessage response = cell.sendCellMessageAndWait(cscm);
        if (response instanceof ErrorMessage) {
            logger.log(Level.WARNING, "Unable to add movable component "
                    + "for Cell " + cell.getName() + " with ID "
                    + cell.getCellID(),
                    ((ErrorMessage) response).getErrorCause());

                    return null;
        } else {
            return cell.getComponent(MovableComponent.class);
        }
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
            logger.warning("Acquiring lock in getMovable()!");
            if(!cell.getStatus().equals(CellStatus.DISK)) {
                movableLock.acquire();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return amc;
        }
    }

    class TranslationProcessor extends ProcessorComponent {

        private WorldManager worldManager = null;

        private Vector3f translate = null;

        private Node target = null;

        private String name = null;

        int frameIndex = 0;
        float xInc = 0;
        float yInc = 0;
        float zInc = 0;
        private boolean done = false;
        private CellTransform transform = null;

        public TranslationProcessor(String name, float increment) {
            this.worldManager = ClientContextJME.getWorldManager();
            
            this.name = name;
            setArmingCondition(new NewFrameCondition(this));
            //translate = target.getLocalTranslation();
            transform = cell.getLocalTransform();
            translate = transform.getTranslation(null);//cell.getLocalTransform().getTranslation(null);//target.getLocalTranslation();
            xInc = (float) (x / Double.valueOf(30*seconds));
            yInc = (float) (y / Double.valueOf(30*seconds));
            zInc = (float) (z / Double.valueOf(30*seconds));
            done = false;
            
        }

            @Override
        public String toString() {
            return (name);
        }


        public void initialize() {
        }

        public void compute(ProcessorArmingCollection collection) {
            if(frameIndex >=  30*seconds) {
                done = true;
                return;
                //this.getEntity().removeComponent(TranslationProcessor.class);
            }
            translate.x += xInc;
            translate.y += yInc;
            translate.z += zInc;
            String position = "X: " + translate.x + "\n"
                    + "Y: " +translate.y + "\n"
                    + "Z: " +translate.z;
//            System.out.println(position);
            //quaternion.fromAngles(0.0f, degrees, 0.0f);
            frameIndex +=1;

        }

        public void commit(ProcessorArmingCollection collection) {
            if(done) {
                this.getEntity().removeComponent(TranslationProcessor.class);
                
                lock.release(); //this should give control back to the state machine.
//                String position = "X: " + translate.x + "\n"
//                        +       "Y: " + translate.y + "\n"
//                        +       "Z: " + translate.z;
//
//                String incs = "Xinc: " + xInc + "\n"
//                        +       "Yinc: " + yInc + "\n"
//                        +       "Zinc: " + zInc;
//
//                System.out.println("global position: " + translation);
//                System.out.println("local position: " + inverted);
//                System.out.println("Incs: "+ incs);
                return;
            }

            transform.setTranslation(translate);
            getAnotherMovable(cell).localMoveRequest(transform,false);
            //cell.setLocalTransform(transform, ChangeSource.LOCAL);
        }
    }
}
