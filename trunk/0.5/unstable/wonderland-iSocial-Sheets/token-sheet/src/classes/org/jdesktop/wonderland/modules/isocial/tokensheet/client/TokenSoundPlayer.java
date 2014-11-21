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
package org.jdesktop.wonderland.modules.isocial.tokensheet.client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

/**
 * This class is created to play the "ding" sound on the token assignment. It uses
 * the classes AudioCacheHandler and VolumeConverter borrowed from Open Wonderland
 * module EZScript
 * 
 * @author Kaustubh
 */
public class TokenSoundPlayer {

    private static TokenSoundPlayer instance;
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/isocial/tokensheet/client/Bundle");
    private static URL tokenAudioSource;
    private static AudioCacheHandler audioCacheHandler;
    private static VolumeConverter volumeConverter;

    /**
     * 
     * @return 
     */
    public static TokenSoundPlayer getInstance() {
        if (instance != null) {
            return instance;
        } else {
            return instance = new TokenSoundPlayer();
        }
    }

    /**
     * 
     */
    private TokenSoundPlayer() {
        this.tokenAudioSource = getClass().getResource(BUNDLE.getString("audioSource"));
        audioCacheHandler = new AudioCacheHandler();
        volumeConverter = new VolumeConverter(0, 100);
        try {
            audioCacheHandler.initialize();
        } catch (AudioCacheHandlerException ex) {
            Logger.getLogger(TokenGuideView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @param source
     * @return 
     */
    private String getCacheURL(URL source) {
        try {
            return audioCacheHandler.cacheURL(source);
        } catch (AudioCacheHandlerException ex) {
            Logger.getLogger(TokenSoundPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This method plays the sound when token is assigned.
     */
    static void playTokenSound() {
        String cacheURL = getInstance().getCacheURL(tokenAudioSource);
        try {
            SoftphoneControlImpl.getInstance().sendCommandToSoftphone("playFile=" + cacheURL + "=" + volumeConverter.getVolume(50));
        } catch (IOException ex) {
            Logger.getLogger(TokenSoundPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method plays the sound when strike is assigned.
     */
    static void playStrikeSound() {
        String cacheURL = getInstance().getCacheURL(tokenAudioSource);
//        try {
//            SoftphoneControlImpl.getInstance().sendCommandToSoftphone("playFile=" + cacheURL + "=" + volumeConverter.getVolume(50));
//        } catch (IOException ex) {
//            Logger.getLogger(TokenSoundPlayer.class.getName()).log(Level.SEVERE, null, ex);
//        }

    }

    static void playPassSound() {
    }
}
