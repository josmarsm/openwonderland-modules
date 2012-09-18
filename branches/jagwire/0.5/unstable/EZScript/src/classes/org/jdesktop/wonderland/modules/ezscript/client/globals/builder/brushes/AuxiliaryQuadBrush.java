/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.globals.builder.brushes;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeViewerEntity;
import static org.jdesktop.wonderland.modules.ezscript.client.globals.Builder.STRING_TO_COLORS;
/**
 *
 * @author Ryan
 */
public class AuxiliaryQuadBrush {
    private final ShapeViewerEntity QUAD;

        public AuxiliaryQuadBrush() {
            QUAD = new ShapeViewerEntity("QUAD");

            QUAD.setAppearance(STRING_TO_COLORS.get("red"));
            QUAD.setBlended(false);
        }

        public void paintQuad(Vector3f position, Quaternion rotation) {
            QUAD.showShape();
            QUAD.updateTransform(position, rotation);
        }

        public ShapeViewerEntity getQuad() {
            return QUAD;
        }
}
