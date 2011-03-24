package org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.ruler.common.MeasurementUnits;
import org.jdesktop.wonderland.modules.ruler.common.RulerInfoHolder;
import org.jdesktop.wonderland.modules.ruler.common.RulerType;

/**
 * This renderer is used to perform visual rendering for the RulerCell.
 *
 * @author Carl Jokl
 */
public class RulerCellRenderer extends BasicRenderer {

    private Node node;

    public RulerCellRenderer(Cell cell) {
        super(cell);
    }

    /**
     * Create a triangle mesh for the specified RulerType.
     *
     * @param id The id of the object being created.
     * @param rulerType The type of ruler being used.
     * @param units The measurementUnits to use for creating the units.
     * @param rulerScale The scaling factor to use for the ruler relative to the number of units of the given
     *                   unit type for the e.g. number of meters or feet length of the ruler to measure.
     * @return A triangle mesh for the specified ruler attributes.
     */
    private static Node getRulerNode(String id, RulerType rulerType, MeasurementUnits units, float rulerScale) {
        Node rulerNode = null;
        if (rulerType != null && units != null) {
            switch(rulerType) {
                default:
                case STRAIGHT:
                    rulerNode = new StraightRuler(id, new Vector3f(0.0f, 0.0f, 0.0f), 0.02f, 0.005f, units, rulerScale);
                    break;
                case LSHAPE:
                    //ToDo create LShape
                    break;
                case THREE_AXIS:
                    //ToDo create 3D Ruler shape.
                    break;
            }
        }
        return rulerNode;
    }

    /**
     * Get the bounding volume for the RulerCell.
     *
     * @param rulerType The type of ruler being used.
     * @param units The measurement units used for this ruler.
     * @param rulerScale The scale of the ruler i.e. how many times the large unit the ruler is in length.
     * @return The bounding volume for the RulerCell.
     */
    private static BoundingVolume getBoundingVolume(RulerType rulerType, MeasurementUnits units, float rulerScale) {
        BoundingVolume volume = null;
        if (rulerType != null && units != null) {
            switch(rulerType) {
                default:
                case STRAIGHT:
                    volume = new BoundingBox(new Vector3f(), 0.02f, 0.005f, units.getMainUnitsPerMetre() * rulerScale);
                    break;
                case LSHAPE:
                    //ToDo create LShape
                    break;
                case THREE_AXIS:
                    //ToDo create 3D Ruler shape.
                    break;
            }
        }
        return volume;
    }

    /**
     * Create the scene graph for the specified entity.
     *
     * @param entity The entity for which to create a scene graph.
     * @return The root node of the screen graph.
     */
    @Override
    protected Node createSceneGraph(Entity entity) {
        if (cell != null) {
            if (cell instanceof RulerInfoHolder) {
                RulerInfoHolder rulerInfo = (RulerInfoHolder) cell;
                node = getRulerNode(cell.getCellID().toString(), rulerInfo.getRulerType(), rulerInfo.getUnits(), rulerInfo.getRulerScale());
            }
        }
        return node;
    }

    /**
     * Update the rendering of the RulerCell after a change has been made.
     */
    public void updateRuler() {
        if (cell != null && node != null) {
            if (cell instanceof RulerInfoHolder) {
                SceneWorker.addWorker(new RulerWorkCommit(node, cell.getCellID().toString(), (RulerInfoHolder) cell));
            }
        }
    }

    /**
     * Private class for use for WorkCommit operations which can be used for updates.
     */
    private static class RulerWorkCommit implements WorkCommit {

        private Node node;
        private final String name;
        private final RulerType rulerType;
        private final MeasurementUnits units;
        private final float scale;

        /**
         * Create a new RulerWorkCommit to run a RulerCell update using a SceneWorker.
         *
         * @param node The node which is the root of the Ruler screen-graph.
         * @param name The name of the ruler node.
         * @param rulerInfo An object containing information about the ruler in the RulerCell.
         */
        public RulerWorkCommit(final Node node, final String name, final RulerInfoHolder rulerInfo) {
            this(node, name, rulerInfo.getRulerType(), rulerInfo.getUnits(), rulerInfo.getRulerScale());
        }

        /**
         * Create a new RulerWorkCommit to run a RulerCell update using a SceneWorker.
         *
         * @param node The node which is the root of the Ruler screen-graph.
         * @param name The name of the ruler node.
         * @param rulerType The type of ruler being used.
         * @param units The units of the ruler.
         * @param scale The scale of the ruler.
         */
        public RulerWorkCommit(final Node node, final String name, final RulerType rulerType, final MeasurementUnits units, final float scale) {
            this.node = node;
            this.name = name;
            this.rulerType = rulerType;
            this.units = units;
            this.scale = scale;
        }

        @Override
        public void commit() {
            if (node != null) {
                node.detachAllChildren();
            }
            node = getRulerNode(name, rulerType, units, scale);
            if (node != null) {
                ClientContextJME.getWorldManager().addToUpdateList(node);
            }
        }

    }
}
