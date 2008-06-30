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

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.URL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import javax.swing.JButton;

import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD.HUDButton;
import org.jdesktop.lg3d.wonderland.scenemanager.hud.HUD.HUDButton.ButtonListenerDelegate;

/**
 *
 * @author jp1223
 * @author nsimpson
 */
public class VideoCellMenu extends CellMenu {

    private static final Logger logger =
            Logger.getLogger(VideoCellMenu.class.getName());
    private static final int LEFT_INSET = 8;
    private static final int TOP_INSET = 13;
    private static final int ICON_GAP = 2;
    private HashMap buttonMapping;
    private HashMap actionMapping;

    public VideoCellMenu() {
        super();
    }

    @Override
    protected void initializeMenu() {
        super.initializeMenu();

        buttonMapping = new HashMap();
        actionMapping = new HashMap();

        MediaTracker mediaTracker = new MediaTracker(new JButton());
        HashMap buttonPositions = new LinkedHashMap();
        Integer posWidth = 0;
        Integer width = 0;
        Integer position = 0;
        Integer x = origin.x + LEFT_INSET;
        Integer y = origin.y + TOP_INSET;

        // calculate the maximum width of each button position in the
        // HUD menu
        for (Button b : Button.values()) {
            position = b.getPosition();

            URL buttonURL = VideoCellMenu.class.getResource(b.getIconImageName());
            Image img = Toolkit.getDefaultToolkit().getImage(buttonURL);
            mediaTracker.addImage(img, 0);

            try {
                mediaTracker.waitForID(0);
            } catch (InterruptedException ie) {
            }

            width = img.getWidth(null);
            posWidth = (Integer) buttonPositions.get(b.getPosition());
            if (posWidth != null) {
                if (width > posWidth) {
                    buttonPositions.put(position, width);
                }
            } else {
                buttonPositions.put(position, width);
            }
        }

        // calculate the position of each button on the HUD menu based on
        // the widest button at each position
        if (!buttonPositions.isEmpty()) {
            Iterator<Integer> iter = buttonPositions.keySet().iterator();
            while (iter.hasNext()) {
                position = iter.next(); // position 0, 1, ...
                width = (Integer) buttonPositions.get(position); // width of icon at position
                buttonPositions.put(position, x);
                x += width + ICON_GAP;
            }
        }

        // place the buttons on the HUD menu
        for (Button b : Button.values()) {
            URL buttonURL = VideoCellMenu.class.getResource(b.getIconImageName());
            int xpos = ((Integer) buttonPositions.get(b.getPosition())).intValue();

            HUDButton button = hud.addHUDImageButton(buttonURL, b.getIconText(), xpos, y, 0, b.getVisible());
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
                if (pressed == Button.OPEN) {
                    listener.open();
                } else if (pressed == Button.PLAY) {
                    listener.play();
                } else if (pressed == Button.PAUSE) {
                    listener.pause();
                } else if (pressed == Button.REWIND) {
                    listener.rewind();
                } else if (pressed == Button.FORWARD) {
                    listener.fastForward();
                } else if (pressed == Button.STOP) {
                    listener.stop();
                } else if (pressed == Button.SYNC) {
                    listener.sync();
                } else if (pressed == Button.UNSYNC) {
                    listener.unsync();
                }
            }
        }
    }

    public static VideoCellMenu getInstance() {
        return new VideoCellMenu();
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
                    b.changeActive(active, 100);
                }
            }
        }
    }

    protected void enableButton(Button b) {
        b.setVisible(true);
        if (menuInitialized) {
            HUDButton button = (HUDButton) actionMapping.get(b);
            if (!button.isActive()) {
                button.changeActive(true, 0);
            }
        }
    }

    protected void disableButton(Button b) {
        b.setVisible(false);
        if (menuInitialized) {
            HUDButton button = (HUDButton) actionMapping.get(b);
            if (button.isActive()) {
                button.changeActive(false, 0);
            }
        }
    }

    public enum Button {

        OPEN("open.png", "Open Video", 0, true),
        PLAY("play.png", "Play", 1, true),
        PAUSE("pause.png", "Pause", 1, true),
        STOP("stop.png", "Stop", 2, true),
        REWIND("rewind.png", "Rewind", 3, true),
        FORWARD("forward.png", "Fast Forward", 4, true),
        SYNC("sync.png", "Sync", 5, true),
        UNSYNC("unsync.png", "Unsync", 5, true);
        private final String iconName;
        private final String iconText;
        private final int iconPosition;
        private boolean visible;

        Button(String iconName, String iconText, int position, boolean visible) {
            this.iconName = iconName;
            this.iconText = iconText;
            this.iconPosition = position;
            this.visible = visible;
        }

        public String getIconImageName() {
            return imagePath + iconName;
        }

        public String getIconText() {
            return iconText;
        }

        public int getPosition() {
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
