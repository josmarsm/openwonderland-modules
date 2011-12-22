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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.softphone.MicrophoneInfoListener;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.modules.clienttest.test.ui.AudioUtils;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestManager;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestsSection;

/**
 *
 * @author jkaplan
 */
public class RemoteAudioTest extends BaseAudioTest
    implements MicrophoneInfoListener, ActionListener
{
    private static final Logger LOGGER =
            Logger.getLogger(RemoteAudioTest.class.getName());
             
    public static final String ID = RemoteAudioTest.class.getSimpleName();
    
    @Override
    public String getId() {
        return ID;
    }
    
    @Override
    protected void setup() throws IOException {        
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
                startButton.setText(getBundle().getString("Connect"));
                startButton.setActionCommand("start");
                startButton.addActionListener(RemoteAudioTest.this);
            
                AudioUtils.getFrame().setAnswersEnabled(false);
            }            
        });
    }

    @Override
    protected void cleanup() {
        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
        
        if (needsCleanup()) {
            try {
                sc.sendCommandToSoftphone("endCalls");
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error reading mic", ioe);
            }
        }
        
        sc.removeMicrophoneInfoListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JButton startButton = AudioUtils.getFrame().getStartButton();
                startButton.removeActionListener(RemoteAudioTest.this);
            }            
        });
    }

    @Override
    protected void hideWindow() {
        if (needsCleanup()) {
            super.hideWindow();
        }
    }
    
    protected boolean needsCleanup() {
        // XXX assume the next test is the RemoteEchoTest!
        TestsSection section = (TestsSection) TestManager.INSTANCE.getCurrentSection();
        if (section.getMode() == TestsSection.TestMode.SINGLE) {
            return true;
        }
        
        return false;
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
            AudioUtils.getFrame().setAnswersEnabled(true);
            
            try {
                String registrar = AudioUtils.INSTANCE.getRegistrar();
                String callAddress = "echo@" + registrar;
                
                sc.sendCommandToSoftphone("PlaceCall=echo,test," + callAddress);
                sc.startMicVuMeter(true);
            
                startButton.setText(getBundle().getString("Disconnect"));
                startButton.setActionCommand("stop");
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error starting recording", ioe);
            }
        } else {
            startButton.setText(getBundle().getString("Connect"));
            startButton.setActionCommand("start");
        
            try {
                sc.sendCommandToSoftphone("endCalls");
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error stopping recording", ioe);
            }            
        }
    }
}
