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
package org.jdesktop.wonderland.modules.instructortools.client.presenters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.logging.Logger;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.audiomanager.common.VolumeConverter;
import org.jdesktop.wonderland.modules.instructortools.client.InstructorClientConnection;
import org.jdesktop.wonderland.modules.instructortools.client.views.AdjustAudioView;

/**
 *
 * @author Ryan
 */
public class AdjustAudioPresenter {
    
    private HUDComponent hudComponent = null;
    private AdjustAudioView view = null;
    private BigInteger id = null;
    private VolumeConverter volumeConverter;
    
    private boolean speakerIsInitialized = false;
    private boolean microphoneIsInitialized = false;
    
    private static final Logger logger = Logger.getLogger(AdjustAudioPresenter.class.getName());
    public AdjustAudioPresenter(BigInteger id, AdjustAudioView view, HUDComponent hc) {
        this.id = id;
        this.hudComponent = hc;
        this.view = view;
        
        addListeners();
        volumeConverter = new VolumeConverter(view.getMaximumMicrophoneValue());
        
    }
    
    public void setMicrophoneSliderValue(int value) {
        
//        view.setMicrophoneSliderValue(volumeConverter.getVolume(value));
        view.setMicrophoneSliderValue(value);
    }
    
    public void setSpeakerSliderValue(int value) {
//        view.setSpeakerSliderValue(volumeConverter.getVolume(value));
        view.setSpeakerSliderValue(value);
    }
    
    
    public void setViewTitle(String title) {
      
                
    }
    
    public void setVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() { 
            public void run() {
                hudComponent.setVisible(visible);
            }
        });
    }
    
    public void handleCloseButtonPressed(ActionEvent e) {
        setVisible(false);
    }
    
    public void handleMicrophoneSliderChanged(ChangeEvent e) {
        if(!microphoneIsInitialized) {
            microphoneIsInitialized = true;
            return;
        }
        
        JSlider source = (JSlider)e.getSource();
        
        if (!source.getValueIsAdjusting()) { //only process the event if the user has released the mouse.
            logger.warning("MICROPHONE SLIDER CHANGED!");
            InstructorClientConnection client = InstructorClientConnection.getInstance();

            
            logger.warning("SENDING NEW MICROPHONE VOLUME: "+new Float(volumeConverter.getVolume(view.getMicrophoneSliderValue()))+" "
                    + "FROM: "+view.getMicrophoneSliderValue());
            logger.warning("SENDING NEW SPEAKER VOLUME: "+new Float(volumeConverter.getVolume(view.getSpeakerSliderValue()))+" "
                    + "FROM: "+view.getSpeakerSliderValue());
            
            float microphoneVolume = volumeConverter.getVolume(view.getMicrophoneSliderValue());//new Float(volumeConverter.getVolume(view.getMicrophoneSliderValue())).floatValue();
            float speakerVolume  = volumeConverter.getVolume(view.getSpeakerSliderValue());//new Float(volumeConverter.getVolume(view.getSpeakerSliderValue())).intValue();
            client.sendAudioChangeMessage(id, microphoneVolume, speakerVolume);
        }
    }
    
    public void handleVolumeSliderChanged(ChangeEvent e) {
        if(!speakerIsInitialized) {
            speakerIsInitialized = true;
            return;
            
        }
        
        
        JSlider source = (JSlider)e.getSource();
        
        if (!source.getValueIsAdjusting()) { //only process the event if the user has released the mouse.
            logger.warning("VOLUME SLIDER CHANGED!");
            InstructorClientConnection client = InstructorClientConnection.getInstance();

            
            logger.warning("SENDING NEW MICROPHONE VOLUME: "+new Float(volumeConverter.getVolume(view.getMicrophoneSliderValue()))+" "
                    + "FROM: "+view.getMicrophoneSliderValue());
            logger.warning("SENDING NEW SPEAKER VOLUME: "+new Float(volumeConverter.getVolume(view.getSpeakerSliderValue()))+" "
                    + "FROM: "+view.getSpeakerSliderValue());
            
            float microphoneVolume = volumeConverter.getVolume(view.getMicrophoneSliderValue());
            float speakerVolume = volumeConverter.getVolume(view.getSpeakerSliderValue());
            client.sendAudioChangeMessage(id, microphoneVolume, speakerVolume);
        }
    }
    
    protected void addListeners() {
        view.addCloseButtonActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent ae) {
                handleCloseButtonPressed(ae);
            }
        });
        
        view.addMicrophoneSliderChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent ce) {
                handleMicrophoneSliderChanged(ce);
            }
        
        });
        
        view.addVolumeSliderChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ce) {
                handleVolumeSliderChanged(ce);
                            
            }
        });
    }
    
}

