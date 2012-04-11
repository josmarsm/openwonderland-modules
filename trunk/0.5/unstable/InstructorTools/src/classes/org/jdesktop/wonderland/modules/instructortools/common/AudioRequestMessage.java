/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.instructortools.common;

import java.math.BigInteger;
import java.util.Set;
import org.jdesktop.wonderland.common.messages.Message;

/**
 *
 * @author Ryan
 */
public class AudioRequestMessage extends Message {
    
    private Set<BigInteger> IDs;
    private BigInteger sourceID;
    
    public AudioRequestMessage(Set<BigInteger> IDs, BigInteger sourceID) {
        this.IDs = IDs;
        this.sourceID = sourceID;
    }

    public Set<BigInteger> getIDs() {
        return IDs;
    }

    public void setIDs(Set<BigInteger> IDs) {
        this.IDs = IDs;
    }

    public BigInteger getSourceID() {
        return sourceID;
    }

    public void setSourceID(BigInteger sourceID) {
        this.sourceID = sourceID;
    }
    
    
    
}
