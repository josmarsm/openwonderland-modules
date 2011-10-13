/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.videoplayer.client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author nsimpson
 */
public class VideoMeter extends javax.swing.JPanel implements TimedEventSource, TimeListener {

    private static final int LEFT_MARGIN_DEFAULT = 10;
    private static final int RIGHT_MARGIN_DEFAULT = 10;
    private static final int TOP_MARGIN_DEFAULT = 10;
    private static final int BOTTOM_MARGIN_DEFAULT = 10;
    private static final int LINE_THICKNESS_DEFAULT = 10;
    private static final int PUCK_DIAMETER_DEFAULT = 10;
    private static final int LABEL_GAP_DEFAULT = 10;
    private static final double TIMELINE_DURATION_DEFAULT = 60;
    private static final Color BACKGROUND_COLOR_DEFAULT = Color.BLACK;
    private static final Color ELAPSED_COLOR_DEFAULT = Color.DARK_GRAY;
    private static final Color UNELAPSED_COLOR_DEFAULT = Color.LIGHT_GRAY;
    private static final Color PUCK_COLOR_DEFAULT = Color.BLUE;
    
    private static final Color ELAPSED_COLOR_DISABLED = Color.GRAY;
    private static final Color UNELAPSED_COLOR_DISABLED = Color.LIGHT_GRAY;
    private static final Color PUCK_COLOR_DISABLED = Color.LIGHT_GRAY;
    
    private static final String DEFAULT_FONT = "SansSerif";
    private static final int DEFAULT_FONT_SIZE = 12;
    private Stroke timelineStroke;
    private int leftMargin = LEFT_MARGIN_DEFAULT;
    private int rightMargin = RIGHT_MARGIN_DEFAULT;
    private int topMargin = TOP_MARGIN_DEFAULT;
    private int bottomMargin = BOTTOM_MARGIN_DEFAULT;
    private int lineThickness = LINE_THICKNESS_DEFAULT;
    private int puckDiameter = PUCK_DIAMETER_DEFAULT;
    private int labelGap = LABEL_GAP_DEFAULT;
    private Color backgroundColor = BACKGROUND_COLOR_DEFAULT;
    private Color elapsedColor = ELAPSED_COLOR_DEFAULT;
    private Color unelapsedColor = UNELAPSED_COLOR_DEFAULT;
    private Color puckColor = PUCK_COLOR_DEFAULT;
    private String fontName = DEFAULT_FONT;
    private int fontSize = DEFAULT_FONT_SIZE;
    private Font font;
    private FontMetrics fontMetrics;
    double timelineDuration = TIMELINE_DURATION_DEFAULT;
    double currentTime = 0.0d;
    private int timeLabelWidth;
    private int timeLabelHeight;
    private Point puckPosition = new Point();
    private Dimension labelBounds;
    
    private boolean draggable = false;
    private double dragTime;

    private List timeListeners;

    public VideoMeter() {
        initComponents();
        font = new Font(fontName, Font.BOLD, fontSize);
        timelineStroke = new BasicStroke(lineThickness, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND);
        setDuration(timelineDuration);
        setTime(0.0d);
    }

    /**
     * Add a listener for time changes
     *
     * @param listener a time listener to be notified of time changes
     */
    public void addTimeListener(TimeListener listener) {
        if (timeListeners == null) {
            timeListeners = Collections.synchronizedList(new LinkedList());
        }
        timeListeners.add(listener);
    }

    /**
     * Remove a listener for time changes
     * 
     * @param listener a time listener to be removed
     */
    public void removeTimeListener(TimeListener listener) {
        if (timeListeners != null) {
            timeListeners.remove(listener);
        }
    }

    /**
     * Notify all the time listeners of a time change
     *
     * @param newTime the time of the event
     */
    private void notifyTimeListeners(double newTime) {
        if (timeListeners != null) {
            ListIterator<TimeListener> iter = timeListeners.listIterator();
            while (iter.hasNext()) {
                TimeListener listener = iter.next();
                listener.timeChanged(newTime);
            }
        }
    }

    /**
     * Set the margins around the timeline
     * @param leftMargin the margin between the left of the panel and the time counter
     * @param rightMargin the margin between the right of the timeline and the panel
     * @param topMargin the margin between the top of the timeline and the panel
     * @param bottomMargin the margin between the bottom of the timeline and the panel
     */
    public void setMargins(int leftMargin, int rightMargin, int topMargin, int bottomMargin) {
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
        this.topMargin = topMargin;
        this.bottomMargin = bottomMargin;
    }

    /**
     * Test the video meter by cycling from 0.0s to the timeline duration
     */
    public void testVideoMeter() {
        this.addTimeListener(this);
        new Thread(new Runnable() {

            public void run() {
                for (double time = 0.0d; time <= timelineDuration; time += timelineDuration / 120.0d) {
                    setTime(time);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();
    }

    /**
     * Test time formatting
     */
    public void testTimeAsString() {
        // 0
        System.err.println("0: " + getTimeAsString(0));
        // 0.05 seconds
        System.err.println("0.05: " + getTimeAsString(0.05));
        // 0.12 seconds
        System.err.println("0.12: " + getTimeAsString(0.12));
        // 4 seconds
        System.err.println("4 seconds: " + getTimeAsString(4));
        // 12 seconds
        System.err.println("12 seconds: " + getTimeAsString(12));
        // 2 minutes 4 seconds
        System.err.println("2 minutes 4 seconds: " + getTimeAsString(2 * 60 + 4));
        // 12 minutes
        System.err.println("12 minutes: " + getTimeAsString(12 * 60));
        // 12 hours
        System.err.println("12 hours: " + getTimeAsString(12 * 60 * 60));
        // 2 hours, 33 minutes, 59 seconds and 0.12 seconds
        System.err.println("2 hours, 3 minutes, 9 seconds and 0.05 seconds: " + getTimeAsString(2 * 3600 + 3 * 60 + 9 + 0.05));
    }

    /**
     * Set the duration of the timeline
     *
     * @param timelineDuration the duration of the timeline in seconds
     */
    public void setDuration(double timelineDuration) {
        this.timelineDuration = timelineDuration;
        labelBounds = null; // force recalculation of elapsed time counter
        repaint();
    }

    /**
     * Get the duration of the timeline
     * @return the duration of the timeline in seconds
     */
    public double getTimelineDuration() {
        return timelineDuration;
    }

    /**
     * Set the current time
     *
     * @param currentTime the new time in seconds
     */
    public void setTime(double currentTime) {
        // normalize to timeline limits
        this.currentTime = (currentTime > timelineDuration) ? timelineDuration
                : (currentTime < 0.0d) ? 0.0d : currentTime;
        repaint();
        //notifyTimeListeners(currentTime);
    }

    /**
     * Get the current time
     *
     * @return the current time in seconds
     */
    public double getTime() {
        return currentTime;
    }

    /**
     * The time changed
     * @param time the new time in seconds
     */
    public void timeChanged(double time) {
        //logger.fine("meter time changed: " + time);
        setTime(time);
    }

    /**
     * Enable or disable this component
     * @param enabled true if the component is enabled, or false if not
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        
        if (enabled) {
            setElapsedColor(ELAPSED_COLOR_DEFAULT);
            setUnelapsedColor(UNELAPSED_COLOR_DEFAULT);
            setPuckColor(PUCK_COLOR_DEFAULT);
        } else {
            setElapsedColor(ELAPSED_COLOR_DISABLED);
            setUnelapsedColor(UNELAPSED_COLOR_DISABLED);
            setPuckColor(PUCK_COLOR_DISABLED);
        }
    }
    
    /**
     * Set the color of the puck
     *
     * @param puckColor the new puck color
     */
    public void setPuckColor(Color puckColor) {
        this.puckColor = puckColor;
        repaint();
    }

    /**
     * Set the background color
     *
     * @param backgroundColor the new background color
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }

    /**
     * Set the color of elapsed time
     *
     * @param elapsedColor the new elapsed time color
     */
    public void setElapsedColor(Color elapsedColor) {
        this.elapsedColor = elapsedColor;
        repaint();
    }

    /**
     * Set the color of unelapsed time
     *
     * @param unelapsedColor the new unelapsed color
     */
    public void setUnelapsedColor(Color unelapsedColor) {
        this.unelapsedColor = unelapsedColor;
        repaint();
    }

    /**
     * Get the specified time as a time string
     *
     * @param time the time in seconds
     * @return the time formatted in hh:mm:ss.nn format
     */
    public String getTimeAsString(double time) {
        int hours;
        int minutes;
        int seconds;
        hours = (int) time / 3600;
        minutes = ((int) time - hours * 3600) / 60;
        seconds = (int) time - (hours * 3600 + minutes * 60);
        int fractions = (int) ((time - (int) time) * 100);
        // time strings display only significant digits:
        // hh:mm:ss.nn
        // 00:00:00.21 -> 0.21
        // 00:00:03.21 -> 3.21
        // 00:00:43.21 -> 43.21
        // 00:05:43.21 -> 5:43.21
        // 00:55:43.21 -> 55.43.21
        // 06:04:03.21 -> 6:04:03.21
        // 06:55:43.21 -> 6:55:32.21
        // 16:55:43.21 -> 16:55:43.21

        String timeString = (hours > 0) ? String.valueOf(hours) + ":" : "";
        timeString += (minutes > 0 || hours > 0) ? (((minutes < 10 && hours > 0) ? "0" : "") + String.valueOf(minutes) + ":") : "";
        timeString += (seconds < 10 && (minutes > 0 || hours > 0) ? "0" : "") + String.valueOf(seconds) + ".";
        timeString += ((fractions < 10) ? "0" : "") + String.valueOf(fractions);

        return timeString;
    }

    /**
     * Get the width of the timeline
     *
     * @return the width of the timeline in seconds
     */
    private int getTimelineWidth() {
        return getWidth() - leftMargin - getMaxLabelBounds().width - labelGap - rightMargin;
    }

    /**
     * Get the current position of the puck
     *
     * @param point the position of the puck in pixels
     * @return the position of the puck in pixels
     */
    private Point getPuckPosition(Point point) {
        return getPuckPosition(point, currentTime);
    }

    /**
     * Get the position of the puck for the given time
     */
    private Point getPuckPosition(Point point, double time) {
        point.x = leftMargin + labelBounds.width + labelGap + (int) ((time / timelineDuration) * getTimelineWidth());
        point.y = topMargin + lineThickness / 2 - puckDiameter / 2;

        return point;
    }

    /**
     * Get the specified x position as a time
     *
     * @param x a position in the timeline panel in pixels
     * @return the corresponding time in seconds
     */
    private double getPositionAsTime(int x) {
        int leftX = leftMargin + labelBounds.width + labelGap;
        int rightX = getWidth() - rightMargin;
        x = (x < leftX) ? leftX : ((x > rightX) ? rightX : x);

        return ((double) (x - leftX) / (double) (rightX - leftX)) * timelineDuration;
    }

    /**
     * Get the maximum bounds of the time counter
     *
     * @return the maximum bounds of the time counter
     */
    private Dimension getMaxLabelBounds() {
        // possible labels:
        //    < 60s:       ss.n
        // < 60*60s:    mm:ss.n
        // > 60*60s: hh:mm:ss.n
        String maxTimeString = getTimeAsString(timelineDuration);

        // calculate the sizes of the label and value strings
        timeLabelWidth = fontMetrics.stringWidth(maxTimeString);
        timeLabelHeight = fontMetrics.getHeight();

        return new Dimension(timeLabelWidth, timeLabelHeight);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Stroke defaultStroke = g2.getStroke();

        if (fontMetrics == null) {
            fontMetrics = g2.getFontMetrics(font);
        }
        if (labelBounds == null) {
            labelBounds = getMaxLabelBounds();
        }
        
        double time = currentTime;
        if (draggable) {
            time = dragTime;
        }
        
        getPuckPosition(puckPosition, time);

        // set rendering quality
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // fill background
        g2.setColor(backgroundColor);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // draw timeline
        g2.setColor(unelapsedColor);
        g2.setStroke(timelineStroke);
        g2.drawLine(leftMargin + labelBounds.width + labelGap, topMargin + lineThickness / 2, getWidth() - rightMargin, topMargin + lineThickness / 2);

        // draw elapsed portion of timeline
        g2.setColor(elapsedColor);
        g2.drawLine(leftMargin + labelBounds.width + labelGap, topMargin + lineThickness / 2, puckPosition.x, topMargin + lineThickness / 2);

        // draw time puck
        g2.setStroke(defaultStroke);
        g2.setColor(puckColor);
        g2.fillOval(puckPosition.x - puckDiameter / 2, puckPosition.y, puckDiameter, puckDiameter);

        // draw the value
        g2.setColor(Color.WHITE);
        g2.setFont(font);
        g2.drawString(getTimeAsString(time), leftMargin, topMargin + lineThickness / 2 + labelBounds.height / 3);

        g2.setStroke(defaultStroke);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setName("Form"); // NOI18N
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        if (!isEnabled()) {
            return;
        }
        
        int x = evt.getX();
        int y = evt.getY();
        Point puckXY = new Point();
        getPuckPosition(puckXY);
        if (((x >= puckXY.x - puckDiameter / 2) && (x <= puckXY.x + puckDiameter / 2))
                && ((y >= puckXY.y) && (y <= puckXY.y + puckDiameter))) {
            // the mouse was pressed on the puck, allow the user to drag the
            // puck directly
            draggable = true;
            dragTime = getPositionAsTime(evt.getX());
        } else {
            // the mouse was not pressed on the puck, move the puck to the
            // pressed position
            notifyTimeListeners(getPositionAsTime(evt.getX()));
        }
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        if (draggable) {
            notifyTimeListeners(dragTime);
        }

        draggable = false;
    }//GEN-LAST:event_formMouseReleased

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        if (!isEnabled()) {
            return;
        }
        
        dragTime = getPositionAsTime(evt.getX());

        // don't update until drag is finished
        //if (draggable) {
        //    notifyTimeListeners(getPositionAsTime(evt.getX()));
        //}
    }//GEN-LAST:event_formMouseDragged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
