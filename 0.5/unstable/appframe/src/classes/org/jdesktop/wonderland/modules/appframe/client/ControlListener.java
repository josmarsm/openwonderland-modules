/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.appframe.client;

import java.awt.Color;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb.ControlChangeListener;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameProp;

/**
 *
 * @author nilang
 */
//this class is responsible to change the color of appframe border and set the dirtybit for document and apps
public class ControlListener implements ControlChangeListener {

    public AppFrame parentCell;

    public ControlListener(AppFrame parentCell) {
        this.parentCell = parentCell;
    }

    public void updateControl(ControlArb ca) {
        try {
            if (ca.hasControl()) {
                AppFrameProp afp = (AppFrameProp) parentCell.propertyMap.get("afp");
                parentCell.borderColor = parseColorString(afp.getBorderColor());
                new ColorChange(parentCell).changeColor(parentCell, new Color(2, 2, 2), parentCell.borderColor, true);
                Cell cell = parentCell.getChildren().iterator().next();
                CellServerState css = parentCell.getServerState(cell);
                String extention = parentCell.getFileExtension(css.getName());
                if (css.getName().equals("Whiteboard")) {

                    parentCell.dirtyMap.putBoolean("dirty", true);
                } else if (extention != null && (extention.equals("svg"))) {

                    parentCell.dirtyMap.putBoolean("dirty", true);
                }
            } else {
                new ColorChange(parentCell).changeColor(parentCell, parentCell.borderColor, new Color(2, 2, 2), false);


                Cell cell = parentCell.getChildren().iterator().next();
                CellServerState css = parentCell.getServerState(cell);
                String extention = parentCell.getFileExtension(css.getName());
                if (css.getName().equals("Whiteboard")) {
                    AppFrameProp afp = (AppFrameProp) parentCell.propertyMap.get("afp");
                    parentCell.dirtyMap.putBoolean("dirty", true);
                } else if (extention != null && (extention.equals("svg"))) {
                    AppFrameProp afp = (AppFrameProp) parentCell.propertyMap.get("afp");
                    parentCell.dirtyMap.putBoolean("dirty", true);
                } else {
                    parentCell.store();
                }
            }
        } catch (Exception ei) {
            ei.printStackTrace();
        }
    }

    public Color parseColorString(String colorString) {
        try {
            String[] c = colorString.split(":");
            if (c.length < 3) {
                parentCell.LOGGER.severe("Improperly formatted color string passed: " + colorString);
                return null;
            }
            Integer r = Integer.parseInt(c[0]);
            Integer g = Integer.parseInt(c[1]);
            Integer b = Integer.parseInt(c[2]);
            Color newColor = new Color(r, g, b);
            return newColor;

        } catch (Exception ei) {
            ei.printStackTrace();
        }
        return null;
    }
}
