package org.jdesktop.wonderland.modules.ruler.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.ruler.common.MeasurementUnits;
import org.jdesktop.wonderland.modules.ruler.common.RulerCellUnitChangeMessage;
import org.jdesktop.wonderland.modules.ruler.common.RulerCellClientState;
import org.jdesktop.wonderland.modules.ruler.common.RulerCellServerState;
import org.jdesktop.wonderland.modules.ruler.common.RulerCellState;
import org.jdesktop.wonderland.modules.ruler.common.RulerCellTypeChangeMessage;
import org.jdesktop.wonderland.modules.ruler.common.RulerType;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * This class represents a server managed object for the RulerCell.
 *
 * @author Carl Jokl
 */
public class RulerCellMO extends CellMO implements RulerCellState {

    /**
     * The class name of the client RuleCell.
     */
    public static final String CLIENT_CELL_CLASS_NAME = "org.jdesktop.wonderland.modules.ruler.client.RulerCell";

    private RulerType rulerType;
    private MeasurementUnits units;
    private float scale;

    /**
     * Get the name of the RuleCell client class.
     *
     * @param clientID The id of the Wonderland client for which to get the cell class.
     * @param capabilities The capabilities of the Wonderland client.
     * @return The name of the client RuleCell class to use.
     */
    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return CLIENT_CELL_CLASS_NAME;
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
    public MeasurementUnits getUnits() {
        return units;
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
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new RulerCellServerState(rulerType, units);
        }
        return super.getServerState(state);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        if (state instanceof RulerCellState) {
            RulerCellState rulerState = (RulerCellState) state;
            rulerType = rulerState.getRulerType();
            units = rulerState.getUnits();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new RulerCellClientState(rulerType, units);
        }
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);
        ChannelComponentMO channel = this.getComponent(ChannelComponentMO.class);
        if (live) {
            RulerCellMessageReceiver messageReceiver = new RulerCellMessageReceiver(this);
            channel.addMessageReceiver(RulerCellTypeChangeMessage.class, messageReceiver);
            channel.addMessageReceiver(RulerCellUnitChangeMessage.class, messageReceiver);
        }
        else {
            channel.removeMessageReceiver(RulerCellTypeChangeMessage.class);
            channel.removeMessageReceiver(RulerCellUnitChangeMessage.class);
        }
    }

    /**
     * Private internal class for handling listening to RulerCell messages being sent to the server.
     */
    private static class RulerCellMessageReceiver extends AbstractComponentMessageReceiver {

        /**
         * Create a new instance of RulerCellMessageReceiver.
         *
         * @param cellMO The RulerCell managed object for which to receive messages.
         */
        public RulerCellMessageReceiver(RulerCellMO cellMO) {
            super(cellMO);
        }

        /**
         * Handle messages sent to the RulerCell managed object.
         *
         * @param sender The source WonderlandClient from which messages originated.
         * @param clientID The client id of the Wonderland client on which the RulerCell resides.
         * @param message The CellMessage containing the message from the cell.
         */
        @Override
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            CellMO cell = getCell();
            if (cell instanceof RulerCellMO) {
                RulerCellMO rulerCell = (RulerCellMO) cell;
                if (message instanceof RulerCellUnitChangeMessage) {
                    rulerCell.setUnits(((RulerCellUnitChangeMessage) message).getUnits());
                    cell.sendCellMessage(clientID, message);
                }
                else if (message instanceof RulerCellTypeChangeMessage) {
                    rulerCell.setRulerType(((RulerCellTypeChangeMessage) message).getRulerType());
                    cell.sendCellMessage(clientID, message);
                }
            }
        }

    }
}

