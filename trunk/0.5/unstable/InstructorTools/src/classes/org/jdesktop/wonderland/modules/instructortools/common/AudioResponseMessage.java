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
public class AudioResponseMessage extends Message {
    private BigInteger source;
    private BigInteger target;
    private int micVolume;
    private int speakerVolume;
    
    public AudioResponseMessage(BigInteger source, BigInteger target, int micVolume, int speakerVolume) {
        super();
        
        this.source = source;
        this.target = target;
        this.micVolume = micVolume;
        this.speakerVolume = speakerVolume;
    }

    public int getMicVolume() {
        return micVolume;
    }

    public void setMicVolume(int micVolume) {
        this.micVolume = micVolume;
    }

    public int getSpeakerVolume() {
        return speakerVolume;
    }

    public void setSpeakerVolume(int speakerVolume) {
        this.speakerVolume = speakerVolume;
    }

    public BigInteger getTarget() {
        return target;
    }

    public void setTarget(BigInteger target) {
        this.target = target;
    }

    public BigInteger getSource() {
        return source;
    }

    public void setSource(BigInteger source) {
        this.source = source;
    }
    
    
    
}
