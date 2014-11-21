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
package org.jdesktop.wonderland.modules.isocial.timer.client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.*;
import java.util.List;
import javax.swing.JComponent;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.ResultDetails;
import org.jdesktop.wonderland.modules.isocial.timer.common.TimerResult;
import org.jdesktop.wonderland.modules.isocial.timer.common.TimerSection;

/**
 *
 * @author Kaustubh, Ryan @isocial
 */
public class TimerCanvas extends JComponent {

    final static int maxCharHeight = 15;
    final static int minFontSize = 6;
    final static Color background = Color.white;
    final static BasicStroke stroke = new BasicStroke(2.0f);
    //final static Color red = Color.red;
    //final static Color yellow = Color.yellow;
    //final static Color black = Color.black;
    private Color foreground = Color.black;
    private final int rectHeight = 200;
    private double redRectHeight = 0;
    private float[] sectionMarkers;
    private String[] sectionNames;

    public TimerCanvas(List<TimerSection> sections) {
        //For every section in the list
        int sectionIndex = 0;
        sectionMarkers = new float[sections.size()];
        sectionNames = new String[sections.size()];

        //Aggregate of all sections. i.e. 15 minutes + 15 minutes = 30 minutes
        int totalTime = 0;

        for (TimerSection section : sections) {
            totalTime += section.getSectionTime();
        }

        float multiplier = (float) rectHeight / (float) totalTime;

        for (TimerSection section : sections) {
            //set the marker and name for the given index
            sectionMarkers[sectionIndex] = ((float) section.getSectionTime() / (float) totalTime) * (float) rectHeight;
            //System.out.println("Setting Marker: " + sectionIndex + " to: " + sectionMarkers[sectionIndex]);

            sectionNames[sectionIndex] = section.getSectionName();
            sectionIndex++;
        }

    }

    public int getRectHeight() {
        return rectHeight;
    }

    public double getRedRectHeight() {
        return redRectHeight;
    }

    public void setRedRectHeight(double redRectHeight) {
        this.redRectHeight = redRectHeight;
    }

    public void setCanvasSectionMarkers(float[] sectionMarkers, String[] sectionNames) {
        this.sectionMarkers = sectionMarkers;
        this.sectionNames = sectionNames;
//        System.out.println("setCanvasSectionMarkers sectionMarkers[0] " + this.sectionMarkers[0]);
//        System.out.println("setCanvasSectionMarkers sectionMarkers[1] " + this.sectionMarkers[1]);
//        System.out.println("setCanvasSectionMarkers sectionMarkers[2] " + this.sectionMarkers[2]);
//        System.out.println("setCanvasSectionMarkers sectionMarkers[3] " + this.sectionMarkers[3]);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int gridWidth = getWidth();
        int gridHeight = getHeight();

        Color fg3D = Color.lightGray;

        // draw entire component white
//        g.setColor(Color.white);
//        g.fillRect(0, 0, gridWidth, gridHeight);

        int x = 3;
        int y = 7;
        int rectWidth = gridWidth - 3;


        // fill Rectangle2D.Double (red)
        g2.setPaint(Color.yellow);
        g2.setFont(new Font("TimesRoman", Font.PLAIN, 10));
        //System.out.println(rectHeight);
        g2.fill(new Rectangle2D.Double(x, y, rectWidth, rectHeight));
        g2.setPaint(foreground);

        g2.setPaint(Color.red);
        //System.out.println(redRectHeight);
        g2.fill(new Rectangle2D.Double(x, y, rectWidth, redRectHeight));
        g2.setPaint(foreground);

        // draw String
        g2.setPaint(Color.black);
        int currentMarker = 0;
        for (int i = 0; i < sectionMarkers.length; i++) {
            currentMarker += sectionMarkers[i];
            g2.drawString(sectionNames[i], x, currentMarker);
            g2.drawLine(x, y + currentMarker, x + rectWidth, y + currentMarker);
        }

    }

    public Dimension getPreferredSize() {
        return new Dimension(70, 210);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

}
