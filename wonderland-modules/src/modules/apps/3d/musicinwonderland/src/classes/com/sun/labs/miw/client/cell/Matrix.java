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
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

public class Matrix {
    /** Creates a new instance of Matrix */
    public Matrix(Transform3D t) {
        mat = t;
    }
    public Matrix() {
        mat = new Transform3D();
        mat.setIdentity();
    }
    void translate(double x, double y, double z) {
       mat.setTranslation(new Vector3d(x,y,z));
    }
    void rotate(double x, double y, double z, double rad) {
        Matrix4d m = new Matrix4d();
        mat.get(m);
        Matrix4d rMat = new Matrix4d();
        rMat.set(new AxisAngle4d(x,y,z,rad));
        m.mul(rMat);
        mat.set(m);
    }
    void scale(double s) {
        mat.setScale(s);
    }
    Vector3d getPos() {
        Vector3d v = new Vector3d();
        mat.get(v);
        return v;
    }
    void print() {
        System.out.println(mat);
    }
    Transform3D getTransform() {
        return new Transform3D(mat);
    }
    private Transform3D mat;
}
