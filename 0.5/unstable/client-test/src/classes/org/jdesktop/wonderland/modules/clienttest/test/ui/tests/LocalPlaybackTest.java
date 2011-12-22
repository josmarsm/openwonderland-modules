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
package org.jdesktop.wonderland.modules.clienttest.test.ui.tests;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.client.softphone.SpeakerInfoListener;
import org.jdesktop.wonderland.modules.clienttest.test.ui.AudioUtils;
import org.jdesktop.wonderland.modules.clienttest.test.ui.Test;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestManager;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestResult;

/**
 *
 * @author jkaplan
 */
public class LocalPlaybackTest extends BaseAudioTest
    implements SpeakerInfoListener, ActionListener
{
    private static final Logger LOGGER =
            Logger.getLogger(LocalPlaybackTest.class.getName());
    
    public static final String ID = LocalPlaybackTest.class.getSimpleName();
    
    private SwingWorker recordWorker;
    
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isRunnable() {
        Test rec = TestManager.INSTANCE.getTest(LocalRecordingTest.ID);
        if (rec == null) {
            LOGGER.log(Level.WARNING, "Unable to find recording test");
            return false;
        }
        
        if (rec.getResult() != TestResult.PASS) {
            LOGGER.log(Level.WARNING, "Unable to run playback test: recording " +
                                      "failed");
            return false;
        }
        
        File recordFile = (File) rec.getProperties().get(LocalRecordingTest.FILE_PROP);
        if (recordFile == null || !recordFile.exists()) {
            LOGGER.log(Level.WARNING, "Unable to find recording");
            return false;
        }
        
        return true;
    }
    
    @Override
    protected void setup() throws IOException {
        AudioUtils.INSTANCE.startSoftphone();
        
        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
        if (!sc.isRunning()) {
            throw new IOException("Softphone not running");
        }
        
        // set volume to initialize microphone
        sc.addSpeakerInfoListener(this);
        sc.startSpeakerVuMeter(true);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JButton startButton = AudioUtils.getFrame().getStartButton();
                startButton.setText(getBundle().getString("Start_Playback"));
                startButton.setActionCommand("start");
                startButton.addActionListener(LocalPlaybackTest.this);
            
                AudioUtils.getFrame().setAnswersEnabled(false);
            }            
        });
    }

    @Override
    protected void cleanup() {
        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
        
        try {
            sc.sendCommandToSoftphone("stopPlayingFile");
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error stopping file", ioe);
        }
        
        sc.removeSpeakerInfoListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JButton startButton = AudioUtils.getFrame().getStartButton();
                startButton.removeActionListener(LocalPlaybackTest.this);
            }            
        });        
    }

    public void speakerVuMeterValue(String value) {
        float fVal = Float.parseFloat(value);
        float rounded = Math.round(Math.sqrt(fVal) * 100) / 100f;
        
        AudioUtils.getFrame().setVolume(rounded);
    }

    public void speakerVolume(String volume) {
    }
    
    public void actionPerformed(ActionEvent e) {
        JButton startButton = AudioUtils.getFrame().getStartButton();
        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
        
        startButton.setText(getBundle().getString("Restart_Playback"));
            
        try {
            // stop any existing playback
            sc.sendCommandToSoftphone("stopPlayingFile");
            
            Test rec = TestManager.INSTANCE.getTest(LocalRecordingTest.ID); 
            File recordFile = (File) rec.getProperties().get(LocalRecordingTest.FILE_PROP);
                
            LOGGER.log(Level.INFO, "Play from " + recordFile.getPath());
                
            sc.sendCommandToSoftphone("playFile=" + recordFile.getPath() + "=1.0");
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error starting recording", ioe);
        }
            
        AudioUtils.getFrame().setAnswersEnabled(true);
    }
}
