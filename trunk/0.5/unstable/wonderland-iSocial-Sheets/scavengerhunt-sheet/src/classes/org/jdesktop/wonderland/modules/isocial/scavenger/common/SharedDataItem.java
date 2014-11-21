/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
import org.jdesktop.wonderland.modules.sharedstate.common.annotation.SharedStateTransient;

/** Wrapper for {@link ScavnegerHuntItem} which allows placing it
 * inside shared state component map.
 *
 * @author Vladimir Djurovic
 */
@SharedStateTransient
public class SharedDataItem extends SharedData {
    
    /** ACtual scavenger hunt item. */
    private ScavengerHuntItem item;
    
    /**
     * Creates new instance.
     * 
     * @param item  item
     */
    public SharedDataItem(ScavengerHuntItem item){
        this.item = item;
    }

    public ScavengerHuntItem getItem() {
        return item;
    }

    public void setItem(ScavengerHuntItem item) {
        this.item = item;
    }
 
}
