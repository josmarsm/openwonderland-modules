/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.whiteboard.client;

import java.awt.Color;
import org.jdesktop.wonderland.client.hud.HUDObject.DisplayMode;

/**
 * Class to manage the selected tool
 * @author bhoran
 * @author nsimpson
 */
public class WhiteboardToolManager implements WhiteboardCellMenuListener {

    private boolean hudState = false;

    public enum WhiteboardTool {

        NEW, SELECTOR, LINE, RECT, ELLIPSE, TEXT, FILL, DRAW, SYNC, UNSYNC
    }

    public enum WhiteboardColor {

        BLACK(Color.BLACK),
        WHITE(Color.WHITE),
        RED(Color.RED),
        GREEN(Color.GREEN),
        BLUE(Color.BLUE);
        private final Color c;

        WhiteboardColor(Color c) {
            this.c = c;
        }

        Color getColor() {
            return c;
        }
    }
    private WhiteboardColor currentColor = null;
    private WhiteboardTool currentTool = null;
    private boolean filled = false;
    private WhiteboardWindow whiteboardWindow;

    WhiteboardToolManager(WhiteboardWindow whiteboardWindow) {
        this.whiteboardWindow = whiteboardWindow;
        setTool(WhiteboardTool.LINE);
        setColor(WhiteboardColor.BLACK);
    }

    // WhiteboardCellMenuListener methods
    public void newDoc() {
        whiteboardWindow.newDocument(true);
    }

    public void openDoc() {
        whiteboardWindow.showSVGDialog();
    }

    public void selector() {
        setTool(WhiteboardTool.SELECTOR);
    }

    public void line() {
        setTool(WhiteboardTool.LINE);
    }

    public void rect() {
        setTool(WhiteboardTool.RECT);
    }

    public void ellipse() {
        setTool(WhiteboardTool.ELLIPSE);
    }

    public void text() {
        setTool(WhiteboardTool.TEXT);
    }

    public void fill() {
        setFilled(true);
    }

    public void draw() {
        setFilled(false);
    }

    public void black() {
        setColor(WhiteboardColor.BLACK);
    }

    public void white() {
        setColor(WhiteboardColor.WHITE);
    }

    public void red() {
        setColor(WhiteboardColor.RED);
    }

    public void green() {
        setColor(WhiteboardColor.GREEN);
    }

    public void blue() {
        setColor(WhiteboardColor.BLUE);
    }

    public void zoomIn() {
        //zoomTo(1.1f, true);
    }

    public void zoomOut() {
        //zoomTo(0.9f, true);
    }

    public void sync() {
        hudState = !hudState;
        whiteboardWindow.sync(!whiteboardWindow.isSynced());
    }

    public void unsync() {
        hudState = !hudState;
        whiteboardWindow.sync(!whiteboardWindow.isSynced());
    }

    public void toggleHUD() {
        if (whiteboardWindow.getDisplayMode().equals(DisplayMode.HUD)) {
            whiteboardWindow.setDisplayMode(DisplayMode.WORLD);
        } else {
            whiteboardWindow.setDisplayMode(DisplayMode.HUD);
        }
        whiteboardWindow.showControls(true);
    }

    public boolean isOnHUD() {
        return (whiteboardWindow.getDisplayMode().equals(DisplayMode.HUD));
    }

    private void setTool(WhiteboardTool newTool) {
        if (currentTool == newTool) {
            //no change
            return;
        }
        if (currentTool != null) {
            //Untoggle the tool
            whiteboardWindow.deselectTool(currentTool);
            currentTool = null;
        }
        if (newTool != null) {
            //toggle the new tool
            whiteboardWindow.selectTool(newTool);
            currentTool = newTool;
        }
    }

    /**
     * @return the currentTool
     */
    public WhiteboardTool getTool() {
        return currentTool;
    }

    private void setColor(WhiteboardColor newColor) {
        if (currentColor == newColor) {
            // no change
            return;
        }
        if (currentColor != null) {
            // untoggle the color
            whiteboardWindow.deselectColor(currentColor);
            currentColor = null;
        }
        if (newColor != null) {
            // toggle the new tool
            whiteboardWindow.selectColor(newColor);
            currentColor = newColor;
        }
    }

    /**
     * @return the currentColor
     */
    public Color getColor() {
        return currentColor.getColor();
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
        whiteboardWindow.updateMenu();
    }

    public boolean isFilled() {
        return filled;
    }
}
