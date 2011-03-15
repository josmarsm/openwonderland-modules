/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.SPI;

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
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
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
        return "animation";
    }

    public MovableComponent getMovable(Cell cell) {
        if(cell.getComponent(MovableComponent.class) != null) {
            return cell.getComponent(MovableComponent.class);
        }
        return null;
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

        public ScaleProcessor(String name, Node target, float scale) {
            this.worldManager = ClientContextJME.getWorldManager();
            this.target = target;
            this.scale = scale;
            this.name = name;
            setArmingCondition(new NewFrameCondition(this));
            currentScale = target.getLocalScale().length();
            target.getLocalScale().length();

            // s = 5; scale = 12
            // 12 - 5 = 6
            // 6/(30 * 3) = 1/15
            // 5 += (1/15)
            //
            // s = 5; scale = 3
            // 3 - 5 = -2
            // -2/(30*3) = -1/30
            //5 += (-1/30)
            Vector3f scaleVector = new Vector3f();
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
                this.getEntity().removeComponent(ScaleProcessor.class);
                lock.release();
                CellTransform transform = cell.getLocalTransform();
                transform.setScaling(this.scale);
                getMovable(cell).localMoveRequest(transform);
                return;
            }
            currentScale += inc;
            frameIndex +=1;
        }

        public void commit(ProcessorArmingCollection collection) {

            target.setLocalScale(currentScale);
            worldManager.addToUpdateList(target);
        }
    }
}
