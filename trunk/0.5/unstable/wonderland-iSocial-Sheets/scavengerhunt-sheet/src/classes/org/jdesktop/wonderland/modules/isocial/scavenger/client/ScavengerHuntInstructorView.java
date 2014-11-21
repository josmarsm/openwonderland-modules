/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.client;

import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.client.view.ResultListener;
import org.jdesktop.wonderland.modules.isocial.client.view.SheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.annotation.View;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntSheet;

/**
 *  View for instructor in Scavenger Hunt.
 *
 * @author Vladimir Djurovic
 */
@View (value = ScavengerHuntSheet.class, roles = {Role.GUIDE, Role.ADMIN})
public class ScavengerHuntInstructorView implements SheetView, ResultListener {
    
    private ISocialManager manager;
    private Sheet sheet;
    private HUDComponent hudComponent;
    private ScavengerHuntInstructorViewPanel panel;
    

    public void initialize(ISocialManager ism, Sheet sheet, Role role) {
        this.manager = ism;
        this.sheet = sheet;
        panel = new ScavengerHuntInstructorViewPanel(ism, sheet);
        manager.addResultListener(sheet.getId(), this);
    }

    public String getMenuName() {
         ScavengerHuntSheet details = (ScavengerHuntSheet) sheet.getDetails();
        return details.getName();
    }

    public boolean isAutoOpen() {
        return ((ScavengerHuntSheet) sheet.getDetails()).isAutoOpen();
    }

    public HUDComponent open(HUD hud) {
        panel.validate();
        hudComponent = hud.createComponent(panel);
        return hudComponent;
    }

    public void close() {
        
    }

    public void resultAdded(Result result) {
        panel.addStudent(result.getCreator());
        panel.updateItemTable();
    }

    public void resultUpdated(Result result) {
        panel.updateItemTable();
    }
    
}
