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
package org.jdesktop.wonderland.modules.cmu.common.events;

import java.io.Serializable;

/**
 * A Wonderland eventpaired with the appropriate response from the CMU
 * scene.  CMU cells should register listeners to respond to these events,
 * and inform the server of the appropriate response when they receive one.
 * @author kevin
 */
public class EventResponsePair implements Serializable {

    private WonderlandEvent event = null;
    private WonderlandResponse response = null;

    public EventResponsePair(WonderlandEvent event, WonderlandResponse response) {
        this.setEvent(event);
        this.setResponse(response);
    }

    public WonderlandEvent getEvent() {
        return event;
    }

    public void setEvent(WonderlandEvent event) {
        this.event = event;
    }

    public WonderlandResponse getResponse() {
        return response;
    }

    public void setResponse(WonderlandResponse response) {
        this.response = response;
    }
}
