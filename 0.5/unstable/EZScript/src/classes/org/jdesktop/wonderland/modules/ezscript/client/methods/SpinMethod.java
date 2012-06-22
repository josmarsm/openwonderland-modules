
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.FastMath;
import com.jme.scene.Node;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
//import org.jdesktop.wonderland.common.wfs.CellList.Cell;
//import org.jdesktop.wonderland.common.wfs.CellList.Cell;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.cell.AnotherMovableComponent;

/**
 * LOTS OF MATH PROBLEMS HERE.
 *
 * @author JagWire
 */
//@ScriptMethod
public class SpinMethod implements ScriptMethodSPI {

    Cell cell;
    float rotations;
    float time; //in seconds, assume 30 frames per second
    private Semaphore lock;
    private final static Logger logger = Logger.getLogger(SpinMethod.class.getName());
    private AnotherMovableComponent amc = null;
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
//        SceneWorker.addWorker(new WorkCommit() {
//
//            public void commit() {
//                BasicRenderer r = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
//                Node node = r.getSceneRoot();
//
//                if(r.getEntity().hasComponent(SpinProcessor.class)) {
//                    lock.release();
//                    return;
//                }
//
//                r.getEntity().addComponent(SpinProcessor.class,
//                        new SpinProcessor("Spin",
//                                           node,
//                                           (rotations * FastMath.PI * 2)/(30.0f*time)));
//            }
//        });
//        try {
//            lock.acquire();
//        } catch(InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            System.out.println("spin finished...");
//        }
    }

    public String getDescription() {
        return "usage: spin(cell, rotations, time)\n\n"
                +"-spin a cell a certain amount of rotations in the specified duration of seconds."
                +"-blocks on the executing thread until animation is finished.";
                
    }

    public String getCategory() {
        return "Object Movement";
    }




}
