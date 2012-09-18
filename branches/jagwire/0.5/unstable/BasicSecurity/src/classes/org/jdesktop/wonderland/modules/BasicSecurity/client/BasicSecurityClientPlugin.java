/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.BasicSecurity.client;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellCache.CellCacheListener;
import org.jdesktop.wonderland.client.cell.EnvironmentCell;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.security.client.SecurityComponentFactory;
import org.jdesktop.wonderland.modules.security.client.SecurityQueryComponent;
import org.jdesktop.wonderland.modules.securitygroups.common.GroupDTO;
import org.jdesktop.wonderland.modules.securitygroups.common.GroupUtils;

/**
 *
 * @author JagWire
 */
@Plugin
public class BasicSecurityClientPlugin implements ClientPlugin,
                                                  SessionLifecycleListener,
                                                  CellCacheListener{

    private ServerSessionManager sessionManager;
    private String username;
    private Set<String> groups;
    private static Logger logger = Logger.getLogger(BasicSecurityClientPlugin.class.getName());
    
    public void getGroups() {
        Set<GroupDTO> userGroups = new LinkedHashSet<GroupDTO>();
        try {

            userGroups.addAll(GroupUtils.getGroupsForUser(sessionManager.getServerURL(),
                    username,
                    false,
                    sessionManager.getCredentialManager()));
            for (GroupDTO d : userGroups) {
                groups.add(d.getId());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void initialize(ServerSessionManager loginInfo) {
//        throw new UnsupportedOperationException("Not supported yet.");
        sessionManager = loginInfo;
        username = loginInfo.getUsername();
        groups = new LinkedHashSet<String>();
        
        getGroups();
        sessionManager.addLifecycleListener(this);
    }

    public void cleanup() {
//        throw new UnsupportedOperationException("Not supported yet.");
        sessionManager.removeLifecycleListener(this);
    }

    public void sessionCreated(WonderlandSession session) {
//        throw new UnsupportedOperationException("Not supported yet.");
        CellCache cache = ClientContext.getCellCache(session);
        cache.addCellCacheListener(this);
    }

    public void primarySession(WonderlandSession session) {
        //do nothing
    }

    public void cellLoaded(CellID cellID, Cell cell) {
        
        if(cell instanceof AvatarCell ||
           cell instanceof EnvironmentCell) {
            //ignore avatar and environment cells, they shouldn't be secured, yet.
            return;
            
        }
        
        ContextMenuComponent cmc = new ContextMenuComponent(cell);
        if(!groups.contains("admin")) {
            cmc.setShowStandardMenuItems(false);
        } else {
            if(cell.getComponent(SecurityQueryComponent.class) == null) {
                final Cell relevantCell = cell;
//                System.out.println("Processing cellID: "+ cell.getCellID());
                logger.warning("Processing cellID: "+cell.getCellID());
                new Thread(new Runnable() {
                    public void run() {

                        SecurityComponentFactory f = new SecurityComponentFactory();
                        CellComponentServerState s = f.getDefaultCellComponentServerState();
                        CellServerComponentMessage csm =
                            CellServerComponentMessage.newAddMessage(relevantCell.getCellID(), s);
                        ResponseMessage response = relevantCell.sendCellMessageAndWait(csm);
                        if(response instanceof ErrorMessage) {
                            logger.warning("Unable to add security component!");
//                            System.out.println("Unable to add security component.");
                           
                        }
                    }
                }).start();
            }
        }
        cell.addComponent(cmc, ContextMenuComponent.class);

        
        
        
    }

    public void cellLoadFailed(CellID cellID, String className, CellID parentCellID, Throwable cause) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void cellUnloaded(CellID cellID, Cell cell) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
