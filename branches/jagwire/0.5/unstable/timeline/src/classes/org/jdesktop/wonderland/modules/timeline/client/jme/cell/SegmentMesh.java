/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.client.jme.cell;

import java.nio.FloatBuffer;

import com.jme.math.Vector3f;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.util.geom.BufferUtils;
import java.util.logging.Logger;


// top (vertex in center)
//          s3
// 3------------------2
//  \                /
// s0\      4       / s2
//    \            /
//     0----------1
//          s1

//          s3
// 8------------------7
//  \                /
// s0\      9       / s2
//    \            /
//     5----------6
//          s1

/**
 * A four sided, two dimensional shape (a quadrilateral).
 * <p>
 * The local height of the {@code Quad} defines it's size about the y-axis,
 * while the trapHeight defines the x-axis. The z-axis will always be 0.
 *
 * @author Mark Powell
 * @version $Revision: 4131 $, $Date: 2009-03-19 13:15:28 -0700 (Thu, 19 Mar 2009) $
 */
public class SegmentMesh extends TriMesh {
    private static Logger logger = Logger.getLogger(SegmentMesh.class.getName());

    private static final long serialVersionUID = 1L;

    protected float trapSmallBase = 0;
    protected float trapLargeBase = 0;
    protected float trapHeight = 0;
    // change in 'x' from a small base vertex out to a large base vertex
    // (if trap side = hypotenuse, change = len of leg that is not height of trap)
    protected float change = 0;

    private static final int NUM_POINTS = 10;
    
    

    /** how thick this mesh will be */
    public static final float THICKNESS = 0.15f;

    public SegmentMesh() {
    }

    public SegmentMesh(String name, float in, float out, float w, float change, float climbPerMesh) {
      super(name);
      updateGeometry(in, out, w, change, climbPerMesh);
    }

    /**
     * <code>getCenter</code> returns the center of the <code>SegmentMesh</code>.
     *
     * @return Vector3f the center of the <code>SegmentMesh</code>.
     */
    public Vector3f getCenter() {
      return worldTranslation;
    }

    /**
     * <code>initialize</code> builds the data for the <code>Quad</code>
     * object.
     *
     * @param trapHeight
     *            the trapHeight of the <code>Quad</code>.
     * @param height
     *            the height of the <code>Quad</code>.
     * @deprecated Use {@link #updateGeometry(float,float)} instead
     */
    public void initialize(float width, float height, float w, float change, float climbPerMesh) {
      updateGeometry(width, height, w, change, climbPerMesh);
    }

    private void setIndexData() {
        getIndexBuffer().rewind();
        // bottom
        getIndexBuffer().put(0);
        getIndexBuffer().put(1);
        getIndexBuffer().put(4);
        getIndexBuffer().put(1);
        getIndexBuffer().put(2);
        getIndexBuffer().put(4);
        getIndexBuffer().put(2);
        getIndexBuffer().put(3);
        getIndexBuffer().put(4);
        getIndexBuffer().put(3);
        getIndexBuffer().put(0);
        getIndexBuffer().put(4);
        // s0
        getIndexBuffer().put(5);
        getIndexBuffer().put(0);
        getIndexBuffer().put(3);
        getIndexBuffer().put(3);
        getIndexBuffer().put(8);
        getIndexBuffer().put(5);
        // s1
        getIndexBuffer().put(5);
        getIndexBuffer().put(0);
        getIndexBuffer().put(1);
        getIndexBuffer().put(1);
        getIndexBuffer().put(6);
        getIndexBuffer().put(5);
        // s2
        getIndexBuffer().put(6);
        getIndexBuffer().put(1);
        getIndexBuffer().put(2);
        getIndexBuffer().put(2);
        getIndexBuffer().put(7);
        getIndexBuffer().put(6);
        // s3
        getIndexBuffer().put(7);
        getIndexBuffer().put(2);
        getIndexBuffer().put(3);
        getIndexBuffer().put(3);
        getIndexBuffer().put(8);
        getIndexBuffer().put(7);

        // top
        getIndexBuffer().put(5);
        getIndexBuffer().put(6);
        getIndexBuffer().put(9);
        getIndexBuffer().put(6);
        getIndexBuffer().put(7);
        getIndexBuffer().put(9);
        getIndexBuffer().put(7);
        getIndexBuffer().put(8);
        getIndexBuffer().put(9);
        getIndexBuffer().put(8);
        getIndexBuffer().put(5);
        getIndexBuffer().put(9);
    }

    /**
     * Sets all the default vertex normals to 'up', +1 in the Z direction.
     */
    private void setNormalData() {
      Vector3f v0 = new Vector3f(), v1 = new Vector3f(), v2 = new Vector3f();
      Vector3f res1, res2, normal;
      FloatBuffer norms = getNormalBuffer();
      norms.rewind();

      // bottom
//      logger.info("TRI count is " + getTriangleCount());
      int count = 0;
      for(int i = 0; i < (getTriangleCount() * 3); i+=3){
//        logger.info("getting normal for tri " + count++);
        fillVectors(v0,v1,v2,i);
        res1 = v1.subtract(v0);
        res2 = v2.subtract(v0);

        normal = res1.cross(res2);
        
//        logger.info("v0, 1, 2: " + v0 + " " + v1 + " " + v2);
//        logger.info("r1, r2: " + res1 + " " + res2);
//        logger.info("cross: "  + normal);
        
        normal.normalize();
        norms.put(normal.getX()).put(normal.getY()).put(normal.getZ());
      }
    }

    private void fillVectors(Vector3f v0, Vector3f v1, Vector3f v2, int i){
      fillVector(v0, i); // vert 1
      fillVector(v1, i+1); // vert 2
      fillVector(v2, i+2); // vert 3
    }

    private void fillVector(Vector3f v, int i){
//      logger.info("fill vector, from i:" + i);
      int idx = getIndexBuffer().get(i) * 3;
      // * 3 so for idx 2, skip the first 6elts
      // = the first 2 x/y/z trios = the first 2 vertices
      v.setX(getVertexBuffer().get(idx));
      v.setY(getVertexBuffer().get(idx+1));
      v.setZ(getVertexBuffer().get(idx+2));
    }

    private void setTextureData() {
      // what should go in here?
//      FloatBuffer tbuf = BufferUtils.createVector2Buffer(getVertexCount());
//      setTextureCoords(new TexCoords(tbuf));
//      tbuf.put(new float[] { 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1 });
      // quad
//      FloatBuffer tbuf = BufferUtils.createVector2Buffer(getVertexCount());
//      setTextureCoords(new TexCoords(tbuf));
//      tbuf.put(new float[] { 0, 1, 0, 0, 1, 0, 1, 1 });
      // hex
//        getTextureCoords().get(0).coords.put(0.25f).put(0);
//        getTextureCoords().get(0).coords.put(0.75f).put(0);
//        getTextureCoords().get(0).coords.put(1.0f).put(0.5f);
//        getTextureCoords().get(0).coords.put(0.75f).put(1.0f);
//        getTextureCoords().get(0).coords.put(0.25f).put(1.0f);
//        getTextureCoords().get(0).coords.put(0.0f).put(0.5f);
//        getTextureCoords().get(0).coords.put(0.5f).put(0.5f);
    }

    public Vector3f getVertex(int i){
      Vector3f v = new Vector3f();
      v.setX(getVertexBuffer().get(i * 3));
      v.setY(getVertexBuffer().get(i * 3 + 1));
      v.setZ(getVertexBuffer().get(i * 3 + 2));
      return v;
    }

    private void setVertexData(float climbPerMesh) {
      logger.fine("setting vertex data with climb per mesh of " + climbPerMesh);
      logger.fine("vtx 4: " + trapSmallBase *0.5f + " " + climbPerMesh * 0.5f + " " + trapHeight*0.5f);
      // movement in 'x' from small side points vs large side points
      // BOTTOM
      // 0
      getVertexBuffer().put(0.0f).put(0.0f).put(0.0f);
      // 1
      getVertexBuffer().put(trapSmallBase).put(climbPerMesh).put(0.0f);
      // 2
      getVertexBuffer().put(trapSmallBase + change).put(climbPerMesh).put(trapHeight);
      // 3
      getVertexBuffer().put(0.0f - change).put(0.0f).put(trapHeight);
      // 4
      getVertexBuffer().put(trapSmallBase*0.5f).put(climbPerMesh * 0.5f).put(trapHeight*0.5f);

      // TOP
      // 5
      getVertexBuffer().put(0.0f).put(THICKNESS).put(0.0f);
      // 6
      getVertexBuffer().put(trapSmallBase).put(THICKNESS + climbPerMesh).put(0.0f);
      // 7
      getVertexBuffer().put(trapSmallBase + change).put(THICKNESS + climbPerMesh).put(trapHeight);
      // 8
      getVertexBuffer().put(0.0f - change).put(THICKNESS).put(trapHeight);
      // 9
      getVertexBuffer().put(trapSmallBase*0.5f).put(THICKNESS + climbPerMesh * 0.5f).put(trapHeight*0.5f);
    }

    /**
     * Rebuild this quad based on a new set of parameters.
     *
     * @param trapHeight the trapHeight of the quad.
     * @param height the height of the quad.
     */
    public void updateGeometry(float in, float out, float w, float c, float climbPerMesh) {
      trapSmallBase = in;
      trapLargeBase = out;
      trapHeight = w;
      change = c;
      setVertexCount(NUM_POINTS);
      setTriangleQuantity(16);
      setVertexBuffer(BufferUtils.createVector3Buffer(getVertexCount()));
      setNormalBuffer(BufferUtils.createVector3Buffer(getTriangleCount()));
      getTextureCoords().set(0,
                new TexCoords(BufferUtils.createVector2Buffer(getVertexCount())));
      
      setIndexBuffer(BufferUtils.createIntBuffer(getTriangleCount() * 3));

      setVertexData(climbPerMesh);
      setIndexData();
//      setTextureData();
      setNormalData();
    }

}
