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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import org.jdesktop.j3d.util.SceneGraphUtil;

public class Album implements Cloneable {
    BranchGroup node;
    String name;
    private URL textureBase;
    private String image;
    private Appearance app, texture;
    private boolean active;
    private Vector<Transform3D> transformStack;
    private NodeMover nodeMover;
    
    public Album(String name, Appearance texture) {
        this.name = name;
        this.texture = texture;
        init();
    }
    /** Creates a new instance of Album */
    public Album(String name, URL textureBase, String image) {
        this.textureBase = textureBase;
        this.image = image;
        this.name = name;
        
        texture = Util.TextureApp(textureBase, image, 128, 128);
        init();
    }
    
    public void pushTransform(Transform3D transform3D) {
        transformStack.add(transform3D);
        nodeMover.move(transform3D);
    }
    
    public void popTransform() {
        if (transformStack.size()==0) return;
        transformStack.remove(transformStack.lastElement());
        if (transformStack.isEmpty())  nodeMover.move(new Transform3D());
        else nodeMover.move(transformStack.lastElement());
    }
    
    public void setTransform(Transform3D transform) {
        popTransform();
        pushTransform(transform);
    }
    
    public Transform3D getTransform() {
        if (transformStack.size() > 0) {
            return transformStack.lastElement();
        } else {
            return new Transform3D();
        }           
    }
    
    public void update() {
        nodeMover.update();
    }
    
    public void setActive(boolean Active) {
        active = Active;
        ColoringAttributes colorAttr = app.getColoringAttributes();
        if (active) colorAttr.setColor(1,0,0);
        else colorAttr.setColor(1,1,1);
    }
   
    public URL getURL() {
        try {
            return new URL(textureBase.toExternalForm() + "/" + image);
        } catch (MalformedURLException mue) {
            throw new IllegalStateException("Invalid url: " + textureBase + 
                                            "/" + image, mue);
        }  
    }
    
    private void init() {
        int flags = Box.GENERATE_NORMALS|Box.GENERATE_TEXTURE_COORDS;
        Box box = new Box(0.9f,0.9f,.1f,flags,texture);
        box.setPickable(false);
        app = Util.colorApp(1,1,1);
        Box boxCase = new Box(1.0f,1.0f,.05f,Box.GENERATE_NORMALS,app);
        boxCase.setPickable(true);
        boxCase.setName("Album: "+name);
        TransformGroup transformNode = new TransformGroup();
        transformNode.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transformNode.addChild(box);
        transformNode.addChild(boxCase);
        node = new BranchGroup();
        node.setCapability(node.ALLOW_DETACH);
        node.setName("Album node: "+name);
        node.addChild(transformNode);
        transformStack = new Vector<Transform3D>();
        nodeMover = new NodeMover(transformNode);
        active = false;
        
        SceneGraphUtil.setCapabilitiesGraph(node, false);
    }
    
    @Override
    public Album clone() {
        Album copy = new Album(name,texture);
        copy.textureBase = textureBase;
        copy.image = image;
        copy.setTransform(getTransform());
        Transform3D tr = new Transform3D();
        node.getLocalToVworld(tr);
        tr.mul(getTransform());
        copy.nodeMover.set(tr);
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Album other = (Album) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
