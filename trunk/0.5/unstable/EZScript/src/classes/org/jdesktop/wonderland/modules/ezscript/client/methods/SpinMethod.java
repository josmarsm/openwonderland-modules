
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.scene.Node;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
//import org.jdesktop.wonderland.common.wfs.CellList.Cell;
//import org.jdesktop.wonderland.common.wfs.CellList.Cell;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.cell.AnotherMovableComponent;

/**
 * LOTS OF MATH PROBLEMS HERE.
 *
 * @author JagWire
 */
@ScriptMethod
public class SpinMethod implements ScriptMethodSPI {

    Cell cell;
    float rotations;
    float time; //in seconds, assume 30 frames per second
    private Semaphore lock;
    private final static Logger logger = Logger.getLogger(SpinMethod.class.getName());
    public String getFunctionName() {
        return "spin";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        rotations = ((Double)args[1]).floatValue();
        time = ((Double)args[2]).floatValue();
        lock = new Semaphore(0);       
    }

    public void run() {
        SceneWorker.addWorker(new WorkCommit() {

            public void commit() {
                BasicRenderer r = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                Node node = r.getSceneRoot();                

                if(r.getEntity().hasComponent(SpinProcessor.class)) {
                    lock.release();
                    return;
                }
                
                r.getEntity().addComponent(SpinProcessor.class,
                        new SpinProcessor("Spin",
                                           node,
                                           (rotations * FastMath.PI * 2)/(30.0f*time)));
            }
        });
        try {
            lock.acquire();
        } catch(InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("spin finished...");
        }
    }

    public String getDescription() {
        return "usage: spin(cell, rotations, time)\n\n"
                +"-spin a cell a certain amount of rotations in the specified duration of seconds."
                +"-blocks on the executing thread until animation is finished.";
                
    }

    public String getCategory() {
        return "animation";
    }

    public MovableComponent getMovable(Cell cell) {
        if (cell.getComponent(MovableComponent.class) != null) {
            return cell.getComponent(MovableComponent.class);
        }


        //try and add MovableComponent manually
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

    class SpinProcessor extends ProcessorComponent {

        private float increment = 0.0f;
        private Quaternion quaternion = new Quaternion();
        private String name = null;
        private float[] angles;
        int frameIndex = 0;
        private boolean done = false;
        CellTransform transform;

        public SpinProcessor(String name, Node target, float increment) {
            this.increment = increment;
            this.name = name;
            setArmingCondition(new NewFrameCondition(this));
            
            transform = cell.getLocalTransform();
            quaternion = transform.getRotation(null);
            angles = quaternion.toAngles(null);

            for(float f: angles)
                System.out.println(f);
        }

        @Override
        public String toString() {
            return (name);
        }

        public void initialize() {
        }

        public void compute(ProcessorArmingCollection collection) {
            if(frameIndex >= 30*time) {
                this.getEntity().removeComponent(SpinProcessor.class);
                lock.release();
                done = true;
            }
            angles[1] += increment;
            //1 revolution = (3.14 * 2) ~ 6.28
            //
            System.out.println("current radians: "+angles[1]);
            //quaternion.fromAngles(0.0f, increment, 0.0f);
            quaternion = new Quaternion(new float[] {angles[0], angles[1], angles[2] });
            frameIndex +=1;
        }

        public void commit(ProcessorArmingCollection collection) {
            if(done)
                return;

            CellTransform transform = cell.getLocalTransform();
            transform.setRotation(quaternion);
            getMovable(cell).localMoveRequest(transform);
            Set<String> s = new HashSet<String>();
            
        }
    }
}
