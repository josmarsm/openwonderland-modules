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

import org.jdesktop.lg3d.wonderland.darkstar.client.ChannelController;
import org.jdesktop.lg3d.wonderland.darkstar.client.cell.CellMenu;

import org.jdesktop.lg3d.wg.event.LgEvent;
import org.jdesktop.lg3d.wg.event.LgEventListener;
import org.jdesktop.lg3d.wg.event.MouseButtonEvent3D;
import org.jdesktop.lg3d.wg.internal.j3d.j3dnodes.J3dLgBranchGroup;

import org.jdesktop.lg3d.wonderland.scenemanager.CellMenuManager;

public class Billboard implements LgEventListener {
    
    private AlbumCloudCell albumCloudCell;

    /** Creates a new instance of Billboard */
    public Billboard(String Name, Transform3D Transform, 
	    AlbumCloudCell albumCloudCell) {

        transform = Transform; name = Name;
	this.albumCloudCell = albumCloudCell;
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
        box.setPickable(true);
        TransformGroup tr = new TransformGroup(transform);
        tr.addChild(box);
        Shape3D shape = new Shape3D(text);
        shape.setPickable(true);
        tr.addChild(shape);
        node.addChild(tr);
        J3dLgBranchGroup bg = new J3dLgBranchGroup();
        bg.setCapabilities();
        bg.setMouseEventEnabled(true);
        bg.setMouseEventSource(MouseButtonEvent3D.class, true);
        bg.addListener(this);
        SceneGraphUtil.setCapabilitiesGraph(node, false);
	bg.addChild(node);
	node = bg;
    }
    @SuppressWarnings("unchecked")
    public Class<LgEvent>[] getTargetEventClasses() {
        return new Class[] { MouseButtonEvent3D.class };
    }
    public void processEvent(LgEvent evt) {
	System.out.println("got mouse event " + evt);

	if (evt instanceof  MouseButtonEvent3D) {
            MouseButtonEvent3D mevt = (MouseButtonEvent3D) evt;
            if (mevt.isClicked()) {
		BoxCellMenu menu = BoxCellMenu.getInstance();
		String name = albumCloudCell.getCellID().toString();
                menu.setCallId(name);
                CellMenuManager.getInstance().showMenu(albumCloudCell, menu,
                    "Volume for " + name);
	    }
	}
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
