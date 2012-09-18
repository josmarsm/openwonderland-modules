/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.cell;

import com.jme.math.Vector3f;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;

/**
 *
 * @author JagWire
 */
public class EZCellWrapper {
    private final Cell cell;
    
    private Logger logger = Logger.getLogger(EZCellWrapper.class.getName());
    
    public EZCellWrapper(Cell cell) {
        this.cell = cell;
    }

    private AnotherMovableComponent getMovable() {
        if(cell.getComponent(AnotherMovableComponent.class) != null) {
            return cell.getComponent(AnotherMovableComponent.class);
        }
        logger.warning("NO MOVABLE ATTACHED!");
        return null;
    }
    
    public Vector3f getPosition() {
        return cell.getLocalTransform().getTranslation(null);
    }
    
}
