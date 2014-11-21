/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 * Wrapper for placing a list in shared state component.
 * 
 * @author Vladimir Djurovic
 */
@XmlRootElement(name="shared-data-list")
@ServerState
public class SharedDataList extends SharedData {
   
    private List<String> list;
    
    /**
     * Default no-arg constructor required by JAXB
     */
    public SharedDataList(){
        
    }
    
    /**
     * Constructs new instance by copying data from supplied list.
     * 
     * @param list 
     */
    public SharedDataList(List<String> list){
        this.list = new ArrayList<String>(list);
    }

    public List<String> getList() {
        return list;
    }
    
}
