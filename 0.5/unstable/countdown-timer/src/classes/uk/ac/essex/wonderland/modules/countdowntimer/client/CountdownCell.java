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
package uk.ac.essex.wonderland.modules.countdowntimer.client;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import uk.ac.essex.wonderland.modules.countdowntimer.common.CountdownCellClientState;
import uk.ac.essex.wonderland.modules.countdowntimer.common.CountdownMessage;
import uk.ac.essex.wonderland.modules.countdowntimer.common.Time;


public class CountdownCell extends Cell implements ComponentMessageReceiver {
    private CountdownRenderer renderer = null;
    private Time time;
    
    @UsesCellComponent
    private ChannelComponent channel;
    
    public CountdownCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
    
    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        switch(rendererType) {
            case RENDERER_JME:
                renderer = new CountdownRenderer(this);
                break;
                
            default:
                throw new IllegalStateException("Cell does not support " +
                                                rendererType);
        }
        
        return renderer;
    }
    
    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        switch (status) {
            case ACTIVE: {
                if (increasing) {
                    channel.addMessageReceiver(CountdownMessage.class, this);
                } else {
                    channel.removeMessageReceiver(CountdownMessage.class);
                }
                break;
            }
        }
    }
    
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        CountdownCellClientState cccState = (CountdownCellClientState)state;
        time = cccState.getTime();
    }

    public void messageReceived(CellMessage message) {
        CountdownMessage cMessage = (CountdownMessage) message;
        time = cMessage.getTime();
        renderer.updateTime(time);
    }

    Time getTime() {
        return time;
    }
}
