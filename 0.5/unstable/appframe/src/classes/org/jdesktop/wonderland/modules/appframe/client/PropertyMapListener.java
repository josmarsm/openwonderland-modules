/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.appframe.client;

import com.jme.renderer.ColorRGBA;
import java.awt.Color;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameProp;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;

/**
 *
 * @author nilang
 */
public class PropertyMapListener implements SharedMapListenerCli {

    AppFrame parentCell;
    String newAspectRatio, newOrientation, newMaxHistory, newBorderColor;
    String oldAspectRatio, oldOrientation, oldMaxHistory, oldBorderColor;

    public PropertyMapListener(AppFrame parentCell) {
        this.parentCell = parentCell;
        if (!parentCell.propertyMap.isEmpty()) {
            AppFrameProp afp = (AppFrameProp) parentCell.propertyMap.get("afp");

            oldAspectRatio = afp.getAspectRatio();
            oldOrientation = afp.getOrientation();
            oldMaxHistory = afp.getMaxHistory();

            oldBorderColor = afp.getBorderColor();
        } 
    }

    public void propertyChanged(SharedMapEventCli smec) {
        try {
            AppFrameProp afpNew = (AppFrameProp) smec.getNewValue();
            //   newPinToMenu.putAll(afpNew.getPinToMenu());
            newOrientation = afpNew.getOrientation();
            newAspectRatio = afpNew.getAspectRatio();
            newBorderColor = afpNew.getBorderColor();

            if (parentCell.appFrameProperties != null) {

                parentCell.appFrameProperties.newMaxHistory = afpNew.getMaxHistory();
                parentCell.appFrameProperties.newBoarderColor = afpNew.getBorderColor();
                parentCell.appFrameProperties.populateMaxHistory();

                parentCell.appFrameProperties.updateBorderColor();
                parentCell.appFrameProperties.newOrientation = newOrientation;
                parentCell.appFrameProperties.newAspectRatio = newAspectRatio;
                parentCell.appFrameProperties.populateProp();
            }
            Color newColor = AppFrameProperties.parseColorString(newBorderColor);
            MouseClickListener.HIGHLIGHT_COLOR = new ColorRGBA(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), newColor.getAlpha());
            if (oldAspectRatio == null) {
                AppFrameProp afpOld = (AppFrameProp) smec.getOldValue();
                if(afpOld!=null) {
                oldAspectRatio = afpOld.getAspectRatio();
                oldOrientation = afpOld.getOrientation();
                  oldMaxHistory = afpOld.getMaxHistory();

                //     oldPinToMenu.putAll(afpOld.getPinToMenu());
                oldBorderColor = afpOld.getBorderColor();
                }
            }
            if (!newOrientation.equalsIgnoreCase(oldOrientation)) {
                oldOrientation = newOrientation;
                AppFrameProperties.updateOrientationParent(parentCell, newOrientation, "parent");
                if (parentCell.getNumChildren() == 1) {
                    float origWidth = 0, origHeight = 0;
                    Cell child = parentCell.getChildren().iterator().next();
                    if (child instanceof App2DCell) {
                        App2DCell childCell = (App2DCell) child;
                        origWidth = parentCell.origWidth * childCell.getApp().getPixelScale().getX();
                        origHeight = parentCell.origHeight * childCell.getApp().getPixelScale().getY();
                    } else {
                        origWidth = parentCell.origImageWidth;
                        origHeight = parentCell.origImageHeight;
                    }
                    AppFrameProperties.updateOrientation(child, newOrientation, newAspectRatio, origWidth, origHeight);
                    AppFrameProperties.updateAspect(child, newOrientation, newAspectRatio, origWidth, origHeight);
                }
            }
            if (!newAspectRatio.equalsIgnoreCase(oldAspectRatio)) {
                oldAspectRatio = newAspectRatio;
                if (newAspectRatio.equalsIgnoreCase("16*9")) {
                    AppFrameProperties.aspectRatio169(newAspectRatio, parentCell);
                    if (parentCell.getNumChildren() == 1) {
                        float origWidth = 0, origHeight = 0;
                        Cell child = parentCell.getChildren().iterator().next();
                        if (child instanceof App2DCell) {
                            App2DCell childCell = (App2DCell) child;
                            origWidth = parentCell.origWidth;
                            origHeight = parentCell.origHeight;
                            origWidth = parentCell.origWidth * childCell.getApp().getPixelScale().getX();
                            origHeight = parentCell.origHeight * childCell.getApp().getPixelScale().getY();
                        } else {
                            origWidth = parentCell.origImageWidth;
                            origHeight = parentCell.origImageHeight;
                        }
                        AppFrameProperties.updateAspect(child, newOrientation, newAspectRatio, origWidth, origHeight);
                    }
                } else {
                    AppFrameProperties.aspectRatio34(newAspectRatio, parentCell);
                    if (parentCell.getNumChildren() == 1) {
       
                        float origWidth = 0, origHeight = 0;
                        Cell child = parentCell.getChildren().iterator().next();
                        if (child instanceof App2DCell) {
                            App2DCell childCell = (App2DCell) child;
//                            origWidth = parentCell.origWidth;
//                            origHeight = parentCell.origHeight;
                            origWidth = parentCell.origWidth * childCell.getApp().getPixelScale().getX();
                            origHeight = parentCell.origHeight * childCell.getApp().getPixelScale().getY();
                        } else {
                            origWidth = parentCell.origImageWidth;
                            origHeight = parentCell.origImageHeight;
                        }
                        AppFrameProperties.updateAspect(child, newOrientation, newAspectRatio, origWidth, origHeight);
                    }

                }

            }
        } catch (Exception ei) {
            ei.printStackTrace();
        }
    }
}
