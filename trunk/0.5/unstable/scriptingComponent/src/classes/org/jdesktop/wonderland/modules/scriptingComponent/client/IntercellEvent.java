/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.scriptingComponent.client;

import org.jdesktop.wonderland.client.input.Event;

/**
 * Event on the receipt of an Intercell message
 * @author Morris Ford
 */
public class IntercellEvent extends Event {
    private String payload = null;

    /** Default constructor */
    public IntercellEvent() {
    }
    
    /** Constructor, takes the Enitity that has been entered/exited. */
    public IntercellEvent(String Payload) {
        this.payload = Payload;
    }

    /**
     * Returns true if this is an enter event, false if it is an exit event.
     * 
     * @return True for enter, false for exit
     */
    /** 
     * {@inheritDoc}
     * <br>
     * If event is null, a new event of this class is created and returned.
     */
    @Override
    public Event clone (Event event) {
        if (event == null) {
            event = new IntercellEvent();
        }
        ((IntercellEvent) event).payload = payload;
        return super.clone(event);
    }
}
