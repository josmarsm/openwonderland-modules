package org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.TexCoords;
import com.jme.scene.state.MaterialState;
import com.jme.system.DisplaySystem;
import com.jme.util.geom.BufferUtils;
import java.nio.IntBuffer;

/**
 * This is an abstract base class from which the different ruler model classes are derived.
 *
 * @author Carl Jokl
 */
public abstract class AbstractRuler extends Node {

    /**
     * The base color for the ruler.
     */
    public static final ColorRGBA RULER_COLOR = new ColorRGBA(0.78f, 0.63f, 0.0f, 1.0f);

    /**
     * This static utility method can be used to insert the specified vector i.e. a normal into an array of vectors.
     * 
     * @param destination The destination array of vectors into which the specified vector will be inserted the 
     *                    specified number of times.
     * @param startIndex The start index within the destination at which to start inserting the vector.
     * @param count The number times the vector is to be inserted which will be inserted starting at the start
     *              index up to the start index + the count minus one.
     */
    protected static void insertInto(Vector3f[] destination, Vector3f vectorToInsert, final int startIndex, final int count) {
        final int limit = startIndex + count;
        for (int index = startIndex; index < limit; index++) {
            destination[index] = vectorToInsert;
        }
    }

    /**
     * This static utility method can be used to insert the specified color into an array of colors.
     *
     * @param destination The destination array of colors into which the specified color will be inserted the
     *                    specified number of times.
     * @param startIndex The start index within the destination at which to start inserting the color.
     * @param count The number times the color is to be inserted which will be inserted starting at the start
     *              index up to the start index + the count minus one.
     */
    protected static void insertInto(ColorRGBA[] colors, ColorRGBA colorToInsert, final int startIndex, final int count) {
        final int limit = startIndex + count;
        for (int index = startIndex; index < limit; index++) {
            colors[index] = colorToInsert;
        }
    }

    /**
     * Create a TextCoords object containing the texture coordinates for an object which has a common
     * repeating texture coordinate pattern. The returned texture coordinates will be the specified TextureCoordinates
     * duplicated the specified number of times.
     *
     * @param noOfGroups The number of repeated groups of the specified texture coordinates to be created.
     * @param groupCoords A variable size group of texture coordinates to be created.
     * @return A TextCoords object containing the created TextureCoordinates.
     */
    protected static TexCoords createTexCoords(int noOfGroups, Vector2f... groupCoords) {
        Vector2f[] textureCoordinates = new Vector2f[groupCoords.length * noOfGroups];
        for (int groupIndex = 0, itemIndex = 0; groupIndex < noOfGroups; groupIndex++) {
            for (int groupItemIndex = 0; groupItemIndex < groupCoords.length; groupItemIndex++, itemIndex++) {
                textureCoordinates[itemIndex] = groupCoords[groupItemIndex];
            }
        }
        return TexCoords.makeNew(textureCoordinates);
    }

    /**
     * This is a standard default method when the mesh of a ruler is based on rectangles (which is normal for rulers).
     *
     * @param noOfGroups The number of rectangles for which to create standard texture coordinates.
     * @return The generated texture coordinates in a texture coordinates object.
     */
    protected static TexCoords createStandardQuadTexCoords(int noOfGroups) {
        return createTexCoords(noOfGroups, new Vector2f(0.0f, 0.0f), new Vector2f(1.0f, 0.0f), new Vector2f(0.0f, 1.0f), new Vector2f(1.0f, 1.0f));
    }

    /**
     * Create a new index buffer using a repeated pattern of index offsets.
     *
     * @param noOfGroups The number of repeated offset groups.
     * @param verticesPerGroup The number of vertices which are used to create the geometry of each group.
     *                         This is used because the offset values may include repeated vertices hiding the
     *                         number of vertices from which the offsets are being taken.
     * @param offsets A variable number of offsets in the groups.
     * @return An IntBuffer which contains the indices.
     */
    protected static IntBuffer createIndices(int noOfGroups, int verticesPerGroup, int... offsets) {
        int[] indices = new int[offsets.length * noOfGroups];
        for (int groupIndex = 0, itemIndex = 0, groupStart = 0; groupIndex < noOfGroups; groupIndex++, groupStart = groupIndex * verticesPerGroup) {
            for (int offsetIndex = 0; offsetIndex < offsets.length; offsetIndex++, itemIndex++) {
                indices[itemIndex] = offsets[offsetIndex] + groupStart;
            }
        }
        return BufferUtils.createIntBuffer(indices);
    }

    /**
     * This constructor is used for setting up common
     * render state as used by any ruler.
     *
     * @param name The name of this ruler.
     */
    protected AbstractRuler(String name) {
        super(name);
    }

    /**
     * This method is called by subclass rulers when the ruler is ready to initialize the
     * geometry to a Ruler material.
     */
    protected void initRulerMaterial() {
        Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
        MaterialState rulerMaterial = renderer.createMaterialState();
        rulerMaterial.setAmbient(new ColorRGBA(0.78f, 0.63f, 0.0f, 1.0f));
        rulerMaterial.setDiffuse(new ColorRGBA(0.78f, 0.63f, 0.0f, 1.0f));
        rulerMaterial.setSpecular(new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
        rulerMaterial.setShininess(0.4f);
        setRenderState(rulerMaterial);
    }

    /**
     * Standard initialization of ruler bounds.
     * This method can be overridden if needed.
     */
    protected void initBounds() {
        setModelBound(new BoundingBox());
        updateModelBound();
    }
}
