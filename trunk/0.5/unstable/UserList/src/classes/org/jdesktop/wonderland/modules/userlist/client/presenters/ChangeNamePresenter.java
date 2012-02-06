/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.userlist.client.presenters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
import org.jdesktop.wonderland.modules.userlist.client.UserListManager;
import org.jdesktop.wonderland.modules.userlist.client.views.ChangeNameView;

/**
 *
 * @author Ryan
 */
public class ChangeNamePresenter {
    
    private ChangeNameView view;
    private UserListManager model;
    private UserListPresenter userListPresenter = null;
    private HUDComponent hudComponent;
    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/userlist/client/resources/Bundle");
    
    
    
    public ChangeNamePresenter(UserListPresenter userListPresenter, ChangeNameView view, HUDComponent c) {
        this.view = view;
        this.model = UserListManager.INSTANCE;
        this.userListPresenter = userListPresenter;
        this.hudComponent = c;
        addListeners();
    }
    
    public void setVisible(boolean visible) {
        hudComponent.setVisible(visible);
    }
    
    public void handleAliasTextActionPerformed() {
        //when the user enters text and presses enter, it's the same as pressing
        //the OK Button
        handleOKButtonPressed();
    }
    
    public void handleOKButtonPressed() {
        PresenceInfo[] infos = model.getAllUsers();

        String alias = view.getAliasFieldText();

        PresenceInfo localInfo = model.getLocalPresenceInfo();
        
        for(PresenceInfo info: infos ) {
            if(info.getUsernameAlias().equals(alias)
                    || info.getUserID().getUsername().equals(alias)) {
                if(!localInfo.equals(info)) {
                    view.setStatusLabel(BUNDLE.getString("Alias_Used"));
                    return;
                }
            }
        }
        
        view.setStatusLabel("");

        localInfo.setUsernameAlias(view.getAliasFieldText());
        model.requestChangeUsernameAlias(localInfo.getUsernameAlias());
        userListPresenter.changeUsernameAlias(localInfo);
        setVisible(false);
    }
    
    public void handleCancelButtonPressed() {
        setVisible(false);
    }
    
    
    private void addListeners() {
        view.addAliasTextFormActionListener(new ActionListener() {                       
            public void actionPerformed(ActionEvent event) {
                handleAliasTextActionPerformed();
            }
        });
        
        view.addOKButtonActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                handleOKButtonPressed();
            }
        });
        
        view.addCancelButtonActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                handleCancelButtonPressed();
            }
        });
        
    }
}
