/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.SPI;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
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

    public String getFunctionName() {
       return "animateScale";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        scale = ((Double)args[1]).floatValue();
        seconds = ((Double)args[2]).floatValue();
    }

    public void run() {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                BasicRenderer r = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                Node n = r.getSceneRoot();
                r.getEntity().addComponent(ScaleProcessor.class, new ScaleProcessor("scale", n, scale));
            }
        });
    }

    class ScaleProcessor extends ProcessorComponent {
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
        private Vector3f s; //scale
        private float xInc;
        private float yInc;
        private float zInc;

        public ScaleProcessor(String name, Node target, float increment) {
            this.worldManager = ClientContextJME.getWorldManager();
            this.target = target;
            this.increment = increment;
            this.name = name;
            setArmingCondition(new NewFrameCondition(this));
            s = target.getLocalScale();
            xInc = scale/(30*seconds);
            yInc = scale/(30*seconds);
            zInc = scale/(30*seconds);

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
            if(frameIndex == 30*seconds) {
                this.getEntity().removeComponent(ScaleProcessor.class);

            }
            s.x += xInc;
            s.y += yInc;
            s.z += zInc;
            frameIndex +=1;
        }

        /**
         * The commit method
         */
        public void commit(ProcessorArmingCollection collection) {
            target.setLocalScale(s);
            worldManager.addToUpdateList(target);
        }
    }


}
