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

package org.jdesktop.wonderland.modules.movierecorder.client;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 * Control panel for movie recorder. Provides buttons to start & stop recording, and
 * function to set the location of recorded movie.
 * @author Bernard Horan
 */
public class MovieControlPanel extends javax.swing.JPanel {
    private static final Logger logger = Logger.getLogger(MovieControlPanel.class.getName());
    private MovieRecorderCell recorderCell;

    /** Creates new form MovieControlPanel
     * @param recorderCell the movie recorder cell controlled by this panel
     */
    public MovieControlPanel(MovieRecorderCell recorderCell) {
        this.recorderCell = recorderCell;
        initComponents();
        movieDirectoryField.setText(getDefaultMovieDirectory());
        picturesDirectoryField.setText(getDefaultStillCaptureDirectory());
        previewPanel.setLayout(new GridBagLayout());
        previewPanel.add(recorderCell.getCaptureComponent());
        if (recorderCell.isRemoteRecording()) {
            disableAllButtons();
        }
        recorderCell.getVideoButtonModel().addItemListener(new VideoButtonListener());
    }

    void setRemoteRecording(boolean b) {
        enableAllButtons(!b);
        //If the remote client is no longer recording, disable the stop button
        if (!b) {
            stopButton.setEnabled(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        controlPanel = new javax.swing.JPanel();
        recorderStatusLabel = new javax.swing.JLabel();
        recordButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        movieDirectoryField = new javax.swing.JTextField();
        movieDirectoryLabel = new javax.swing.JLabel();
        moviePathBrowseButton = new javax.swing.JButton();
        picturesLabelDirectory = new javax.swing.JLabel();
        picturesDirectoryField = new javax.swing.JTextField();
        picturesPathBrowseButton = new javax.swing.JButton();
        previewPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        recorderStatusLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        recorderStatusLabel.setText("Offline");
        recorderStatusLabel.setToolTipText("Recorder Status");

        recordButton.setText("Record");
        recordButton.setToolTipText("Click to start recording the world");
        recordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recordButtonActionPerformed(evt);
            }
        });

        stopButton.setText("Stop");
        stopButton.setToolTipText("Click to stop recording and create a movie");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        movieDirectoryField.setEditable(false);
        movieDirectoryField.setToolTipText("Directory for saving the Movie");

        movieDirectoryLabel.setText("Movie Directory:");

        moviePathBrowseButton.setText("Select...");
        moviePathBrowseButton.setToolTipText("Select Directory for Movies");
        moviePathBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moviePathBrowseButtonActionPerformed(evt);
            }
        });

        picturesLabelDirectory.setText("Pictures Directory");

        picturesDirectoryField.setEditable(false);
        picturesDirectoryField.setToolTipText("Directory for saving the Pictures");

        picturesPathBrowseButton.setText("Select...");
        picturesPathBrowseButton.setToolTipText("Select Directory for Pictures");
        picturesPathBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                picturesPathBrowseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addComponent(picturesLabelDirectory)
                        .addGap(18, 18, 18)
                        .addComponent(picturesDirectoryField))
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addComponent(movieDirectoryLabel)
                        .addGap(28, 28, 28)
                        .addComponent(movieDirectoryField, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(controlPanelLayout.createSequentialGroup()
                        .addComponent(recordButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(moviePathBrowseButton)
                    .addComponent(recorderStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(picturesPathBrowseButton))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        controlPanelLayout.setVerticalGroup(
            controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPanelLayout.createSequentialGroup()
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(picturesLabelDirectory)
                    .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(picturesDirectoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(picturesPathBrowseButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(movieDirectoryLabel)
                    .addComponent(movieDirectoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(moviePathBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(recordButton)
                    .addComponent(stopButton)
                    .addComponent(recorderStatusLabel))
                .addContainerGap())
        );

        add(controlPanel, java.awt.BorderLayout.PAGE_END);

        javax.swing.GroupLayout previewPanelLayout = new javax.swing.GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 434, Short.MAX_VALUE)
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 373, Short.MAX_VALUE)
        );

        add(previewPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void recordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordButtonActionPerformed
        recorderCell.getVideoButtonModel().setSelected(true);
        //Rest of the action takes place in the videoButtonModel listeners
        //See this class's inner class VideoButonListener for display updates
        //See MovieRecorderCell's inner class VideoButtonListener for "model" updates    
    }//GEN-LAST:event_recordButtonActionPerformed

    private void disableLocalButtons() {
        enableLocalButtons(false);
    }

    private void disableAllButtons() {
        enableAllButtons(false);
    }

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        recorderCell.getVideoButtonModel().setSelected(false);
        //Rest of the action takes place in the videoButtonModel listeners
        //See this class's inner class VideoButonListener for display updates
        //See MovieRecorderCell's inner class VideoButtonListener for "model" updates       
}//GEN-LAST:event_stopButtonActionPerformed

    

    void enableLocalButtons() {
        enableLocalButtons(true);
    }

    private void enableLocalButtons(boolean enable) {
        recordButton.setEnabled(enable);
        moviePathBrowseButton.setEnabled(enable);
        movieDirectoryField.setEnabled(enable);
        picturesPathBrowseButton.setEnabled(enable);
        picturesDirectoryField.setEnabled(enable);
    }

    private void enableAllButtons(boolean enable) {
        enableLocalButtons(enable);
        stopButton.setEnabled(enable);
    }

    /**
     * The location of the JPEGs
     * @return A File identifying a directory/folder that contains the JPEGs of the recording
     */
    public File getImageDirectory() {
        return MovieRecorderCell.getImageDirectory();
    }

    /**
     * The direcotry into which the movie should be created
     * @return The absolute path of the location of the hdirectory in which the movie should be saved
     */
    public String getMovieDirectory() {
        return movieDirectoryField.getText();
    }

    /**
     * The direcotry into which the movie should be created
     * @return The absolute path of the location of the hdirectory in which the movie should be saved
     */
    public String getPicturesDirectory() {
        return picturesDirectoryField.getText();
    }

    /**
     * The frames per second at which the JPEGs were recorded
     * @return a float representing the frames per second that the JPEGs weere recorded
     */
    public float getCapturedFrameRate() {
        return recorderCell.getCapturedFrameRate();
    }

    private String getDefaultMovieDirectory() {
        String home = System.getProperty("user.home");
        //
        //Are we on a PC?
        File myDocuments = new File(home + File.separator + "My Documents");
        if (myDocuments.exists()) {
            return myDocuments.toString();
        }
        //
        //Or a Mac?
        File movies = new File(home + File.separator + "Movies");
        if (movies.exists()) {
            return movies.toString();
        }
        //
        //Or Gnome?
        File documents = new File(home + File.separator + "Documents");
        if (documents.exists()) {
            return documents.toString();
        }
        //
        //Otherwise
        return home;
    }

    private String getDefaultStillCaptureDirectory() {
        String home = System.getProperty("user.home");
        //
        //Are we on a PC?
        File myDocuments = new File(home + File.separator + "My Documents");
        if (myDocuments.exists()) {
            return myDocuments.toString();
        }
        //
        //Or a Mac?
        File pictures = new File(home + File.separator + "Pictures");
        if (pictures.exists()) {
            return pictures.toString();
        }
        //
        //Or Gnome?
        File documents = new File(home + File.separator + "Documents");
        if (documents.exists()) {
            return documents.toString();
        }
        //
        //Otherwise
        return home;
    }

    private void moviePathBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moviePathBrowseButtonActionPerformed
        JFileChooser outputPathFileChooser = new JFileChooser();

        outputPathFileChooser.setDialogTitle("Movie Directory");
        outputPathFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        outputPathFileChooser.setAcceptAllFileFilterUsed(false);

        int outputPath = outputPathFileChooser.showOpenDialog(this);

        if (outputPath == JFileChooser.APPROVE_OPTION) {
            movieDirectoryField.setText(outputPathFileChooser.getSelectedFile().getAbsolutePath());
        }
}//GEN-LAST:event_moviePathBrowseButtonActionPerformed

    private void picturesPathBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_picturesPathBrowseButtonActionPerformed
        JFileChooser outputPathFileChooser = new JFileChooser();

        outputPathFileChooser.setDialogTitle("Pictures Directory");
        outputPathFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        outputPathFileChooser.setAcceptAllFileFilterUsed(false);

        int outputPath = outputPathFileChooser.showOpenDialog(this);

        if (outputPath == JFileChooser.APPROVE_OPTION) {
            picturesDirectoryField.setText(outputPathFileChooser.getSelectedFile().getAbsolutePath());
        }
}//GEN-LAST:event_picturesPathBrowseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlPanel;
    private javax.swing.JTextField movieDirectoryField;
    private javax.swing.JLabel movieDirectoryLabel;
    private javax.swing.JButton moviePathBrowseButton;
    private javax.swing.JTextField picturesDirectoryField;
    private javax.swing.JLabel picturesLabelDirectory;
    private javax.swing.JButton picturesPathBrowseButton;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JButton recordButton;
    private javax.swing.JLabel recorderStatusLabel;
    private javax.swing.JButton stopButton;
    // End of variables declaration//GEN-END:variables

    class VideoButtonListener implements ItemListener {

        public void itemStateChanged(ItemEvent event) {
            //update the control panel
            logger.info("event: " + event);
            if (event.getStateChange() == ItemEvent.SELECTED) {
                recorderStatusLabel.setText("Recording");
                recorderStatusLabel.setForeground(Color.red);
                stopButton.setEnabled(true);
                disableLocalButtons();
            } else {
                stopButton.setEnabled(false);
                recorderCell.stopRecording();
                recorderStatusLabel.setText("Offline");
                recorderStatusLabel.setForeground(Color.BLACK);
            }
        }
    }
}
