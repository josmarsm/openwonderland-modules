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
import java.awt.Toolkit;
import java.awt.event.PaintEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jdesktop.wonderland.modules.isocial.common.model.ResultDetails;
import org.jdesktop.wonderland.modules.isocial.tokensheet.common.TokenResult;

/**
 * Creates the Passes and strikes panel for token and behavior system. The panel
 * contains a label with an image to show the student's current passes and 
 * strikes.
 *
 * @author Kaustubh
 */
public class PassStrikeStudentPanel extends ImageIcon {

    private ImageIcon imageIcon;
    private TokenResult details;
    private Image image;
    private Graphics2D graphics;
    private final int iHeight;
    private final int iWidth;
    private final Color passColor = new Color(71, 164, 173);
    String[] strikeValues = new String[]{"0", "W", "1", "2", "3"};
    private final Color strikeColor = new Color(183, 40, 47);
    private CustomDimension[] passD, strikeD;

    PassStrikeStudentPanel(String url) {
        super(url);
        image = Toolkit.getDefaultToolkit().createImage(getClass().getResource(url));
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(PassStrikeStudentPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        while (tracker.statusAll(true) != MediaTracker.COMPLETE) {
        }

        BufferedImage bfImage = new BufferedImage(image.getWidth(component),
                image.getHeight(component), BufferedImage.TYPE_4BYTE_ABGR);
        graphics = bfImage.createGraphics();
        graphics.drawImage(image, 0, 0, component);
        this.imageIcon = new ImageIcon(bfImage);
        iWidth = image.getWidth(component);
        iHeight = image.getHeight(component);
        passD = new CustomDimension[3];
        strikeD = new CustomDimension[4];
        passD[0] = new CustomDimension(57, 8, 14, 14);
        passD[1] = new CustomDimension(62, 31, 14, 14);
        passD[2] = new CustomDimension(53, 51, 14, 14);

        strikeD[0] = new CustomDimension(92, 1, 14, 14);
        strikeD[1] = new CustomDimension(144, 3, 14, 14);
        strikeD[2] = new CustomDimension(151, 24, 14, 14);
        strikeD[3] = new CustomDimension(144, 45, 14, 14);
    }

    public ImageIcon getImageIcon() {
        return this.imageIcon;
    }

    @Override
    public synchronized void paintIcon(Component cmpnt, Graphics grphcs, int x, int y) {
        grphcs.setColor(Color.BLACK);
        grphcs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        grphcs.drawString(String.valueOf(details.getStudentResult().getPassesValue()), x, y);
        grphcs.drawString(String.valueOf(strikeValues[details.getStudentResult().getStrikesValue()]), x + 90, y);
        int passesValue = details.getStudentResult().getPassesValue();
        int strikesValue = details.getStudentResult().getStrikesValue();

        //paint pass value
        for (int i = 0; i < passD.length; i++) {
            CustomDimension customDimension = passD[i];
            int x1 = customDimension.getX();
            int y1 = customDimension.getY();
            if (i < passesValue) {
                grphcs.setColor(Color.BLACK);
                grphcs.drawOval(x1, y1, 14, 14);
                grphcs.setColor(passColor);
                grphcs.fillOval(x1, y1, 14, 14);
            }
        }

        //paint strike value
        for (int i = 0; i < strikeD.length; i++) {
            CustomDimension customDimension = strikeD[i];
            int x1 = customDimension.getX();
            int y1 = customDimension.getY();
            if (i < strikesValue) {
                grphcs.setColor(Color.BLACK);
                grphcs.drawOval(x1, y1, 14, 14);
                grphcs.setColor(strikeColor);
                grphcs.fillOval(x1, y1, 14, 14);
            }
        }
    }

    /**
     * Updates the images upon student getting the pass or strikes from guide
     */
    synchronized void updateStudentStrikesPasses(TokenResult tokenResult) {
        this.details = tokenResult;
        paintIcon(component, graphics, 25, 45);
        component.repaint();
        PaintEvent e = new PaintEvent(component, PaintEvent.UPDATE,
                new Rectangle(component.getX(), component.getY(), component.getWidth(),
                component.getHeight()));
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
    }

    /**
     * Resets the image to initial state before updating any passes and strikes
     * to it.
     */
    synchronized void resetImage() {
        graphics.drawImage(image, 0, 0, component);
    }
}
