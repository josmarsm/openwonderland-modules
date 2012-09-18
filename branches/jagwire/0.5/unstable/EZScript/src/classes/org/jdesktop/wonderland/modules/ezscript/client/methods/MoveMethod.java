
package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class MoveMethod implements ScriptMethodSPI, Runnable {


    private float x = 0;
    private float y = 0;
    private float z = 0;
    private Cell cell;


    public String getFunctionName() {
        return "move";
    }
    public void setArguments(Object[] args) {
    
        if(args.length > 4 || args.length < 4) {
            System.out.println("ArgumentLength error!");
            return;
        }
        System.out.println("argument length: " + args.length);

        if(args == null) {
            System.out.println("ARGS is null!");
        }

        if(args[1] == null) {
            System.out.println("Cell argument is null!");
            return;
        }
        cell = (Cell)args[0];
        //Float[] coordinate = (Float[])args;
       /* for(Object o : args) {

            System.out.println(o.toString());
        }*/
        x = ((Double)args[1]).floatValue();
        y = ((Double)args[2]).floatValue();
        z = ((Double)args[3]).floatValue();
        System.out.println("setArguments finished!");
    }

    public void run() {
        System.out.println("moving cell: "+cell.getClass());
        MovableComponent mc = cell.getComponent(MovableComponent.class);
        CellTransform translate = cell.getLocalTransform();
        translate.setTranslation(new Vector3f(x, y, z));
        mc.localMoveRequest(translate);
    }

    public String getDescription() {
        return "move(cell, x, y, z)\n\n"
                +"-immediately moves a cell to x, y, z";
    }

    public String getCategory() {
        return "Object Movement";
    }
}
