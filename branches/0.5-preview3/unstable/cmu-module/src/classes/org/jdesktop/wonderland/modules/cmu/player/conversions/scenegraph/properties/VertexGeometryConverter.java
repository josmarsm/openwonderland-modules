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
package org.jdesktop.wonderland.modules.cmu.player.conversions.scenegraph.properties;

import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.util.geom.BufferUtils;
import edu.cmu.cs.dennisc.math.Point3;
import edu.cmu.cs.dennisc.scenegraph.Vertex;
import edu.cmu.cs.dennisc.scenegraph.VertexGeometry;
import edu.cmu.cs.dennisc.texture.TextureCoordinate2f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Extracts a jME-compatible mesh from a CMU VertexGeometry object.
 * @param <VertexGeometryType> Specific subclass of VertexGeometry which is
 * being converted
 * @author kevin
 */
public class VertexGeometryConverter<VertexGeometryType extends VertexGeometry>
        extends GeometryConverter<VertexGeometryType> {

    final static private int PER_VERTEX = 3;
    final static private int PER_TEX_COORD = 2;
    final static private String VERTICES_PROPERTY_NAME = "vertices";
    final static private String INDICES_PROPERTY_NAME = "polygonData";
    final private TriMesh mesh;

    /**
     * Standard constructor.
     * @param vertexGeometry The Geometry to translate
     */
    public VertexGeometryConverter(VertexGeometryType vertexGeometry) {
        super(vertexGeometry);

        // Get vertex data.
        Vertex[] vertices = (Vertex[]) vertexGeometry.getPropertyNamed(VERTICES_PROPERTY_NAME).getValue(vertexGeometry);
        int[] indices = (int[]) vertexGeometry.getPropertyNamed(INDICES_PROPERTY_NAME).getValue(vertexGeometry);

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
        mesh.setName(vertexGeometry.getName());
    }

    /**
     * Get the geometry being wrapped.
     * @return The geometry for this object
     */
    @Override
    public VertexGeometryType getCMUGeometry() {
        return super.getCMUGeometry();
    }

    /**
     * Get the mesh described by this Geometry.
     * @return The Geometry's mesh
     */
    @Override
    public TriMesh getJMEGeometry() {
        return mesh;
    }

    /**
     * Vertex geometries consist of large amounts of data, and are not allowed
     * to change for bandwidth reasons; thus they are persistent.
     * @return True in any case
     */
    @Override
    public boolean isPersistent() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<String> getExpectedPropertyNames() {
        Collection<String> retVal = new ArrayList<String>();
        retVal.add(INDICES_PROPERTY_NAME);
        retVal.add(VERTICES_PROPERTY_NAME);
        return retVal;
    }
}
