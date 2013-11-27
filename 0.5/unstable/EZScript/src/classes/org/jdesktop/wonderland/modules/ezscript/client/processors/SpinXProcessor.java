/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.processors;

import com.jme.math.Quaternion;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.ezscript.client.cell.AnotherMovableComponent;

/**
 *
 * @author JagWire
 */
public class SpinXProcessor extends ProcessorComponent {

    protected float increment = 0.0f;
    protected Quaternion quaternion = new Quaternion();
    private String name = null;
    protected float[] angles;
    protected int frameIndex = 0;
    protected boolean done = false;
    protected CellTransform transform;
    protected float time;
    protected Semaphore lock;
    private Cell cell;
    private static final Logger logger = Logger.getLogger(SpinXProcessor.class.getName());
    private AnotherMovableComponent amc;

    public SpinXProcessor(String name,
            Cell cell,
            float time,
            float increment,
            Semaphore lock) {
        this.increment = increment;
        this.name = name;
        this.cell = cell;
        this.time = time;
        this.lock = lock;

        setArmingCondition(new NewFrameCondition(this));

        transform = cell.getLocalTransform();
        quaternion = transform.getRotation(null);
        angles = quaternion.toAngles(null);

        String anglesToString = "";
        for (float f : angles) {
//            System.out.println(f);
//            logger.warning("ANGLES: ")
            anglesToString += (f + ", ");
        }
        logger.warning("ANGLES: "+anglesToString);
    }

    @Override
    public String toString() {
        return (name);
    }

    public void initialize() {
    }

    public void compute(ProcessorArmingCollection collection) {
        if (frameIndex >= 30 * time) {
            this.getEntity().removeComponent(SpinXProcessor.class);
            lock.release();
            done = true;
        }
        angles[0] += increment;
        //1 revolution = (3.14 * 2) ~ 6.28
        //
        //logger.info("current (X) radians: " + angles[0]);
        //quaternion.fromAngles(0.0f, increment, 0.0f);
        quaternion = new Quaternion(new float[]{angles[0], angles[1], angles[2]});
        frameIndex += 1;
    }

    public void commit(ProcessorArmingCollection collection) {
        if (done) {
            return;
        }

        CellTransform transform = cell.getLocalTransform();
        transform.setRotation(quaternion);
//        getMovable(cell).localMoveRequest(transform, false);//transform);
        
        /*
         * Changing the above commented line to use true instead of false for
         * second parameter. The implication here is that the server will see
         * the move request. A new pattern should be adopted as this is quite
         * confusing.
         */
        
        AnotherMovableComponent amc = getMovable(cell);
        if(amc!=null) {
            amc.localMoveRequest(transform, false);
        }
    }

    public AnotherMovableComponent getMovable(final Cell cell) {
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
            System.out.println("cell.getStatus() : "+cell.getStatus());
            if(!cell.getStatus().equals(CellStatus.DISK)) {
                movableLock.acquire();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return amc;
        }
    }
}
