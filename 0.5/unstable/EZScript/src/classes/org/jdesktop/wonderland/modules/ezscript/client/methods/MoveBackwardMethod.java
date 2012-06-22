/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
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
public class MoveBackwardMethod implements ScriptMethodSPI {
    Logger logger = Logger.getLogger(MoveBackwardMethod.class.getName());
    private Cell cell;
    private CellTransform transform;
    private Vector3f position = new Vector3f();
    private Vector3f lookAt = new Vector3f(0, 0, -1);
    private Semaphore lock = new Semaphore(0);

    private Vector3f normal = null;
    private MovableComponent mcc;
    private boolean fail = true;


    public String getFunctionName() {
        //transform.getTranslation(null).normalize().m
        //logger.warning("inside getFunctionName.");
        return "MoveBack";
    }

    public void setArguments(Object[] args) {
        lookAt = new Vector3f(0, 0, -1);
        //logger.warning("inside setArguments");
        cell = (Cell) args[0];

        //fail fast
        if (cell == null) {
            logger.warning("cell is null in setArguments!");
            return;
        }

        transform = cell.getLocalTransform();
        logger.warning("\ncurrent cell transform: " + transform);

        lookAt.multLocal(transform.getScaling());
        logger.warning("\nlookAt after scaling: " + lookAt);
        transform.getRotation(null).multLocal(lookAt);
        logger.warning("\nlookAt after scaling and rotation: " + lookAt);
        //transform.getLookAt(position, lookAt);

        position = transform.getTranslation(position);

        lookAt.y = position.y;
        normal = lookAt.normalize();//lookAt.subtract(position);

        mcc = cell.getComponent(MovableComponent.class);
        if (mcc != null) {
            fail = false;
        }
        // logger.warning("CellLookDirection: "+lookAt.normalizeLocal());
    }

    public String getDescription() {
        return "Move the cell backward one unit in it's look direction\n"
                + "usage: MoveBackward(cell);";
    }

    public String getCategory() {
        return "Object Movement";
    }

    public void run() {
        if (fail) {
            return;
        }
        position.x += normal.x;
        position.z += normal.z;
        transform.setTranslation(position);
        logger.warning("\nNew Cell Transform: " + transform);
        // transform.setRotation(transform.getRotation(null).identityRotation);
        //transform.setTranslation(position);
        mcc.localMoveRequest(transform);
    }
}
