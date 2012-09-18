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
public class RequestScreenShotMessage extends Message {
    private Set<BigInteger> targetSessionIDs;
    private BigInteger sourceID;
    
    public RequestScreenShotMessage(Set<BigInteger> targetSessionIDs, BigInteger sourceID) {
        super();
        
        this.targetSessionIDs = targetSessionIDs;
        this.sourceID = sourceID;
    }

    public BigInteger getSourceID() {
        return sourceID;
    }

    public void setSourceID(BigInteger sourceID) {
        this.sourceID = sourceID;
    }

    public Set<BigInteger> getTargetSessionIDs() {
        return targetSessionIDs;
    }

    public void setTargetSessionIDs(Set<BigInteger> targetSessionIDs) {
        this.targetSessionIDs = targetSessionIDs;
    }
    

}
