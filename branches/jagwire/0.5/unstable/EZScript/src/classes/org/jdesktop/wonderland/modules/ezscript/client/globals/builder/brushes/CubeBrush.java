/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.globals.builder.brushes;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeViewerEntity;
import org.jdesktop.wonderland.modules.ezscript.client.cell.SmallCell;
import static org.jdesktop.wonderland.modules.ezscript.client.globals.Builder.STRING_TO_COLORS;

/**
 *
 * @author Ryan
 */
public class CubeBrush {

    private final ShapeViewerEntity BOX;

    private static final Logger logger = Logger.getLogger(CubeBrush.class.getName());
    private final ColorRGBA color;
    private final String textureURL;
    public CubeBrush(String textureURL, String color, boolean blend) {
        BOX = new ShapeViewerEntity("BOX");

        this.color = STRING_TO_COLORS.get(color.toLowerCase());
        this.textureURL = textureURL;
        BOX.setAppearance(STRING_TO_COLORS.get(color.toLowerCase()));
        BOX.setBlended(blend);
    }

    public void paintCube(final Vector3f position) {
//        BOX.showShape();
//        BOX.updateTransform(position, new Quaternion());

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(new Runnable() {
            public void run() {
                //create cell
                Cell cell = CubeCellCreator.create(textureURL, color, position);

                logger.warning("CELL CREATION FINISHED!");
                
                //remove entity
//                BOX.setVisible(false);
            }
        });



    }

    public ShapeViewerEntity getCube() {
        return BOX;
    }
}
