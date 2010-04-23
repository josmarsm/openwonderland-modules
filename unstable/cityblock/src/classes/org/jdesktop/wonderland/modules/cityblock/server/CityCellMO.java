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
package org.jdesktop.wonderland.modules.cityblock.server;

import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.util.ScalableList;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.modules.cityblock.common.CityCellServerState;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellMOFactory;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * A sample cell
 * @author jkaplan
 */
@ExperimentalAPI
public class CityCellMO extends CellMO {
    /** block size */
    private static final float STREET_SIZE = 91.4f;
    private static final float AVENUE_SIZE = 304.8f;

    /* server state */
    private CityCellServerState state;

    /** the list of cells to add */
    private final ManagedReference<List<AddBlockRecord>> recordsRef;

    /** Default constructor, used when cell is created via WFS */
    public CityCellMO() {
        List<AddBlockRecord> records = new ScalableList<AddBlockRecord>();
        recordsRef = AppContext.getDataManager().createReference(records);
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        if (live && !state.getCellsAdded()) {
            addCells();
        }
    }

    @Override 
    protected String getClientCellClassName(WonderlandClientID clientID, 
                                            ClientCapabilities capabilities)
    {
        return null;
    }

    @Override
    public void setServerState(CellServerState serverState) {
        super.setServerState(serverState);

        this.state = (CityCellServerState) serverState;

        if (isLive() && !state.getCellsAdded()) {
            addCells();
        }
    }

    @Override
    public CellServerState getServerState(CellServerState cellServerState) {
        if (cellServerState == null) {
            cellServerState = new CityCellServerState();
        }

        ((CityCellServerState) cellServerState).setCellsAdded(state.getCellsAdded());
        ((CityCellServerState) cellServerState).setStreetCount(state.getStreetCount());
        ((CityCellServerState) cellServerState).setAvenueCount(state.getAvenueCount());

        return super.getServerState(cellServerState);
    }

    protected void addCells() {
        List<AddBlockRecord> records = recordsRef.get();

        for (int i = 0; i < state.getAvenueCount(); i++) {
            for (int j = 0; j < state.getStreetCount(); j++) {
                records.add(new AddBlockRecord(j, i));
            }
        }

        // schedule a new task to start adding blocks
        AppContext.getTaskManager().scheduleTask(new AddBlockTask(state, recordsRef));

        state.setCellsAdded(true);
    }

    private static class AddBlockTask implements Task, Serializable {
        private final CityCellServerState state;
        private final ManagedReference<List<AddBlockRecord>> recordsRef;

        private static final String BLOCK_XML = "block-0-wlc.xml";
        private static final String[] BUILDING_XML = {
            "140Franklin-2-wlc.xml", "140Franklin-3-wlc.xml",
            "housing-5-wlc.xml", "housing-6-wlc.xml",
            "state-1-1-wlc.xml", "state-1-8-wlc.xml",
            "state-2-2-wlc.xml", "state-2-4-wlc.xml",
            "state-3-7-wlc.xml"
        };

        public AddBlockTask(CityCellServerState state,
                            ManagedReference<List<AddBlockRecord>> recordsRef)
        {
            this.state = state;
            this.recordsRef = recordsRef;
        }

        public void run() {
            // get the first record
            AddBlockRecord record = recordsRef.get().remove(0);

            // process it
            processRecord(record);

            // if there are still records left, schedule a new task to add
            // the next one
            if (!recordsRef.get().isEmpty()) {
                AppContext.getTaskManager().scheduleTask(this);
            }
        }

        public void processRecord(AddBlockRecord record) {
            float xpos = getX(record.getStreet(), record.getAvenue());
            float zpos = getZ(record.getStreet(), record.getAvenue());

            try {
                // create the block in the right position
                CellServerState blockState = getServerState(BLOCK_XML);
                PositionComponentServerState pcss = (PositionComponentServerState)
                        blockState.getComponentServerState(PositionComponentServerState.class);
                pcss.setTranslation(new Vector3f(xpos, 0f, zpos));
                CellMO blockCell = createCell(blockState, null);

                // create the buildings relative to the block
                for (String building : BUILDING_XML) {
                    CellServerState buildingState = getServerState(building);
                    createCell(buildingState, blockCell);
                }
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Error creating block", ioe);
            } catch (JAXBException je) {
                logger.log(Level.WARNING, "Error creating block", je);
            }
        }

        public float getX(int street, int avenue) {
            float startX = -1 * (state.getAvenueCount() / 2f) * AVENUE_SIZE;
            return (startX + (avenue * AVENUE_SIZE));
        }

        public float getZ(int street, int avenue) {
            float startZ = -1 * (state.getStreetCount() / 2f) * STREET_SIZE;
            return (startZ + (street * STREET_SIZE));
        }

        public CellServerState getServerState(String name)
            throws IOException, JAXBException
        {
            System.out.println("Loading " + name);
            InputStream is = CityCellMO.class.getResourceAsStream("resources/" + name);
            return CellServerState.decode(new InputStreamReader(is));
        }

        public CellMO createCell(CellServerState setup, CellMO parent)
            throws IOException
        {
            /*
             * Create the cell and pass it the setup information
             */
            String className = setup.getServerClassName();
            CellMO cellMO = CellMOFactory.loadCellMO(className);
            if (cellMO == null) {
                /* Log a warning and move onto the next cell */
                throw new IOException("Unable to load cell MO: " + className);
            }

            /* Call the cell's setup method */
            try {
                cellMO.setServerState(setup);
            } catch (ClassCastException cce) {
                throw new IOException("Error setting up new cell " +
                        cellMO.getName() + " of type " +
                        cellMO.getClass(), cce);
            }

            /*
             * Add the child to the cell hierarchy. If the cell has no parent,
             * then we insert it directly into the world
             */
            try {
                if (parent == null) {
                    WonderlandContext.getCellManager().insertCellInWorld(cellMO);
                } else {
                    parent.addChild(cellMO);
                }
            } catch (MultipleParentException excp) {
                logger.log(Level.WARNING, "Attempting to add a new cell with " +
                        "multiple parents: " + cellMO.getName());
                return null;
            }

            return cellMO;
        }
    }

    private static class AddBlockRecord implements Serializable {
        private int street;
        private int avenue;

        public AddBlockRecord(int street, int avenue) {
            this.street = street;
            this.avenue = avenue;
        }

        public int getStreet() {
            return street;
        }

        public int getAvenue() {
            return avenue;
        }
    }
}
