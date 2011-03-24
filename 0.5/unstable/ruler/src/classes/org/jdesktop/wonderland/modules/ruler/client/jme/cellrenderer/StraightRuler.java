package org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.system.DisplaySystem;
import com.jme.util.geom.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.jdesktop.wonderland.modules.ruler.common.MeasurementUnits;

/**
 * This class represents a straight ruler object as built up from a triangle mesh.
 *
 * @author Carl Jokl
 */
public class StraightRuler extends AbstractRuler {

    /**
     * Create the vertex buffer for the vertex positions for the StraightRuler.
     *
     * @param origin The origin of the ruler.
     * @param rulerWidth The width of the ruler.
     * @param rulerThickness The thickness of the ruler.
     * @param length The length of the ruler.
     * @return A float buffer which acts as the vertex buffer for the StraightRuler.
     */
    private static FloatBuffer createRulerVertexBuffer(Vector3f origin, float rulerWidth, float rulerThickness, float length) {
        final float offsetWidth = origin.x + rulerWidth;
        final float offsetHeight = origin.y + rulerThickness;
        final float offsetLength = origin.z + length;
        Vector3f bll = origin;
        Vector3f blr = new Vector3f(offsetWidth, origin.y, origin.z);
        Vector3f bul = new Vector3f(origin.x, origin.y, offsetLength);
        Vector3f bur = new Vector3f(offsetWidth, origin.y, offsetLength);
        Vector3f tll = new Vector3f(origin.x, offsetHeight, origin.z);
        Vector3f tlr = new Vector3f(offsetWidth, offsetHeight, origin.z);
        Vector3f tul = new Vector3f(origin.x, offsetHeight, offsetLength);
        Vector3f tur = new Vector3f(offsetWidth, offsetHeight, offsetLength);
        //No Y+ face as this is a separate mesh with the ruler measures in the form of a texture on it.
        Vector3f[] vertices = new Vector3f[] {
            //Z+ face Vertices
            tul, tur, bul, bur,
            //X- face Vertices
            tll, tul, bll, bul,
            //Z- face Vertices
            tlr, tll, blr, bll,
            //X+ face Vertices
            tur, tlr, bur, blr,
            //Y- face Vertices
            blr, bll, bur, bul
        };
        return BufferUtils.createFloatBuffer(vertices);
    }

    /**
     * Create the Normal vectors to go with the Vertices in the StraightRuler.
     *
     * @return A FloatBuffer containing the Vertex Normals for the StraightRuler vertices.
     */
    private static FloatBuffer createRulerVertexNormals() {
        //The vertex normals for this straight ruler (rectangular prism).
        Vector3f xPos = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f xNeg = new Vector3f(-1.0f, 0.0f, 0.0f);
        //No yPos as that face is on a separate Mesh.
        Vector3f yNeg = new Vector3f(0.0f, -1.0f, 0.0f);
        Vector3f zPos = new Vector3f(0.0f, 0.0f, 1.0f);
        Vector3f zNeg = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f[] normals = new Vector3f[20];
        insertInto(normals, zPos, 0, 4);
        insertInto(normals, xNeg, 4, 4);
        insertInto(normals, zNeg, 8, 4);
        insertInto(normals, xPos, 12, 4);
        insertInto(normals, yNeg, 16, 4);
        return BufferUtils.createFloatBuffer(normals);
    }

    /**
     * Create a texture coordinates object for the
     * texture coordinates of the ruler back.
     *
     * @return The texture coordinates for the back of the ruler.
     */
    private static TexCoords createTextureCoordinates() {
        return createStandardQuadTexCoords(5);
    }

    /**
     * Get the Indices of the Vertices used to build the
     * triangles of the ruler.
     *
     * @return An IntBuffer containing the
     */
    private static IntBuffer createFaceVertexIndices() {
        //5 groups with 4 vertices per group and the specified vertex offsets.
        return createIndices(5, 4, 0, 2, 1, 2, 3 , 1);
    }

    private TriMesh rulerBack;
    private RulerFace face;

    /**
     * Create a new instance of a StraightRuler with the specified name and dimensions.
     *
     * @param name The id of the ruler object.
     * @param origin The vector which is the origin position of the ruler (bottom left corner).
     * @param rulerWidth The width of the ruler.
     * @param rulerThickness The thickness (height) of the ruler.
     * @param units
     * @param scale
     */
    public StraightRuler(String id, Vector3f origin, float rulerWidth, float rulerThickness, MeasurementUnits units, float scale) {
        super(String.format("Straight Ruler (%s)", id));
        rulerBack = new TriMesh("Ruler Back",
                                createRulerVertexBuffer(origin, rulerWidth, rulerThickness, scale * units.getMainUnitsPerMetre()),
                                createRulerVertexNormals(),
                                null,
                                createTextureCoordinates(),
                                createFaceVertexIndices());
        face = new RulerFace(String.format("%s : face", name), new Vector3f(origin.x, origin.y + rulerThickness, origin.z), rulerWidth, units, scale);
        attachChild(rulerBack);
        attachChild(face);
        initRulerMaterial();
        initBounds();
    }
}
