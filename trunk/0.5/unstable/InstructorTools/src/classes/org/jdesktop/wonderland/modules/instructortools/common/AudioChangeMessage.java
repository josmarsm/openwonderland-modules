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
import org.jdesktop.wonderland.common.messages.Message;

/**
 *
 * @author Ryan
 */
public class AudioChangeMessage extends Message {
    
    private BigInteger target;
    private float micVolume;
    private float speakerVolume;
    
    public AudioChangeMessage(BigInteger target, float micVolume, float speakerVolume) {
        super();
        this.target = target;
        this.micVolume = micVolume;
        this.speakerVolume = speakerVolume;
    }

    public float getMicVolume() {
        return micVolume;
    }

    public void setMicVolume(float micVolume) {
        this.micVolume = micVolume;
    }

    public float getSpeakerVolume() {
        return speakerVolume;
    }

    public void setSpeakerVolume(float speakerVolume) {
        this.speakerVolume = speakerVolume;
    }

    public BigInteger getTarget() {
        return target;
    }

    public void setTarget(BigInteger target) {
        this.target = target;
    }
    
    
}
