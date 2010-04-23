/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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

import edu.cmu.cs.dennisc.alice.ast.AbstractType;
import edu.cmu.cs.dennisc.property.event.PropertyListener;
import edu.cmu.cs.dennisc.scenegraph.event.AbsoluteTransformationEvent;
import edu.cmu.cs.dennisc.scenegraph.event.AbsoluteTransformationListener;
import java.util.HashSet;
import java.util.Set;
import org.jdesktop.wonderland.modules.cmu.common.NodeID;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.NodeUpdateMessage;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.TransformationMessage;
import org.jdesktop.wonderland.modules.cmu.player.NodeUpdateListener;
import org.jdesktop.wonderland.modules.cmu.player.conversions.math.OrthogonalMatrix3x3Converter;
import org.jdesktop.wonderland.modules.cmu.player.conversions.math.Point3Converter;
import org.jdesktop.wonderland.modules.cmu.player.conversions.pattern.PropertyOwnerConverter;

/**
 * Converter for a CMU Transformable; listens to transformation updates,
 * and allows listeners to register to receive updates for the Transformable
 * which is being wrapped.
 * @param <TransformableType> The class of the Transformable which is being
 * wrapped.  Making this generic allows subclasses to force the
 * <code>getTransformable()</code> method to return a particular subclass of
 * Transformable, allowing for more flexible wrapping of subclasses of
 * Transformable without having to duplicate data.
 * @author kevin
 */
public abstract class TransformableConverter<TransformableType extends org.alice.apis.moveandturn.Transformable>
        extends PropertyOwnerConverter implements AbsoluteTransformationListener, PropertyListener {

    // Unique ID
    private final NodeID nodeID;

    // CMU class identifier
    //private final AbstractType type;

    // Scene graph objects
    private final TransformableType transformable;
    private final edu.cmu.cs.dennisc.scenegraph.Transformable sgTransformable;

    // Serializable data for this transformable
    private final TransformationMessage transformation;

    // Update listeners
    private final Set<NodeUpdateListener> updateListeners = new HashSet<NodeUpdateListener>();

    /**
     * Standard constructor.
     * @param transformable The Transformable to wrap
     */
    public TransformableConverter(TransformableType transformable) {
        super(transformable);
        this.nodeID = NodeID.generateNewID();
        this.transformable = transformable;
        this.sgTransformable = transformable.getSGTransformable();
        this.sgTransformable.addAbsoluteTransformationListener(this);
        this.transformation = new TransformationMessage(getNodeID());
        updateTransformation();
    }

    /**
     * Get the unique ID for this transformable.
     * @return The node ID, uniquely generated at construction
     */
    public NodeID getNodeID() {
        return nodeID;
    }

    /**
     * Get the Transformable which is being wrapped by this converter.
     * @return The wrapped transformable
     */
    public TransformableType getTransformable() {
        return transformable;
    }

    /**
     * Get the CMU scene graph element associated with the Transformable
     * being wrapped by this converter.
     * @return The associated scene graph element
     */
    protected edu.cmu.cs.dennisc.scenegraph.Transformable getSGTransformable() {
        return sgTransformable;
    }

    /**
     * The transformation in serializable form.
     * @return The TransformationMessage associated with this visual
     */
    public TransformationMessage getTransformationMessage() {
        synchronized (transformation) {
            return new TransformationMessage(transformation);
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
     * Get the current transformation, and load it into this wrapper.
     * Send the updated transformation to any update listeners.
     */
    protected void updateTransformation() {
        TransformationMessage newTransform = null;
        synchronized (transformation) {
            transformation.setTranslation(new Point3Converter(getSGTransformable().getTranslation(getSGTransformable().getRoot())).getVector3f());
            transformation.setRotation(new OrthogonalMatrix3x3Converter(getSGTransformable().getTransformation(getSGTransformable().getRoot()).orientation).getMatrix3f());
            newTransform = new TransformationMessage(transformation);
        }
        fireNodeUpdated(newTransform);
    }

    /**
     * Disassociate this wrapper with its transformable.
     */
    public void unload() {
        getSGTransformable().removeAbsoluteTransformationListener(this);
    }

    /**
     * Add a listener for changes that should be propagated to any visual
     * node matching this transformable.
     * @param listener The listener to add
     */
    public void addNodeUpdateListener(NodeUpdateListener listener) {
        synchronized (this.updateListeners) {
            this.updateListeners.add(listener);
        }
    }

    /**
     * Remove a node update listener.
     * @param listener The listener to remove
     */
    public void removeNodeUpdateListener(NodeUpdateListener listener) {
        synchronized (this.updateListeners) {
            this.updateListeners.remove(listener);
        }
    }

    /**
     * Notify listeners that the node has changed.
     * @param message The notification message to send
     */
    protected void fireNodeUpdated(NodeUpdateMessage message) {
        synchronized (this.updateListeners) {
            for (NodeUpdateListener listener : this.updateListeners) {
                listener.modelUpdated(message);
            }
        }
    }
}
