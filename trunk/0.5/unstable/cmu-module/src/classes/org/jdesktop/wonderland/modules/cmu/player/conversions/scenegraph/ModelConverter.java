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
package org.jdesktop.wonderland.modules.cmu.player.conversions.scenegraph;

import org.jdesktop.wonderland.modules.cmu.player.conversions.scenegraph.properties.VertexGeometryConverter;
import org.jdesktop.wonderland.modules.cmu.player.conversions.scenegraph.properties.AppearanceConverter;
import org.jdesktop.wonderland.modules.cmu.player.conversions.math.ScaleConverter;
import edu.cmu.cs.dennisc.property.event.PropertyEvent;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;
import edu.cmu.cs.dennisc.scenegraph.Appearance;
import edu.cmu.cs.dennisc.scenegraph.Geometry;
import edu.cmu.cs.dennisc.scenegraph.VertexGeometry;
import edu.cmu.cs.dennisc.scenegraph.Visual;
import java.util.logging.Logger;
import org.alice.apis.moveandturn.Composite;
import org.alice.apis.moveandturn.Model;
import org.alice.apis.moveandturn.Scene;
import org.alice.apis.moveandturn.Transformable;
import org.alice.apis.moveandturn.event.MouseButtonListener;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.AppearancePropertyMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualPropertyMessage;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes;
import org.jdesktop.wonderland.modules.cmu.player.MouseButtonEventFromWorld;

/**
 * Wraps a CMU Model and its associated scene graph Visual.  Note that we're
 * "flattening" the separation between Model and Visual, since we interact
 * with them as one and the same thing.  Thus, we handle mouse click
 * functionality, geometry parsing, transformation updates, and visible
 * property updates using one object for each Model.
 * @param <ModelType> The class of the CMU Model which we're wrapping;
 * defining this generically allows subclasses of this wrapper to safely
 * (i.e. independently of superclass implementation) override the
 * <code>getTransformable()</code> method to return a subclass of Model.
 * @author kevin
 */
public class ModelConverter<ModelType extends Model> extends TransformableConverter<ModelType> {

    // Scene graph objects/properties
    private final Visual visual;
    private final Appearance frontAppearance;

    // Property converters
    private final AppearanceConverter visualAppearance;

    // Data used by clients to load this model
    private final VisualAttributes visualAttributes;

    // Node properties
    private final VisualPropertyMessage visualProperties;
    private final AppearancePropertyMessage appearanceProperties;

    /**
     * Standard constructor.
     * @param model The CMU Model to wrap
     */
    public ModelConverter(ModelType model) {
        super(model);
        this.visual = model.getSGVisual();
        visualProperties = new VisualPropertyMessage(getNodeID());
        appearanceProperties = new AppearancePropertyMessage(getNodeID());
        assert visual != null;

        ////////////////////////////
        // Initialize visual data //
        ////////////////////////////
        
        visualAttributes = new VisualAttributes();
        
        // Set name
        visualAttributes.setName(visual.getName());

        // Get meshes
        for (Geometry g : visual.geometries.getValue()) {
            if (g instanceof VertexGeometry) {
                visualAttributes.addMesh(new VertexGeometryConverter((VertexGeometry) g).getMesh());
            } else {
                Logger.getLogger(ModelConverter.class.getName()).severe("Unrecognized geometry: " + g);
            }
        }

        // Get appearance properties
        frontAppearance = visual.frontFacingAppearance.getValue();

        visualAppearance = new AppearanceConverter(frontAppearance);
        if (visualAppearance.getTexture() != null) {
            visualAttributes.setTexture(visualAppearance.getTexture(), visualAppearance.getTextureWidth(), visualAppearance.getTextureHeight());
        }

        visual.addPropertyListener(this);
        frontAppearance.addPropertyListener(this);
        updateVisualProperties();
        updateAppearanceProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelType getTransformable() {
        return super.getTransformable();
    }

    /**
     * Get the message which can be used to load this visual on the client side.
     * Fill it with current node properties and transformation data.
     * @return The VisualMessage associated with this model
     */
    public VisualMessage getVisualMessage() {
        return new VisualMessage(getVisualAttributes().getID(), getTransformationMessage(),
                getVisualPropertyMessage(), getAppearancePropertyMessage());

    }

    /**
     * Get the properties of this visual (scale, visibility).
     * @return The VisualPropertyMessage associated with this model
     */
    public VisualPropertyMessage getVisualPropertyMessage() {
        return new VisualPropertyMessage(visualProperties);
    }

    /**
     * Get the appearance properties of this visual (color,
     * opacity, etc.).
     * @return The AppearancePropertyMessage associated with this model
     */
    public AppearancePropertyMessage getAppearancePropertyMessage() {
        return new AppearancePropertyMessage(appearanceProperties);
    }

    /**
     * Get the serializable attributes of this model.
     * @return VisualAttributes associated with this model
     */
    public VisualAttributes getVisualAttributes() {
        return visualAttributes;
    }

    /**
     * Simulate a mouse click on this node, passing the click in to
     * all listeners at or above the node.  Simulate each click in
     * a new thread.
     */
    public void click() {
        final MouseButtonEventFromWorld mouseEvent = new MouseButtonEventFromWorld(getTransformable());
        Composite currentComposite = getTransformable();

        // Walk up the scene graph and apply the mouse click to all containing composites.
        while (currentComposite != null) {
            String threadName = "Mouse click " + this + " " + currentComposite;
            synchronized (currentComposite) {
                if (currentComposite instanceof Model) {
                    Model currentModel = (Model) currentComposite;
                    for (MouseButtonListener listener : currentModel.getMouseButtonListeners()) {
                        final MouseButtonListener finalListener = listener;
                        new Thread(new Runnable() {

                            public void run() {
                                finalListener.mouseButtonClicked(mouseEvent);
                            }
                        }, threadName).start();
                    }
                    currentComposite = currentModel.getVehicle();
                } else if (currentComposite instanceof Scene) {
                    for (MouseButtonListener listener : ((Scene) currentComposite).getMouseButtonListeners()) {
                        final MouseButtonListener finalListener = listener;
                        new Thread(new Runnable() {

                            public void run() {
                                finalListener.mouseButtonClicked(mouseEvent);
                            }
                        }, threadName).start();
                    }
                    currentComposite = null;
                } else if (currentComposite instanceof Transformable) {
                    currentComposite = ((Transformable) currentComposite).getVehicle();
                } else {
                    currentComposite = null;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * @param e {@inheritDoc}
     */
    public void propertyChanging(PropertyEvent e) {
        // No action
    }

    /**
     * Callback function when a CMU property is updated - this covers
     * both Visual properties and Appearance properties.
     * @param e {@inheritDoc}
     */
    public void propertyChanged(PropertyEvent e) {
        if (e.getOwner().equals(this.visual)) {
            updateVisualProperties();
        } else if (e.getOwner() instanceof Appearance) {
            updateAppearanceProperties();
        } else {
            Logger.getLogger(ModelConverter.class.getName()).severe("Unrecognized property owner: " + e.getOwner());
        }
    }

    /**
     * Update the properties for this model's visual, based on the current state
     * of the visual.
     */
    protected void updateVisualProperties() {
        VisualPropertyMessage newProperties = null;
        synchronized (visualProperties) {
            visualProperties.setScale(new ScaleConverter(visual.scale.getCopy(visual)).getScale());
            visualProperties.setVisible(visual.isShowing.getValue().booleanValue());
            newProperties = new VisualPropertyMessage(visualProperties);
        }
        fireNodeUpdated(newProperties);
    }

    /**
     * Update the properties for this model's appearance, based on the current
     * state of the appearance.
     */
    protected void updateAppearanceProperties() {
        AppearancePropertyMessage newProperties = null;
        synchronized (appearanceProperties) {
            appearanceProperties.setAmbientColor(visualAppearance.getAmbientColor());
            appearanceProperties.setDiffuseColor(visualAppearance.getDiffuseColor());
            appearanceProperties.setEmissiveColor(visualAppearance.getEmissiveColor());
            appearanceProperties.setSpecularColor(visualAppearance.getSpecularColor());
            appearanceProperties.setOpacity(visualAppearance.getOpacity());
            newProperties = new AppearancePropertyMessage(appearanceProperties);
        }
        fireNodeUpdated(newProperties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unload() {
        super.unload();
        visual.removePropertyListener(this);
        frontAppearance.removePropertyListener(this);
    }
}