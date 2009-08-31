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
package org.jdesktop.wonderland.modules.cmu.player;

import edu.cmu.cs.dennisc.property.event.PropertyEvent;
import edu.cmu.cs.dennisc.property.event.PropertyListener;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.TransformationMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;
import edu.cmu.cs.dennisc.scenegraph.Appearance;
import edu.cmu.cs.dennisc.scenegraph.Geometry;
import edu.cmu.cs.dennisc.scenegraph.Visual;
import edu.cmu.cs.dennisc.scenegraph.event.AbsoluteTransformationEvent;
import edu.cmu.cs.dennisc.scenegraph.event.AbsoluteTransformationListener;
import java.util.HashSet;
import java.util.Set;
import org.alice.apis.moveandturn.Composite;
import org.alice.apis.moveandturn.Model;
import org.alice.apis.moveandturn.Scene;
import org.alice.apis.moveandturn.Transformable;
import org.alice.apis.moveandturn.event.MouseButtonListener;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;
import org.jdesktop.wonderland.modules.cmu.common.web.VisualAttributes;
import org.jdesktop.wonderland.modules.cmu.player.conversions.AppearanceConverter;
import org.jdesktop.wonderland.modules.cmu.player.conversions.GeometryConverter;
import org.jdesktop.wonderland.modules.cmu.player.conversions.OrthogonalMatrix3x3Converter;
import org.jdesktop.wonderland.modules.cmu.player.conversions.Point3Converter;
import org.jdesktop.wonderland.modules.cmu.player.conversions.ScaleConverter;

/**
 * Wraps/serializes a CMU scene graph visual, and listens for transformation updates,
 * then passes them on to anyone who is listening.
 * @author kevin
 */
public class ModelWrapper implements AbsoluteTransformationListener, PropertyListener {

    private static long numNodes = 0;        // Used to assign unique IDs to each node.
    private final Model model;
    private final Visual cmuVisual;         // The wrapped visual.
    private final VisualAttributes visualAttributes;     // The serializable attributes for this visual.
    private final TransformationMessage transformation;
    private final Set<TransformationMessageListener> transformationListeners = new HashSet<TransformationMessageListener>();
    protected final NodeID nodeID;             // Unique ID for this visual.


    {
        // Set node ID in every constructor.
        synchronized (this.getClass()) {
            this.nodeID = new NodeID(numNodes);
            numNodes++;
        }
    }

    /**
     * Constructor; attach this node to a CMU Visual.
     * @param v The CMU visual to attach
     */
    public ModelWrapper(Model m) {
        model = m;
        cmuVisual = m.getSGVisual();
        visualAttributes = new VisualAttributes();
        transformation = new TransformationMessage(getNodeID());
        loadVisual();
    }

    /**
     * Get the unique ID for this node.
     * @return The node ID, uniquely generated at construction
     */
    public NodeID getNodeID() {
        return nodeID;
    }

    public Model getModel() {
        return model;
    }

    /**
     * Get the message which can be used to load this visual on the client side.
     * Fill it with (a copy of) the current transformation.
     * @return The VisualMessage associated with this visual
     */
    public VisualMessage getVisualMessage() {
        synchronized (transformation) {
            return new VisualMessage(visualAttributes.getID(), new TransformationMessage(transformation));
        }
    }

    /**
     * The transformation in serializable form.
     * @return The TransformationMessage associated with this visual
     */
    public TransformationMessage getTransformationMessage() {
        return this.transformation;
    }

    /**
     * Get the serializable attributes of this visual.
     * @return VisualAttributes associated with this visual
     */
    public VisualAttributes getVisualAttributes() {
        return this.visualAttributes;
    }

    /**
     * Simulate a mouse click on this node, passing the click in to
     * all listeners at or above the node.  Simulate these clicks in
     * a new thread.
     */
    public void click() {
        final MouseButtonEventFromWorld mouseEvent = new MouseButtonEventFromWorld(ModelWrapper.this);
        Composite currentComposite = model;

        // Walk up the scene graph and apply the mouse click to all containing composites.
        while (currentComposite != null) {
            String threadName = "Mouse click " + this + " " + currentComposite;
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

    /**
     * Callback function when the CMU node is updated.
     * @param e {@inheritDoc}
     */
    @Override
    public void absoluteTransformationChanged(AbsoluteTransformationEvent e) {
        updateTransformation();
    }

    /**
     * {@inheritDoc}
     */
    public void propertyChanging(PropertyEvent e) {
    }

    /**
     * Callback function when a CMU property (in our case, the only
     * property of concern is scale, for transformation update purposes)
     * is updated.
     * @param e {@inheritDoc}
     */
    public void propertyChanged(PropertyEvent e) {
        updateTransformation();
    }

    /**
     * Get the current CMU node transformation, and load it into this wrapper.
     * Synchronize this on our transformation message, so that all updates happen
     * at once.
     */
    protected void updateTransformation() {
        synchronized (transformation) {
            transformation.setScale(new ScaleConverter(cmuVisual.scale.getCopy(cmuVisual)).getScale());
            transformation.setTranslation(new Point3Converter(cmuVisual.getTranslation(cmuVisual.getRoot())).getVector3f());
            transformation.setRotation(new OrthogonalMatrix3x3Converter(cmuVisual.getTransformation(cmuVisual.getRoot()).orientation).getMatrix3f());
        }
        fireTransformationMessageChanged();
    }

    /**
     * Set the CMU visual to mirror, and load its visual properties.
     * Synchronize on the visual message during this process, so that it isn't e.g. sent
     * halfway through being updated.
     * @param v The CMU visual to mirror
     */
    protected void loadVisual() {
        assert cmuVisual != null;
        synchronized (visualAttributes) {
            // Set name.
            visualAttributes.setName(cmuVisual.getName());

            // Get meshes.
            for (Geometry g : cmuVisual.geometries.getValue()) {
                visualAttributes.addMesh(new GeometryConverter(g).getMesh());
            }

            // Get appearance properties.
            Appearance app = cmuVisual.frontFacingAppearance.getValue();

            AppearanceConverter appConverter = new AppearanceConverter(app);
            if (appConverter.getTexture() != null) {
                visualAttributes.setTexture(appConverter.getTexture(), appConverter.getTextureWidth(), appConverter.getTextureHeight());
            }

            cmuVisual.addAbsoluteTransformationListener(this);
            cmuVisual.addPropertyListener(this);
            updateTransformation();
        }
    }

    /**
     * Disassociate this wrapper with its visual.
     */
    public void unload() {
        cmuVisual.removeAbsoluteTransformationListener(this);
    }

    /**
     * Add a listener for changes in the transformation of the
     * associated visual.
     * @param listener The listener to add
     */
    public void addTransformationMessageListener(TransformationMessageListener listener) {
        synchronized (this.transformationListeners) {
            this.transformationListeners.add(listener);
        }
    }

    /**
     * Remove a listener for changes in the transformation of the
     * associated visual
     * @param listener The listener to remove
     */
    public void removeTransformationMessageListener(TransformationMessageListener listener) {
        synchronized (this.transformationListeners) {
            this.transformationListeners.remove(listener);
        }
    }

    /**
     * Notify listeners that the transformation has changed.
     */
    private void fireTransformationMessageChanged() {
        synchronized (this.transformationListeners) {
            for (TransformationMessageListener listener : this.transformationListeners) {
                synchronized (this.getTransformationMessage()) {
                    listener.transformationMessageChanged(new TransformationMessage(this.getTransformationMessage()));
                }
            }

        }
    }
}
