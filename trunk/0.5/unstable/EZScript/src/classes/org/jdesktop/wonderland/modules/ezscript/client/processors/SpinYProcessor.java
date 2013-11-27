/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.processors;

import com.jme.math.Quaternion;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.scenemanager.SceneManager;
/**
 *
 * @author JagWire
 */
public class SpinYProcessor extends SpinXProcessor {

    private static final Logger logger =
            Logger.getLogger(SpinYProcessor.class.getName());
    
    
    public SpinYProcessor(String name,
            Cell cell,
            float time,
            float increment,
            Semaphore lock) {
        super(name, cell, time, increment, lock);
        
    }

    @Override
        public void compute(ProcessorArmingCollection collection) {
        if (frameIndex >= 30 * time) {
            this.getEntity().removeComponent(SpinYProcessor.class);
            lock.release();
            done = true;
        }
        angles[1] += increment;
        //1 revolution = (3.14 * 2) ~ 6.28
        
        //logger.info("CURRENT (Y) radians:" + angles[1]);
        //quaternion.fromAngles(0.0f, increment, 0.0f);
        quaternion = new Quaternion(new float[]{angles[0], angles[1], angles[2]});
        frameIndex += 1;
    }
}
