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

package com.sun.labs.miw.client.cell;

import com.sun.labs.miw.common.AlbumCloudSetup;
import com.sun.labs.miw.common.AlbumInfoMessage;
import com.sun.labs.miw.common.AlbumInfoCellMessage;
import com.sun.labs.miw.common.MIWTrack;
import com.sun.labs.miw.common.MIWAlbum;
import com.sun.labs.miw.common.NowPlayingMessage;
import com.sun.labs.miw.common.PlaylistAction;
import com.sun.labs.miw.common.PlaylistMessage;
import com.sun.labs.miw.common.PlaylistCellMessage;
import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.SessionId;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.Bounds;
import javax.media.j3d.Node;
import javax.vecmath.Matrix4d;
import org.jdesktop.j3d.util.SceneGraphUtil;
import org.jdesktop.lg3d.wg.event.LgEvent;
import org.jdesktop.lg3d.wg.event.LgEventListener;
import org.jdesktop.lg3d.wg.event.MouseButtonEvent3D;
import org.jdesktop.lg3d.wg.internal.j3d.j3dnodes.J3dLgBranchGroup;
import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.darkstar.client.ExtendedClientChannelListener;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.Cell;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellID;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellSetup;
import org.jdesktop.lg3d.wonderland.darkstar.common.CellStatus;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.AvatarCellMessage;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.Message;

/**
 *
 * @author jkaplan, Jeff Moguillansky
 */
public class AlbumCloudCell extends Cell implements ExtendedClientChannelListener {
    // the logger to use
    private static final Logger logger = 
            Logger.getLogger(AlbumCloud.class.getName());

    // whether to use local textures
    // TODO: make configuration option
    private static final boolean LOCAL_TEXTURES = false;

    // the base url for art
    private String baseURL;
    
    // the album collection
    private AlbumCollection collection;
    
    public AlbumCloudCell(CellID cellID, String channelName,    
                          Matrix4d cellOrigin) 
    {  
        super(cellID, channelName, cellOrigin);
        //new Exception("AlbumCloudCell").printStackTrace();
    }

    public void setChannel(ClientChannel channel) {
        this.channel = channel;
    }

    public AlbumCollection getAlbumCollection() {
        return collection;
    }
    
    @Override
    public void setup(CellSetup setup) {
        AlbumCloudSetup acs = (AlbumCloudSetup) setup;
        UI.albumCloudCell = this;
        
        // read data from the setup packet
        this.baseURL = acs.getBaseURL();
        
        // initialize the album collection
        try {
            collection = new URLAlbumCollection(acs.getAlbumListURL());
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Unable to load albums from " +
                       acs.getBaseURL(), ioe);
        }
        
        // create the album cloud view
        UI.albumCloud = new AlbumCloud(baseURL, this);
        UI.albumCloud.addAlbums(collection, 372);
        J3dLgBranchGroup node = new J3dLgBranchGroup();
        node.addChild(UI.albumCloud.node);
        addMouseInput(node);
        SceneGraphUtil.setCapabilities(node);
        cellLocal.addChild(node);
        
        // set the initial playlist
        handlePlayList(PlaylistAction.NEW, acs.getPlayList());
    }
    
    public void reconfigure(Matrix4d origin, Bounds bounds, CellSetup setupData) {
        super.reconfigure(origin, cellBounds, setupData);
        setup(setupData);
    }
    
    void requestAlbumInfo(String albumName) {
        AlbumInfoCellMessage message = new AlbumInfoCellMessage(getCellID(),albumName);
        ChannelController.getController().sendMessage(message);
    }
    
    void requestAddToPlaylist(List<MIWTrack> tracks, PlaylistAction action) {
        PlaylistCellMessage message = new PlaylistCellMessage(getCellID(),action,tracks);
        ChannelController.getController().sendMessage(message);
    }
    
    void requestAddToPlaylist(String album, PlaylistAction action) {
        PlaylistCellMessage message = new PlaylistCellMessage(getCellID(),action,album);
        ChannelController.getController().sendMessage(message);
    }
    
    void addMouseInput(J3dLgBranchGroup node) {
        node.setMouseEventEnabled(true);
        node.setMouseEventSource(MouseButtonEvent3D.class,true);
        node.addListener(new LgEventListener() {
                 public void processEvent(LgEvent evt) {
                    MouseButtonEvent3D event = (MouseButtonEvent3D)evt;
                    if (!event.isClicked()) return;
                    Node node = event.getIntersectedNode(0).getParent();
                    String name = node.getName();
                    if (name == null) return;
                    if (name.startsWith("Album: ")) {
                        String albumName = name.substring("Album: ".length());
                        if (event.getButton() == MouseButtonEvent3D.ButtonId.BUTTON3) {
                            requestAlbumInfo(albumName);
                        }
                        else if (event.getButton() == MouseButtonEvent3D.ButtonId.BUTTON1) {
                            requestAddToPlaylist(albumName,PlaylistAction.APPEND_FRONT);
                        }
                     }
                 }
                 public Class<LgEvent>[] getTargetEventClasses() {
                    return new Class[] { MouseButtonEvent3D.class };
                }
            });
    }
    
    public void receivedMessage(ClientChannel channel, SessionId session,
                                byte[] data) 
    {
        logger.info("Received message");
        
        Message message = Message.extractMessage(data);
        
        if (message instanceof NowPlayingMessage) {
            NowPlayingMessage npm = (NowPlayingMessage) message;
            handleNowPlaying(npm.getTrack());
        }
        
        else if (message instanceof PlaylistMessage) {
            PlaylistMessage plm = (PlaylistMessage) message;
            handlePlayList(plm.getAction(), plm.getTracks());
        }
        else if (message instanceof AlbumInfoMessage) {
            AlbumInfoMessage msg = (AlbumInfoMessage) message;
            handleAlbumInfo(msg.getAlbumName(),msg.getAlbumInfo());
        }
    }
    
    protected void handleAlbumInfo(String albumName, MIWAlbum miwAlbum) {
        UI.albumViewer.setAlbum(miwAlbum);
    }
    
    /**
     * Called when the track we are playing changes
     * @param trackId the id of the track to switch to
     */
    protected void handleNowPlaying(MIWTrack track) {
        logger.fine("NOW PLAYING: " + track.getAlbum() + " : " + track.getName());
        UI.playlist.setNowPlaying(track);
    }

     /**
      * Called when the playlist changes.
      * @param action whether to overwrite or append the playlist
      * @param playList the play list
      */
     protected void handlePlayList(PlaylistAction action, 
                                   List<MIWTrack> tracks)
     {
         if (logger.isLoggable(Level.FINE)) {
             logger.fine(action + " Playlist: ");
         
            for (MIWTrack track : tracks) {
                logger.fine("  " + track.getAlbum() + 
                            " : " + track.getName());
            }
         }
         
         UI.playlist.setPlaylist(action, tracks);
     }

     public void leftChannel(ClientChannel channel) {
     }

     @Override
     public boolean setStatus(CellStatus status) {
        // tell the super class the status which handles load/unload
        boolean ret = super.setStatus(status);

        if (!ret)
            return ret;
        
        // Start/stop the animation based upon the status
        switch (status) {
            case VISIBLE:
                UI.albumCloud.start();
                break;
            case ACTIVE:
            case INACTIVE:
                break;
            case BOUNDS :
                cellLocal.removeAllChildren();
                break;
        }
        return ret;
    }

    public void setVolume(String callId, double volume) {
        AvatarCellMessage msg = new AvatarCellMessage(callId, volume);
            ChannelController.getController().sendMessage(msg);
    }

}
