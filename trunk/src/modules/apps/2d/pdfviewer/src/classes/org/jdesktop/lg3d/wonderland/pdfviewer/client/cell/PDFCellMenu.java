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
package org.jdesktop.lg3d.wonderland.pdfviewer.client.cell;

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
public class PDFCellMenu extends CellMenu {

    private static final Logger logger =
            Logger.getLogger(PDFCellMenu.class.getName());
    private static final String imagePath = "resources/";
    private static final int LEFT_INSET = 8;
    private static final int TOP_INSET = 13;
    private static final int ICON_GAP = 2;
    private HashMap buttonMapping;
    private HashMap actionMapping;

    public PDFCellMenu() {
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

            URL buttonURL = PDFCellMenu.class.getResource(b.getIconImageName());
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
            URL buttonURL = PDFCellMenu.class.getResource(b.getIconImageName());
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

    public void enableButton(Button b) {
        b.setVisible(true);
        if (menuInitialized) {
            HUDButton button = (HUDButton) actionMapping.get(b);
            button.changeActive(true, 250);
        }
    }

    public void disableButton(Button b) {
        b.setVisible(false);
        if (menuInitialized) {
            HUDButton button = (HUDButton) actionMapping.get(b);
            button.changeActive(false, 250);
        }
    }

    protected void handleActionEvent(ActionEvent evt) {
        ButtonListenerDelegate del = (ButtonListenerDelegate) evt.getSource();
        Object source = del.getSource();

        if (source instanceof HUDButton) {
            Button pressed = (Button) buttonMapping.get(source);
            for (PDFCellMenuListener listener : cellMenuListeners) {
                if (pressed == Button.OPEN) {
                    listener.open();
                } else if (pressed == Button.FIRST) {
                    listener.first();
                } else if (pressed == Button.PREVIOUS) {
                    listener.previous();
                } else if (pressed == Button.GOTO_PAGE) {
                    listener.gotoPage();
                } else if (pressed == Button.NEXT) {
                    listener.next();
                } else if (pressed == Button.LAST) {
                    listener.last();
                } else if (pressed == Button.START_SLIDESHOW) {
                    listener.startSlideShow();
                } else if (pressed == Button.PAUSE_SLIDESHOW) {
                    listener.pauseSlideShow();
                } else if (pressed == Button.ZOOM_IN) {
                    listener.zoomIn();
                } else if (pressed == Button.ZOOM_OUT) {
                    listener.zoomOut();
                } else if (pressed == Button.SYNC) {
                    listener.sync();
                } else if (pressed == Button.UNSYNC) {
                    listener.unsync();
                }
            }
        }
    }

    public static PDFCellMenu getInstance() {
        return new PDFCellMenu();
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

        OPEN("open.png", "Open PDF", 0, true),
        FIRST("page_first.png", "First page", 1, true),
        PREVIOUS("page_previous.png", "Previous Page", 2, true),
        GOTO_PAGE("page_goto.png", "Goto Page", 3, true),
        NEXT("page_next.png", "Next Page", 4, true),
        LAST("page_last.png", "Last Page", 5, true),
        START_SLIDESHOW("play.png", "Start Slideshow", 6, true),
        PAUSE_SLIDESHOW("pause.png", "Pause Slideshow", 6, true),
        ZOOM_IN("zoom_in.png", "Zoom In", 7, true),
        ZOOM_OUT("zoom_out.png", "Zoom Out", 8, true),
        SYNC("sync.png", "Sync", 9, true),
        UNSYNC("unsync.png", "Unsync", 9, true);
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
