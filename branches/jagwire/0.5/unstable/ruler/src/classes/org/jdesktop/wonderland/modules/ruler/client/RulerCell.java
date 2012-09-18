package org.jdesktop.wonderland.modules.ruler.client;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.ruler.client.jme.cellrenderer.RulerCellRenderer;
import org.jdesktop.wonderland.modules.ruler.common.MeasurementUnits;
import org.jdesktop.wonderland.modules.ruler.common.RulerCellUnitChangeMessage;
import org.jdesktop.wonderland.modules.ruler.common.RulerCellState;
import org.jdesktop.wonderland.modules.ruler.common.RulerCellTypeChangeMessage;
import org.jdesktop.wonderland.modules.ruler.common.RulerType;

/**
 * This class represents a RulerCell which can be used to provide a ruler for
 * measurement. This can assist in getting an idea of how big representations
 * of objects are in world compared to real world units.
 *
 * @author Carl Jokl
 */
public class RulerCell extends Cell implements RulerCellState {

    private RulerType rulerType;
    private MeasurementUnits units;
    private float scale;
    private RulerCellRenderer renderer = null;
    private MouseEventListener listener = null;

    /**
     * Create a new instance of a RulerCell.
     *
     * @param cellID The id of the RulerCell instance which will be used to identify it later.
     * @param cellCache The CellCache to use to setup the RulerCell.
     */
    public RulerCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        scale = 1.0f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RulerType getRulerType() {
        return rulerType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRulerType(RulerType rulerType) {
        this.rulerType = rulerType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeasurementUnits getUnits() {
        return units;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUnits(MeasurementUnits units) {
        this.units = units;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getRulerScale() {
        return scale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRulerScale(float scale) {
        this.scale = scale;
    }

    /**
     * Set the state of the RulerCell using the specified
     * CellClientCell instance.
     *
     * @param state The CellClientState instance with which
     *              to set state of the RulerCell. The
     *              state is normally expected to be an
     *              instance of RulerCellState.
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        if (state instanceof RulerCellState) {
            RulerCellState rulerState = (RulerCellState) state;
            rulerType = rulerState.getRulerType();
            units = rulerState.getUnits();
        }
    }

    /**
     * Create a CellRenderer to render this RulerCell.
     *
     * @param rendererType An enumeration representation of the type of renderer to use to render the cell.
     * @return A CellRenderer which is used to visually render this cell.
     */
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            renderer = new RulerCellRenderer(this);
            return renderer;
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }

    /**
     * Set the CellStatus of this RulerCell.
     *
     * @param status The new status of the RulerCell.
     * @param increasing Whether the state is going to an increased or decreased state.
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        if ( renderer != null) {
            if (status == CellStatus.INACTIVE && !increasing && listener != null) {
                listener.removeFromEntity(renderer.getEntity());
            }
            else if (status == CellStatus.RENDERING && increasing && listener == null) {
                listener = new MouseEventListener();
                listener.addToEntity(renderer.getEntity());
            }
        }
    }

    /**
     * Mouse event listener which listens for mouse events which occur on this ruler.
     */
    private class MouseEventListener extends EventClassListener {

        private int nextTypeIndex;
        private final RulerType[] types = RulerType.values();

        /**
         * Create a new instance of the MouseEventListener for this RulerCell.
         */
        public MouseEventListener() {
           
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseButtonEvent3D.class };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void commitEvent(Event event) {
            if (event instanceof MouseButtonEvent3D) {
                MouseButtonEvent3D mouseButtonEvent = (MouseButtonEvent3D) event;
                if (mouseButtonEvent.isClicked() && mouseButtonEvent.getButton() == ButtonId.BUTTON1) {
                    
                }
            }
        }
    }

    /**
     * Private inner class used by the RulerCell to receive messages.
     */
    private class RulerCellMessageReceiver implements ComponentMessageReceiver {

        /**
         * Receive message for the RulerCell.
         *
         * @param message The message received for the RulerCell.
         */
        @Override
        public void messageReceived(CellMessage message) {
            if (renderer != null && !message.getSenderID().equals(getCellCache().getSession().getID())) {
                if (message instanceof RulerCellUnitChangeMessage) {
                    units = ((RulerCellUnitChangeMessage) message).getUnits();
                    renderer.updateRuler();
                }
                else if(message instanceof RulerCellTypeChangeMessage) {
                    rulerType = ((RulerCellTypeChangeMessage) message).getRulerType();
                    renderer.updateRuler();
                }
            }
        }
    }
}
