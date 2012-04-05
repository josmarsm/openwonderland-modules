
package org.jdesktop.wonderland.modules.userlist.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import org.jdesktop.wonderland.modules.userlist.client.presenters.WonderlandUserListPresenter;
import org.jdesktop.wonderland.modules.userlist.client.views.WonderlandUserListView;
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
    

    public UserListClientPlugin() {
        
        
    }
    
        @Override
    public void initialize(ServerSessionManager loginManager) {
        loginManager.addLifecycleListener(this);
        super.initialize(loginManager);
    }
    


    
    @Override
    public void activate() {
        
    }
    
    
    @Override
    public void deactivate() {
//        JmeClientMain.getFrame().removeFromWindowMenu(userListMenuItem);
//        if(component != null) {
//            presenter.setVisible(false);
//            HUD main = HUDManagerFactory.getHUDManager().getHUD("main");
//            main.removeComponent(component);
//            component = null;
//        }
        
    }

     
    
    public void viewConfigured(LocalAvatar localAvatar) {
        WonderlandUserList.INSTANCE.initialize();
        UserListPresenterManager.INSTANCE.intialize();
        UserListPresenterManager.INSTANCE.showActivePresenter();
        
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
