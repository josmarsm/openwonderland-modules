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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Avatar configuration element that sets the gender (MALE, FEMALE).
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name = "avatar-gender")
public class MultimeshGenderConfigElement extends MultimeshConfigElement {

    /** Enumeration of Gender types */
    public static final int MALE = 1;
    public static final int FEMALE = 2;

    @XmlElement(name="gender")
    private int gender;

    /** Default constructor, for JAXB */
    public MultimeshGenderConfigElement() {
    }
    
    @XmlTransient
    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(CharacterParams attrs) {
        attrs.setGender(gender).setUsePhongLightingForHead(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Gender: " + gender;
    }
}
