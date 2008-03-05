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

import com.sun.j3d.utils.geometry.Box;
import java.awt.Font;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import org.jdesktop.j3d.util.SceneGraphUtil;

public class Billboard {
    
    /** Creates a new instance of Billboard */
    public Billboard(String Name, Transform3D Transform) {
        transform = Transform; name = Name;
        init();
    }
    void init() {
        node = new BranchGroup();
        node.setName("Billboard: "+name);
        Font3D font = new Font3D(new Font("monospaced", Font.BOLD, 1),new FontExtrusion());
        text = new Text3D(font,"",new Point3f(-4,-.4f,0));
        text.setCapability(text.ALLOW_POSITION_READ);
        text.setCapability(text.ALLOW_POSITION_WRITE);
        text.setCapability(text.ALLOW_STRING_WRITE);
        Box box = new Box(4.2f,.8f,.1f,Util.colorApp(1,0,0));
        box.setPickable(false);
        TransformGroup tr = new TransformGroup(transform);
        tr.addChild(box);
        Shape3D shape = new Shape3D(text);
        shape.setPickable(false);
        tr.addChild(shape);
        node.addChild(tr);
        SceneGraphUtil.setCapabilitiesGraph(node, false);
    }
    void setText(String message) {
        if (message.length() > 15) message = message.substring(0,12)+"...";
        text.setString(message);
        Point3f pos = new Point3f();
        text.getPosition(pos);
        pos.x = -4;
        text.setPosition(pos);
    }
    void update() {
        /*BoundingBox bounds = new BoundingBox();
        text.getBoundingBox(bounds);
        Point3d lower = new Point3d();
        Point3d upper = new Point3d();
        bounds.getLower(lower); bounds.getUpper(upper);
        
        Point3f pos = new Point3f();
        text.getPosition(pos);
        pos.x -= 0.1f;
        if (upper.x < -4) pos.x = -4;
        text.setPosition(pos);*/
    }
    BranchGroup node;
    private Transform3D transform;
    private Text3D text;
    private String name;
}
