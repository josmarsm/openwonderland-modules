/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.scene.Node;
import com.jme.scene.Spatial.CullHint;
import org.jdesktop.mtgame.WorldManager;
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

    public String getFunctionName() {
        return "SetVisible";
    }

    public void setArguments(Object[] args) {
//        cell = (Cell)args[0];
//        visible = ((Boolean)args[1]).booleanValue();

        new SetVisibilityInvoker((Cell)args[0],
                ((Boolean)args[1]).booleanValue()).invoke();
        
    }

    public String getDescription() {
        return "Make a cell in/visible.\n"
                + "-- usage: SetVisible(cell, true);\n"
                + "-- usage: SetVisible(cell, false);";
    }

    public String getCategory() {
        return "utilities";
    }

    public void run() {
//        SceneWorker.addWorker(new WorkCommit() {
//            public void commit() {
//                renderer = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
//                node = renderer.getSceneRoot();
//                if(visible) {
//                    node.setCullHint(CullHint.Never);
//                } else {
//                    node.setCullHint(CullHint.Always);
//                }               
//                WorldManager.getDefaultWorldManager().addToUpdateList(node);
//            }
//
//        });
    }
    
    private static class SetVisibilityInvoker {
        private final Cell cell;
        private final boolean visible;
        public SetVisibilityInvoker(Cell cell, boolean visible) {
            this.cell = cell;
            this.visible = visible;
        }
        
        public void invoke() {
            SceneWorker.addWorker(new WorkCommit() {
                public void commit() {
                    BasicRenderer r = (BasicRenderer)cell
                                        .getCellRenderer(Cell.RendererType.RENDERER_JME);
                    
                    Node node = r.getSceneRoot();
                    
                    if(visible) {
                        //make visible
                        node.setCullHint(CullHint.Never);
                        
                    } else {
                        //make invisible
                        node.setCullHint(CullHint.Always);
                    }
                    WorldManager.getDefaultWorldManager().addToUpdateList(node);
                }
            });
        }
    }

}
