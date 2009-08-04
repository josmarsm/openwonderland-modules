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

import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.TransformationMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;
import edu.cmu.cs.dennisc.scenegraph.Appearance;
import edu.cmu.cs.dennisc.scenegraph.Geometry;
import edu.cmu.cs.dennisc.scenegraph.Visual;
import edu.cmu.cs.dennisc.scenegraph.event.AbsoluteTransformationEvent;
import edu.cmu.cs.dennisc.scenegraph.event.AbsoluteTransformationListener;
import java.util.Collection;
import java.util.Vector;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;
import org.jdesktop.wonderland.modules.cmu.player.conversions.AppearanceConverter;
import org.jdesktop.wonderland.modules.cmu.player.conversions.GeometryConverter;
import org.jdesktop.wonderland.modules.cmu.player.conversions.OrthogonalMatrix3x3Converter;
import org.jdesktop.wonderland.modules.cmu.player.conversions.Point3Converter;
import org.jdesktop.wonderland.modules.cmu.player.conversions.ScaleConverter;

/**
 * Wraps/serializes a CMU visual node, and listens for transformation updates,
 * then passes them on to anyone who is listening.
 * @author kevin
 */
public class VisualWrapper implements AbsoluteTransformationListener {

    private static long numNodes = 0;        // Used to assign unique IDs to each node.
    private final Visual cmuVisual;         // The wrapped visual.
    private final VisualMessage visualMessage;     // The serializable data for this visual.
    private final Collection<TransformationMessageListener> transformationListeners = new Vector<TransformationMessageListener>();
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
    public VisualWrapper(Visual v) {
        cmuVisual = v;
        visualMessage = new VisualMessage(this.getNodeID());
        loadVisual();
    }

    /**
     * Get the associated CMU visual.
     * @return The associated CMU visual
     */
    public Visual getVisual() {
        return cmuVisual;
    }

    /**
     * Get the unique ID for this node.
     * @return The node ID, uniquely generated at construction
     */
    public NodeID getNodeID() {
        return nodeID;
    }

    /**
     * The entire visual in serializable form.
     * @return The VisualMessage associated with this visual
     */
    public VisualMessage getVisualMessage() {
        return this.visualMessage;
    }

    /**
     * The transformation in serializable form.
     * @return The TransformationMessage associated with this visual
     */
    public TransformationMessage getTransformationMessage() {
        return this.visualMessage.getTransformation();
    }

    /**
     * Callback function when the CMU node is updated.
     * @param e {@inheritDoc}
     */
    @Override
    public void absoluteTransformationChanged(AbsoluteTransformationEvent e) {
        updateTransformation();
        fireTransformationMessageChanged();
    }

    /**
     * Get the current CMU node transformation, and load it into this wrapper.
     * Synchronize this on our transformation message, so that all updates happen
     * at once.
     */
    protected void updateTransformation() {
        TransformationMessage transformation = this.getTransformationMessage();
        synchronized (transformation) {
            transformation.setScale(new ScaleConverter(cmuVisual.scale.getCopy(cmuVisual)).getScale());
            transformation.setTranslation(new Point3Converter(cmuVisual.getTranslation(cmuVisual.getRoot())).getVector3f());
            transformation.setRotation(new OrthogonalMatrix3x3Converter(cmuVisual.getTransformation(cmuVisual.getRoot()).orientation).getMatrix3f());
        }
    }

    /**
     * Set the CMU visual to mirror, and load its visual properties.
     * Synchronize on the visual message during this process, so that it isn't e.g. sent
     * halfway through being updated.
     * @param v The CMU visual to mirror
     */
    protected void loadVisual() {
        assert cmuVisual != null;
        Visual v = cmuVisual;

        synchronized (this.visualMessage) {
            // Set name.
            System.out.println("Visual named: " + v.getName());
            visualMessage.setName(v.getName());

            // Get meshes.
            for (Geometry g : v.geometries.getValue()) {
                visualMessage.addMesh(new GeometryConverter(g).getMesh());
            }

            // Get appearance properties.
            Appearance app = v.frontFacingAppearance.getValue();

            AppearanceConverter appConverter = new AppearanceConverter(app);
            if (appConverter.getTexture() != null) {
                visualMessage.setTexture(appConverter.getTexture(), appConverter.getTextureWidth(), appConverter.getTextureHeight());
            }

            //TODO: Process other appearance properties.

            cmuVisual.addAbsoluteTransformationListener(this);
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
        synchronized(this.transformationListeners) {
            this.transformationListeners.add(listener);
        }
    }

    /**
     * Remove a listener for changes in the transformation of the
     * associated visual
     * @param listener The listener to remove
     */
    public void removeTransformationMessageListener(TransformationMessageListener listener) {
        synchronized(this.transformationListeners) {
            this.transformationListeners.remove(listener);
        }
    }

    /**
     * Notify listeners that the transformation has changed.
     */
    private void fireTransformationMessageChanged() {
        synchronized(this.transformationListeners) {
            for (TransformationMessageListener listener : this.transformationListeners) {
                synchronized(this.getTransformationMessage()) {
                    listener.transformationMessageChanged(new TransformationMessage(this.getTransformationMessage()));
                }
            }
            
        }
    }
}
