/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.bulletphysics.model;

import com.bulletphysics.linearmath.Transform;
import javax.vecmath.Vector3f;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ReturnableScriptMethod;

/**
 *
 * @author JagWire
 */
@ReturnableScriptMethod
public class TransformMethod implements ReturnableScriptMethodSPI {

    private Vector3f origin;
    private Transform _transform;
    public String getDescription() {
        return "A physical object representing an object's transform.\n"
                + "-- usage var o = Origin(1, 1, 1);\n"
                + "--       var t = Transform(o);";
    }

    public String getFunctionName() {
        return "Transform";
    }

    public String getCategory() {
        return "physics";
    }

    public void setArguments(Object[] args) {
        origin = (Vector3f)args[0];
    }

    public Object returns() {
        return _transform;
    }

    public void run() {
        _transform = new Transform();
        _transform.setIdentity();
        _transform.origin.set(origin);
    }
    
}
