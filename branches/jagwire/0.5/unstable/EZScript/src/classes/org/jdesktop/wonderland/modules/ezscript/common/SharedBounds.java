/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.common;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 *
 * @author JagWire
 */
@ServerState
@XmlRootElement(name="shared-BoundingVolume")
public class SharedBounds  extends SharedData {

    private static final long serialVersionUID = 1L;
    private String value;
    private float[] extents = { 0, 0, 0};

    public static final SharedBounds BOX = SharedBounds.valueOf(
                                                    "BOX",
                                                     new float[] { 1, 1, 1 });

    public static final SharedBounds SPHERE = SharedBounds.valueOf(
                                                    "SPHERE",
                                                     new float[] { 1, 0, 0});

    public SharedBounds() {
        value = "BOX";
        extents[0] = 1;
        extents[1] = 1;
        extents[2] = 1;
    }

    private SharedBounds(String value, float[] extents) {
        this.value = value;
        this.extents = extents;
    }

    public String getValue() {
        return value;
    }
    public float[] getExtents() {
        return extents;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setExtents(float[] extents) {
        this.extents = extents;
    }

    @Override
    public String toString() {
        return value + "\nx/r: " +extents[0]+"\n"
                +"y: "+extents[1]+"\n"
                +"z: "+extents[2];
    }

    public static SharedBounds valueOf(String s, float[] xs) {
        return new SharedBounds(s, xs);
    }

    public static SharedBounds valueOf(BoundingVolume volume) {
        if(volume instanceof BoundingBox) {
            BoundingBox b = (BoundingBox)volume;
            float[] xs = { b.xExtent, b.yExtent, b.zExtent };
            return new SharedBounds("BOX", xs);
        } else if(volume instanceof BoundingSphere) {
            BoundingSphere s = (BoundingSphere)volume;
            float[] xs = { s.radius };
            return new SharedBounds("SPHERE", xs);
        }
        return null;
    }
}
