package org.jdesktop.wonderland.modules.path.common;

import com.jme.math.Vector3f;

/**
 * This interface represents a node which has a position
 * which can be converted into a JME Vector3f.
 *
 * @author Carl Jokl
 */
public interface Positioned {

    /**
     * Get the local position of the item within its parent.
     * .
     * @return The position of the item within its parent in the form of a 3D Vector object (Vector3f). This will be the translation
     *         in terms of the local coordinate space.
     */
    public Vector3f getPosition();
}
