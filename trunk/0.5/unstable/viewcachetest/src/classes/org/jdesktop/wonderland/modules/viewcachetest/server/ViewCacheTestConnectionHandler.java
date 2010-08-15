/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.viewcachetest.server;

import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.viewcachetest.common.CreateMessage;
import org.jdesktop.wonderland.modules.viewcachetest.common.MoveMessage;
import org.jdesktop.wonderland.modules.viewcachetest.common.ViewCacheTestConnectionType;
import org.jdesktop.wonderland.server.cell.CellManagerMO;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.comms.annotation.ClientHandler;

/**
 *
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
@ClientHandler
public class ViewCacheTestConnectionHandler
        implements ClientConnectionHandler, ManagedObject, Serializable
{
    private static final Logger LOGGER =
            Logger.getLogger(ViewCacheTestConnectionHandler.class.getName());

    private static final CellTransform START = new CellTransform();
    private static final CellTransform FAR = new CellTransform();

    static {
        FAR.setTranslation(new Vector3f(2000, 0, 0));
    }

    private final List<CellID> movers = new LinkedList<CellID>();

    public ConnectionType getConnectionType() {
        return ViewCacheTestConnectionType.CONNECTION_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
    }

    public void clientConnected(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                Properties properties)
    {
    }

    public void messageReceived(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                Message message)
    {
        if (message instanceof CreateMessage) {
            handleAdd(((CreateMessage) message).getCount());
        } else if (message instanceof MoveMessage) {
            handleMove((MoveMessage) message);
        }
    }

    public void clientDisconnected(WonderlandClientSender sender, 
                                   WonderlandClientID clientID)
    {
    }

    private void handleAdd(int count) {
        int startIdx = movers.size();

        for (int i = 0; i < count; i++) {
            int idx = startIdx + i;
            float x = (idx % 10) * 3f;
            float z = (idx / 10) * 3f;

            CellTransform xform = new CellTransform();
            xform.setTranslation(new Vector3f(x, 0, z));

            MoverCellMO mover = new MoverCellMO(xform);

            try {
                CellManagerMO.getCellManager().insertCellInWorld(mover);
            } catch (MultipleParentException ex) {
                LOGGER.log(Level.WARNING, "Error adding cell", ex);
                continue;
            }

            movers.add(mover.getCellID());
        }
    }

    private void handleMove(MoveMessage message) {
        switch (message.getWhere()) {
            case START:
                handleMove(START);
                break;
            case FAR:
                handleMove(FAR);
                break;
        }
    }

    private void handleMove(CellTransform xform) {
        LOGGER.warning("Moving " + movers.size() + " cells to " + xform);

        // schedule moves
        for (int i = 0; i < movers.size(); i += 5) {
            int count = Math.min(movers.size() - i, 5);

            LOGGER.fine("Scheduling task for ids " + i + " to " + (i + count));

            List<CellID> ids = new ArrayList<CellID>(movers.subList(i, i + count));

            AppContext.getTaskManager().scheduleTask(
                    new MoverTask(ids, xform));
        }

        // schedule checks
        for (CellID check : movers) {
            AppContext.getTaskManager().scheduleTask(
                    new CheckLoadedTask(check, movers), 5000);
        }
    }

    private static class MoverTask implements Task, Serializable {
        private final List<CellID> cellIDs;
        private final CellTransform xform;

        public MoverTask(List<CellID> cellIDs, CellTransform xform) {
            this.cellIDs = cellIDs;
            this.xform = xform;
        }

        public void run() throws Exception {
            for (CellID cellID : cellIDs) {
                LOGGER.fine("Move " + cellID + " to " + xform);

                MoverCellMO cellMO = (MoverCellMO) CellManagerMO.getCell(cellID);
                cellMO.moveTo(xform);
            }
        }
    }

    private static class CheckLoadedTask implements Task, Serializable {
        private final List<CellID> allCells;
        private final CellID check;

        public CheckLoadedTask(CellID check, List<CellID> allCells) {
            this.check = check;
            this.allCells = allCells;
        }

        public void run() throws Exception {
            MoverCellMO cellMO = (MoverCellMO) CellManagerMO.getCell(check);
            MoverViewCellCacheMO cache = cellMO.getCellCache();

            Set<CellID> missing = new LinkedHashSet<CellID>();

            for (CellID id : allCells) {
                if (!cache.isLoaded(id)) {
                    missing.add(id);
                }
            }

            if (!missing.isEmpty()) {
                LOGGER.warning("Cell " + check + " missing " + missing);
            }
        }
    }
}
