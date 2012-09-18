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
package org.jdesktop.wonderland.modules.cmu.common.messages.serverclient;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.cmu.common.events.responses.CMUResponseFunction;

/**
 * Message to inform the server that a Wonderland event has ocurred, and that
 * a particular response should be taken.
 * @author kevin
 */
public class EventResponseMessage extends CellMessage {

    private final CMUResponseFunction response;

    public EventResponseMessage(CMUResponseFunction response) {
        this.response = response;
    }

    public CMUResponseFunction getResponse() {
        return response;
    }
}
