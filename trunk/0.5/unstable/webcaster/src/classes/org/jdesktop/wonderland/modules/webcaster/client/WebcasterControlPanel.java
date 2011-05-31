/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.webcaster.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Control panel for Webcaster. Provides button to start & stop webcasting,
 * and function to set the name of the stream.
 * @author Christian O'Connell
 * @author Bernard Horan
 */
public class WebcasterControlPanel extends javax.swing.JPanel {

    private final WebcasterCell cell;

    /** Creates new form WebcasterControlPanel
     * @param cell the movie recorder cell controlled by this panel
     */
    public WebcasterControlPanel(WebcasterCell cell) {
        this.cell = cell;
        initComponents();
        dirField.setText("wonderland");
        previewPanel.setLayout(new BorderLayout());
        previewPanel.add(cell.getCaptureComponent(), BorderLayout.CENTER);
        setRemoteWebcasting(cell.isRemoteWebcasting());
    }

    public String getStreamName(){
        return dirField.getText();
    }

    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        previewPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        streamBox = new javax.swing.JLabel();
        dirField = new javax.swing.JTextField();
        recordButton = new javax.swing.JToggleButton();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        previewPanel.setMaximumSize(new java.awt.Dimension(640, 360));
        previewPanel.setMinimumSize(new java.awt.Dimension(640, 360));
        previewPanel.setPreferredSize(new java.awt.Dimension(640, 360));
        previewPanel.setLayout(new java.awt.GridBagLayout());
        add(previewPanel);

        jPanel1.setFocusable(false);
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        streamBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        streamBox.setText("Stream Name:");
        streamBox.setAlignmentX(0.5f);
        streamBox.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jPanel1.add(streamBox);

        dirField.setText("wonderland"); // NOI18N
        dirField.setMaximumSize(new Dimension(Integer.MAX_VALUE, dirField.getPreferredSize().height) );
        jPanel1.add(dirField);

        recordButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/webcaster/client/resources/icon.jpg"))); // NOI18N
        recordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recordButtonActionPerformed(evt);
            }
        });
        jPanel1.add(recordButton);

        add(jPanel1);
    }// </editor-fold>//GEN-END:initComponents

    
    private void record(boolean state)
    {
        //recordButton.setText(!cell.getRecording()?"Stop Capture":"Begin Capture");
        cell.setRecording(state);
        streamBox.setEnabled(!cell.getRecording());
        dirField.setEnabled(!cell.getRecording());
    }

    private void recordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordButtonActionPerformed
        record(recordButton.isSelected());
    }//GEN-LAST:event_recordButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField dirField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JToggleButton recordButton;
    private javax.swing.JLabel streamBox;
    // End of variables declaration//GEN-END:variables

    void setRemoteWebcasting(boolean b) {
        //Logger.getLogger(WebcasterControlPanel.class.getSimpleName()).warning("remoteWebcasting: " + b);
        dirField.setEnabled(!b);
        recordButton.setEnabled(!b);
        streamBox.setEnabled(!b);
    }

    void updateWebcasting() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                recordButton.setSelected(cell.getRecording());
                streamBox.setEnabled(!cell.getRecording());
                dirField.setEnabled(!cell.getRecording());
            }
        });
        
    }
    
}
