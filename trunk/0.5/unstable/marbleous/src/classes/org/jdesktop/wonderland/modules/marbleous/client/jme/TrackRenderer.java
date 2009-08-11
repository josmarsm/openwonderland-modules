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

import org.jdesktop.wonderland.modules.marbleous.common.TCBKeyFrame;
import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;
import com.jme.scene.Line;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Extrusion;
import java.nio.FloatBuffer;
import java.util.ArrayList;
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

            drawKnot(splineTest.interp, ret);
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
            drawKnot(spline, ret);
            //drawSpline(spline, ret);
            ret.attachChild(createTrackMesh(spline));
        }

        return ret;
    }

    private void drawKnot(RotPosScaleTCBSplinePath spline, Node root) {
        int size = spline.getArrayLength();
        for(int i=0; i<size; i++) {
            TCBKeyFrame key = spline.getKeyFrame(i);
            Box box = new Box("knot-"+i, key.position, 0.5f, 0.5f, 0.5f);
            root.attachChild(box);
        }
    }

    private void drawSpline(RotPosScaleTCBSplinePath spline, Node root) {
        float step = 0.01f;

        Matrix4f mat = new Matrix4f();
        spline.computeTransform(0, mat);
        root.attachChild(createBox(0.1f, mat));

        for(float s=step; s<=1; s+=0.01f) {
            spline.computeTransform(s, mat);
            root.attachChild(createBox(0.1f, mat));
        }
    }

    private TriMesh createTrackMesh(RotPosScaleTCBSplinePath spline) {
        float step = 0.01f;

        Line extrusionShape = new Line();
        extrusionShape.setMode(Line.Mode.Connected);
        float[] points = new float[] {
            -1, 0.5f, 0f,
            0,0,0,
            0,0,0,
            1,0.5f,0
        };

        // TODO fix normals
        float[] normals = new float[] {
            0,1,0,
            0,1,0,
            0,1,0,
            0,1,0
        };

        extrusionShape.setVertexBuffer(FloatBuffer.wrap(points));
        extrusionShape.setNormalBuffer(FloatBuffer.wrap(normals));

        Matrix4f mat = new Matrix4f();
        Vector3f pos;
        ArrayList<Vector3f> path = new ArrayList();
        for(float s=0; s<=1; s+=0.01f) {
            spline.computeTransform(s, mat);
            pos = mat.mult(Vector3f.ZERO);
            path.add(pos);
            System.err.println(pos);
        }

        Extrusion ext = new Extrusion(extrusionShape, path, new Vector3f(0,1,0));
        return ext;
    }

    private TriMesh createBox(float size, Matrix4f transform) {
        Box b = new Box(null, Vector3f.ZERO, size, size, size);

        b.setLocalTranslation(transform.toTranslationVector());
        b.setLocalRotation(transform.toRotationQuat());

        return b;
    }

}
