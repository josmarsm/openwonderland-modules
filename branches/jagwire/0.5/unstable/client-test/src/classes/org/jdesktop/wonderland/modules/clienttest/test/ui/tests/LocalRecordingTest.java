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
import org.jdesktop.wonderland.client.softphone.MicrophoneInfoListener;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.modules.clienttest.test.ui.AudioUtils;

/**
 *
 * @author jkaplan
 */
public class LocalRecordingTest extends BaseAudioTest
    implements MicrophoneInfoListener, ActionListener
{
    private static final Logger LOGGER =
            Logger.getLogger(LocalRecordingTest.class.getName());
    
    public static final String ID = LocalRecordingTest.class.getSimpleName();
    public static final String FILE_PROP = "RecordFile";
    
    private SwingWorker recordWorker;
    
    @Override
    public String getId() {
        return ID;
    }
        
    @Override
    protected void setup() throws IOException {
        getProperties().remove(FILE_PROP);
        
        AudioUtils.INSTANCE.startSoftphone();
        
        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
        if (!sc.isRunning()) {
            throw new IOException("Softphone not running");
        }
        
        // set volume to initialize microphone
        sc.addMicrophoneInfoListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JButton startButton = AudioUtils.getFrame().getStartButton();
                startButton.setText(getBundle().getString("Start_Recording"));
                startButton.setActionCommand("start");
                startButton.addActionListener(LocalRecordingTest.this);
            
                AudioUtils.getFrame().setAnswersEnabled(false);
            }            
        });
    }

    @Override
    protected void cleanup() {
        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
        try {
            sc.sendCommandToSoftphone("stopReadingMic");
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error reading mic", ioe);
        }
        sc.removeMicrophoneInfoListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JButton startButton = AudioUtils.getFrame().getStartButton();
                startButton.removeActionListener(LocalRecordingTest.this);
            }            
        });
        
        stopWorker();
    }

    public void microphoneVuMeterValue(String value) {
        float fVal = Float.parseFloat(value);
        float rounded = Math.round(Math.sqrt(fVal) * 100) / 100f;
        
        AudioUtils.getFrame().setVolume(rounded);
    }

    public void microphoneVolume(String volume) {
    }

    public void actionPerformed(ActionEvent e) {
        JButton startButton = AudioUtils.getFrame().getStartButton();
        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
        
        if (e.getActionCommand().equals("start")) {
            startButton.setText(getBundle().getString("Stop_Recording"));
            startButton.setActionCommand("stop");
            
            try {
                sc.sendCommandToSoftphone("startReadingMic");
                sc.startMicVuMeter(true);

                File tmpFile = File.createTempFile("audio", ".au");
                tmpFile.delete();
                
                LOGGER.log(Level.INFO, "Recording to " + tmpFile.getPath());
                
                sc.recordAudio(tmpFile.getPath(), true);
            
                getProperties().put(FILE_PROP, tmpFile);
                startWorker();
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error starting recording", ioe);
            }
        } else {
            startButton.setText(getBundle().getString("Start_Over"));
            startButton.setActionCommand("start");
        
            try {
                sc.stopRecordingAudio();
                sc.sendCommandToSoftphone("stopReadingMic");
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error stopping recording", ioe);
            }
            
            AudioUtils.getFrame().setAnswersEnabled(true);
            stopWorker();
        }
    }
    
    private void startWorker() {
        if (recordWorker != null) {
            recordWorker.cancel(true);
        }
        
        recordWorker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ie) {
                    // ignore
                }
                
                return null;
            }

            @Override
            protected void done() {
                if (!isCancelled()) {
                    AudioUtils.getFrame().getStartButton().doClick();
                }   
            }
        };
        recordWorker.execute();
    }
    
    private void stopWorker() {
        if (recordWorker != null) {
            recordWorker.cancel(true);
            recordWorker = null;
        }
    }
}
