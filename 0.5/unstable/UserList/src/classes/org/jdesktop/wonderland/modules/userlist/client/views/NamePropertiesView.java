/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.userlist.client.views;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarNameEvent.EventType;
import org.jdesktop.wonderland.modules.userlist.client.views.NamePropertiesPanel.NameTagAttribute;

/**
 *
 * @author JagWire
 */
public interface NamePropertiesView {
 
    public void addOKButtonActionListener(ActionListener listener);
    
    public void addCancelButtonActionListener(ActionListener listener);
    
    public void addShowMyNameItemListener(ItemListener listener);
    
    public void addShowOthersNamesItemListener(ItemListener listener);
    
    public void addMyFontSizeChangeListener(ChangeListener listener);
    
    public void addOthersFontSizeChangeListener(ChangeListener listener);
    
    public void setVisible(boolean visible);
    
    public JCheckBox getShowMyNameCheckbox();
    
    public JCheckBox getShowOthersNamesCheckbox();
    
    public void updateMyNameTag(boolean showingName);
    
    public void updateOthersNameTag(boolean showingName);
    
    public void makeOrbsVisible(boolean visible);
    
    public NameTagAttribute getMyNameTagAttribute();
    
    public NameTagAttribute getMyOriginalNameTagAttribute();
    
    public NameTagAttribute getOthersNameTagAttribute();
    
    public NameTagAttribute getOthersOriginalNameTagAttribute();
    
    public void setMyOriginalNameTagAttribute(NameTagAttribute nta);
    
    public void setOthersOriginalNameTagAttributes(NameTagAttribute nta);
}
