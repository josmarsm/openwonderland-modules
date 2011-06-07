/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.camera;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.client.jme.ViewManager;

/**
 *
 * @author jagwire
 */
public class CameraUtils {

    public static final Vector3f RIGHT_OFFSET = new Vector3f(-10, 1.8f, 0);
    public static final Vector3f LEFT_OFFSET = new Vector3f(10, 1.8f, 0);
    public static final Vector3f FRONT_OFFSET = new Vector3f(0, 1.8f, 10);
    public static final Vector3f BACK_OFFSET = new Vector3f(0, 1.8f, -10);
    public static final Vector3f TOP_OFFSET = new Vector3f(0, 10f, 0);
    public static final Vector3f DEFAULT_LOOK_OFFSET = new Vector3f(0, 1.8f, 0);

}
