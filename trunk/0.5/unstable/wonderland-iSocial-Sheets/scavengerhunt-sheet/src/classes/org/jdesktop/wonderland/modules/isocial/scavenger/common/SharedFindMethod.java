/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.common;

import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 *  Wrapper for {@link FindMethod} class, so it can be placed into shared map component.
 *  Wrapped is needed because {@link FindMethod} contains JAXB annotations which messes up sharedstate component
 *  handling.
 *
 * @author Vladimir Djurovic
 */
public class SharedFindMethod extends SharedData {
    
    /** Wrapped object. */
    private FindMethod method;
    
    
    public SharedFindMethod(FindMethod method){
        this.method = method;
    }

    public FindMethod getMethod() {
        return method;
    }

    public void setMethod(FindMethod method) {
        this.method = method;
    }
    
}
