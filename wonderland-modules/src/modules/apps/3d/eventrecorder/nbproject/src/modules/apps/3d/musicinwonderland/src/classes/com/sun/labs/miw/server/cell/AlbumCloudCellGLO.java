/**
 * Project Wonderland
 *
 * $URL$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Rev$
 * $Date$
 * $Author$
 */
package com.sun.labs.miw.server.cell;

import com.sun.sgs.app.ClientSession;
import com.sun.voip.client.connector.CallStatus;
import javax.media.j3d.Bounds;
import javax.vecmath.Matrix4d;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import com.sun.labs.miw.common.AlbumCloudSetup;
import com.sun.labs.miw.common.AlbumInfoCellMessage;
import com.sun.labs.miw.common.AlbumInfoMessage;
import com.sun.labs.miw.common.MIWAlbum;
import com.sun.labs.miw.common.MIWTrack;
import com.sun.labs.miw.common.NowPlayingMessage;
import com.sun.labs.miw.common.PlaylistAction;
import com.sun.labs.miw.common.PlaylistMessage;
import com.sun.labs.miw.common.PlaylistCellMessage;
import com.sun.labs.miw.service.MusicManager;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.VoiceHandler;
import com.sun.mpk20.voicelib.impl.app.VoiceHandlerImpl;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.PeriodicTaskHandle;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.vecmath.Vector3d;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.CellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;
import org.jdesktop.lg3d.wonderland.darkstar.server.CellMessageListener;
import org.jdesktop.lg3d.wonderland.darkstar.server.cell.StationaryCellGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BasicCellGLOSetup;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.BeanSetupGLO;
import org.jdesktop.lg3d.wonderland.darkstar.server.setup.CellGLOSetup;

/**
 * A server cell that demonstrates simple networking
 * @author jkaplan, Jeff Moguillansky
 */
public class AlbumCloudCellGLO extends StationaryCellGLO
    implements BeanSetupGLO, CellMessageListener, ManagedCallStatusListener
{   
    /** a logger */
    private static final Logger logger =
            Logger.getLogger(AlbumCloudCellGLO.class.getName());
    
    /** minimum playlist size */
    private int minPlaylistSize = 7;
    
    /** the maximum playlist size */
    private int maxPlaylistSize = 30;
    
    /** the album list URL */
    private URL albumListURL;
    
    /** the time in milleseconds to auto advance */
    private long autoAdvanceTime = 10000;
    
    /** whether to actually play audio */
    private boolean playAudio;
    
    /** the volume to play audio at */
    private float audioVolume = 0.5f;
    
    /** the task used to change tracks */
    private PeriodicTaskHandle changeTrackTask;
    
    /** the call id */
    private String callId;
    
    /** The current play list. */
    private LinkedList<MIWTrack> playList;
    
    /**
    
    /**
     * Create a new album cell managed object.  This constructor is used
     * by WFS
     */
    public AlbumCloudCellGLO() {
        this (null, null);
    }
    
    /**
     * Create a new album cell managed object
     * @param bounds the 3D bounds
     * @param the center of the bounds
     */
    public AlbumCloudCellGLO(Bounds bounds, Matrix4d center) {
        super (bounds, center);
        
        this.playList = new LinkedList<MIWTrack>();
    }
    
    /**
     * Create a new album cell managed object
     * @param bounds the 3D bounds
     * @param the center of the bounds
     * @param playAudio whether to play audio to the voice service,
     * or just auto-advance every 15 seconds
     * @param albumListURL the URL of the album list file
     */
    public AlbumCloudCellGLO(Bounds bounds, Matrix4d center,
                             boolean playAudio, URL albumListURL)
    {
        this (bounds, center);
        
        this.playAudio = playAudio;
        this.albumListURL = albumListURL; 
    }
    
    /**
     * Used by WFS to setup the cell
     * @param setupData the information about this cell
     */   
    public void setupCell(CellGLOSetup setupData) {
        super.setupCell((BasicCellGLOSetup<AlbumCloudSetup>) setupData);
        
        AlbumCloudCellGLOSetup setup = (AlbumCloudCellGLOSetup) setupData;
  
        this.playAudio = setup.getPlayAudio();
        this.audioVolume = setup.getAudioVolume();
        
        setAlbumListURL(setup.getAlbumListURL());
        setMinPlaylistSize(setup.getMinPlaylistSize());
        setMaxPlaylistSize(setup.getMaxPlaylistSize());
    }

    /**
     * Used by WFS when the cell changes
     * @param setupData the data about the cell
     */
    public void reconfigureCell(CellGLOSetup setupData) {
        super.reconfigureCell((AlbumCloudCellGLOSetup) setupData);
    }

    /**
     * Get the current setup of the cell
     * @return the current setup
     */
    public CellGLOSetup getCellGLOSetup() {
        AlbumCloudCellGLOSetup out = 
                new AlbumCloudCellGLOSetup(getBounds(), getOrigin(), null);
        
        out.setAlbumListURL(getAlbumListURL());
        out.setMinPlaylistSize(getMinPlaylistSize());
        out.setMaxPlaylistSize(getMaxPlaylistSize());
        out.setPlayAudio(playAudio);
        
        return out;
    }
    
    @Override
    protected void addParentCell(ManagedReference parentRef) {
        super.addParentCell(parentRef);
        
        // start audio now that we know our world coordinates
        startAudio(playAudio);
    }
    
    /**
     * Start playing audio
     * @param playAudio if true, play audio, if not, just change tracks
     * periodically
     */
    protected void startAudio(boolean playAudio) {
        // see if there is already an audio task
        if (changeTrackTask != null) {
            changeTrackTask.cancel();
            changeTrackTask = null;
        }
                
        // stop the current treatment
        if (callId != null) {
            VoiceHandler vh = VoiceHandlerImpl.getInstance();
            vh.endCall(callId);
            callId = null;
        }
        
        TaskManager tm = AppContext.getTaskManager();
        if (playAudio) {
            // schedule a task to play the first song
            tm.scheduleTask(new ChangeTrackTask(thisRef), 1000);
        } else {
            // schedule a task to automatically advance songs
            changeTrackTask = tm.schedulePeriodicTask(
                    new ChangeTrackTask(thisRef), 1000, autoAdvanceTime);
        }
    }
    
    /**
     * Advance to the next track.
     */
    protected void next() {
        // remove the currently playing track from the list
        if (!playList.isEmpty()) {
            playList.remove(0);
        }
        
        // see if we need to refill the list with more tracks
        if (playList.size() <= getMinPlaylistSize()) {
            // get tracks from the service
            refillPlaylist();
        }
        
        // send a now playing message
        MIWTrack next = playList.getFirst();
        logger.info("Now playing: " + next);
        
        // start the treatment in the bridge
        if (playAudio) {
            changeAudioTrack(next);
        }
        
        Message message = new NowPlayingMessage(next);
        getCellChannel().send(message.getBytes());
    }
     
    /**
     * Refill the playlist up to the maximum size.
     */
    protected void refillPlaylist() {
        // decide how many tracks to get
        int trackCount = getMaxPlaylistSize() - playList.size();
        
        // get new tracks from the service
        MusicManager mm = AppContext.getManager(MusicManager.class);
        List<MIWTrack> tracks = mm.suggestTracks(trackCount);
        
        // add tracks to the playlist
        playList.addAll(tracks);
        
        // send a message announcing the new tracks
        Message msg = new PlaylistMessage(PlaylistAction.APPEND_BACK,
                                          tracks);
        getCellChannel().send(msg.getBytes());
    }
    
    /**
     * Change the track that is currently playing
     * @param track the new track to play
     */
    protected void changeAudioTrack(MIWTrack track) {
         // get the audio file in the music manager
         MusicManager mm = AppContext.getManager(MusicManager.class);
         String audioFile = mm.getAudioFile(track);
            
         VoiceHandler vh = VoiceHandlerImpl.getInstance();
         if (callId == null) {
            callId = setupCall(vh, audioFile);
         } else {
            vh.newInputTreatment(callId, audioFile, null);
         }
    }
    
    /**
     * Get the maximum size of the playlist.  When the playlist gets
     * down to the minimum size, this is the size it will be filled
     * to by adding new similar songs.
     * @return the maximum playlist size
     */
    public int getMaxPlaylistSize() {
        return maxPlaylistSize;
    }

    /**
     * Set the maximum size of the playlist.
     * @param maxPlayListSize the maximum playlist size
     */
    public void setMaxPlaylistSize(int maxPlaylistSize) {
        this.maxPlaylistSize = maxPlaylistSize;
    }

    /**
     * Get the minimum play list size.  When the playlist gets to
     * this size it will be refilled to the maximum size.
     * @return the minimum playlist size
     */
    public int getMinPlaylistSize() {
        return minPlaylistSize;
    }

    /**
     * Set the minimum playlist size.
     * @param minPlayListSize the minimum size
     */
    public void setMinPlaylistSize(int minPlaylistSize) {
        this.minPlaylistSize = minPlaylistSize;
    }

    /**
     * Get the audio volume
     * @return the audio volume
     */
    public float getAudioVolume() {
        return audioVolume;
    }
    
    /**
     * Set the audio volume
     * @param audioVolume the volume to set
     */
    public void setAudioVolume(float audioVolume) {
        this.audioVolume = audioVolume;
    }
    
    /**
     * Get the path to artwork.
     * @return the path to art
     */
    public URL getAlbumListURL() {
        return albumListURL;
    }
    
    /**
     * Set the album list URL.  This list is to a file in the album list
     * format which describes where the album artwork can be loaded from.
     * @param albumListURL the album list URL
     */
    public void setAlbumListURL(URL albumListURL) {
        this.albumListURL = albumListURL;
    }
    
    /**
     * Get the current playlist.  This is the list of upcoming songs
     * to play.
     * <p>
     * This is stored as a list of String Track ids, which can be 
     * translated into Tracks using the <code>getTracks()</code> method.
     * @return the current playlist
     */
    public List<MIWTrack> getPlayList() {
        return playList;
    }

     /**
     * Get the name of the client cell class
     * @return the fully-qualified class name of the album cloud cell class
     */
    public String getClientCellClassName() {
        return "com.sun.labs.miw.client.cell.AlbumCloudCell";
    }
    
    /**
     * Return the setup data for the cell.  This is the initial data that
     * gets sent to the client when they discover the cell.
     * @return the cell setup data, an instance of AlbumCloudSetup
     */
    public CellSetup getSetupData() {
        return new AlbumCloudSetup(baseUrl, getPlayList(), getAlbumListURL());
    }
      
    /**
     * Open the defaul cell channel.  This calls
     * <code>openDefaultChannel()</code>.
     */
    @Override
    public void openChannel() {
        this.openDefaultChannel();
    }
    
   
    /**
     * Called when a message is received from a client
     * @param client the client that sent the message
     * @param message the message the client sent
     */
    public void receivedMessage(ClientSession client, CellMessage message) {
        // get a reference to the MusicManager
        MusicManager mm = AppContext.getManager(MusicManager.class);
        
        if (message instanceof AlbumInfoCellMessage) {
            // request for information about an album
            String albumName = ((AlbumInfoCellMessage)message).getAlbumName();
            
            // get the album information from the music manager
            MIWAlbum album = mm.getAlbum(albumName);
            
            // send the message back to the requester
            Message m = new AlbumInfoMessage(albumName, album);
            getCellChannel().send(client, m.getBytes());
        } else if (message instanceof PlaylistCellMessage) {
            // make a change to the playlist
            PlaylistCellMessage pcm = (PlaylistCellMessage) message;
           
            // get the action and album name from the message
            PlaylistAction action = pcm.getAction();
            String albumName      = pcm.getAlbum();
           
            logger.info("Playlist message: " + action + " " + albumName);
           
            // get track information
            List<MIWTrack> tracks = null; 
            if (!albumName.equals("")) {
                // if just an album name was specified, choose the first
                // track on the album
                MIWAlbum album = mm.getAlbum(albumName);
                tracks = Collections.singletonList(album.getTracks().get(0));
            } else {
                // choose the given tracks
                tracks = pcm.getTracks(); 
            }
           
            if (tracks.isEmpty()) {
                return;
            }
           
            // the track to play after this operation, or null if
            // the currently playing track hasn't changed
            MIWTrack changeTrack = null;
            
            switch (action) {
            case APPEND_FRONT:
                // add to the beginning of the list
                playList.addAll(0, tracks);
                changeTrack = tracks.get(0);
                break;
            case APPEND_BACK:
                // if the list is empty, we need to update the currently
                // playing track to the first one we are adding
                if (playList.isEmpty()) {
                    changeTrack = tracks.get(0);
                }
                
                // add to the end of the list
                playList.addAll(tracks);
                break;
            case NEW:
               playList.clear();
               playList.addAll(tracks);
               changeTrack = tracks.get(0);
               break;
            }
            
            // change the audio
            if (changeTrack != null && playAudio) {
                changeAudioTrack(changeTrack);
            }
            
            // announce changes over channen
            Message m = new PlaylistMessage(action, tracks);
            getCellChannel().send(m.getBytes());
        }
    }
    
    /**
     * Called when the currently playing track ends.  Move to the next
     * track.
     * @param status the status message
     */
    public void callStatusChanged(CallStatus status) {
        if (status.getCode() == CallStatus.ENDED) {
	    logger.info("Call ended unexpectedly:  " + status);
	    callId = null;
	    next();
        } else if (status.getCode() == CallStatus.TREATMENTDONE) {
            next();
        }
    }
   
    /**
     * Setup a new call and return the call id
     * @param vh the voice handler to set up the call with
     * @param treatment the first treatment to play
     * @return the callId of the new call
     */
    protected String setupCall(VoiceHandler vh, String treatment) {  
        String id = getCellID().toString();
        
        // get the origin in world coordinates
        Vector3d trans = new Vector3d();
        getOriginWorld().get(trans);
        
        // start the treatment in the area of the orb
        double minX = trans.x - 50.0;
        double minY = 0;
        double minZ = trans.z - 50.0;
        double maxX = trans.x + 50;
        double maxY = 25;
        double maxZ = trans.z + 20;
        
        logger.info("Start treatment " + treatment + " at " + trans);
        
        vh.setupTreatment(id, treatment, null, this, minX, minZ, minY,
                                                     maxX, maxZ, maxY);
        vh.setAttenuationVolume(id, getAudioVolume());
        return id;
    }
    
    /**
     * A task that periodically changes track
     */
    public static class ChangeTrackTask 
            implements Task, Serializable 
    { 
        private ManagedReference cellRef;
        
        public ChangeTrackTask(ManagedReference cellRef) {
            this.cellRef = cellRef;
        }

        public void run() throws Exception {
            AlbumCloudCellGLO cell = cellRef.get(AlbumCloudCellGLO.class);
            cell.next();
        }
    }
}
