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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A configuration element that is model-based, but also specifies shapes
 * (whatever that means).
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class MultimeshShapesConfigElement extends MultimeshModelConfigElement {
    @XmlElement(name="shape")
    private String[] shapes;

    /** Default constructor, for JAXB */
    public MultimeshShapesConfigElement() {
    }

    @XmlTransient
    public String[] getShapes() {
        return shapes;
    }

    public void setShapes(String[] shapes) {
        this.shapes = shapes;
    }
}
