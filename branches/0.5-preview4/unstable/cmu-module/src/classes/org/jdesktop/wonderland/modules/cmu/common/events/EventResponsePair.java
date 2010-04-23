/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.cmu.common.events;

import org.jdesktop.wonderland.modules.cmu.common.events.responses.CMUResponseFunction;
import java.io.Serializable;

/**
 * A Wonderland eventpaired with the appropriate response from the CMU
 * scene.  CMU cells should register listeners to respond to these events,
 * and inform the server of the appropriate response when they receive one.
 * @author kevin
 */
public class EventResponsePair implements Serializable {

    private WonderlandEvent event = null;
    private CMUResponseFunction response = null;

    public EventResponsePair() {
        
    }

    public EventResponsePair(WonderlandEvent event, CMUResponseFunction response) {
        this.setEvent(event);
        this.setResponse(response);
    }

    public WonderlandEvent getEvent() {
        return event;
    }

    public void setEvent(WonderlandEvent event) {
        this.event = event;
    }

    public CMUResponseFunction getResponse() {
        return response;
    }

    public void setResponse(CMUResponseFunction response) {
        this.response = response;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof EventResponsePair && this.getClass().equals(other.getClass())) {
            EventResponsePair otherPair = (EventResponsePair) other;

            if (this.getEvent().equals(otherPair.getEvent()) &&
                    this.getResponse().equals(otherPair.getResponse())) {
                return true;
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.event != null ? this.event.hashCode() : 0);
        hash = 23 * hash + (this.response != null ? this.response.hashCode() : 0);
        return hash;
    }
}
