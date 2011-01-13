/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.RoundEllie4.client;

/**
 *
 * @author morris
 */
import com.sun.scenario.animation.TimingTarget;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;

/**
 *
 * @author paulby
 */
public abstract class AnimationProcessorComponent extends ProcessorComponent implements TimingTarget {

    public AnimationProcessorComponent(Entity entity) {
        addToEntity(entity);
    }

    /**
     * Add this processor to the entity
     * @param entity
     */
    void addToEntity(Entity entity) {
        ProcessorCollectionComponent coll = (ProcessorCollectionComponent) entity.getComponent(ProcessorCollectionComponent.class);
        if (coll==null) {
            coll = new ProcessorCollectionComponent();
            entity.addComponent(ProcessorCollectionComponent.class, coll);
        }
        coll.addProcessor(this);
    }
}

