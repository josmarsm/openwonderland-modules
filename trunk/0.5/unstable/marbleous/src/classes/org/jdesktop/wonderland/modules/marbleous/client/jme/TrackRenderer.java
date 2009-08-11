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
package org.jdesktop.wonderland.modules.marbleous.client.jme;

import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import java.util.Collection;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.marbleous.client.cell.TrackCell;
import org.jdesktop.wonderland.modules.marbleous.common.Track;

/**
 *
 * @author paulby
 */
public class TrackRenderer extends BasicRenderer {

    public TrackRenderer(Cell cell) {
        super(cell);
    }

    @Override
    protected Node createSceneGraph(Entity entity) {
        Node ret = null;
        if (false) {
            // Code for visualizing test splines
            SplineTest splineTest = new SplineTest();

            ret = new Node("Spline Test");

            addPositions(splineTest.interp, ret);
            drawSpline(splineTest.interp, ret);
        } else {
            ret = new Node("TrackRoot");
            Track track = ((TrackCell)cell).getTrack();
            Collection<TCBKeyFrame> keyFrames = track.buildTrack();
            System.err.println("SIZE "+keyFrames.size());
            for(TCBKeyFrame f : keyFrames) {
                System.err.println(f);
            }
            RotPosScaleTCBSplinePath spline = new RotPosScaleTCBSplinePath(keyFrames.toArray(new TCBKeyFrame[keyFrames.size()]));
            addPositions(spline, ret);
            drawSpline(spline, ret);
        }

        return ret;
    }

    private void addPositions(RotPosScaleTCBSplinePath interp, Node root) {
        int size = interp.getArrayLength();
        for(int i=0; i<size; i++) {
            TCBKeyFrame key = interp.getKeyFrame(i);
            Box box = new Box("knot-"+i, key.position, 0.5f, 0.5f, 0.5f);
            root.attachChild(box);
        }
    }

    private void drawSpline(RotPosScaleTCBSplinePath interp, Node root) {
        float step = 0.01f;

        Matrix4f mat = new Matrix4f();
        interp.computeTransform(0, mat);
        root.attachChild(createBox(0.1f, mat));

        for(float s=step; s<=1; s+=0.01f) {
            interp.computeTransform(s, mat);
            root.attachChild(createBox(0.1f, mat));
        }
    }

    private TriMesh createBox(float size, Matrix4f transform) {
        Box b = new Box(null, Vector3f.ZERO, size, size, size);

        b.setLocalTranslation(transform.toTranslationVector());
        b.setLocalRotation(transform.toRotationQuat());

        return b;
    }

}
