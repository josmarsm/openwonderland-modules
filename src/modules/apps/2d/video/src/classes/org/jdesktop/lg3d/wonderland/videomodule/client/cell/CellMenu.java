/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.lg3d.wonderland.videomodule.client.cell;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.URL;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.jdesktop.lg3d.wonderland.darkstar.client.cell.Cell;

import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD.HUDObject;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD.HUDButton;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUDFactory;

/**
 *
 * @author jh215363
 * @author jp1223
 * @author nsimpson
 */
public abstract class CellMenu {

    private static final Logger logger =
            Logger.getLogger(CellMenu.class.getName());
    private Cell currentCell;
    private HUDObject menuBackground;
    private HUDButton closeMenuButton;
    private HUDObject menuText;
    protected static String imagePath = "resources/";
    protected String MENU_BG_IMG = "background.png";
    protected String CANCEL_ICON_IMG = "cancel.png";
    protected HUD hud;
    protected Point origin;
    protected boolean menuInitialized = false;
    protected boolean isActive;
    protected ArrayList<VideoCellMenuListener> cellMenuListeners = new ArrayList();
    // menu positioning variables
    protected int menuWidth;
    protected int rightMargin;
    protected int xOffset;
    protected int yOffset;
    protected int closeOffsetX = -21;
    protected int closeOffsetY = 5;

        
    public CellMenu() {
        hud = HUDFactory.getHUD();
        menuWidth = 274;
        rightMargin = 20;
        xOffset = menuWidth + rightMargin;
        yOffset = 150;
    }

    protected void initializeMenu() {
        origin = new Point(-xOffset, -yOffset);
        
        // create the background for the menu
        URL backgroundURL = CellMenu.class.getResource(imagePath + MENU_BG_IMG);
        menuBackground = hud.addHUDImage(backgroundURL, origin.x, origin.y, 0, false);
        menuBackground.setDraggable(false);

        // add a close button
        URL cancelURL = CellMenu.class.getResource(imagePath + CANCEL_ICON_IMG);
        closeMenuButton = hud.addHUDImageButton(cancelURL, "Close Menu", origin.x + menuWidth + closeOffsetX, origin.y + closeOffsetY, 0, false);
        closeMenuButton.setDraggable(false);
        closeMenuButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                CellMenuManager.getInstance().hideMenu();
            }
        });
        menuInitialized = true;
    }

    protected Point getOrigin() {
        return origin;
    }

    protected void addCellMenuListener(VideoCellMenuListener listener) {
        synchronized (cellMenuListeners) {
            cellMenuListeners.add(listener);
        }
    }

    protected void removeCellMenuListener(VideoCellMenuListener listener) {
        synchronized (cellMenuListeners) {
            cellMenuListeners.remove(listener);
        }
    }

    protected void handleActionEvent(ActionEvent evt) {

    }

    protected void updateMenu() {
        
    }
    
    public void setActive(Cell cell, String title, boolean active) {
        currentCell = cell;
        if (!menuInitialized) {
            initializeMenu();
        }

        setActive(title, active);
    }

    public void setActive(String title, boolean active) {
        menuBackground.changeActive(true, 500);

        if (menuText != null) {
            menuText.remove();
        }

        if (title != null) {
            menuText = hud.addText(title, origin.x + 30, origin.y + 18, 0, false);
            menuText.changeActive(true, 500);
        }

        setActive(active);
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;

        menuBackground.changeActive(isActive, 500);
        if (menuText != null) {
            menuText.changeActive(isActive, 500);
        }
        closeMenuButton.changeActive(isActive, 500);
    }

    public void setInactive() {
        setActive(false);
    }

    public boolean isActive() {
        return isActive;
    }
}
