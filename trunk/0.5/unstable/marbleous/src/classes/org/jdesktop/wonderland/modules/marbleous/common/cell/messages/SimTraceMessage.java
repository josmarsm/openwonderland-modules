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
package org.jdesktop.wonderland.modules.marbleous.common.cell.messages;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.marbleous.common.trace.SimTrace;

/**
 * Message giving the simulation trace of the simulation
 *
 * @author jslott
 */
public class SimTraceMessage extends CellMessage {

    // The time series of simulation samples
    private SimTrace simTrace = null;

    public SimTraceMessage(SimTrace simTrace) {
        this.simTrace = simTrace;
    }

    public SimTrace getSimTrace() {
        return simTrace;
    }
}
