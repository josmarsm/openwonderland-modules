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

import com.sun.labs.miw.common.MIWAlbum;
import com.sun.labs.miw.common.MIWTrack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import org.jdesktop.j3d.util.SceneGraphUtil;

public class AlbumQueue {
    BranchGroup node;
    Billboard nowPlaying;
    List<Album> albumQueue;
    
    public AlbumQueue() {
        init();
    }
    
    void init() {
        node = new BranchGroup();
        node.setName("Album Queue");
        node.setCapability(node.ALLOW_CHILDREN_EXTEND);
        node.setCapability(node.ALLOW_CHILDREN_WRITE);
        SceneGraphUtil.setCapabilitiesGraph(node, false);
        
        albumQueue = Collections.synchronizedList(new ArrayList<Album>());
        nowPlaying = new Billboard("Now Playing",Util.makeTransform(0.0,4.2,0.0,1.0));
    }
    
    void clear() {
        albumQueue.clear();
        setTransforms();
    }
    
    void add(MIWTrack track) {
        Album album = getAlbum(track.getAlbum());
        Album copy = album.clone();
        copy.setActive(true);
        albumQueue.add(copy);
        album.setActive(true);
        setTransforms();
    }
    
    void set(MIWTrack track, int index) {
        Album album = getAlbum(track.getAlbum());
        Album copy = album.clone();
        copy.setActive(true);
        if (albumQueue.size() > index)  {
            albumQueue.get(index).setActive(false);
            albumQueue.set(index,copy);
        }
        else albumQueue.add(copy);
        album.setActive(true);
        setTransforms();
    }
    
    Album getAlbum(String albumName) {
        Album album = UI.albums.get(albumName);
        if (album == null) {
            MIWAlbum m = UI.albumCloudCell.getAlbumCollection().getAlbum(albumName);
            if (m != null) {
                album = UI.albumCloud.addAlbum(m);
            }
        }
        
        return album;
    }
    
    void add(List<MIWTrack> tracks) {
        if (!albumQueue.isEmpty()) {
            for (Album album : albumQueue) album.setActive(false);
        }
        albumQueue.clear();
        for (MIWTrack track : tracks) add(track);
    }
    
    void addToFront(List<MIWTrack> tracks) {
        int index = 0;
        for (MIWTrack track : tracks) {
            set(track, index);
            index++;
        }
    }
    
    void next() {
        albumQueue.remove(0);
        setTransforms();
    }
    
    void update() {
        for (Album album : albumQueue) album.update();
        nowPlaying.update();
    }
    
    boolean isEmpty() {
        return false;
    }
    
    private void setTransforms() {
        int s = Math.min(albumQueue.size(),7);
        node.removeAllChildren();
        node.addChild(nowPlaying.node);
        for (int j = 0; j<s; j++) {
            Album album = albumQueue.get(j);
            if (j==0) nowPlaying.setText(album.name);
            Transform3D tr = null;
            if (j == 0) tr = Util.makeTransform(0,0,0,2);
            else if (j<4) tr = Util.makeTransform(-3.2,-2.2*(j-2),0,1);
            else if (j<7) tr = Util.makeTransform(3.2,-2.2*(j-5),0,1);
            album.setTransform(tr);
            album.node.setName("Queue: "+j);
            node.addChild(album.node);
        }
        
        SceneGraphUtil.setCapabilitiesGraph(node, false);
    }
}