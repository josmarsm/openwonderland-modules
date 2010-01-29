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
import javax.xml.bind.annotation.XmlTransient;

/**
 * An abstract base class that represents some configuration element for the
 * multi-mesh avatar.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class MultimeshConfigElement {

    // The name of the preset
    @XmlElement(name = "name")
    private String name = null;

    /** Default constructor, for JAXB */
    public MultimeshConfigElement() {
    }
    
    @XmlTransient
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Applies the attributes to the avatar configuration parameters.
     *
     * @param attrs The avatar's parameter
     */
    public abstract void apply(CharacterParams attrs);
}
