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

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.labs.miw.common.MIWAlbum;
import com.sun.labs.miw.common.MIWTrack;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.swing.Timer;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class AlbumCloud {
    
    /** Creates a new instance of AlbumCloud */
    public AlbumCloud(String artPath, AlbumCloudCell albumCloudCell) {
        this.artPath = artPath;
        
        init(albumCloudCell);
    }
    
    public void addAlbums(AlbumCollection albums, int count) {
        int c = 0;
        for (MIWAlbum album : albums.getAlbums()) {
            addAlbum(album.getName(), getBaseURL(album), getImage(album));
            if (c++ > count) {
                break;
            }
        }
        
        albumLayout.addAlbums(UI.albums.values().iterator());
    }
    public Album addAlbum(MIWAlbum album) {
        return addAlbum(album.getName(), getBaseURL(album), getImage(album));
    }
    
    public void update() {
        for (Album album : UI.albums.values()) album.update();
        albumQueue.update();
    }
    
    private Album addAlbum(String name, URL textureBase, String textureImage) {
        //System.out.println("Album " + textureBase + " " + textureImage);
        
        Album album = new Album(name, textureBase, textureImage); 
        UI.albums.put(name,album);
        return album;
    }
    
    private void addBackground() {
        int flags = Sphere.GENERATE_NORMALS_INWARD|Sphere.GENERATE_TEXTURE_COORDS;
        String texture = "textures/spheremap.jpg";
        
        try {
            Sphere sphere = new Sphere(100.0f,flags,64,Util.TextureApp(Util.URL(artPath+texture)));
            sphere.setPickable(false);
            node.addChild(sphere);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private URL getBaseURL(MIWAlbum album) {
        try {
            URL u = new URL(album.getArtURL(), "/");
            String ext = u.toExternalForm();
            u = new URL(ext.substring(0, ext.length() - 1));
            return u;
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
            return album.getArtURL();
        }
    }
    
    private String getImage(MIWAlbum album) {
        String image = album.getArtURL().getFile();
        if (image.startsWith("/")) {
            image = image.substring(1);
        }
        
        return image;
    }
    
    private void init(AlbumCloudCell albumCloudCell) {
        UI.init();
        node = new BranchGroup();
        node.setCapability(node.ALLOW_DETACH);
        albumLayout = new AlbumSphereLayout(30.0);
        albumsNode = albumLayout.node;
        Transform3D tr = new Transform3D();
        tr.setTranslation(new Vector3d(0,4,0));
        node.addChild(new AlbumCloudAnimator(albumsNode).node);
        albumQueue = new AlbumQueue(albumCloudCell);
        node.addChild(albumQueue.node);
        addBackground();

        //addTimer();
        node.addChild(new AlbumCloudAnimationBehavior(this));
    }
    public void start() {
        //timer.start();
    }
    private void addTimer() {
         timer = new Timer(10,new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                update(); }});
    }

    static class AlbumCloudAnimationBehavior extends Behavior {      
        WakeupCriterion wakeup = new WakeupOnElapsedFrames(0);
        AlbumCloud      cloud;
        
        public AlbumCloudAnimationBehavior(AlbumCloud cloud) {
            this.cloud = cloud;
            setSchedulingBounds(new BoundingSphere(new Point3d(), 100.0));
        }
        
        public void initialize() {
            wakeupOn(wakeup);
        }
        
        public void processStimulus(java.util.Enumeration criteria) {
            if (cloud != null) {
                cloud.update();
            }
            
            wakeupOn(wakeup);
        }
    }

    public BranchGroup node;
    BranchGroup albumsNode;
    Console console;
    AlbumQueue albumQueue;
    private AlbumSphereLayout albumLayout; 
    private Timer timer;
    private String artPath;
}
