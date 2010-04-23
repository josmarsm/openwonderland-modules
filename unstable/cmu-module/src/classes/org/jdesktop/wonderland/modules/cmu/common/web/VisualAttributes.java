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
package org.jdesktop.wonderland.modules.cmu.common.web;

import com.jme.scene.Geometry;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.cmu.common.messages.cmuclient.VisualMessage;

/**
 * Set of serializable attributes for a CMU visual.  IDs can be generated which
 * are unique to each set of attributes (not unique to a VisualAttributes instance,
 * however), and which can be used to identify a particular visual in a content
 * repository directory.  We use VisualAttributes objects to store large-scale
 * persistent data about a visual (mesh geometries, textures, etc.), and then
 * send smaller-scale updates directly via messages to a cell.
 * @author kevin
 */
public class VisualAttributes implements Serializable {

    private final Collection<Geometry> geometries = new Vector<Geometry>();
    private String name = null;
    private int[] texturePixels = null;
    private int textureWidth = 0,  textureHeight = 0;

    /**
     * Basic constructor.
     */
    public VisualAttributes() {
    }

    /**
     * Set the visual name.
     * @param name Name of this visual
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the visual name.
     * @return Name of this visual
     */
    public String getName() {
        return name;
    }

    /**
     * Get the geometries associated with this visual.  
     * @return Geometries for this visual
     */
    public Collection<Geometry> getGeometries() {
        synchronized (this.geometries) {
            return Collections.unmodifiableCollection(geometries);
        }
    }

    /**
     * Add a geometry to this visual.
     * @param geometry The geometry to add
     */
    public void addGeometry(Geometry geometry) {
        synchronized (this.geometries) {
            this.geometries.add(geometry);
        }
    }

    /**
     * Set the texture for this visual.  The image is converted to and stored
     * in serializable form.
     * @param texture The image to use as the texture
     * @param width Width of the image
     * @param height Height of the image
     */
    public void setTexture(Image texture, int width, int height) {
        texturePixels = new int[width * height];
        textureWidth = width;
        textureHeight = height;

        PixelGrabber pg = new PixelGrabber(texture, 0, 0, width, height, texturePixels, 0, width);

        try {
            pg.grabPixels();
        } catch (InterruptedException ex) {
            Logger.getLogger(VisualMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get the texture for this visual as an image.
     * @return Texture for this visual
     */
    public Image getTexture() {
        if (hasTexture()) {
            MemoryImageSource mis = new MemoryImageSource(textureWidth, textureHeight,
                    texturePixels, 0, textureWidth);
            Toolkit tk = Toolkit.getDefaultToolkit();
            return tk.createImage(mis);
        }
        return null;
    }

    /**
     * Find out whether this visual has an associated texture.
     * @return True if the visual has an associated texture, false otherwise.
     */
    public boolean hasTexture() {
        return texturePixels != null;
    }

    /**
     * Get the repository identifier for this visual.
     * @return Repository identifier
     */
    public VisualAttributesIdentifier getID() {
        return new VisualAttributesIdentifier(this);
    }

    /**
     * Object which can be used to identify CMU visuals based on their attributes;
     * makes this identification based on the attributes themselves (as opposed
     * to simply storing references to VisualAttributes instances), so that
     * visuals can be reloaded, shared across multiple scenes, etc., and still
     * share the same identifiers.  This allows resources to be efficiently
     * stored in and accessed from content repositories.
     */
    public static class VisualAttributesIdentifier implements Serializable {

        private static final String NAME_PREFIX = "cmu_";
        private final String contentNodeName;

        /**
         * Standard constructor; we do not allow these objects to be publicly
         * constructed, since an existing set of attributes is required for
         * them to exist.
         * @param attributes The set of attributes to identify
         */
        protected VisualAttributesIdentifier(VisualAttributes attributes) {
            String nodeName = NAME_PREFIX;
            nodeName += attributes.getName();
            if (attributes.getGeometries() != null) {
                for (Geometry mesh : attributes.getGeometries()) {
                    nodeName += "_" + mesh.getVertexCount();
                    if (attributes.texturePixels != null) {
                        long total = 0;
                        for (int i = 0; i < attributes.texturePixels.length; i++) {
                            total += attributes.texturePixels[i];
                        }
                        nodeName += "_" + total;
                        nodeName += "_" + attributes.textureWidth;
                        nodeName += "_" + attributes.textureHeight;
                    }
                }
            }
            contentNodeName = nodeName;
        }

        /**
         * Get a name that can be applied to a content node for this visual;
         * this name is unique to the set of properties which initialized this
         * identifier.
         * @return Name to use with a content node
         */
        public String getContentNodeName() {
            return contentNodeName + ".cmu";
        }

        /**
         * Compare this identifier to another object.
         * @param other The other object to compare
         * @return True if the other object is another identifier which
         * would point to the same file in a content repository
         */
        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other instanceof VisualAttributesIdentifier &&
                    getContentNodeName().equals(((VisualAttributesIdentifier) other).getContentNodeName())) {
                return true;
            }
            return false;
        }

        /**
         * Generate a hash code for the identifier.
         * @return Hash code for the identifier
         */
        @Override
        public int hashCode() {
            int hash = 5;
            hash = 11 * hash + (this.contentNodeName != null ? this.contentNodeName.hashCode() : 0);
            return hash;
        }
    }
}
