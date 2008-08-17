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

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

public class NodeMover {
    
    /** Creates a new instance of Controller */
    public NodeMover(TransformGroup TransformNode) {
        transformNode = TransformNode;
        init();
    }
    public void move(Transform3D Target) {
        frame = 0;
        target = Target;
    }
    public void set(Transform3D Target) {
        move(Target);
        transformNode.setTransform(Target);
    }
    private void init() {
        lastError = new Vector3d(); 
        integrator = new Vector3d();
        vel = new Vector3d();
    }
    private void advance() {
        Transform3D tr = new Transform3D();
        transformNode.getTransform(tr);
        Vector3d curPos = new Vector3d();
        Vector3d setPos = new Vector3d();
        tr.get(curPos);
        target.get(setPos);
        Vector3d error = Util.sub(setPos,curPos);
        Vector3d delta = Util.sub(lastError,error);
        Vector3d cmd = Util.sub(Util.add(Util.scale(KP,error) ,Util.scale(KI,integrator)),Util.scale(KD,delta));
        vel.add(cmd);
        curPos.add(vel);
        tr.set(target);
        tr.setTranslation(curPos);
        transformNode.setTransform(tr);
        lastError.set(error);
    }
    public void update() {
        if (frame == -1) return;
        advance();
        frame++;
        if (frame == 100) {
            transformNode.setTransform(target);
            frame = -1;
        }
    }
    private int frame = -1;
    private Vector3d lastError;
    private Transform3D target;
    private Vector3d integrator, vel;
    private TransformGroup transformNode;
    private double KP=.02, KI=.000015, KD=.12;
}
