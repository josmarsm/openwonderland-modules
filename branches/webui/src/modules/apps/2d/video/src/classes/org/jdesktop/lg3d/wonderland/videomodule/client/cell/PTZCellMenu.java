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
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD.HUDButton;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD.HUDButton.ButtonListenerDelegate;

/**
 *
 * @author nsimpson
 */
public class PTZCellMenu extends CellMenu {

    private static final Logger logger =
            Logger.getLogger(PTZCellMenu.class.getName());
    private static PTZCellMenu ptzCellMenu;
    private HashMap buttonMapping;
    private HashMap actionMapping;

    private PTZCellMenu() {
        super();
        MENU_BG_IMG = "ptz_background.png";
        menuWidth = 242;
        rightMargin = 20;
        xOffset = menuWidth + rightMargin;
        yOffset = 228;
    }

    @Override
    protected void initializeMenu() {
        super.initializeMenu();

        buttonMapping = new HashMap();
        actionMapping = new HashMap();

        // place the buttons on the HUD menu
        for (Button b : Button.values()) {
            URL buttonURL = PTZCellMenu.class.getResource(b.getIconImageName());
            Point position = b.getPosition();

            HUDButton button = hud.addHUDImageButton(buttonURL, b.getIconText(),
                    origin.x + position.x, origin.y + position.y, 0, b.getVisible());
            button.setDraggable(false);

            buttonMapping.put(button, b);
            actionMapping.put(b, button);

            ActionListener listener = new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    handleActionEvent(evt);
                }
            };
            button.addActionListener(listener);
        }
    }

    @Override
    protected void handleActionEvent(ActionEvent evt) {
        ButtonListenerDelegate del = (ButtonListenerDelegate) evt.getSource();
        Object source = del.getSource();

        if (source instanceof HUDButton) {
            Button pressed = (Button) buttonMapping.get(source);
            for (VideoCellMenuListener listener : cellMenuListeners) {
                if (pressed == Button.PAN_LEFT) {
                    ((PTZCellMenuListener) listener).panLeft();
                } else if (pressed == Button.PAN_RIGHT) {
                    ((PTZCellMenuListener) listener).panRight();
                } else if (pressed == Button.TILT_UP) {
                    ((PTZCellMenuListener) listener).tiltUp();
                } else if (pressed == Button.TILT_DOWN) {
                    ((PTZCellMenuListener) listener).tiltDown();
                } else if (pressed == Button.CENTER) {
                    ((PTZCellMenuListener) listener).center();
                } else if (pressed == Button.OPEN) {
                    ((PTZCellMenuListener) listener).open();
                } else if (pressed == Button.ZOOM_IN) {
                    ((PTZCellMenuListener) listener).zoomIn();
                } else if (pressed == Button.ZOOM_OUT) {
                    ((PTZCellMenuListener) listener).zoomOut();
                } else if (pressed == Button.ZOOM_IN_FULLY) {
                    ((PTZCellMenuListener) listener).zoomInFully();
                } else if (pressed == Button.ZOOM_OUT_FULLY) {
                    ((PTZCellMenuListener) listener).zoomOutFully();
                }
            }
        }
    }

    public static PTZCellMenu getInstance() {
        if (ptzCellMenu == null) {
            ptzCellMenu = new PTZCellMenu();
        }

        return ptzCellMenu;
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);

        if (!buttonMapping.isEmpty()) {
            // show or hide the enabled buttons
            Iterator iter = buttonMapping.keySet().iterator();
            while (iter.hasNext()) {
                HUDButton b = (HUDButton) iter.next();
                Button button = (Button) buttonMapping.get(b);
                if (button.getVisible() == true) {
                    b.setActive(active);
                }
            }
        }
    }

    public enum Button {

        PAN_LEFT("pan_left.png", "Pan Left", 12, 52, true),
        PAN_RIGHT("pan_right.png", "Pan Right", 92, 52, true),
        TILT_UP("tilt_up.png", "Tilt Up", 52, 12, true),
        TILT_DOWN("tilt_down.png", "Tilt Down", 52, 92, true),
        CENTER("center.png", "Center", 52, 52, true),
        OPEN("open.png", "Open Video", 152, 12, true),
        ZOOM_IN("zoom_in.png", "Zoom In", 152, 52, true),
        ZOOM_OUT("zoom_out.png", "Zoom Out", 192, 52, true),
        ZOOM_IN_FULLY("zoom_in_max.png", "Zoom In Fully", 152, 92, true),
        ZOOM_OUT_FULLY("zoom_out_max.png", "Zoom Out Fully", 192, 92, true);
        private final String iconName;
        private final String iconText;
        private final Point iconPosition;
        private boolean visible;

        Button(String iconName, String iconText, int xpos, int ypos, boolean visible) {
            this.iconName = iconName;
            this.iconText = iconText;
            this.iconPosition = new Point(xpos, ypos);
            this.visible = visible;
        }

        public String getIconImageName() {
            return imagePath + iconName;
        }

        public String getIconText() {
            return iconText;
        }

        public Point getPosition() {
            return iconPosition;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean getVisible() {
            return visible;
        }
    }
}
