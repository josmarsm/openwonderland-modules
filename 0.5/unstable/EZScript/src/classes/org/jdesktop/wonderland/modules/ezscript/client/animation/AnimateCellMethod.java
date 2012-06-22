/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.animation;

import com.jme.animation.AnimationController;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class AnimateCellMethod implements ScriptMethodSPI {

    private Cell cell = null;
    BasicRenderer renderer = null;
    public String getFunctionName() {
        return "AnimateCell";
    }

    public void setArguments(Object[] args) {
        cell = (Cell)args[0];
    }

    public String getDescription() {
        return "usage: AnimateCell(cell);\n\n"
                +"- plays an animation associated with a collada model";
    }

    public String getCategory() {
        return "Object Movement";
    }

    public void run() {
        renderer = (BasicRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        System.out.println("[EZScript] searching for animatable nodes.");
        List<AnimationNode> nodes = getAnimationNodes(renderer.getSceneRoot());
        AnimationFactory factory = new AnimationFactory(renderer, nodes);
        System.out.println("[EZScript] processing animatable nodes");
        factory.processAllNodes();
        System.out.println("[EZScript] intializing animatable nodes");
        factory.initializeNodes();
        System.out.println("[EZScript] playing animation");
        factory.animateNodes();

    }


    public List<AnimationNode> getAnimationNodes(Node node) {
       if(node.getChildren().isEmpty()) {
           return new ArrayList();
       }

       List<AnimationNode> nodes = new ArrayList<AnimationNode>();
       //for every spatial that is a child of node.
       for(Spatial s: node.getChildren()) {
           //check to see if the spatial is a node
           if(s instanceof Node) {
               //if so, check to see if it has animation controllers
               if( s.getControllerCount() > 0) {
                    //if so, check to see if it is animatable
                   List as = ((AnimationController)s.getController(0)).getAnimations();
                   if(as != null) {
                       //if so, add to the list
                       AnimationNode animationNode = new AnimationNode();
                       animationNode.setName(s.getName());
                       nodes.add(animationNode);
                   }
               }
               nodes.addAll(getAnimationNodes((Node)s));
           }
            
        }
       return nodes;
    }
}
