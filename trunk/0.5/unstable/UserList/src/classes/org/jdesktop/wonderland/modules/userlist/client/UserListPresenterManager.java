/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.userlist.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.*;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.modules.userlist.client.presenters.UserListPresenter;
import org.jdesktop.wonderland.modules.userlist.client.views.WonderlandUserList;

/**
 *
 * @author JagWire
 */
public enum UserListPresenterManager implements HUDEventListener {
    INSTANCE;
    
    private Map<String, UserListPresenter> presenters = new HashMap<String, UserListPresenter>();
    
    private UserListPresenter defaultPresenter;
    private HUDComponent hudComponent;
    private String activePresenter = "default";
    private static final String DEFAULT = "default";
    private ImageIcon userListIcon = null;
    private JMenuItem userListMenuItem = null;
    private boolean initialized = false;
    private List<DefaultUserListListener> listeners = new ArrayList<DefaultUserListListener>();
    
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/userlist/client/resources/Bundle");
    public static final String TABBED_PANEL_PROP = 
            "AudioManagerClient.Tabbed.Panel";
    private static final Logger logger = Logger.getLogger(UserListPresenterManager.class.getName());
    
    
    public void intialize() {
        if (!initialized) {
            logger.warning("INITIALIZING PRESENTER MANAGER");
                    
            initializeMenuItem();
            
            userListIcon = new ImageIcon(getClass().getResource(
                    "/org/jdesktop/wonderland/modules/userlist/client/"
                    + "resources/GenericUsers32x32.png"));


            HUD main = HUDManagerFactory.getHUDManager().getHUD("main");
            WonderlandUserList view =
                    new WonderlandUserList(new UserListCellRenderer());

            if (Boolean.parseBoolean(System.getProperty(TABBED_PANEL_PROP))) {
                HUDTabbedPanel tabbedPanel = HUDTabbedPanel.getInstance();
                hudComponent = main.createComponent(tabbedPanel);
                tabbedPanel.addTab("Users", view);
            } else {
                hudComponent = main.createComponent(view);
            }

            hudComponent.setDecoratable(true);
            hudComponent.setPreferredLocation(CompassLayout.Layout.NORTHWEST);
            hudComponent.setIcon(userListIcon);
            hudComponent.setName("Users (0)");
            hudComponent.addEventListener(this);

            main.addComponent(hudComponent);
            defaultPresenter = new UserListPresenter(view,
                    hudComponent);

            presenters.put("default", defaultPresenter);

            initialized = true;
            notifyListeners();
        }
    }
    
    public void HUDObjectChanged(HUDEvent event) {
        HUDEvent.HUDEventType type = event.getEventType();
        if(type == HUDEvent.HUDEventType.CLOSED
         ||type == HUDEvent.HUDEventType.MINIMIZED
         ||type == HUDEvent.HUDEventType.DISAPPEARED) {
            userListMenuItem.setSelected(false);
        } else {
            userListMenuItem.setSelected(true);
        }            
    }
    
    public UserListPresenter getDefaultPresenter() {
        return presenters.get(DEFAULT);
    }
    
    public void addPresenter(String name, UserListPresenter presenter) {
        if(name == null || name.equals(DEFAULT)) {
            logger.warning("CANNOT OVERWRITE DEFAULT PRESENTER.");
            return;
        }
        
        synchronized(presenters) {
            presenters.put(name, presenter);                
        }        
    }
    
    public void removePresenter(String name) {
        if(name == null || name.equals(DEFAULT)) {
            logger.warning("CANNOT REMOVE DEFAULT PRESENTER!");
            return;
        }
        
        synchronized(presenters) {
            presenters.remove(name);
        }
    }
    
    public void setActivePresenter(String name) {
        synchronized(presenters) {
            if(name == null || !presenters.containsKey(name)) {
                logger.warning("CANNOT ACTIVATE KEY THAT DOES NOT EXIST!");
                return;
            }
            
            hideActivePresenter();
            activePresenter = name;
            
            final UserListPresenter ulp = presenters.get(name);
            
            SwingUtilities.invokeLater(new Runnable() { 
                public void run() {
                   ulp.setVisible(true);
                }
            });
            
            
        }
    }
    
    public void hideActivePresenter() {

        synchronized (presenters) {
            if (activePresenter == null || !presenters.containsKey(activePresenter)) {
                logger.warning("TRIED TO HIDE NONEXISTANT ACTIVE PRESENTER: "
                        + activePresenter);
                return;
            }

            final UserListPresenter ulp = presenters.get(activePresenter);

            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    ulp.setVisible(false);
                }
            });
        }
    }
    
    public void showActivePresenter() {
        synchronized(presenters) {
            if(activePresenter == null || !presenters.containsKey(activePresenter)) {
                logger.warning("ACTIVE PRESENTER DOES NOT EXIST: "+activePresenter);
                return;
            }
            
            UserListPresenter presenter = presenters.get(activePresenter);
            presenter.setVisible(true);
            presenter.updateUserList();
        }
    }
        
    private void initializeMenuItem() {
        userListMenuItem = new JCheckBoxMenuItem();
        userListMenuItem.setSelected(false);
        userListMenuItem.setText("Users");
        userListMenuItem.setEnabled(false);

        userListMenuItem.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent event) {
                handleMenuItemPress(event);
            }
        });
        userListMenuItem.setEnabled(true);
        
        JmeClientMain.getFrame().addToWindowMenu(userListMenuItem, 5);
                        
    }
    
    private void handleMenuItemPress(ActionEvent event) {
        if(userListMenuItem.isSelected()) {
            showActivePresenter();
        } else {
            hideActivePresenter();
        }
        

    }
    
    public void addUserListListener(DefaultUserListListener l) {
        synchronized(listeners) {
            listeners.add(l);
            
            //if our listener has registered after we've been initialized, 
            //go ahead and notify it of activation.
            if(initialized) {
                l.listActivated();
            }
        }
    }
    
    public void removeUserListListener(DefaultUserListListener l) {
        synchronized(listeners) {
            if(listeners.contains(l)) {
                listeners.remove(l);
            }
        }
    }
    
    public void notifyListeners() {
        synchronized(listeners) {
            for(DefaultUserListListener l: listeners) {
                l.listActivated();
            }
        }
    }
            
}
