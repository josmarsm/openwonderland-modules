
package org.jdesktop.wonderland.modules.ezscript.client.SPI;

import com.jme.math.Quaternion;
import com.jme.scene.Node;
import java.util.concurrent.Semaphore;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.ezscript.client.StringStateMachine;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class SpinMethod implements ScriptMethodSPI {

    Cell cell;
    float rotations;
    float time; //in seconds, assume 30 frames per second
    StringStateMachine machine;//optional
    private Semaphore lock;
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
                r.getEntity().addComponent(SpinProcessor.class, new SpinProcessor("Spin", node, 360/(30*time)));
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

    class SpinProcessor extends ProcessorComponent {
        /**
         * The WorldManager - used for adding to update list
         */
        private WorldManager worldManager = null;
        /**
         * The current degrees of rotation
         */
        private float degrees = 0.0f;

        /**
         * The increment to rotate each frame
         */
        private float increment = 0.0f;

        /**
         * The rotation matrix to apply to the target
         */
        private Quaternion quaternion = new Quaternion();

        /**
         * The rotation target
         */
        private Node target = null;

        /**
         * A name
         */
        private String name = null;

        /**
         * The constructor
         */
        int frameIndex = 0;
        public SpinProcessor(String name, Node target, float increment) {
            this.worldManager = ClientContextJME.getWorldManager();
            this.target = target;
            this.increment = increment;
            this.name = name;
            setArmingCondition(new NewFrameCondition(this));
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
            if(frameIndex == 30*time) {
                this.getEntity().removeComponent(SpinProcessor.class);
                lock.release();

            }
            degrees += increment;
            quaternion.fromAngles(0.0f, degrees, 0.0f);
            frameIndex +=1;

        }

        /**
         * The commit method
         */
        public void commit(ProcessorArmingCollection collection) {
            target.setLocalRotation(quaternion);
            worldManager.addToUpdateList(target);
        }
    }
}
