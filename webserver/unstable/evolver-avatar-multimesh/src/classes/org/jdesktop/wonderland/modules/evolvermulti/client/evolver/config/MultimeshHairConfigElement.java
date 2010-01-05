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

import imi.character.AttachmentParams;
import imi.character.CharacterParams;
import imi.scene.PMatrix;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration element for the avatar hair.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@XmlRootElement(name = "avatar-hair")
public class MultimeshHairConfigElement extends MultimeshShapesConfigElement {
    /** Default constructor, for JAXB */
    public MultimeshHairConfigElement() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(CharacterParams attrs) {
        String model = getModel();
        if (model != null && model.equals("") == false) {
            super.apply(attrs);

            Logger.getLogger(MultimeshHairConfigElement.class.getName()).warning(
                    "HAIR ATTACHMENT SHAPE " + getShapes()[0] + " MODEL " +
                    getModelPath());

            // Take the first shape as the mesh to use for the hair. We
            // assume there is at least one
            AttachmentParams params = new AttachmentParams(
                    getShapes()[0], // Mesh
                    "HairAttach", // Parent Joint
                    PMatrix.IDENTITY, // Orientation
                    "HairAttachmentJoint", // Attachment Joint Name
                    getModelPath());              // Owning File Name
            attrs.addAttachmentInstruction(params);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Hair: " + getModel();
    }
}
