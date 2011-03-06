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
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 * Usage: animateMove(cell, x, y, z, time);
 */
/**
 *
 * @author JagWire
 */
@ScriptMethod
public class AnimateMoveMethod implements ScriptMethodSPI {

    private Cell cell;
    private float x;
    private float y;
    private float z;
    private float seconds;
    private Semaphore lock;
    private Node node;
    public String getFunctionName() {
        return "animateMove";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        BasicRenderer r = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        node = r.getSceneRoot();
        
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
                node = r.getSceneRoot();
                
                r.getEntity().addComponent(TranslationProcessor.class,
                                           new TranslationProcessor("trans", node,0));
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
                +"-moves relative to the current position";
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

    class TranslationProcessor extends ProcessorComponent {
        /**
         * The WorldManager - used for adding to update list
         */
        private WorldManager worldManager = null;

        /**
         * The rotation matrix to apply to the target
         */
        private Vector3f translate;

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
        float xInc = 0;
        float yInc = 0;
        float zInc = 0;

        public TranslationProcessor(String name, Node target, float increment) {
            this.worldManager = ClientContextJME.getWorldManager();
            this.target = target;
            this.name = name;
            setArmingCondition(new NewFrameCondition(this));
            translate = target.getLocalTranslation();
            xInc = (float) (x / Double.valueOf(30*seconds));
            yInc = (float) (y / Double.valueOf(30*seconds));
            zInc = (float) (z / Double.valueOf(30*seconds));
            
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
            if(frameIndex >  30*seconds) {
                this.getEntity().removeComponent(TranslationProcessor.class);

                CellTransform transform = cell.getLocalTransform();
                transform.setTranslation(translate);
                getMovable(cell).localMoveRequest(transform);
                lock.release(); //this should give control back to the state machine.
                return;
            }
            translate.x += xInc;
            translate.y += yInc;
            translate.z += zInc;
            //quaternion.fromAngles(0.0f, degrees, 0.0f);
            frameIndex +=1;

        }

        /**
         * The commit method
         */
        public void commit(ProcessorArmingCollection collection) {
            target.setLocalTranslation(translate);
            worldManager.addToUpdateList(target);
        }
    }
}
