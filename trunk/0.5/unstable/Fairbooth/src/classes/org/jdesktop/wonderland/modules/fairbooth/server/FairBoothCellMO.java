/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.fairbooth.server;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.Task;
import com.wonderbuilders.modules.colortheme.server.ColorThemeComponentMO;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.fairbooth.common.FairBoothClientState;
import org.jdesktop.wonderland.modules.fairbooth.common.FairBoothServerState;
import org.jdesktop.wonderland.modules.sharedstate.server.SharedStateComponentMO;
import org.jdesktop.wonderland.server.ServerPlugin;
import org.jdesktop.wonderland.server.cell.*;
import org.jdesktop.wonderland.server.cell.annotation.DependsOnCellComponentMO;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;


/**
 *
 * @author Nilang
 */
@DependsOnCellComponentMO({MovableComponentMO.class,ColorThemeComponentMO.class})
public class FairBoothCellMO extends CellMO {
    private static final Logger LOGGER =
            Logger.getLogger(FairBoothCellMO.class.getName());
    
    String boothName="Untitled Booth";
    private int colorTheme=0;
    private String infoText="Untitled";
    private int leftPanelFrames=1;
    private int rightPanelFrames=1;
   
    @UsesCellComponentMO(ColorThemeComponentMO.class)
    private ManagedReference<ColorThemeComponentMO> ctcMO;
    
    private Queue<String> SUBCOMPONENTS = new LinkedList<String>();
    
    public FairBoothCellMO() {
    }
        
    @Override
    public String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
         return "org.jdesktop.wonderland.modules.fairbooth.client.FairBoothCell";
    }

    @Override
    public CellClientState getClientState(CellClientState state, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (state == null) {
            state = new FairBoothClientState();
        }
        ((FairBoothClientState)state).setBoothName(boothName);
        ((FairBoothClientState)state).setColorTheme(colorTheme);
        ((FairBoothClientState)state).setInfoText(infoText);
        ((FairBoothClientState)state).setLeftPanelFrames(leftPanelFrames);
        ((FairBoothClientState)state).setRightPanelFrames(rightPanelFrames);
        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellServerState getServerState(CellServerState state) {
        if (state == null) {
            state = new FairBoothServerState();
        }
        // if we get the server state through the cell, we know the
        // children have been added
        ((FairBoothServerState)state).setBoothNameAdded(true);
        ((FairBoothServerState)state).setBoothName(boothName);
        ((FairBoothServerState)state).setColorTheme(colorTheme);
        ((FairBoothServerState)state).setInfoText(infoText);
        ((FairBoothServerState)state).setLeftPanelFrames(leftPanelFrames);
        ((FairBoothServerState)state).setRightPanelFrames(rightPanelFrames);
        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellServerState clientState) {
        super.setServerState(clientState);
        
        
        if(!((FairBoothServerState) clientState).getBoothNameAdded()) {
            SUBCOMPONENTS.add("ImageFrame-BoothName-right-wlc.xml");
            SUBCOMPONENTS.add("ImageFrame-BoothName-left-wlc.xml");
            //SUBCOMPONENTS.add("desk-wld/ImageFrame-InfoText-wlc.xml");
            if(((FairBoothServerState) clientState).getLeftPanelFrames()==1) {
                SUBCOMPONENTS.add("leftFrame10-wlc.xml");
            } else if(((FairBoothServerState) clientState).getLeftPanelFrames()==2) {
                SUBCOMPONENTS.add("leftFrame20-wlc.xml");
                SUBCOMPONENTS.add("leftFrame21-wlc.xml");
            } else if(((FairBoothServerState) clientState).getLeftPanelFrames()==4) {
                SUBCOMPONENTS.add("leftFrame40-wlc.xml");
                SUBCOMPONENTS.add("leftFrame41-wlc.xml");
                SUBCOMPONENTS.add("leftFrame42-wlc.xml");
                SUBCOMPONENTS.add("leftFrame43-wlc.xml");
            }
            if(((FairBoothServerState) clientState).getRightPanelFrames()==1) {
                SUBCOMPONENTS.add("rightFrame10-wlc.xml");
            } else if(((FairBoothServerState) clientState).getRightPanelFrames()==2) {
                SUBCOMPONENTS.add("rightFrame20-wlc.xml");
                SUBCOMPONENTS.add("rightFrame21-wlc.xml");
            } else if(((FairBoothServerState) clientState).getRightPanelFrames()==4) {
                SUBCOMPONENTS.add("rightFrame40-wlc.xml");
                SUBCOMPONENTS.add("rightFrame41-wlc.xml");
                SUBCOMPONENTS.add("rightFrame42-wlc.xml");
                SUBCOMPONENTS.add("rightFrame43-wlc.xml");
            }
            SUBCOMPONENTS.add("desk-wlc.xml");
            addChildren();
        }
        boothName = ((FairBoothServerState) clientState).getBoothName();
        colorTheme = ((FairBoothServerState) clientState).getColorTheme();
        infoText = ((FairBoothServerState) clientState).getInfoText();
        leftPanelFrames = ((FairBoothServerState) clientState).getLeftPanelFrames();
        rightPanelFrames = ((FairBoothServerState) clientState).getRightPanelFrames();
    }

    private void addChildren() {
        AppContext.getTaskManager().scheduleTask(new FairBoothCellMO.AddChildrenTask(this, SUBCOMPONENTS));
    }
    
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

    }
   
    static class AddChildrenTask implements Task, Serializable {
        private final ManagedReference<CellMO> parentRef;
        private final Queue<String> subcomponents;
        private final Queue<ManagedReference<CellMO>> toAdd = new LinkedList();
        
        public AddChildrenTask(CellMO parent, Queue<String> subcomponents) {
            parentRef = AppContext.getDataManager().createReference(parent);
            this.subcomponents = subcomponents;
        }

        public void run() throws RuntimeException, IOException, MultipleParentException {
            // if the parent is not yet live, reschedule ourselves
            if (!parentRef.get().isLive()) {
                AppContext.getTaskManager().scheduleTask(this, 500);
                return;
            }
            
            String name = subcomponents.poll();
            if (name != null) {
                // process the next child and reschedule the task
                toAdd.add(createCell(name));
                AppContext.getTaskManager().scheduleTask(this);
            } else {
                // no more components, add children to parent
                ManagedReference<CellMO> addRef = toAdd.poll();
                if (addRef != null) {
                    
                    parentRef.get().addChild(addRef.get());
                    AppContext.getTaskManager().scheduleTask(this);
                }
            }
        }
        
        private ManagedReference<CellMO> createCell(String name) throws RuntimeException, IOException {
            CellServerState state = getServerState(name);
            
            // fetch the server-side cell class name and create the cell
            String className = state.getServerClassName();
            CellMO codeMO = CellMOFactory.loadCellMO(className);
            
            // call the cell's setup method
            try {
                codeMO.setServerState(state);
                if(name.equals("desk-wlc.xml")) {
                    
                    CellServerState st = getServerState("desk-wld/ImageFrame-InfoText-wlc.xml");
                    CellMO infoTextCellMO = CellMOFactory.loadCellMO(st.getServerClassName());
                    infoTextCellMO.setServerState(st);
                    codeMO.addChild(infoTextCellMO);
                }
                return AppContext.getDataManager().createReference(codeMO);
            } catch (Exception cce) {
                if(cce instanceof RuntimeException) {
                    throw(RuntimeException) cce;
                } else {
                    throw new RuntimeException(cce);
                }
            } 
        }

        private CellServerState getServerState(String name)
                throws IOException
        {
            InputStream is = FairBoothCellMO.class.getResourceAsStream("resources/" + name);
            if (is == null) {
                throw new IOException("Unable to find state for " + name);
            }

            try {
                return CellServerState.decode(new InputStreamReader(is));
            } catch (JAXBException je) {
                throw new IOException(je);
            }
        }
    }
    
    // server plugin to make sure the environment cell has a shared state
    // component
    @Plugin
    public static class EnvironmentCellSSC implements ServerPlugin {
        public void initialize() {
            CellManagerMO.getCellManager().registerCellComponent(EnvironmentCellMO.class,
                                                                 SharedStateComponentMO.class);
        }
    }
    
}
