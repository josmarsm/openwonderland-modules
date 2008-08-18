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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.TransformGroup;

public class AlbumCloudAnimator {
    
    /** Creates a new instance of AlbumSphereAnimator */
    public AlbumCloudAnimator(BranchGroup Node) {
        transformNode = new TransformGroup();
        transformNode.setCapability(transformNode.ALLOW_TRANSFORM_WRITE);
        transformNode.addChild(Node);
        init();
    }
    public void update() {
        mat.rotate(0,1,0,0.0002);
        transformNode.setTransform(mat.getTransform());
    }
    private void init() {
        node = new BranchGroup();
        node.setName("Album Cloud Animator");
        node.addChild(transformNode);
        mat = new Matrix();
        addTimer();
    }
     private void addTimer() {
         Timer timer = new Timer(10,new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                update(); }});
        timer.start();
    }
    BranchGroup node;
    private TransformGroup transformNode;
    private Matrix mat;
}
