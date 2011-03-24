package org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer;

import com.jme.image.Texture;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.geom.BufferUtils;
import java.nio.IntBuffer;
import org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer.RulerImageFactory.RulerDisplayMetaData;
import org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer.RulerImageFactory.RulerImageHeight;
import org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer.RulerImageFactory.RulerStyleMetaData;
import org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer.RulerImageFactory.TextureOffsets;
import org.jdesktop.wonderland.modules.ruler.common.MeasurementUnits;

/**
 * This class is an implementation of a RulerFace which deals with drawing the measurement
 * units for use in measurement.
 *
 * @author Carl Jokl
 */
public class RulerFace extends TriMesh {

    /**
     * The different planes which the ruler can be drawn in depending on the ruler it is a part of.
     * Normally the ruler will be in the XZ plane as if it is on the floor facing upwards. However
     * for a ruler which measures in 3 dimensions a face will be needed placed vertically which
     * can either be in the XY or YZ planes.
     */
    public enum FacePlane {

        /**
         * The face is to be drawn in the X-Z plane.
         */
        XZ,

        /**
         * The face is to be drawn in the X-Y plane.
         */
        XY,

        /**
         * The face is to be drawn in the Y-Z plane.
         */
        YZ
    }

    /**
     * The smallest acknowledged size difference when setting a ruler of a given scale. If the
     * leftover difference between the minor units length divided by the length of the ruler
     * is lower than the tolerance then the final division will simply be stretched out to
     * the length of the ruler and no extra small division will be created.
     */
    private static final float TOLERANCE = 0.005f;

    /**
     * Calculate the number of divisions to use for the for the ruler.
     *
     * @param rulerLength The length of the ruler.
     * @param divisionLength The length of each ruler division.
     * @return The number of divisions which the ruler should use, taking into account tolerance.
     */
    private static int getNoOfDivisions(float rulerLength, float divisionLength) {
        int noOfDivisions = (int) (rulerLength / divisionLength);
        if ((rulerLength - (noOfDivisions * divisionLength)) > TOLERANCE) {
            noOfDivisions++;
        }
        return noOfDivisions;
    }

    /**
     * This constructor creates a new instance of a RulerFace.
     *
     * @param name The name of this RulerFace.
     * @param origin The origin position of the RulerFace.
     * @param rulerWidth The width of the ruler.
     * @param units The units of measure for the RulerFace.
     * @param scale The scale of the ruler face i.e. how many times the large unit of the unit scale is the
     *              length of the ruler face.
     * @param plane The plane in which the ruler will be drawn.
     * @param rotated Whether the ruler face should be drawn rotated 90 degrees from its normal direction i.e. at a right angle.
     */
    public RulerFace(String name, Vector3f origin, float rulerWidth, MeasurementUnits units, float scale, FacePlane plane, boolean rotated) {
        super(name);
        if (origin == null) {
            throw new IllegalArgumentException("The origin for the ruler face cannot be null!");
        }
        if (units == null) {
            throw new IllegalArgumentException("The units for the ruler face cannot be null!");
        }
        generateMesh(origin, rulerWidth, units, scale, plane, rotated);
    }

    /**
     * This constructor creates a new instance of a RulerFace. The face will not be rotated and will be drawn in the XZ plane.
     *
     * @param name The name of this RulerFace.
     * @param origin The origin position of the RulerFace.
     * @param rulerWidth The width of the ruler.
     * @param units The units of measure for the RulerFace.
     * @param scale The scale of the ruler face i.e. how many times the large unit of the unit scale is the
     *              length of the ruler face.
     */
    public RulerFace(String name, Vector3f origin, float rulerWidth, MeasurementUnits units, float scale) {
        this(name, origin, rulerWidth, units, scale, FacePlane.XZ, false);
    }

    /**
     * Generate the indices of the ruler faces.
     *
     * @param noOfDivisions The number of ruler divisions.
     * @return The indices of the ruler faces.
     */
    private static IntBuffer generateFaceIndices(int noOfDivisions) {
        return AbstractRuler.createIndices(noOfDivisions, 4, 0, 2, 1, 2, 3, 1);
    }

    /**
     * Generate a texture for the notches on the ruler.
     *
     * @param noOfGroups The number of groups on the ruler.
     * @param notchesPerGroup The number of notches per unit group of the ruler.
     * @param lastGroupScale The scaling factor for the coordinates of the last group if the last group is shorter.
     * @param labelNotches Whether the number label should apply to the number of notches. True for the number to be the total number of notches up to that point and
     *                     false if the label is the number of groups up to that point.
     * @param significantNotches An array containing the notches which are significant and should have higher marks.
     * @return The texture offset of where in the texture the notches for each group are located.
     *         These are two groups with alternate colors.
     */
    private TexCoords setRulerFaceTexture(final int noOfGroups, final int notchesPerGroup, final float lastGroupScale, boolean labelNotches, int[] significantNotches) {
        //TextureOffsets offsets = new TextureOffsets();
        final int noOfVertices = noOfGroups * 4;
        Vector2f[] textureCoordVectors = new Vector2f[noOfVertices];
        Texture rulerNotchesTexture = RulerImageFactory.createTexture(new RulerDisplayMetaData(noOfGroups, notchesPerGroup, RulerImageHeight.MEDIUM, 0, labelNotches, lastGroupScale, significantNotches),
                                                                      new RulerStyleMetaData(2, ColorRGBA.black.clone(), ColorRGBA.lightGray.clone(),  ColorRGBA.white.clone(), ColorRGBA.black.clone()),
                                                                      textureCoordVectors);
        Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
        TextureState textureState = renderer.createTextureState();
        textureState.setTexture(rulerNotchesTexture);
        setRenderState(textureState);
        return TexCoords.makeNew(textureCoordVectors);
    }

    /**
     * Create the texture coordinates for this ruler face.
     *
     * @param noOfVertices The number of vertices on the RulerFace for which texture coordinates must be generated.
     * @param noOfDivisions The number of divisions
     * @param rulerLength The length of the ruler.
     * @param groupSpacing The spacing between different group faces.
     * @return The texture coordinate for the ruler.
     */
    private static TexCoords generateTextureCoordinates(int noOfVertices, int noOfDivisions, float rulerLength, float groupSpacing, TextureOffsets offsets) {
        Vector2f textureCoords[] = new Vector2f[noOfVertices];
        Vector2f gp1ll = new Vector2f(offsets.group1MinU, 0.0f);
        Vector2f gp1lr = new Vector2f(offsets.group1MaxU, 0.0f);
        Vector2f gp1ul = new Vector2f(offsets.group1MinU, 1.0f);
        Vector2f gp1ur = new Vector2f(offsets.group1MaxU, 1.0f);
        Vector2f gp2ll = new Vector2f(offsets.group2MinU, 0.0f);
        Vector2f gp2lr = new Vector2f(offsets.group2MaxU, 0.0f);
        Vector2f gp2ul = new Vector2f(offsets.group2MinU, 1.0f);
        Vector2f gp2ur = new Vector2f(offsets.group2MaxU, 1.0f);
        float currentOffset = 0.0f;
        float currentU = 0.0f;
        boolean gp1 = true;
        for (int vertexIndex = 0; (vertexIndex + 3) < noOfVertices; vertexIndex++, gp1 = (vertexIndex % 8 == 0)) {
            textureCoords[vertexIndex] = gp1 ? gp1ll : gp2ll;
            vertexIndex++;
            textureCoords[vertexIndex] = gp1 ? gp1ul : gp2ul;
            vertexIndex++;
            if (vertexIndex == (noOfVertices - 2)) {
                currentU = gp1 ? offsets.group1MaxU : offsets.group2MaxU;
                currentOffset = ((rulerLength - ((noOfDivisions - 1) * groupSpacing)) / groupSpacing) * currentU;
                
                textureCoords[vertexIndex] = new Vector2f(currentOffset, 0.0f);
                vertexIndex++;
                textureCoords[vertexIndex] = new Vector2f(currentOffset, 1.0f);
            }
            else {
                textureCoords[vertexIndex] = gp1 ? gp1lr : gp2lr;
                vertexIndex++;
                textureCoords[vertexIndex] = gp1 ? gp1ur : gp2ur;
            }
        }
        return TexCoords.makeNew(textureCoords);
    }

    /**
     * This method is used to generate the mesh for the RulerFace.
     *
     * @param origin The origin of the of the RulerFace.
     * @param rulerWidth The width of the RulerFace.
     * @param units The units which are used in the RulerFace.
     * @param scale The scale i.e. the number of times the large MeasumentUnits size the ruler is in length.
     */
    protected final void generateMesh(Vector3f origin, float rulerWidth, MeasurementUnits units, float scale, FacePlane plane, boolean rotated) {
        final float rulerLength = units.getMainUnitsPerMetre() * scale;
        final float groupSpacing = units.getSmallUnitsPerMetre();
        final int noOfDivisions = getNoOfDivisions(rulerLength, groupSpacing);
        Vector3f[] vertices = createVertices(noOfDivisions, groupSpacing, origin, rulerWidth, rulerLength, plane, rotated);
        Vector3f[] normals = new Vector3f[vertices.length];
        float lastGroupScale = (groupSpacing - ((groupSpacing * noOfDivisions) - rulerLength)) / groupSpacing;
        if (1.0f - lastGroupScale < TOLERANCE) {
            lastGroupScale = 1.0f;
        }
        AbstractRuler.insertInto(normals, new Vector3f(0.0f, 1.0f, 0.0f), 0, normals.length);
        reconstruct(BufferUtils.createFloatBuffer(vertices),
                    BufferUtils.createFloatBuffer(normals),
                    null,
                    setRulerFaceTexture(noOfDivisions, units.getSubUnitsInSmall(), lastGroupScale, units.isSmallestUnitCounted(), units.getSignificantCounts()),
                    generateFaceIndices(noOfDivisions));

    }

    /**
     * Get the end positions for each axis given the origin and the orientation of the ruler.
     *
     * @param noOfDivisions The number of divisions which the ruler is divided into.
     * @param groupSpacing The spacing between different groups.
     * @param origin The point of origin for the ruler.
     * @param rulerWidth The width of the ruler.
     * @param units The units of measure that the ruler is using.
     * @param scale The scale of the ruler i.e. how many times the length of the large unit which the ruler is in length.
     * @param plane The plane in which the RulerFace will be drawn.
     * @return The end coordinates for the RulerFace.
     */
    private Vector3f[] createVertices(final int noOfDivisions, final float groupSpacing, Vector3f origin, float rulerWidth, float rulerLength, FacePlane plane, boolean rotated) {
        final int noOfVertices = noOfDivisions * 4;
        final int max = noOfVertices - 2;
        Vector3f[] vertices = new Vector3f[noOfVertices];
        Vector3f end = null;
        vertices[0] = origin;
        float currentOffset = 0.0f;
        if (plane == FacePlane.XY) {
            if (rotated) {
                end = new Vector3f(origin.x + rulerWidth, origin.y + rulerLength, origin.z);
                vertices[1] = new Vector3f(end.x, origin.y, origin.z);
                for (int vertexIndex = 2, divisionIndex = 1; (vertexIndex + 3) < max; vertexIndex++, divisionIndex++) {
                    currentOffset = (groupSpacing * divisionIndex) + origin.z;
                    vertices[vertexIndex] = new Vector3f(origin.x, currentOffset, origin.z);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(end.x, currentOffset, origin.z);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(origin.x, currentOffset, origin.z);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(end.x, currentOffset, origin.z);
                }
                vertices[noOfVertices - 2] = new Vector3f(origin.x, end.y, end.z);
            }
            else {
                end = new Vector3f(origin.x + rulerLength, origin.y + rulerWidth, origin.z);
                vertices[1] = new Vector3f(origin.x, end.y, origin.z);
                for (int vertexIndex = 2, divisionIndex = 1; (vertexIndex + 3) < max; vertexIndex++, divisionIndex++) {
                    currentOffset = (groupSpacing * divisionIndex) + origin.z;
                    vertices[vertexIndex] = new Vector3f(currentOffset, origin.y, origin.z);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(currentOffset, end.y, origin.z);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(currentOffset, origin.y, origin.z);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(currentOffset, end.y, origin.z);
                }
                vertices[noOfVertices - 2] = new Vector3f(end.x, origin.y, end.z);
            }
        }
        else if (plane == FacePlane.YZ) {
            if (rotated) {
                end = new Vector3f(origin.x, origin.y + rulerLength, origin.z + rulerWidth);
                vertices[1] = new Vector3f(origin.x, origin.y, end.z);
                for (int vertexIndex = 2, divisionIndex = 1; (vertexIndex + 3) < max; vertexIndex++, divisionIndex++) {
                    currentOffset = (groupSpacing * divisionIndex) + origin.z;
                    vertices[vertexIndex] = new Vector3f(origin.x, currentOffset, origin.z);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(origin.x, currentOffset, end.z);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(origin.x, currentOffset, origin.z);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(origin.x, currentOffset, end.z);
                }
                vertices[noOfVertices - 2] = new Vector3f(end.x, end.y, origin.z);
            }
            else {
                end = new Vector3f(origin.x, origin.y + rulerWidth, origin.z + rulerLength);
                vertices[1] = new Vector3f(origin.x, end.y, origin.z);
                for (int vertexIndex = 2, divisionIndex = 1; (vertexIndex + 3) < max; vertexIndex++, divisionIndex++) {
                    currentOffset = (groupSpacing * divisionIndex) + origin.z;
                    vertices[vertexIndex] = new Vector3f(origin.x, origin.y, currentOffset);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(origin.x, end.y, currentOffset);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(origin.x, origin.y, currentOffset);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(origin.x, end.y, currentOffset);
                }
                vertices[noOfVertices - 2] = new Vector3f(end.x, origin.y, end.z);
            }
        }
        else {
            if (rotated) {
                end = new Vector3f(origin.x + rulerLength, origin.y, origin.z + rulerWidth);
                vertices[1] = new Vector3f(origin.x, origin.y, end.z);
                for (int vertexIndex = 2, divisionIndex = 1; (vertexIndex + 3) < max; vertexIndex++, divisionIndex++) {
                    currentOffset = (groupSpacing * divisionIndex) + origin.z;
                    vertices[vertexIndex] = new Vector3f(currentOffset, origin.y, origin.z);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(currentOffset, origin.y, end.z);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(currentOffset, origin.y, origin.z);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(currentOffset, origin.y, end.z);
                }
                vertices[noOfVertices - 2] = new Vector3f(end.x, end.y, origin.z);
            }
            else {
                end = new Vector3f(origin.x + rulerWidth, origin.y, origin.z + rulerLength);
                vertices[1] = new Vector3f(end.x, origin.y, origin.z);
                for (int vertexIndex = 2, divisionIndex = 1; (vertexIndex + 3) < max; vertexIndex++, divisionIndex++) {
                    currentOffset = (groupSpacing * divisionIndex) + origin.z;
                    vertices[vertexIndex] = new Vector3f(origin.x, origin.y, currentOffset);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(end.x, origin.y, currentOffset);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(origin.x, origin.y, currentOffset);
                    vertexIndex++;
                    vertices[vertexIndex] = new Vector3f(end.x, origin.y, currentOffset);
                }
                vertices[noOfVertices - 2] = new Vector3f(origin.x, end.y, end.z);
            }
        }
        vertices[noOfVertices - 1] = end;
        return vertices;
    }
}
