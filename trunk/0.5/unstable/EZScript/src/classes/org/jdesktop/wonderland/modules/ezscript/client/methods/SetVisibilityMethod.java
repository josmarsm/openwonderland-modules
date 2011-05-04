/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.scene.Node;
import com.jme.scene.Spatial.CullHint;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class SetVisibilityMethod implements ScriptMethodSPI {

    private Cell cell;
    private BasicRenderer renderer;
    private Node node;
    private boolean visible;
    public String getFunctionName() {
        return "SetVisible";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
        visible = ((Boolean)args[1]).booleanValue();

    }

    public String getDescription() {
        return "make a cell in/visible";
    }

    public String getCategory() {
        return "utilities";
    }

    public void run() {
        SceneWorker.addWorker(new WorkCommit() {
            public void commit() {
                renderer = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                node = renderer.getSceneRoot();
                if(visible) {
                    node.setCullHint(CullHint.Inherit);
                } else {
                    node.setCullHint(CullHint.Always);
                }               
            }

        });
    }

}
