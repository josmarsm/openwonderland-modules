/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.userlist.client.views;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import org.jdesktop.wonderland.client.hud.HUDComponent;

/**
 *
 * @author Ryan
 */
public interface ChangeNameView {
    
    public void setStatusLabel(String text);
    
    public String getAliasFieldText();
    
    public void addOKButtonActionListener(ActionListener listener);
    
    public void addCancelButtonActionListener(ActionListener listener);
    
    public void addAliasTextFormActionListener(ActionListener listener); 
    
}
