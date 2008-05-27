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
import java.util.Iterator;
import javax.media.j3d.BranchGroup;
import org.jdesktop.j3d.util.SceneGraphUtil;

public class AlbumSphereLayout {
    /** Creates a new instance of AlbumSphereLayout */
    public AlbumSphereLayout(double Radius) {
        init(Radius);
    }
   
    //set album transform, given coordinates
    //rx is the radius of the ring, y is the ring height
    //r is the sphere radius, theta is the vertical tilt, and phi is the horizontal tilt
    private void setAlbumTransform(Album album, double rx,double y, double r, double theta, double phi) {
        Matrix m = new Matrix();
        m.translate(rx*Math.cos(theta),y,rx*Math.sin(theta)); //set location
        m.rotate(0,1,0,Math.PI/2-theta); //set horizontal tilt
        m.rotate(1,0,0,-phi); //set vertical tilt
        album.pushTransform(m.getTransform());
    }
    void addAlbums(Iterator<Album> albumIt) {
        double r = radius; int c = 60;
         for (int k = 1; k<14; k++) {
            double phi = Math.PI/2.0 * k/16;
            double rx = r*Math.cos(phi), y = r*Math.sin(phi);
            for (int j = 0; j<c; j+=2) {
                if (!albumIt.hasNext()) return;
                double theta = 2*Math.PI*j/c;
                Album album = albumIt.next();
                setAlbumTransform(album,rx,y,r,theta,phi);
                node.addChild(album.node);
                if (!albumIt.hasNext()) return;
                album = albumIt.next();
                setAlbumTransform(album,rx,-y,r,theta,-phi);
                node.addChild(album.node);
            }
            c -= 4;
        }
    }
    //arrange albums in spherical layout
    private void init(double r) { 
        radius = r;
        node = new BranchGroup();
        node.setName("Album Sphere Layout");
        node.setCapability(node.ALLOW_CHILDREN_WRITE);
        node.setCapability(node.ALLOW_CHILDREN_EXTEND);
        int flags = Sphere.GENERATE_NORMALS_INWARD;
        Sphere sphere = new Sphere((float)r,flags,64,Util.LineApp());
        sphere.setPickable(false);
        node.addChild(sphere);
        
        SceneGraphUtil.setCapabilitiesGraph(node, false);
    }
    private void update() {
        
    }
    BranchGroup node;
    private double radius;
}
