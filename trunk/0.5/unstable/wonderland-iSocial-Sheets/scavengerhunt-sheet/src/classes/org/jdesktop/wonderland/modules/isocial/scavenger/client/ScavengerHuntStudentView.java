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
import org.jdesktop.wonderland.modules.isocial.common.model.ResultDetails;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntResult;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntSheet;

/** View class for scavenger hunt sheet.
 *
 * @author Vladimir Djurovic
 */
@View (value = ScavengerHuntSheet.class, roles = {Role.STUDENT})
public class ScavengerHuntStudentView implements SheetView, ResultListener {
    
    private ISocialManager manager;
    private Sheet sheet;
    private HUDComponent hudComponent;
    private ScavengerHuntStudentViewPanel panel;
    private boolean leaderBoardShown = false;
    
    /** Scavenger hunt leaderboard. */
    private LeaderboardHUD leaderboard;

    public void initialize(ISocialManager ism, Sheet sheet, Role role) {
        this.manager = ism;
        this.sheet = sheet;
        panel = new ScavengerHuntStudentViewPanel(ism, sheet, ism.getUsername());
        leaderboard = new LeaderboardHUD();
        ism.addResultListener(sheet.getId(), this);
    }

    public String getMenuName() {
         ScavengerHuntSheet details = (ScavengerHuntSheet) sheet.getDetails();
        return details.getName();
    }

    public boolean isAutoOpen() {
        return ((ScavengerHuntSheet) sheet.getDetails()).isAutoOpen();
    }

    public HUDComponent open(HUD hud) {
        hudComponent = hud.createComponent(panel);
        return hudComponent;
    }

    public void close() {
       
    }

    public void resultAdded(Result result) {
  
    }

    public void resultUpdated(Result result) {
        ResultDetails rd = result.getDetails();
        if(rd instanceof ScavengerHuntResult && result.getCreator().equals(manager.getUsername())){
            ScavengerHuntResult shr = (ScavengerHuntResult)rd;
            if(shr.getDuration() > -1 && !leaderBoardShown){
                panel.endHunt();
                leaderBoardShown = true;
                leaderboard.setHUDComponentVisible(true);
            }
        }
    }
    
}
