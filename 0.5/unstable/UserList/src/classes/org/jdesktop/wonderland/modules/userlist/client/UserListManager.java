
package org.jdesktop.wonderland.modules.userlist.client;

import com.jme.math.Vector3f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.audiomanager.client.AudioManagerClientPlugin;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.ChangeUsernameAliasMessage;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

/**
 * This class will act as the Model in the MVP architecture.
 * 
 * @author JagWire
 */
public enum UserListManager implements PresenceManagerListener {
    INSTANCE;

    private WonderlandSession session;    
    private PresenceManager manager;
    private Cell cell;
    private PresenceInfo localPresenceInfo;
    private ModelChangedListener listener;
    private ArrayList<PresenceInfo> usersInRange = null;
    private ConcurrentHashMap<String, String> usernameMap;
    private int lastPositionOfInRangeList = 0;
    private VolumeConverter converter;
    
    private static final Logger logger = Logger.getLogger(UserListManager.class.getName());
    
    public void initialize() { 
                           
        logger.warning("inside initialize!");
        this.session = LoginManager.getPrimary().getPrimarySession();
        this.manager = PresenceManagerFactory.getPresenceManager(session);
        this.cell = ClientContextJME.getViewManager().getPrimaryViewCell();
        this.localPresenceInfo = manager.getPresenceInfo(cell.getCellID());
        
        usersInRange = new ArrayList<PresenceInfo>();
        usernameMap = new ConcurrentHashMap<String, String>();
        
        manager.addPresenceManagerListener(this);
        
    }
    
    public Cell getMyCell() {
        CellID cellID = localPresenceInfo.getCellID();
        
        return ClientContext.getCellCache(session).getCell(cellID);
    }
    
    public Cell getCellFromPresenceInfo(PresenceInfo info) {
        CellID cellID = info.getCellID();
        return ClientContext.getCellCache(session).getCell(cellID);
    }
    
    public void setVolumeConverter(int volumeSliderMaximum) {
        this.converter = new VolumeConverter(volumeSliderMaximum);
    }
    
    public VolumeConverter getVolumeConverter() {
        return converter;
    }
    
    public void setWonderlandSession(WonderlandSession session) {
        manager = PresenceManagerFactory.getPresenceManager(session);
        manager.addPresenceManagerListener(this);       
    }
    
    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public void cleanup() {
        if(manager != null) {
            manager.removePresenceManagerListener(this);            
        }
        manager = null;
    }
    
    public void addModelChangedListener(ModelChangedListener listener) {
        this.listener = listener;
    }
    
    public void incrementLastPositionOfInRangeList() {
        lastPositionOfInRangeList += 1;
    }
    
    public void decrementLastPositionOfInRangeList() {
        lastPositionOfInRangeList -= 1;
    }
    
    public int getLastPositionOfInRangeList() {
        return lastPositionOfInRangeList;
    }
    
    public synchronized void presenceInfoChanged(PresenceInfo pi, ChangeType ct) {
        
        
        switch(ct) {
            case UPDATED:
                logger.warning("INFO CHANGED: UPDATED: "+pi.getUsernameAlias());
                break;
            case USER_ADDED:
                logger.warning("INFO CHANGED: ADDED: "+pi.getUsernameAlias());
                break;
            case USER_IN_RANGE:
                logger.warning("INFO CHANGED: MOVED_IN_RANGE: "+pi.getUsernameAlias());
                usersInRange.add(pi);
                listener.userMovedInRange(pi);
                break;
            case USER_OUT_OF_RANGE:
                logger.warning("INFO CHANGED: MOVED_OUT_OF_RANGE: "+pi.getUsernameAlias());
                
                usersInRange.remove(pi);
                listener.userMovedOutOfRange(pi);
                break;
            case USER_REMOVED:
                logger.warning("INFO CHANGED: REMOVED: "+pi.getUsernameAlias());
                break;
        };
    
    }
    
    public boolean isMe(PresenceInfo info) {
        if( cell != null && manager != null) {
            return info.equals(manager.getPresenceInfo(cell.getCellID()));
        }
        
        return false;
    }
    
    public boolean isInRange(PresenceInfo info) {
        return isMe(info) || (usersInRange.contains(info));
    }
    
    public boolean isNewUser(String username) {
        if(!usernameMap.containsKey(username))
            return true;
        
        return false;
    }
    
    public PresenceInfo[] getAllUsers() {
        if(manager != null) {
            return manager.getAllUsers();
        } else {
            
        }
        
        return null;
    }
    
    public void addUserToMap(String username, String displayName) {
        if(usernameMap == null)
            return;
        
        usernameMap.put(username, displayName);
    }

    public String getDisplayNameForUser(String username) {
        if(!usernameMap.containsKey(username)) {
            return null;
        }
        
        return usernameMap.get(username);
    }
    
    public void replace(String username, String displayName) {
        usernameMap.replace(username, displayName);
    }
    
    public Iterator<String> getKeySetIterator()  {
        return usernameMap.keySet().iterator();
    }
    
    public void usernameAliasChanged(PresenceInfo info) {
        listener.aliasChanged(info);
    }
    
    public WonderlandIdentity getIDForAlias(String username) {
        PresenceInfo pi = manager.getAliasPresenceInfo(username);
        return pi.getUserID();
    }
    
    public PresenceControls getPresenceControls() {
        return new PresenceControls(session, manager, localPresenceInfo);
    }
    
    public void sendChangeUsernameAliasMessage(PresenceInfo info) {
        session.send(AudioManagerClientPlugin.getClient(),
                    new ChangeUsernameAliasMessage(info.getCellID(),
                    info));

    }
    
    public PresenceInfo getLocalPresenceInfo() {
        return localPresenceInfo;
    }
    
    public PresenceInfo getAliasInfo(String s) {
        return manager.getAliasPresenceInfo(s);
    }

    public Vector3f getCellPositionForCellID(CellID cellID) {
        return manager.getCellPosition(cellID);
    }
    
    public void requestChangeUsernameAlias(String alias) {
        manager.requestChangeUsernameAlias(alias);
    }
    
    public WonderlandSession getSession() {
        return session;
    }
    
    public static interface ModelChangedListener {
        public void userMovedInRange(PresenceInfo info);
        
        public void userMovedOutOfRange(PresenceInfo info);
        
        public void aliasChanged(PresenceInfo info);                    
    }
}
