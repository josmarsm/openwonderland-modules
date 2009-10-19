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
import org.jdesktop.wonderland.modules.marbleous.common.trace.SampleSubset;

/**
 * Message giving the simulation trace of the simulation. There are different
 * flavors of this: FIRST, giving the first in a subset, MIDDLE, giving some
 * middle packet, and LAST, giving the final packet
 *
 * @author jslott
 */
public class SimTraceMessage extends CellMessage {

    /**
     * FIRST: This is the first messages in the block
     * MIDDLE: This is some intermediate message in the block
     * LAST: This is the last message in the block
     * ONLY: This is the only message in the block
     */
    public enum Type {
        FIRST, MIDDLE, LAST, ONLY
    };

    // The type (first, middle, last)
    private Type type = Type.FIRST;

    // The time series subset of simulation samples
    private SampleSubset sampleSubset = null;

    public SimTraceMessage(Type type, SampleSubset sampleSubset) {
        this.type = type;
        this.sampleSubset = sampleSubset;
    }

    public Type getType() {
        return type;
    }

    public SampleSubset getSampleSubset() {
        return sampleSubset;
    }
}
