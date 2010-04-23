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
import imi.character.CharacterParams.SkinnedMeshParams;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration element for the avatar jacket.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name = "avatar-jacket")
public class MultimeshJacketConfigElement extends MultimeshShapesConfigElement {
    /** Default constructor, for JAXB */
    public MultimeshJacketConfigElement() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(CharacterParams attrs) {
        super.apply(attrs);

        for (String shape : getShapes()) {
            attrs.addSkinnedMeshParams(new SkinnedMeshParams(shape, "UpperBody", getModelPath()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Jacket: " + getModel();
    }
}
