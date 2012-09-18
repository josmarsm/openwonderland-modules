/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2012, University of Essex, UK, 2012, All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package uk.ac.essex.wonderland.modules.countdowntimer.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.PeriodicTaskHandle;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;
import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import uk.ac.essex.wonderland.modules.countdowntimer.common.CountdownCellClientState;
import uk.ac.essex.wonderland.modules.countdowntimer.common.CountdownCellServerState;
import uk.ac.essex.wonderland.modules.countdowntimer.common.CountdownFailureException;
import uk.ac.essex.wonderland.modules.countdowntimer.common.CountdownMessage;
import uk.ac.essex.wonderland.modules.countdowntimer.common.Time;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

public class CountdownCellMO extends CellMO {

    private Time time;
    private PeriodicTaskHandle timerTaskHandle = null;

    public CountdownCellMO() {
        super();
    }

    @Override
    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "uk.ac.essex.wonderland.modules.countdowntimer.client.CountdownCell";
    }

    @Override
    public CellClientState getClientState(CellClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            if (time != null) {
                state = new CountdownCellClientState(time);
            } else {
                state = new CountdownCellClientState();
            }
        }
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new CountdownCellServerState();
        }
        return super.getServerState(state);
    }

    private void createPeriodicTask() {
        TaskManager tm = AppContext.getTaskManager();
        Task timerTask = new TimerTask(this);
        timerTaskHandle = tm.schedulePeriodicTask(timerTask, 0, 1000); //run the task now and then every 1 second
    }

    public void startTimer(int minutes, int seconds) throws CountdownFailureException {
        logger.warning("starting timer");
        if (!isLive()) {
            throw new CountdownFailureException("cell " + getCellID() + " is not live");
        }
        if (timerTaskHandle != null) {
            logger.warning("Resetting task");
            timerTaskHandle.cancel();
            timerTaskHandle = null;
        }
        time = new Time(minutes, seconds);
        createPeriodicTask();
    }
    
    public void cancelTimer() {
        logger.warning("Cancelling timer");
        if (timerTaskHandle != null) {
            logger.warning("cancelling task");
            timerTaskHandle.cancel();
            timerTaskHandle = null;
        }
    }

    private void updateTime() {
        time.decrement();
        if (time.isValid()) {
            sendCellMessage(null, new CountdownMessage(getCellID(), time));
        } else {
            timerTaskHandle.cancel();
            timerTaskHandle = null;
            time = null;
        }
    }

    private static class TimerTask implements Task, Serializable {

        private ManagedReference<CountdownCellMO> cellRef = null;

        public TimerTask(CountdownCellMO cellMO) {
            cellRef = AppContext.getDataManager().createReference(cellMO);
        }

        public void run() throws Exception {
            cellRef.get().updateTime();
        }
    }
}
