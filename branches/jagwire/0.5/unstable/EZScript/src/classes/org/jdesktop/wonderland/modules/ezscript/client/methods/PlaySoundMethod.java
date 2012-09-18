/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.methods;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.modules.ezscript.client.AudioCacheHandler;
import org.jdesktop.wonderland.modules.ezscript.client.AudioCacheHandlerException;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.VolumeConverter;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class PlaySoundMethod implements ScriptMethodSPI {

    String soundFileName = "";
    VolumeConverter volumeConverter;
    AudioCacheHandler audioCacheHandler;
    int volume = 50;

    public String getFunctionName() {
        return "PlaySound";
    }

    public void setArguments(Object[] args) {
        soundFileName = (String)args[0];
        volume = ((Double)args[1]).intValue();
        audioCacheHandler = new AudioCacheHandler();

        try {
            audioCacheHandler.initialize();
            volumeConverter = new VolumeConverter(0, 100);

        } catch (AudioCacheHandlerException ex) {
            Logger.getLogger(PlaySoundMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run() {
        String cacheFilePath = null;
	String audioSource = soundFileName;
        try {
            cacheFilePath = audioCacheHandler.cacheURL(new URL(audioSource));
            SoftphoneControlImpl.getInstance().sendCommandToSoftphone("playFile="
		+ cacheFilePath + "=" + volumeConverter.getVolume(volume));
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public String getDescription() {
        return "usage: PlaySound(\"www.mydomain.com/myfile.au\", volume)\n\n"
                +"-plays a sound file from the specified file URL.";
    }

    public String getCategory() {
        return "audio";
    }
}
