
package org.jdesktop.wonderland.modules.userlist.client.presenters;


import com.jme.math.Vector3f;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.hud.*;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.client.softphone.SoftphoneListener;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.audiomanager.client.AudioManagerClient;
import org.jdesktop.wonderland.modules.audiomanager.client.AudioManagerClientPlugin;
import org.jdesktop.wonderland.modules.audiomanager.client.voicechat.AddHUDPanel;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
import org.jdesktop.wonderland.modules.userlist.client.PresenceControls;
import org.jdesktop.wonderland.modules.userlist.client.UserListManager;
import org.jdesktop.wonderland.modules.userlist.client.UserListManager.ModelChangedListener;
import org.jdesktop.wonderland.modules.userlist.client.views.ChangeNamePanel;
import org.jdesktop.wonderland.modules.userlist.client.views.NamePropertiesPanel;
import org.jdesktop.wonderland.modules.userlist.client.views.UserListView;

/**
 * This acts as a Presenter to the UserListView in the Model, View, Presenter
 * architecture pattern.
 * 
 * To add button controls to the user list, implement a private handle*ButtonPressed(ActionEvent)
 * method and call it from an anonymous ActionListener passed to the view, all inside the
 * addListeners() method.
 * 
 * 
 * @author JagWire
 */
public class UserListPresenter implements SoftphoneListener, ModelChangedListener  {
  
    
    protected UserListView view;
    private PresenceControls presenceControls;
    private PresenceInfo presenceInfo;
    private UserListManager model;
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/userlist/client/resources/Bundle");
    
    
    private static final Logger logger = Logger.getLogger(UserListPresenter.class.getName());
    private HUDComponent userListComponent = null;
    
    public UserListPresenter(UserListView view, HUDComponent c) {
        this.view = view;
        this.userListComponent  = c;
        model = UserListManager.INSTANCE;
        
        model.setVolumeConverter(view.getVolumeSliderMaximum());
        model.addModelChangedListener(this);
        
        presenceControls = model.getPresenceControls();
        presenceInfo = model.getLocalPresenceInfo();
        addListeners();
        
        SoftphoneControlImpl.getInstance().addSoftphoneListener(this);
        
        
        
    }

    public void setVisible(boolean visible) {
        
        userListComponent.setVisible(visible);
    }
       
    public void changeUsernameAlias(PresenceInfo info) {
        model.sendChangeUsernameAliasMessage(info);
    }
    
    
    /**
     * This method involves the use of the ChangeName MVP. The model is the
     * UserListManager, the view is ChangeNamePanel, and presenter is 
     * ChangeNamePresenter. The whole process is started when a user clicks
     * the edit button on the user list.
     * 
     * @param e the ActionEvent 
     */
    private void handleEditButtonPressed(ActionEvent e) {        

        SwingUtilities.invokeLater(new Runnable() { 
            public void run() {
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                ChangeNamePanel changeNameView =
                    new ChangeNamePanel(presenceInfo.getUserID().getUsername());

            
            
                final HUDComponent comp = mainHUD.createComponent(changeNameView);
                    
                ChangeNamePresenter changeNamePresenter = 
                            new ChangeNamePresenter(getLocalPresenter(),
                                                    changeNameView,
                                                    comp);                        
                comp.setPreferredLocation(CompassLayout.Layout.NORTH);
                comp.setName(BUNDLE.getString("Change_Alias"));
                comp.setIcon(new ImageIcon(getClass().getResource(
                        "/org/jdesktop/wonderland/modules/userlist/client/" +
                        "resources/UserListEdit32x32.png")));
                mainHUD.addComponent(comp);                
                changeNamePresenter.setVisible(true);
            }
        });
            
    }
    
    private UserListPresenter getLocalPresenter() {
        return this;
    }
    private void handlePropertiesButtonPressed(ActionEvent e) {
        
        SwingUtilities.invokeLater(new Runnable() { 
        
            public void run() {
                HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                NamePropertiesPanel namePropertiesView =
                        new NamePropertiesPanel();

                HUDComponent nphc = mainHUD.createComponent(namePropertiesView);

                NamePropertiesPresenter presenter = new NamePropertiesPresenter(namePropertiesView, nphc);

                nphc.setPreferredLocation(CompassLayout.Layout.NORTH);
                nphc.setName(BUNDLE.getString("User_Properties"));

                nphc.setIcon(
                        new ImageIcon(getClass().getResource(
                        "/org/jdesktop/wonderland/modules/userlist/client/"
                        + "resources/UserListProperties32x32.png")));

                mainHUD.addComponent(nphc);
                presenter.setVisible(true);
            }
        });
            


    }
    
    private void handleListSelectionChanged(ListSelectionEvent e) {
                Object[] selectedValues = view.getSelectedEntries();

        if (selectedValues.length == 0) {
            view.updateWidgetsForNoSelectedValues();
        } else if (selectedValues.length == 1) {
            String username = NameTagNode.getUsername((String) view.getSelectedEntries()[0]);
            PresenceInfo info = model.getAliasInfo(username);
            
            
            float v1 = presenceControls.getVolume(info);
            int volume = model.getVolumeConverter().getVolume(v1);
            
            view.updateWidgetsForOneSelectedValue(username,
                                                  model.isMe(info),
                                                  volume);
        } else {
            // multiple users
            view.updateWidgetsForMultipleSelectedValues(selectedValues.length);
        }
    }
    
    private void handleTextChatButtonPressed(ActionEvent e) {

        // Fetch the currently selected value in the user list. There should only
        // be one. Start a chat with that person, if one does not already exist
        String selectedUser = (String) view.getSelectedEntry();
        if (selectedUser == null) {
            logger.warning("No user selected on chat window");
            return;
        }

        logger.warning("Selected user is " + selectedUser);
        String userName = NameTagNode.getUsername(selectedUser);
        WonderlandIdentity id = model.getIDForAlias(userName);

        if (id == null) {
            logger.warning("No ID found for user " + selectedUser);
            return;
        }

        model.getPresenceControls().startTextChat(id);
    }
    
    private void handleVoiceChatButtonPressed(ActionEvent e) {
        ArrayList<PresenceInfo> usersToInvite = new ArrayList();

        for (Object selectedValue : view.getSelectedEntries()) {
            String username = NameTagNode.getUsername((String) selectedValue);

            PresenceInfo info = model.getAliasInfo(username);

            if (info == null) {
                logger.warning("no PresenceInfo for " + username);
                continue;
            }

            if (info.equals(presenceInfo)) {
                //I'm the caller and will be added automatically
                continue;
            }

            usersToInvite.add(info);
        }

        presenceControls.startVoiceChat(usersToInvite,
                                        view.getUserListHUDComponent());
    }
    
    private void handleMuteButtonPressed(ActionEvent e) {
        AudioManagerClientPlugin.getClient().toggleMute();
    }
    
    private void handlePhoneButtonPressed(ActionEvent e) {
        AudioManagerClient client = AudioManagerClientPlugin.getClient();
        WonderlandSession session = model.getSession();
        
        PresenceInfo mine = (PresenceInfo)model.getLocalPresenceInfo().clone();
        PresenceInfo caller = (PresenceInfo)model.getLocalPresenceInfo().clone();
        
        AddHUDPanel addHUDPanel = new AddHUDPanel(client,
                                                  session,
                                                  mine,
                                                  caller,
                                                  AddHUDPanel.Mode.INITIATE);

        addHUDPanel.setPhoneType();

        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        HUDComponent addHUDComponent = mainHUD.createComponent(addHUDPanel);
        addHUDComponent.setName(BUNDLE.getString("Call"));
        addHUDComponent.setIcon(new ImageIcon(getClass().getResource(
                "/org/jdesktop/wonderland/modules/audiomanager/client/"
                + "resources/UserListChatVoice32x32.png")));
        addHUDComponent.setPreferredLocation(CompassLayout.Layout.CENTER);

        addHUDPanel.setHUDComponent(addHUDComponent);

        mainHUD.addComponent(addHUDComponent);
        addHUDComponent.addEventListener(new HUDEventListener() {

            public void HUDObjectChanged(HUDEvent e) {
                if (e.getEventType().equals(HUDEvent.HUDEventType.DISAPPEARED)) {
                }
            }
        });

        PropertyChangeListener plistener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent pe) {
            }
        };

        addHUDPanel.addPropertyChangeListener(plistener);
        addHUDComponent.setVisible(true);

    }
    
    private void handleGoToButtonPressed(ActionEvent e) {
            Object[] selectedValues = view.getSelectedEntries();

    if (selectedValues.length == 1) {
        String username =
                NameTagNode.getUsername((String) selectedValues[0]);

        // map the user to a presence info
        PresenceInfo info = model.getAliasInfo(username);
        if (info == null) {
            logger.warning("no PresenceInfo for " + username);
            return;
        }

        // get the position of the other user based on their cellID
        Vector3f position = model.getCellPositionForCellID(info.getCellID());
        if (position == null) {
            logger.warning("unable to find location of " + info.getCellID());
        }

        // get the current look direction of the avatar
        ViewCell viewCell = ViewManager.getViewManager().getPrimaryViewCell();
        CellTransform viewTransform = viewCell.getWorldTransform();

        // go to the new location
        try {
            ClientContextJME.getClientMain().gotoLocation(null, position,
                    viewTransform.getRotation(null));
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error going to location", ioe);
        }
    }
    }
    
    private void handlePanelToggleButtonPressed(ActionEvent e) {
        view.toggleControlPanel();
    }
    
    private void handleVolumeSliderMoved(ChangeEvent e) {

        int v = view.getVolumeSliderValue();

        float volume = model.getVolumeConverter().getVolume(v);

        Object[] selectedValues = view.getSelectedEntries();

        if (selectedValues.length > 0) {
            for (int i = 0; i < selectedValues.length; i++) {
                String username =
                        NameTagNode.getUsername((String) selectedValues[i]);

                PresenceInfo info = model.getAliasInfo(username);

                if (info == null) {
                    logger.warning("no PresenceInfo for " + username);
                    continue;
                }

                presenceControls.setVolume(info, volume);
            }
        }
    }
            
    //<editor-fold defaultstate="collapsed" desc="Add Listeners Method">
    /**
     * Add control listeners to the view for this presenter. Protected so that
     * more buttons can be added if the UserList gets extended.
     */
    protected void addListeners() {
        view.addEditButtonActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                handleEditButtonPressed(ae);
            }
            
        });
        
        view.addPropertiesButtonActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                handlePropertiesButtonPressed(ae);
            }
        });
        
        view.addListSelectionChangedListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent lse) {
                handleListSelectionChanged(lse);
            }
        });
        
        view.addTextChatButtonActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent ae) {
                handleTextChatButtonPressed(ae);
            }
        });
        
        view.addVoiceChatButtonActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent ae) {
                handleVoiceChatButtonPressed(ae);
            }
        });
        
        view.addMuteButtonActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent ae) {
                handleMuteButtonPressed(ae);
            }
        });
        
        view.addPhoneButtonActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent ae) {
               handlePhoneButtonPressed(ae);
            }
        });
        
        view.addGoToUserButtonActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                handleGoToButtonPressed(ae);
            }
        });
        
        view.addPanelToggleButtonActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                handlePanelToggleButtonPressed(ae);
            }
        });
        
        view.addVolumeSliderChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent ce) {

                handleVolumeSliderMoved(ce);
            }
        });
        
        //TODO: add listeners for pull-to-me, secure client, adjust audio, and
        //get screen snapshot
    }
    
    //</editor-fold>

    //<editor-fold desc="softphone event handlers" defaultstate="collapsed">
    public void softphoneVisible(boolean isVisible) {

    }

    public void softphoneMuted(boolean muted) {
        view.updateMuteButton(muted);
    }

    public void softphoneConnected(boolean connected) {

    }

    public void softphoneExited() {

    }

    public void softphoneProblem(String problem) {

    }

    public void softphoneTestUDPPort(int port, int duration) {

    }

    public void microphoneGainTooHigh() {

    }
    //</editor-fold>

    public void userMovedInRange(PresenceInfo info) {
        updateUserList();
    }

    public void userMovedOutOfRange(PresenceInfo info) {
        updateUserList();
    }
    
    public void aliasChanged(PresenceInfo info) {
        updateUserList();
    }
 
    public void updateUserList() {
        SwingUtilities.invokeLater(new Runnable() { 
            public void run() {
                doUpdateList();
            }
        });
    }
    
    private void doUpdateList() {
        //<editor-fold defaultstate="collapsed" desc="legacy list code">
//        PresenceInfo[] presenceInfoList = model.getAllUsers();
//
//
//        for (PresenceInfo info : presenceInfoList) {
//            if (info.getCallID() == null) {
//                return;
//            }
//
//
//            String username = info.getUserID().getUsername();
//            String displayName = NameTagNode.getDisplayName(
//                    info.getUsernameAlias(), info.isSpeaking(), info.isMuted());
//
//            boolean inRange = model.isInRange(info);
//            //displayName = (inRange ? "\u25B8 " : "") + displayName;
//
//            int desiredPosition;
//
//            //if this a user that just logged in...
//            if (model.isNewUser(username)) {
//                //if it is me that just logged in...
//                if (model.isMe(info)) {
//                    //put me at the top of the list.
//                    desiredPosition = 0;
//                } else {
//                    //if it is someone else that just logged in
//                    if (inRange) {
//                        //...and they are in range....
//                        //...put them in the bottom of the "in range" list.
//                        desiredPosition = model.getLastPositionOfInRangeList();
//                    } else {
//                        //...and they are not in range.
//                        //...put them in at the bottom of the overall list.
//                        desiredPosition = view.getNumberOfElements();
//                    }
//                }
//
//                if (inRange) {
//                    model.incrementLastPositionOfInRangeList();
//                }
//
//                model.addUserToMap(username, displayName);
//                view.addEntryToView(displayName, desiredPosition);
//
//            } //if this is an existing user in the model
//            else {
//                //get current name
//                String oldName = model.getDisplayNameForUser(username);
//                
//                int currentIndex = view.getIndexForName(oldName);
//                boolean wasInRange = currentIndex < model.getLastPositionOfInRangeList();
//
//
//                if (inRange != wasInRange) {
//                    boolean reselect = view.isIndexCurrentlySelected(currentIndex);
//                    view.removeEntryAtIndexFromView(currentIndex);
//
//                    //if they were in range, and moved out of range
//                    if (wasInRange) {
//                        logger.warning("USER MOVED OUT OF RANGE!");
////                        model.incrementLastPositionOfInRangeList();
//                        
//                        //decrement the in-range index, since there is an open spot.  
//                        model.decrementLastPositionOfInRangeList();
//                          
//                        desiredPosition = view.getNumberOfElements();
//                    } else { //otherwise they were out of range and moved in range.
//                        desiredPosition = model.getLastPositionOfInRangeList();
//                    }
//                    //add the entry to the actual display
//                    logger.warning("ADDING "+displayName+" to position: "+desiredPosition+" of "+view.getNumberOfElements());
//                    view.addEntryToView(displayName, desiredPosition);
//
//                    if (reselect) {
//                        view.setSelectedIndex(desiredPosition);
//                    }
//                } else {
//                    desiredPosition = currentIndex;
//                }
//
//                if (!displayName.equals(oldName)) {
//                    model.replace(username, displayName);
//                }
//            }
//        }
//
//
//        // search for removed users
//        Iterator<String> iter = model.getKeySetIterator();
//        while (iter.hasNext()) {
//            // for each user previously displayed...
//            String username = (String) iter.next();
//            boolean found = false;
//
//            for (PresenceInfo info : presenceInfoList) {
//                if (username.equals(info.getUserID().getUsername())) {
//                    found = true;
//                    break;
//                }
//            }
//
//            if (!found) {
//                // user is no longer present, remove them from the user list
//                int index = view.getIndexForName(username);
////                int index = userListModel.indexOf(usernameMap.get(username));
//                boolean wasInRange = index < model.getLastPositionOfInRangeList();
//
//                view.removeEntryAtIndexFromView(index);
////                userListModel.removeElement(usernameMap.get(username));
//                if (wasInRange) {
//                    model.decrementLastPositionOfInRangeList();
//                }
//
//            }
//        }
//</editor-fold>
        
        
        //remove all entries
        view.removeAllEntries();
        
        //add my name to the top
        String myName = model.getMyDisplayName();
        PresenceInfo me = model.getLocalPresenceInfo();
        NameTagNode.getDisplayName(myName, me.isSpeaking(), me.isMuted());
        view.addEntryToView(myName, 0);
        
//        synchronized (model.getUsersInRange()) {
            //add all users in range
            for (PresenceInfo info : model.getUsersInRange()) {
                //we've already added me at the top, skip over if found here.
                if (model.isMe(info)) {
                    continue;
                }

                String display = getDisplayName(info);
                view.addEntryToView(display);
            }
//        }
        
//        synchronized (model.getUsersNotInRange()) {
            //add all users not in range
            for (PresenceInfo info : model.getUsersNotInRange()) {

                //we've already added me at the top, skip over if found here.
                if (model.isMe(info)) {
                    continue;
                }

                String display = getDisplayName(info);
                view.addEntryToView(display);
            }
//        }
        //update the title of the view
        
        userListComponent.setName(BUNDLE.getString("Users") + " (" + view.getNumberOfElements() + ") ");
    }  
    
    public String getDisplayName(PresenceInfo info) {
        return NameTagNode.getDisplayName(info.getUsernameAlias(),
                           info.isSpeaking(),
                           info.isMuted());
    }
    
}
