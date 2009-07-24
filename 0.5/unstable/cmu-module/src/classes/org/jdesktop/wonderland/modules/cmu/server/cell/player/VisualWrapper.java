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
package org.jdesktop.wonderland.modules.cmu.server.cell.player;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.cmu.common.cell.player.TransformationMessage;
import org.jdesktop.wonderland.modules.cmu.common.cell.player.VisualMessage;
import edu.cmu.cs.dennisc.scenegraph.Appearance;
import edu.cmu.cs.dennisc.scenegraph.Geometry;
import edu.cmu.cs.dennisc.scenegraph.Visual;
import edu.cmu.cs.dennisc.scenegraph.event.AbsoluteTransformationEvent;
import edu.cmu.cs.dennisc.scenegraph.event.AbsoluteTransformationListener;
import edu.cmu.cs.dennisc.texture.BufferedImageTexture;
import java.awt.image.BufferedImage;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Vector;

/**
 * Treats a CMU Visual instance as a jME node, listens for transformation changes.
 * @author kevin
 */
public class VisualWrapper implements AbsoluteTransformationListener, ConnectionListener {

    private static int numNodes = 0;
    private Visual cmuVisual = null;
    private VisualMessage visualMessage = null;
    private final Vector<ObjectOutputStream> connections = new Vector<ObjectOutputStream>();
    protected final int nodeID;


    {
        // Set node ID in every constructor.
        synchronized (this.getClass()) {
            this.nodeID = numNodes;
            numNodes++;
        }
    }

    /**
     * Constructor; use the default Node constructor, and attach this node
     * to a CMU Visual.
     * @param v The CMU visual to attach
     */
    public VisualWrapper(Visual v, Collection<OutputStream> connections) {
        setVisual(v);
        setConnections(connections);
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
    public int getNodeID() {
        return nodeID;
    }

    /**
     * The entire visual in serializable form.
     * @return The VisualMessage associated with this visual
     */
    protected VisualMessage getVisualMessage() {
        return this.visualMessage;
    }

    /**
     * Copy a full collection of connections into our own vector.
     * @param connections The collection to copy
     */
    public void setConnections(Collection<OutputStream> connections) {
        synchronized(this.connections) {
            this.connections.clear();
            for (OutputStream connection : connections) {
                try {
                    this.connections.add(new ObjectOutputStream(connection));
                } catch (IOException ex) {
                    Logger.getLogger(VisualWrapper.class.getName()).log(Level.SEVERE, null, ex);
                }
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
        sendTransformation();
    }

    /**
     * Get the current CMU node transformation, and load it into this wrapper.
     * Synchronize this on our transformation message, so that all updates happen
     * at once.
     */
    protected void updateTransformation() {
        TransformationMessage transformation = this.getVisualMessage().getTransformation();
        synchronized (transformation) {
            transformation.setScale((float) cmuVisual.scale.getCopy(cmuVisual).right.x);
            transformation.setTranslation(cmuVisual.getTranslation(cmuVisual.getRoot()));
            transformation.setRotation(cmuVisual.getTransformation(cmuVisual.getRoot()).orientation);
        }

    /*
    ClientContextJME.getWorldManager().addRenderUpdater(new RenderUpdater() {

    public void update(Object arg0) {
    Composite sg = cmuVisual.getRoot();

    // Translation, rotation, scaling.
    Point3 translation = cmuVisual.getTranslation(cmuVisual.getRoot());
    OrthogonalMatrix3x3 rotation = new OrthogonalMatrix3x3(cmuVisual.getTransformation(sg).orientation);
    Matrix3x3 scale = cmuVisual.scale.getCopy(cmuVisual);
    }
    },
    null);
    ClientContextJME.getWorldManager().addToUpdateList(this);
     */
    }

    /**
     * Set the CMU visual to mirror, and load its visual properties.
     * Synchronize on the visual message during this process, so that it isn't e.g. sent
     * halfway through being updated.
     * @param v The CMU visual to mirror
     */
    public void setVisual(Visual v) {
        assert v != null;

        cmuVisual = v;
        visualMessage = new VisualMessage(this.getNodeID());

        synchronized (this.getVisualMessage()) {
            for (Geometry g : v.geometries.getValue()) {
                visualMessage.addGeometry(g);
            }

            Appearance app = v.frontFacingAppearance.getValue();

            // Set texture properties.
            edu.cmu.cs.dennisc.texture.Texture cmuText = (edu.cmu.cs.dennisc.texture.Texture) (app.getPropertyNamed("bumpTexture").getValue(app));
            if (cmuText == null) {
                cmuText = (edu.cmu.cs.dennisc.texture.Texture) (app.getPropertyNamed("diffuseColorTexture").getValue(app));
            }

            if (cmuText != null && BufferedImageTexture.class.isAssignableFrom(cmuText.getClass())) {
                BufferedImage image = ((BufferedImageTexture) cmuText).getBufferedImage();
                visualMessage.setTexture(image, image.getWidth(), image.getHeight());
            }

            //TODO: Handle other appearance properties.
            //for (Property p : app.getProperties()) {
            //    System.out.println("APPEARANCE PROPERTY: " + p);
            //    System.out.println(p.getValue(app));
            //}

            cmuVisual.addAbsoluteTransformationListener(this);
            updateTransformation();
            sendVisual();
        }
    }

    /**
     * Transmit the entire visual data across each stored connection.  Make sure
     * that neither the visual nor its transformation changes during each
     * send, and that the connections vector doesn't change throughout the entire
     * process.
     */
    protected void sendVisual() {
        synchronized (this.connections) {
            for (ObjectOutputStream objectStream : this.connections) {
                try {
                    synchronized (this.getVisualMessage()) {
                        synchronized (this.getVisualMessage().getTransformation()) {
                            objectStream.writeObject(this.visualMessage);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(VisualWrapper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Transmit just the transformation data across each stored connection.
     * Make sure that the transformation data doesn't change during each send,
     * and make sure the connections vector doesn't change throughout the entire
     * process.
     */
    protected void sendTransformation() {
        synchronized (this.connections) {
            for (ObjectOutputStream objectStream : this.connections) {
                try {
                    synchronized (this.getVisualMessage().getTransformation()) {
                        objectStream.writeObject(this.getVisualMessage().getTransformation());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(VisualWrapper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Add a connection to transmit data across.
     * @param connection The connection to transmit data
     */
    @Override
    public void connectionAdded(OutputStream connection) {
        synchronized (connections) {
            try {
                connections.add(new ObjectOutputStream(connection));
            } catch (IOException ex) {
                Logger.getLogger(VisualWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /*        // Bounding box and translation.
    Triangle[] tris = new Triangle[mesh.getTriangleCount()];

    BoundingBox bbox = new BoundingBox();
    bbox.computeFromTris(mesh.getMeshAsTriangles(tris), 0, tris.length);

    this.setLocalTranslation(0, 0, 0);
    this.setModelBound(bbox);
     */
}
