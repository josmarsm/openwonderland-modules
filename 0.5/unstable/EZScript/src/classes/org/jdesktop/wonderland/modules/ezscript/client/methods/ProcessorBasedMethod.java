/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.scene.Node;
import java.util.concurrent.Semaphore;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;

/**
 *
 * @author JagWire
 */
public abstract class ProcessorBasedMethod implements ReturnableScriptMethodSPI {
    protected Cell cell;
    protected Semaphore lock = new Semaphore(0);
    
    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
    }

    public void run() {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                BasicRenderer r = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                Node n = r.getSceneRoot();
                r.getEntity().addComponent(ProcessorTemplate.class,
                                           new ProcessorTemplate());
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

    public abstract void _compute();
    public abstract void _commit();
    public abstract boolean finished();

    class ProcessorTemplate extends ProcessorComponent {
 
        private String name = "Processor Template";

        public ProcessorTemplate() {

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
           //do nothing
        }

        /**
         * The Calculate method
         */
        public void compute(ProcessorArmingCollection collection) {
            if(finished()) {
                this.getEntity().removeComponent(ProcessorTemplate.class);
            }
            _compute();
        }

        /**
         * The commit method
         */
        public void commit(ProcessorArmingCollection collection) {
            _commit();
        }
    }
}
