/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.FastMath;
import com.jme.scene.Node;
import java.util.concurrent.Semaphore;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;
import org.jdesktop.wonderland.modules.ezscript.client.processors.SpinXProcessor;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class SpinXMethod implements ScriptMethodSPI {

    private Cell cell;
    private float rotations;
    private float time; //in seconds
    private Semaphore lock;

    public String getFunctionName() {
        return "SpinX";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        rotations = ((Double)args[1]).floatValue();
        time = ((Double)args[2]).floatValue();
        lock = new Semaphore(0);
    }

    public String getDescription() {
        return "";
    }

    public String getCategory() {
        return "Object Movement";
    }

    public void run() {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                BasicRenderer r = (BasicRenderer) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);

                if (r.getEntity().hasComponent(SpinXProcessor.class)) {
                    lock.release();
                    return;
                }

                r.getEntity().addComponent(SpinXProcessor.class,
                        new SpinXProcessor("Spin", cell, time,
                        (rotations * FastMath.PI * 2) / (30.0f * time),
                        lock));
            }
        });
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("SpinX finished...");
        }
    }

}
