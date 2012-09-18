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

/**
 *
 * @author jkaplan
 */
public class RemoteEchoTest extends BaseAudioTest
    implements MicrophoneInfoListener, ActionListener
{
    private static final Logger LOGGER =
            Logger.getLogger(RemoteEchoTest.class.getName());
             
    public static final String ID = RemoteEchoTest.class.getSimpleName();
    
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
        
        boolean connected = false;
        try {
            connected = sc.isConnected();
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error checking softphone", ioe);
        }
        final boolean fConnected = connected;
        
        // set volume to initialize microphone
        sc.addMicrophoneInfoListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                AudioUtils.getFrame().setAnswersReversed(true);
                
                JButton startButton = AudioUtils.getFrame().getStartButton();
  
                if (fConnected) {
                    startButton.setText(getBundle().getString("Disconnect"));
                    startButton.setActionCommand("stop");
                    AudioUtils.getFrame().setAnswersEnabled(true);
                } else {
                    startButton.setText(getBundle().getString("Connect"));
                    startButton.setActionCommand("start");
                    AudioUtils.getFrame().setAnswersEnabled(false);
                }
                
                startButton.addActionListener(RemoteEchoTest.this);
            }            
        });
    }

    @Override
    protected void cleanup() {
        SoftphoneControlImpl sc = SoftphoneControlImpl.getInstance();
        try {
            sc.sendCommandToSoftphone("endCalls");
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Error reading mic", ioe);
        }
        sc.removeMicrophoneInfoListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JButton startButton = AudioUtils.getFrame().getStartButton();
                startButton.removeActionListener(RemoteEchoTest.this);
            }            
        });
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
