/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.client;

import javax.swing.JPanel;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.client.view.DockableSheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.ResultListener;
import org.jdesktop.wonderland.modules.isocial.client.view.SheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.annotation.View;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardSheet;

/**
 *
 */
@View(value=StandardSheet.class, roles={Role.GUIDE, Role.ADMIN})
public class StandardGuideView implements SheetView, ResultListener,DockableSheetView {

    private ISocialManager manager;
    private Sheet sheet;

    private HUDComponent component;
    //private StandardSheetPanel panel;
    JPanel panel;
    
    private HUDComponent hudComponent;
    
    public void initialize(ISocialManager manager, Sheet sheet, Role role) {
        this.manager = manager;
        this.sheet = sheet;
        
        panel = new StandardGuideViewPanel(manager, sheet, this);
        manager.addResultListener(sheet.getId(), this);
        
    }

    public String getMenuName() {
        return ((StandardSheet) sheet.getDetails()).getName();
    }

    public boolean isAutoOpen() {
        return ((StandardSheet) sheet.getDetails()).isAutoOpen();
    }
    
    public boolean isDockable() {
        return ((StandardSheet) sheet.getDetails()).isDockable();
    }

    public HUDComponent open(HUD hud) {
        
        panel.validate();
        hudComponent = hud.createComponent(panel);
        ((StandardGuideViewPanel)panel).renderSheetWhenOpen(hudComponent);
        return hudComponent;
        
        /*if (component == null) {
            component = hud.createComponent(this);
        
            panel.setManager(manager);
            panel.setSh(sheet);
            panel.setSize(component.getWidth() - 30, 1);
            panel.renderSheet((StandardSheet) sheet.getDetails());
        }
        
        // see if there is a result
        StandardGuideViewPanel.NamedResult nr = (StandardGuideViewPanel.NamedResult) studentsCB.getSelectedItem();
        if (nr != null) {
            panel.renderResults((StandardResult) nr.getResult().getDetails());
        }
        
        return component;*/
    }

    public void close() {
        manager.removeResultListener(sheet.getId(), this);
    }

    public void resultAdded(final Result result) {
        //resultsModel.addElement(new StandardGuideViewPanel.NamedResult(result));
        
        //((StandardGuideViewPanel)panel).addStudent(result.getCreator());
    }

    public void resultUpdated(final Result result) {
        /*for (int i = 0; i < resultsModel.getSize(); i++) {
            StandardGuideViewPanel.NamedResult nr = (StandardGuideViewPanel.NamedResult) resultsModel.getElementAt(i);
            if (nr.getResult().getCreator().equals(result.getCreator())) {
                nr.setResult(result);
            }
        }*/
    }
    
}
