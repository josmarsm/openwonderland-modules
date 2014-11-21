/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.standardsheet.client;

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
import org.jdesktop.wonderland.modules.standardsheet.common.StandardResult;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardSheet;

/**
 * Standard sheet view for a single student.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@View(value=StandardSheet.class, roles=Role.STUDENT)
public class StandardStudentView  implements SheetView, ResultListener,DockableSheetView  {

    private ISocialManager manager;
    private Sheet sheet;
    private HUDComponent hudComponent;
    private StandardStudentViewPanel panel;
    
    public void initialize(ISocialManager manager, Sheet sheet, Role role) {
        this.manager = manager;
        this.sheet = sheet;
        panel = new StandardStudentViewPanel(manager, sheet);
        
    }

    public String getMenuName() {
        return sheet.getName();
    }

    public boolean isAutoOpen() {
        return ((StandardSheet) sheet.getDetails()).isAutoOpen();
    }
    
    public boolean isDockable() {
        return ((StandardSheet) sheet.getDetails()).isDockable();
    }

    public HUDComponent open(HUD hud) {
        hudComponent = hud.createComponent(panel);
        panel.renderSheetWhenOpen(hudComponent);
        return hudComponent;
    }

    public void close() {
        manager.removeResultListener(sheet.getId(), this);
    }
    
    public void resultAdded(Result result) {
        if (result.getCreator().equals(manager.getUsername())) {
            panel.currentResult = result;
        }
    }

    public void resultUpdated(Result result) {
        if (result.getCreator().equals(manager.getUsername())) {
            panel.currentResult = result;
        }
    }
    
}
