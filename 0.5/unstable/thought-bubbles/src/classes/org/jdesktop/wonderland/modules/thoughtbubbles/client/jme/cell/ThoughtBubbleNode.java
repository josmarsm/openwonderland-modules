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

package org.jdesktop.wonderland.modules.thoughtbubbles.client.jme.cell;

import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Sphere;
import org.jdesktop.wonderland.modules.thoughtbubbles.common.ThoughtRecord;

/**
 * Node that handles the rendering of a ThoughtRecord. 
 *
 * @author Drew Harry <drew_harry@dev.java.net>
 */
public class ThoughtBubbleNode extends Node {

    private ThoughtRecord record;

    public ThoughtBubbleNode(ThoughtRecord record) {
        this.record = record;

        // Now setup our rendering here.
        TriMesh bubble = new Sphere("thought_bubble_sphere",Vector3f.ZERO, 10, 10, 0.4f);
        this.attachChild(bubble);

        // This 2.0f should get subbed out for a value that's a function of how many
        // comments that person has made at that position. Tracking that will get a little
        // annoying, though.
        this.setLocalTranslation(new Vector3f(record.getX(), record.getY()+2.0f, record.getZ()));
    }
}
