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
package org.jdesktop.wonderland.modules.cmu.client.events.wonderland;

import org.jdesktop.wonderland.modules.cmu.client.CMUCell;
import org.jdesktop.wonderland.modules.cmu.common.events.WonderlandEventResponse;
import org.jdesktop.wonderland.modules.cmu.common.messages.serverclient.EventResponseMessage;

/**
 * Base class for listeners to Wonderland events which send CMU responses.
 * @author kevin
 */
public abstract class CMUWonderlandEventListener {

    private WonderlandEventResponse response = null;
    private final CMUCell parent;

    public CMUWonderlandEventListener(CMUCell parent, WonderlandEventResponse response) {
        this.parent = parent;
        this.setResponse(response);
    }

    public WonderlandEventResponse getResponse() {
        return response;
    }

    public void setResponse(WonderlandEventResponse response) {
        this.response = response;
    }

    public CMUCell getParent() {
        return parent;
    }

    public void eventOccurred() {
        this.getParent().sendCellMessage(new EventResponseMessage(this.getResponse()));
    }
}
