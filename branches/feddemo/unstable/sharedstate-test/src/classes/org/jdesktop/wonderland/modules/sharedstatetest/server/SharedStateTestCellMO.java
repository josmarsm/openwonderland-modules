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
package org.jdesktop.wonderland.modules.sharedstatetest.server;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import java.io.Serializable;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedInteger;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapListenerSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedMapSrv;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.modules.sharedstatetest.common.SharedStateTestCellServerState;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;

/**
 * Test of the shared state component
 */
@ExperimentalAPI
public class SharedStateTestCellMO extends CellMO { 
    @UsesCellComponentMO(SharedStateComponentMO.class)
    private ManagedReference<SharedStateComponentMO> stateRef;
    	
    /** Default constructor, used when cell is created via WFS */
    public SharedStateTestCellMO() {
    }

    public SharedStateTestCellMO(Vector3f center, float size) {
        super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));
    }
    
    @Override protected String getClientCellClassName(WonderlandClientID clientID, 
                                                      ClientCapabilities capabilities)
    {
        return "org.jdesktop.wonderland.modules.sharedstatetest.client.SharedStateTestCell";
    }

    @Override
    public CellClientState getClientState(CellClientState state,
                                          WonderlandClientID clientID,
                                          ClientCapabilities capabilities)
    {
        if (state == null) {
            state = new CellClientState();
        }

        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new SharedStateTestCellServerState();
        }

        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        if (live) {
            SharedStateComponentMO ssc = stateRef.get();
            SharedMapSrv map = ssc.get("testmap");

            // periodically change values
            TestTask tt = new TestTask(map);
            AppContext.getTaskManager().schedulePeriodicTask(tt, 1000, 5000);

            // add a listener that rejects multiples of 3
            map.addSharedMapListener(new NoThreeListener());
        }
    }

    static class NoThreeListener implements SharedMapListenerSrv, Serializable {
        public boolean propertyChanged(SharedMapSrv map, 
                                       WonderlandClientID senderID,
                                       String key, SharedData prevVal,
                                       SharedData newVal)
        {
            if (newVal instanceof SharedInteger) {
                return ((SharedInteger) newVal).getValue() % 3 != 0;
            }

            return true;
        }
    }

    static class TestTask implements Task, Serializable {
        private ManagedReference<SharedMapSrv> mapRef;

        private int count;
        private String[] strs = { "hello", "world" };

        public TestTask(SharedMapSrv map) {
            this.mapRef = AppContext.getDataManager().createReference(map);
        }

        public void run() throws Exception {
            SharedMapSrv map = mapRef.get();

            map.put("a", SharedInteger.valueOf(count++));
            map.put("b", SharedString.valueOf(strs[count % strs.length]));
        }
    }
}
