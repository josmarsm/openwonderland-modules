/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.tokensheet.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.PaintEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
//import org.jdesktop.wonderland.modules.colormanager.client.ColorManager;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.tokensheet.common.TokenResult;

/**
 * Creates the tokens panel for token and behavior system. The panel contains a 
 * label with an image to show the student's current number of token and its 
 * color graph.
 * 
 * @author Kaustubh
 */
public class TokenStudentPanel extends ImageIcon {

    private ImageIcon imageIcon;
    private TokenResult tokenDetails;
    private Image image;
    private Graphics2D graphics;
    private int startMeterX, startMeterY, end;
    private String userName;
    private int iWidth, iHeight;
    private int startTextX, startTextY;
    private int thresholdOffset, classTokens;
    private Color meterColor;
    private final ISocialManager manager;
    private int totalTokens, maxLimit;
    private Sheet sheet;

    TokenStudentPanel(ISocialManager manager, Sheet sheet) {
        //super(url);
        this.userName = manager.getUsername();
        this.manager = manager;
        this.sheet = sheet;
        try {
            if (manager.getCurrentRole() == Role.STUDENT) {
                //meterColor = ColorManager.getInstance().getColorFor(manager.getCurrentInstance().getCohortId(), userName);
            } else {
                meterColor = Color.decode("#FFFFFF");
            }
            String url = ImageManager.getImageNameFor(meterColor);
            image = Toolkit.getDefaultToolkit().createImage(getClass().getResource(url));
        } catch (IOException ex) {
            Logger.getLogger(TokenStudentPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(TokenStudentPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (tracker.statusAll(true) != MediaTracker.COMPLETE) {
        }

        BufferedImage bfImage = new BufferedImage(image.getWidth(component),
                image.getHeight(component), BufferedImage.TYPE_4BYTE_ABGR);
        graphics = bfImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(image, 0, 0, component);
        this.imageIcon = new ImageIcon(bfImage);
        iWidth = image.getWidth(component);
        iHeight = image.getHeight(component);
        startMeterX = 18 * iWidth / 100;
        thresholdOffset = 18 * iWidth / 100;
        startTextX = startMeterX / 2 - 8;
        startMeterY = (int) (iHeight / 3.5);
        startTextY = iHeight / 2 + 8;
    }

    public ImageIcon getImageIcon() {
        return this.imageIcon;
    }

    @Override
    public synchronized void paintIcon(Component cmpnt, Graphics grphcs, int i, int i1) {
        try {
            int meterWidth = image.getWidth(component) - thresholdOffset;
            int meterHeight = image.getHeight(component) / 2 - 5;
            int tokens = tokenDetails.getStudentResult().getTokensValue();

            if (manager.getCurrentRole() != Role.STUDENT) {
                int tokenAmount = totalTokens;
                grphcs.setColor(Color.BLACK);
                grphcs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
                int width = grphcs.getFontMetrics().stringWidth(String.valueOf(tokenAmount));
                grphcs.drawString(String.valueOf(tokenAmount), i - width / 4, i1);
            } else if (userName.equals(tokenDetails.getStudentResult().getName())) {
                grphcs.setColor(Color.BLACK);
                grphcs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
                int width = grphcs.getFontMetrics().stringWidth(String.valueOf(tokens));
                if (totalTokens == 0) {
                    grphcs.drawString(String.valueOf(tokens), i - width / 4, i1);
                } else {
                    grphcs.drawString(String.valueOf(totalTokens), i - width / 4, i1);
                }
            }
            grphcs.setColor(meterColor);
            if (maxLimit == 0) {
                maxLimit = 100;
            }
            float myTokens = tokens * 100 / maxLimit;
            int colorWidth = (int) ((myTokens * meterWidth) / 100);
            grphcs.fillRect(startMeterX, startMeterY, colorWidth, meterHeight);
            startMeterX = startMeterX + colorWidth;
//            grphcs.setColor(Color.BLACK);
//              grphcs.fillRect(startMeterX - 1, startMeterY, 1, meterHeight);
            grphcs.setColor(Color.BLUE);
            int temp = 80 * meterWidth;
            temp = temp / 100;
            grphcs.fillRect(temp + thresholdOffset, startMeterY, 2, meterHeight);
        } catch (IOException ex) {
            Logger.getLogger(TokenStudentPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void updateStudentTokens(TokenResult tokenResult) {
        this.tokenDetails = tokenResult;
        paintIcon(component, graphics, startTextX, startTextY);
        component.repaint();
        PaintEvent e = new PaintEvent(component, PaintEvent.UPDATE,
                new Rectangle(component.getX(), component.getY(), component.getWidth(),
                component.getHeight()));
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
    }

    void updateTokens(Collection<Result> results, int maxLimit, boolean calcTotal) {
        totalTokens = 0;
        this.maxLimit = maxLimit;
        if (calcTotal) {
            calculateTotalTokens(results);
        }

        for (Result result : results) {
            updateStudentTokens((TokenResult) result.getDetails());            
        }
    }

    synchronized void resetImage() {
        graphics.drawImage(image, 0, 0, component);
        startMeterX = 18 * iWidth / 100;
        try {
            if (manager.getCurrentRole() == Role.STUDENT) {
                //meterColor = ColorManager.getInstance().getColorFor(manager.getCurrentInstance().getCohortId(), userName);
            } else {
                meterColor = Color.decode("#FFFFFF");
            }
        } catch (IOException ex) {
            Logger.getLogger(TokenStudentPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void calculateTotalTokens(Collection<Result> results) {
        for (Result result : results) {
            totalTokens += ((TokenResult) result.getDetails()).getStudentResult().getTokensValue();
        }
    }
}
