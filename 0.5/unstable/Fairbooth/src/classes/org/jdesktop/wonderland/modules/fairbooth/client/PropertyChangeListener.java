/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.fairbooth.client;

import com.wonderbuilders.modules.colortheme.client.ColorThemeComponent;
import com.wonderbuilders.modules.colortheme.client.ColorThemeComponentConstants;
import com.wonderbuilders.modules.colortheme.common.ColorTheme;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.modules.fairbooth.common.FairBoothConstants;
import org.jdesktop.wonderland.modules.fairbooth.common.FairBoothProperties;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;

/**
 *
 * @author Nilang
 */
public class PropertyChangeListener implements SharedMapListenerCli{

    private FairBoothCell parentCell;
    private String boothName="Untitled Booth";
    private int colorTheme=0;
    private int oldColorTheme=0;
    private String infoText="Untitled";
    
    public PropertyChangeListener(FairBoothCell parentCell) {
        this.parentCell = parentCell;
    }
    
    public void propertyChanged(SharedMapEventCli smec) {
        
        FairBoothProperties newIfp = (FairBoothProperties) smec.getNewValue();
        FairBoothProperties oldIfp = (FairBoothProperties) smec.getOldValue();
        
        if(oldIfp!=null) {
            boothName = newIfp.getBoothName();
            
            //change booth name & info text
            try {
                infoText = newIfp.getInfoText();
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try {
                            parentCell.changeBoothName(boothName);
                            parentCell.changeInfoText(infoText);
                        } catch (Exception ex) {
                            Logger.getLogger(PropertyChangeListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                t.start();
            } catch (Exception ex) {
                Logger.getLogger(PropertyChangeListener.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //apply color theme
            colorTheme = newIfp.getColorTheme();
            oldColorTheme = oldIfp.getColorTheme();
            if(colorTheme!=oldColorTheme) {
                SharedStateComponent ssc = parentCell.getCellCache().getEnvironmentCell().getComponent(SharedStateComponent.class);
                Map smc = ssc.get(ColorThemeComponentConstants.COLOR_THEME_SHARED_MAP);
                ColorTheme ct = (ColorTheme) smc.get(FairBoothConstants.colorThemes[colorTheme]);
                if(ct!=null) {
                    //System.out.println("FAIRBOOTH : PROPERTY CHANGE");
                    parentCell.ctc.previewColor(FairBoothConstants.colorThemes[colorTheme], smc,ct.getColorMap());

                    //change color for desk
                    Cell boothCell = null;
                    Iterator itr = parentCell.getChildren().iterator();
                    while(itr.hasNext()) {
                        Cell c = (Cell) itr.next();
                        if(c.getName().equals("Booth Desk")) {
                            boothCell = (Cell) c;
                            break;
                        }
                    }
                    ColorThemeComponent ctc = boothCell.getComponent(ColorThemeComponent.class);
                    ctc.previewColor(FairBoothConstants.colorThemes[colorTheme], smc,ct.getColorMap());
                }
            }
        }
        
    }
    
}
