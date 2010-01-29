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
package org.jdesktop.wonderland.modules.evolvermulti.client.evolver.config;

import imi.character.CharacterParams;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.modules.avatarbase.client.imi.WonderlandCharacterParams.SkinColorConfigElement;

/**
 * Model-based configuration element for the head, also include a skin tone.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name = "avatar-head")
public class MultimeshHeadConfigElement extends MultimeshModelConfigElement {
    @XmlElement(name = "skin-tone")
    private SkinColorConfigElement skinTone;

    @XmlElement(name="eyeball-texture")
    private String eyeBallTexture;

    /** Default constructor, for JAXB */
    public MultimeshHeadConfigElement() {
    }

    @XmlTransient
    public SkinColorConfigElement getSkinTone() {
        return skinTone;
    }

    public void setSkinTone(SkinColorConfigElement skinTone) {
        this.skinTone = skinTone;
    }

    @XmlTransient
    public String getEyeBallTexture() {
        return eyeBallTexture;
    }

    public void setEyeBallTexture(String eyeBallTexture) {
        this.eyeBallTexture = eyeBallTexture;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(CharacterParams attrs) {
        Logger.getLogger(MultimeshModelConfigElement.class.getName()).warning("MODEL " + getModelPath());
        attrs.setHeadAttachment(getModelPath());
        if (skinTone != null) {
            attrs.setSkinTone(skinTone.getR(), skinTone.getG(), skinTone.getB());
            attrs.setUsePhongLightingForHead(true);
        }
        if (eyeBallTexture != null) {
            String eyeBallPath = getRelativePath() + eyeBallTexture;
            attrs.setEyeballTexture(eyeBallPath);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Head: " + getModel();
    }
}
