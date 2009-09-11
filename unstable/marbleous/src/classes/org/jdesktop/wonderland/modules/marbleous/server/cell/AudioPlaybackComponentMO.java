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
package org.jdesktop.wonderland.modules.marbleous.server.cell;

import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.VoiceManager;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.marbleous.common.cell.messages.SoundPlaybackMessage;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO.ComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.annotation.UsesCellComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.eventrecorder.RecorderManager;

/**
 * Server-side for component to manage audio playback.
 * @author kevin
 */
public class AudioPlaybackComponentMO extends CellComponentMO {

    @UsesCellComponentMO(ChannelComponentMO.class)
    private ManagedReference<ChannelComponentMO> channelCompRef;
    private final static Logger logger = Logger.getLogger(AudioPlaybackComponentMO.class.getName());

    /**
     * Standard constructor.
     * @param cell The associated CellMO
     */
    public AudioPlaybackComponentMO(CellMO cell) {
        super(cell);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.marbleous.client.cell.AudioPlaybackComponent";
    }

    /**
     * Interfaces with the audio system to play the given sound on the given call.
     * @param callID The call used to play the sound
     * @param uri The URI of the sound to play
     */
    public void playSoundFromURI(String callID, String uri) {
        // Get the URL corresponding to the sound file URI
        String playURL = uriToURL(uri);

        // Get the appropriate Call
        VoiceManager vm = AppContext.getManager(VoiceManager.class);
        Call call = vm.getCall(callID);

        try {
            if (call != null) {
                // Play the sound
                call.playTreatment(playURL);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        ChannelComponentMO channel = channelCompRef.getForUpdate();
        if (live) {
            channel.addMessageReceiver(SoundPlaybackMessage.class, new SoundPlaybackMessageReceiver(this));
        } else {
            channel.removeMessageReceiver(SoundPlaybackMessage.class);
        }
    }

    /**
     * Convert the given treatment URI to a URL.
     * @param treatment The treatment to convert
     * @return The URL pointing to the audio file
     */
    protected static String uriToURL(String treatment) {

        final String PROTOCOL_PREFIX = "wls://";

        final String serverURL = System.getProperty("wonderland.web.server.url");
        if (treatment.startsWith(PROTOCOL_PREFIX)) {
            // Create a URL from wls://<module>/path
            treatment = treatment.substring(PROTOCOL_PREFIX.length());  // skip past wls://

            int ix = treatment.indexOf("/");
            if (ix < 0) {
                logger.warning("Bad URI");
                return null;
            }

            String moduleName = treatment.substring(0, ix);

            String path = treatment.substring(ix + 1);

            URL url;

            try {
                url = new URL(new URL(serverURL),
                        "webdav/content/modules/installed/" + moduleName + "/audio/" + path);
                treatment = url.toString();
            } catch (MalformedURLException ex) {
                logger.warning("Bad URL");
                return null;
            }
        }

        return treatment;
    }

    /**
     * Receives playback messages, interacts with audio API to start sound.
     */
    private static class SoundPlaybackMessageReceiver implements ComponentMessageReceiver {

        private final ManagedReference<AudioPlaybackComponentMO> componentRef;

        /**
         * Standard constructor.
         * @param cellMO The associated TrackCellMO
         */
        public SoundPlaybackMessageReceiver(AudioPlaybackComponentMO componentMO) {
            componentRef = AppContext.getDataManager().createReference(componentMO);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            if (message instanceof SoundPlaybackMessage) {
                SoundPlaybackMessage spm = (SoundPlaybackMessage) message;
                if (spm.shouldPlay()) {
                    componentRef.getForUpdate().playSoundFromURI(spm.getCallID(), spm.getUri());
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            RecorderManager.getDefaultManager().recordMessage(sender, clientID, message);
        }
    }
}
