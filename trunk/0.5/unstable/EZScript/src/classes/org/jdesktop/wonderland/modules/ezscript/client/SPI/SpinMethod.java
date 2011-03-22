
package org.jdesktop.wonderland.modules.ezscript.client.SPI;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
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
 * LOTS OF MATH PROBLEMS HERE.
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

    class SpinProcessor extends ProcessorComponent {

        private WorldManager worldManager = null;

        private float radians = 0.0f;

        private float increment = 0.0f;

        private Quaternion quaternion = new Quaternion();

        private Node target = null;

        private String name = null;
        private float[] angles;
        int frameIndex = 0;
        private boolean done = false;

        public SpinProcessor(String name, Node target, float increment) {
            this.worldManager = ClientContextJME.getWorldManager();
            this.target = target;
            this.increment = increment;
            this.name = name;
            setArmingCondition(new NewFrameCondition(this));
            quaternion = target.getWorldRotation();//cell.getLocalTransform().getRotation(null);
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
            radians += increment;
            //1 revolution = (3.14 * 2) ~ 6.28
            //
            System.out.println("current radians: "+radians);
            //quaternion.fromAngles(0.0f, increment, 0.0f);
            quaternion = new Quaternion(new float[] {angles[0], radians, angles[2] });
            frameIndex +=1;

        }

        public void commit(ProcessorArmingCollection collection) {
            if(done)
                return;

            target.setLocalRotation(quaternion);
            worldManager.addToUpdateList(target);
        }
    }
}
