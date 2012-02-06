
package org.jdesktop.wonderland.modules.userlist.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import org.jdesktop.wonderland.modules.userlist.client.presenters.UserListPresenter;
import org.jdesktop.wonderland.modules.userlist.client.views.WonderlandUserList;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar.ViewCellConfiguredListener;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.hud.*;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 *
 * @author JagWire
 */
@Plugin
public class UserListClientPlugin extends BaseClientPlugin 
implements ViewCellConfiguredListener, SessionLifecycleListener {
    
    private JMenuItem userListMenuItem = null;
    private UserListPresenter presenter = null;
    private WonderlandUserList view = null;
    private UserListManager manager = null;
    private HUDComponent component = null;
    private ImageIcon userListIcon = null;
    
    public static final String TABBED_PANEL_PROP = 
            "AudioManagerClient.Tabbed.Panel";
    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/userlist/client/resources/Bundle");
    
    public UserListClientPlugin() {
        userListMenuItem = new JCheckBoxMenuItem();
        userListMenuItem.setSelected(false);
        userListMenuItem.setText("Users");
        userListMenuItem.setEnabled(false);
        
        userListIcon = new ImageIcon(getClass().getResource(
                "/org/jdesktop/wonderland/modules/userlist/client/"+
                "resources/GenericUsers32x32.png"));
        
        
        
        view = new WonderlandUserList(new UserListCellRenderer());
        
        
        userListMenuItem.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent event) {
                handleMenuItemPress(event);
            }
        });
        userListMenuItem.setEnabled(false);
        
    }
    
        @Override
    public void initialize(ServerSessionManager loginManager) {
        loginManager.addLifecycleListener(this);
        super.initialize(loginManager);
    }
    

    private void handleMenuItemPress(ActionEvent event) {
        if(component == null) {
            initializeHUD();
        }
        

    }
    
    @Override
    public void activate() {
        JmeClientMain.getFrame().addToWindowMenu(userListMenuItem, 5);
    }
    
    private void initializeHUD() {
        HUD main = HUDManagerFactory.getHUDManager().getHUD("main");
        
        if(Boolean.parseBoolean(System.getProperty(TABBED_PANEL_PROP))) {
            HUDTabbedPanel tabbedPanel = HUDTabbedPanel.getInstance();
            component = main.createComponent(tabbedPanel);
        } else {
            component = main.createComponent(view);
        }

        component.setPreferredLocation(CompassLayout.Layout.NORTHWEST);
        component.setName("bleah"+BUNDLE.getString("Users")+ " (0)");
        component.setDecoratable(true);
        component.setIcon(userListIcon);
        component.addEventListener(new HUDEventListener() { 
            public void HUDObjectChanged(HUDEvent event) {
                HUDEventType type = event.getEventType();
                if(type == HUDEventType.CLOSED
                 ||type == HUDEventType.MINIMIZED
                 ||type == HUDEventType.DISAPPEARED) {
                    userListMenuItem.setSelected(false);
                } else {
                    userListMenuItem.setSelected(true);
                }
            }
        });
        
        main.addComponent(component);        
    }
    
    @Override
    public void deactivate() {
        JmeClientMain.getFrame().removeFromWindowMenu(userListMenuItem);
        if(component != null) {
            presenter.setVisible(false);
            HUD main = HUDManagerFactory.getHUDManager().getHUD("main");
            main.removeComponent(component);
            component = null;
        }
        
    }

     
    
    public void viewConfigured(LocalAvatar localAvatar) {
        WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
        
        UserListManager.INSTANCE.initialize();
        SwingUtilities.invokeLater(new Runnable() { 
        
            
            
            public void run() {
                if(component == null) {
                    initializeHUD();
                }                
                
                component.setMaximized();                
                if(presenter == null) {
                    presenter = new UserListPresenter(view, component);
                }
                userListMenuItem.setSelected(true);
                presenter.setVisible(true);
                presenter.updateUserList();
            }
        });
    }

    public void sessionCreated(WonderlandSession session) {
        
//        LocalAvatar avatar = ((CellClientSession) session).getLocalAvatar();
//        
//        avatar.addViewCellConfiguredListener(this);
//        if (avatar!= null) {
//            viewConfigured(avatar);
//        }
    }

    public void primarySession(WonderlandSession session) {
        LocalAvatar avatar = ((CellClientSession) session).getLocalAvatar();        

        avatar.addViewCellConfiguredListener(this);

    }
}
