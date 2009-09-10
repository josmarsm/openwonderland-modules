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
package org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient;

import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes.VisualAttributesIdentifier;

/**
 * Serializable information about a CMU visual; stores initial transformation
 * and property information, as well as an identifier which can be used
 * to access full visual attributes from the content repository (given an
 * appropriate repository root, which is normally sent in the SceneMessage
 * used to initialize a scene).
 * @author kevin
 */
public class VisualMessage extends SingleNodeMessage {

    private final TransformationMessage transformation;
    private final VisualPropertyMessage visualProperties;
    private final AppearancePropertyMessage appearanceProperties;
    private final GeometryUpdateMessage changingGeometries;
    
    private VisualAttributesIdentifier visualID;

    /**
     * Standard constructor, with initial transformation/properties.
     * @param id Identifier for this visual, used to find the visual in the
     * content repository (along with an appropriate repository root)
     * @param initialTransform The initial transformation for the visual
     * @param initialVisualProperties The initial visual properties for the visual
     * @param initialAppearanceProperties The initial appearance properties for the visual
     * @param initialGeometries The initial non-persistent geometries for the visual
     */
    public VisualMessage(VisualAttributesIdentifier id, TransformationMessage initialTransform,
            VisualPropertyMessage initialVisualProperties, AppearancePropertyMessage initialAppearanceProperties,
            GeometryUpdateMessage initialGeometries) {
        super(initialTransform.getNodeID());
        assert initialTransform.getNodeID().equals(initialVisualProperties.getNodeID());
        assert initialTransform.getNodeID().equals(initialAppearanceProperties.getNodeID());

        setVisualID(id);
        this.transformation = initialTransform;
        this.visualProperties = initialVisualProperties;
        this.appearanceProperties = initialAppearanceProperties;
        this.changingGeometries = initialGeometries;
    }

    /**
     * Get initial transformation info as a TransformationMessage.
     * @return Updatable transformation information
     */
    public TransformationMessage getTransformation() {
        return this.transformation;
    }

    /**
     * Get the initial visual properties for this visual.
     * @return Visual properties for this visual
     */
    public VisualPropertyMessage getVisualProperties() {
        return this.visualProperties;
    }

    /**
     * Get the initial appearance properties for this visual.
     * @return Appearance properties for this visual
     */
    public AppearancePropertyMessage getAppearanceProperties() {
        return appearanceProperties;
    }

    /**
     * Get the non-persistent geometries for this visual.
     * @return GeometryUpdateMessage for this visual
     */
    public GeometryUpdateMessage getChangingGeometries() {
        return changingGeometries;
    }

    /**
     * Get the identifier for this visual, used in conjunction with a
     * repository root to find the full visual data in the content repository.
     * @return ID for this visual
     */
    public VisualAttributesIdentifier getVisualID() {
        return this.visualID;
    }


    /**
     * Set the identifier for this visual.
     * @param visualID ID for this visual
     */
    public void setVisualID(VisualAttributesIdentifier visualID) {
        this.visualID = visualID;
    }

    /**
     * Get a String representation of the message, with debug info.
     * @return String representation of the message
     */
    @Override
    public String toString() {
        return super.toString() + "[visualID:" + getVisualID() + "]";
    }
}