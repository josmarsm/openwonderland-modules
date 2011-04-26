/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.SPI;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.util.concurrent.Semaphore;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class MoveForwardMethod implements ScriptMethodSPI {

    private Cell cell;
    private CellTransform transform;
    private Vector3f position;
    private Vector3f lookAt;
    private Semaphore lock = new Semaphore(0);
    private float distance = 0;
    private float seconds = 0; //in seconds
    private Vector3f normal = null;
    public String getFunctionName() {
        //transform.getTranslation(null).normalize().m
        return "MoveForward";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        distance = ((Double)args[1]).floatValue();
        seconds = ((Double)args[2]).floatValue();

        transform = cell.getLocalTransform();
        transform.getLookAt(position, lookAt);

        Vector3f v = position.subtract(lookAt);
        normal = v.normalize();
        
        
    }

    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getCategory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


        class TranslationProcessor extends ProcessorComponent {

        private WorldManager worldManager = null;

        private Vector3f translate = null;

        private Node target = null;

        private String name = null;

        int frameIndex = 0;
        private Vector3f incrementer = null;
        private boolean done = false;

        //as a post process
        private Node parent = null;
        private Node targetClone = null;

        public TranslationProcessor(String name, Node target, float increment) {
            this.worldManager = ClientContextJME.getWorldManager();
            this.target = target;

            this.name = name;
            setArmingCondition(new NewFrameCondition(this));
            //translate = target.getLocalTranslation();
            translate = target.getLocalTranslation();
            normal = normal.mult(distance);
            incrementer = new Vector3f(normal.x/(30*seconds),
                                       0,
                                       normal.z/(30*seconds));
            done = false;

        }

            @Override
        public String toString() {
            return (name);
        }

        /**
         * The initialize method
         */
        public void initialize() {
            //setArmingCondition(new NewFrameCondition(this));
        }

        /**
         * The Calculate method
         */
        public void compute(ProcessorArmingCollection collection) {
            if(frameIndex >=  30*seconds) {
                done = true;
                return;
                //this.getEntity().removeComponent(TranslationProcessor.class);
            }
            translate.add(incrementer);
            String position = "X: " + translate.x + "\n"
                    + "Y: " +translate.y + "\n"
                    + "Z: " +translate.z;
            System.out.println(position);
            //quaternion.fromAngles(0.0f, degrees, 0.0f);
            frameIndex +=1;

        }

        /**
         * Currently, the cell's geometry gets moved without the cell.
         * If we try to move the cell once the geometry has moved, the
         * geometry's translation get's moved with it. -No bueno.
         *
         * If we try to 0 the translation before the cell gets moved, both the
         * cell and geometry wind up at 0, 0, 0 - No bueno tambien.
         *
         * Other ideas?
         */
        public void commit(ProcessorArmingCollection collection) {
            if(done) {
//                this.getEntity().removeComponent(TranslationProcessor.class);
//
//
//                CellTransform transform = cell.getLocalTransform();
//                Vector3f translation = target.getWorldTranslation();
//                Vector3f inverted = new Vector3f();
////                translation.x += translate.x;
////                translation.y += translate.y;
////                translation.z += translate.z;
//                //inverted.x += translate.x * -1;
//                //inverted.y += translate.y * -1;
//                //inverted.z += translate.z * -1;
//                transform.setTranslation(translation);
//                target.setLocalTranslation(inverted);
//                worldManager.addToUpdateList(target);
//                getMovable(cell).localMoveRequest(transform);
//
//                lock.release(); //this should give control back to the state machine.
////                String position = "X: " + translate.x + "\n"
////                        +       "Y: " + translate.y + "\n"
////                        +       "Z: " + translate.z;
////
////                String incs = "Xinc: " + xInc + "\n"
////                        +       "Yinc: " + yInc + "\n"
////                        +       "Zinc: " + zInc;
////
////                System.out.println("global position: " + translation);
////                System.out.println("local position: " + inverted);
////                System.out.println("Incs: "+ incs);
                return;
            }

            //CellTransform transform = cell.getWorldTransform();

           // transform.setTranslation(translate);
            //getMovable(cell).localMoveRequest(transform);

            target.setLocalTranslation(translate);
            worldManager.addToUpdateList(target);
        }
    }

}
