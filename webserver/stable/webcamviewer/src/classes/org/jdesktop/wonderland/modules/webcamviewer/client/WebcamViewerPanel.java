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
package org.jdesktop.wonderland.modules.webcamviewer.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import com.charliemouse.cambozola.shared.ExceptionReporter;
import com.charliemouse.cambozola.shared.ImageChangeEvent;
import com.charliemouse.cambozola.shared.ImageChangeListener;
import java.awt.Image;
import java.text.NumberFormat;

/**
 * A panel for displaying webcam video.
 *
 * @author nsimpson
 */
public class WebcamViewerPanel extends JPanel implements ExceptionReporter,
        ImageChangeListener {

    private static final Logger logger = Logger.getLogger(WebcamViewerPanel.class.getName());
    private WebcamViewerWindow window;
    private Image frame;
    private NumberFormat formatter = NumberFormat.getInstance();

    public WebcamViewerPanel(WebcamViewerWindow window) {
        this.window = window;
        initComponents();
        showSource(java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcamviewer/client/resources/Bundle").getString("NOT_CONNECTED"));
        setConnected(false);
    }

    /**
     * Resize the webcam panel to the specified size
     * @param size the new size of the webcam panel
     */
    private void resizeToFit(final Dimension size) {
        if ((size.getWidth() == 0) || (size.getHeight() == 0)) {
            // don't allow the window to shrink to invisibility
            return;
        }

        if (!size.equals(new Dimension(window.getWidth(), window.getHeight()))) {
            // resize the webcam viewer window
            logger.fine("resizing to: " + size);
            window.setSize((int) size.getWidth(), (int) size.getHeight());
        }
    }

    // Cambozola ExceptionReporter methods
    public void reportError(Throwable t) {
        logger.warning("error: " + t);
    }

    public void reportFailure(String s) {
        logger.warning(s);
        setConnected(false);
    }

    public void reportNote(String s) {
        logger.info(s);
    }

    /**
     * Show the webcam URL
     * @param source the URL of the webcam
     */
    public void showSource(String source) {
        sourceValueLabel.setText(source);
    }

    /**
     * Set the connected indicator status
     * @param connected whether the webcam is connected
     */
    public void setConnected(boolean connected) {
        connectedPanel.setBackground(connected ? Color.GREEN : Color.GRAY);
    }

    /**
     * Disply the current frames per second value
     * @param fps the current frames per second
     */
    public void showFPS(double fps) {
        formatter.setMaximumFractionDigits(1);
        formatter.setMinimumFractionDigits(1);
        fpsValueLabel.setText(formatter.format(fps));
    }

    // Cambozola ImageChangeListener method
    public void imageChanged(ImageChangeEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                frame = window.getFrame();
                if (frame != null) {

                    if ((frame.getWidth(null) > 0) && (frame.getHeight(null) > 0)) {
                        if ((frame.getWidth(WebcamViewerPanel.this) != WebcamViewerPanel.this.getWidth()) ||
                                frame.getHeight(WebcamViewerPanel.this) != WebcamViewerPanel.this.getHeight()) {
                            // video frame isn't the same size as the window,
                            // resize the window to fit the video frame
                            //resizeToFit(new Dimension(frame.getWidth(WebcamViewerPanel.this),
                            //        frame.getHeight(WebcamViewerPanel.this)));
                        }
                    }
                }
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (frame != null) {
            g2.drawImage(frame, 0, 0, this);
            g2.setColor(Color.green);
        } else {
            int w = getWidth();
            int h = getHeight();

            g2.setBackground(Color.black);
            g2.fillRect(0, 0, w, h);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        sourceValueLabel = new javax.swing.JLabel();
        fpsLabel = new javax.swing.JLabel();
        fpsValueLabel = new javax.swing.JLabel();
        connectedPanel = new javax.swing.JPanel();

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));

        sourceValueLabel.setForeground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/webcamviewer/client/resources/Bundle"); // NOI18N
        sourceValueLabel.setText(bundle.getString("NOT_CONNECTED")); // NOI18N

        fpsLabel.setFont(fpsLabel.getFont().deriveFont(fpsLabel.getFont().getStyle() | java.awt.Font.BOLD));
        fpsLabel.setForeground(new java.awt.Color(255, 255, 255));
        fpsLabel.setText(bundle.getString("FPS:")); // NOI18N

        fpsValueLabel.setForeground(new java.awt.Color(255, 255, 255));
        fpsValueLabel.setText(bundle.getString("0.0")); // NOI18N

        connectedPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        org.jdesktop.layout.GroupLayout connectedPanelLayout = new org.jdesktop.layout.GroupLayout(connectedPanel);
        connectedPanel.setLayout(connectedPanelLayout);
        connectedPanelLayout.setHorizontalGroup(
            connectedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 12, Short.MAX_VALUE)
        );
        connectedPanelLayout.setVerticalGroup(
            connectedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 12, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(connectedPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sourceValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 486, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 50, Short.MAX_VALUE)
                .add(fpsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fpsValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(sourceValueLabel)
                    .add(connectedPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fpsLabel)
                    .add(fpsValueLabel))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(452, Short.MAX_VALUE)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel connectedPanel;
    private javax.swing.JLabel fpsLabel;
    private javax.swing.JLabel fpsValueLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel sourceValueLabel;
    // End of variables declaration//GEN-END:variables
}
