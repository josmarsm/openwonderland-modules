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
package org.jdesktop.wonderland.modules.cmu.player.conversions;

import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.util.geom.BufferUtils;
import edu.cmu.cs.dennisc.math.Point3;
import edu.cmu.cs.dennisc.scenegraph.Geometry;
import edu.cmu.cs.dennisc.scenegraph.Vertex;
import edu.cmu.cs.dennisc.texture.TextureCoordinate2f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Extracts a jME-compatible mesh from a CMU Geometry object.
 * @author kevin
 */
public class GeometryConverter {

    final static private int PER_VERTEX = 3;
    final static private int PER_TEX_COORD = 2;
    final static private String VERTICES_PROPERTY_NAME = "vertices";
    final static private String INDICES_PROPERTY_NAME = "polygonData";

    final private TriMesh mesh;

    /**
     * Standard constructor.
     * @param g The Geometry to translate
     */
    public GeometryConverter(Geometry g) {

        // Get vertex data.
        Vertex[] vertices = (Vertex[]) g.getPropertyNamed(VERTICES_PROPERTY_NAME).getValue(g);
        int[] indices = (int[]) g.getPropertyNamed(INDICES_PROPERTY_NAME).getValue(g);

        float[] fVertices = new float[vertices.length * PER_VERTEX];
        float[] fTexCoords = new float[vertices.length * PER_TEX_COORD];
        for (int i = 0; i < vertices.length; i++) {
            Point3 p = vertices[i].position;
            fVertices[i * PER_VERTEX + 0] = (float) p.x;
            fVertices[i * PER_VERTEX + 1] = (float) p.y;
            fVertices[i * PER_VERTEX + 2] = (float) p.z;

            TextureCoordinate2f t = vertices[i].textureCoordinate0;
            fTexCoords[i * PER_TEX_COORD + 0] = t.u;
            fTexCoords[i * PER_TEX_COORD + 1] = t.v;
        }

        // Place vertices.
        mesh = new TriMesh();

        FloatBuffer fVertexBuf = BufferUtils.createFloatBuffer(fVertices);
        FloatBuffer fTexBuf = BufferUtils.createFloatBuffer(fTexCoords);
        mesh.reconstruct(fVertexBuf, null, null, new TexCoords(fTexBuf, PER_TEX_COORD), IntBuffer.wrap(indices));
    }

    /**
     * Get the mesh described by this Geometry.
     * @return The Geometry's mesh
     */
    public TriMesh getMesh() {
        return mesh;
    }
}
