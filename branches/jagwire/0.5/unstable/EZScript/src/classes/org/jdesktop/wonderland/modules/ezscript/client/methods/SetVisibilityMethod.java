/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.scene.Node;
import com.jme.scene.Spatial.CullHint;
import java.util.Iterator;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class SetVisibilityMethod implements ScriptMethodSPI {

    private static final Logger logger = Logger.getLogger(SetVisibilityMethod.class.getName());
    public String getFunctionName() {
        return "SetVisible";
    }

    public void setArguments(Object[] args) {
//        cell = (Cell)args[0];
//        visible = ((Boolean)args[1]).booleanValue();

        new SetVisibilityInvoker((Cell) args[0],
                ((Boolean) args[1]).booleanValue()).invoke();

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

                    /*
                     * This currently hides the side views [on visible=false] 
                     * and fails to show them [on visible=true] 
                     */
                    if (cell instanceof App2DCell) {
                        App2DCell appCell = (App2DCell)cell;
                        Window2D window = appCell.getApp().getPrimaryWindow();
                        Iterator<? extends View2D> views = appCell.getViews();
                        while(views.hasNext()) {
                            
                            View2D view = views.next();
                            logger.warning("SETTING VIEW: "+view.getName()+ "'s visibility to: "+visible);
                            view.setVisibleApp(visible); //set visibility to false
                            view.setVisibleUser(visible, true); //and update
                        }
                        
                        
                        
//                        window.setVisibleApp(false);
//                        window.setVisibleUser(, visible);
                    } else {



                        BasicRenderer r = (BasicRenderer) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);

                        Node node = r.getSceneRoot();

                        if (visible) {
                            //make visible
                            node.setCullHint(CullHint.Never);

                        } else {
                            //make invisible
                            node.setCullHint(CullHint.Always);
                        }
                        WorldManager.getDefaultWorldManager().addToUpdateList(node);
                    }
                }
            });
        }
    }
}
