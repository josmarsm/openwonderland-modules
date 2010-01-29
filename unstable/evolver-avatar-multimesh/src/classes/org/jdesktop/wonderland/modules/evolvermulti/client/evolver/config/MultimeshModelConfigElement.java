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
import javax.xml.bind.annotation.XmlTransient;

/**
 * An abstract base class for all geometry (model)-based avatar configuration
 * attributes.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public abstract class MultimeshModelConfigElement extends MultimeshConfigElement {
    // The relative path of the model
    @XmlElement(name = "model")
    private String model;

    // The base path to prepend to all model paths
    @XmlTransient
    private String relativePath = null;

    /** Default constructor, for JAXB */
    public MultimeshModelConfigElement() {
    }

    @XmlTransient
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Sets the base relative path for all models paths in this configuration.
     * The relative path does not begin with a "/", but should end with a "/".
     *
     * @param relativePath The relative path to prepend to all model paths
     */
    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    /**
     * Returns the relative path to prepend to all model paths.
     *
     * @return The relative path
     */
    public String getRelativePath() {
        return relativePath;
    }

    /**
     * Returns the model including its relative path.
     *
     * @return The relative path + module name
     */
    public String getModelPath() {
        return getRelativePath() + getModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(CharacterParams attrs) {
        Logger.getLogger(MultimeshModelConfigElement.class.getName()).warning("MODEL " + getModelPath());
        attrs.addLoadInstruction(getModelPath());
    }
}
