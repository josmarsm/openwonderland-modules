/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */


package org.jdesktop.wonderland.modules.isocial.scavenger.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.CompassLayout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.client.view.ResultListener;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntResult;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntSheet;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.StudentRank;

/** HUD component for Scavenger Hunt leaderboard.
 *
 * @author Vladimir Djurovic
 */
public class LeaderboardHUD implements ResultListener {
    
    /** Logger instance for this class. */
    private static final Logger LOGGER = Logger.getLogger(LeaderboardHUD.class.getName());
    
    /** Main application HUD display. */
    private HUD mainHUD;
    
    /** HUD component for leaderboard. */
    private HUDComponent leaderboardHUD;
    
    /** Current scavenge rhunt sheet. */
    private Sheet currentSheet;
    
    /** Panel holding HUD UI. */
    private LeaderboardPanel hudPanel;
    
    /** Currently logged in user. */
    private String currentUser;

    /**
     * Creates new instance of this HUD component.
     */
    public LeaderboardHUD(){
        mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        hudPanel = new LeaderboardPanel();
        leaderboardHUD = mainHUD.createComponent(hudPanel);
        leaderboardHUD.setDecoratable(true);
        leaderboardHUD.setName("Leaderboard");
        leaderboardHUD.setPreferredLocation(CompassLayout.Layout.NORTH);
        mainHUD.addComponent(leaderboardHUD);
        currentUser = ISocialManager.INSTANCE.getSession().getUsername();
        // find SH sheet for current lesson and add listener to it
        try {
            List<Sheet>  sheets = ISocialManager.INSTANCE.getCurrentInstance().getSheets();
            for(Sheet s : sheets){
                if(s.getDetails() instanceof ScavengerHuntSheet){
                    currentSheet = s;
                    ISocialManager.INSTANCE.addResultListener(s.getId(), this);
                    break;
                }
            }
        } catch(IOException iex){
            LOGGER.log(Level.SEVERE, "Unable to get sheet ID: {0}", iex.getMessage());
        }
        
    }
    
    /**Calculates ranking of all users who completed the hunt.
     * 
     * @throws IOException 
     */
    private void calculateRanking() throws IOException {
        Collection<Result> results = ISocialManager.INSTANCE.getResults(currentSheet.getId());
        StudentRank[] ranks = new StudentRank[results.size()];
        int i = 0;
        Iterator<Result> it = results.iterator();
        while(it.hasNext()){
            Result r = it.next();
            StudentRank rank = ((ScavengerHuntResult)r.getDetails()).calculateRank();
            rank.setUsername(r.getCreator());
            // calculate actual time for rank in miliseconds
            long time = rank.getTime() * 1000;
            rank.setTime(time);
            ranks[i] = rank;
            i++;
        }
        Arrays.sort(ranks);
        hudPanel.updateRankings(ranks, currentUser);
    }
    
    /**
     * Shows or hides HUD component.
     * 
     * @param status whether to show or hide component.
     */
    public final void setHUDComponentVisible(final boolean status){
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try{
                    if(status){
                        calculateRanking();
                    }
                } catch(IOException ex){
                    LOGGER.log(Level.SEVERE, "Could not get result data: {0}", ex);
                }
                
                leaderboardHUD.setVisible(status);
            }
        });
    }

    /**
     * Invoked when new result is added to current sheet.
     * 
     * @param result new result
     */
    public void resultAdded(Result result) {
        
    }

    /**
     * Invoked when existing result is updated
     * 
     * @param result updated result
     */
    public void resultUpdated(Result result) {
        if(result.getDetails() instanceof ScavengerHuntResult && ((ScavengerHuntResult)result.getDetails()).getDuration() > 0){
            try{
                calculateRanking();
            } catch(IOException ex){
                LOGGER.log(Level.SEVERE, "Could not update result: {0}", ex);
            }
            
        }
    }
}
