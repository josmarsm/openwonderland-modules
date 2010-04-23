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
package org.jdesktop.wonderland.modules.marbleous.common.trace;

import java.io.Serializable;
import java.util.List;

/**
 * A subset of the total sample set, used to transmit a fixed size over a
 * Darkstar channel (so that a timeout doesn't happen). A subset includes
 * a starting sample (first sample is number 0) and a number of samples.
 *
 * @author jslott
 */
public class SampleSubset implements Serializable {

    public int firstSample = 0;
    public int numberSamples = 0;
    public List<SampleInfo> sampleSubset = null;
}
