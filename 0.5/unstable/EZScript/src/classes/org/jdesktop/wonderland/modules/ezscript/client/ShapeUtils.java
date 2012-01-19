/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.TriMesh;

import com.jme.scene.shape.Box;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.shape.Disk;
import com.jme.scene.shape.Dome;
import com.jme.scene.shape.Pyramid;
import com.jme.scene.shape.Quad;
import com.jme.scene.shape.Teapot;
import com.jme.scene.shape.Torus;
import com.jme.scene.shape.Tube;
import com.jme.scene.shape.Sphere;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author JagWire
 */
public enum ShapeUtils {
    
    INSTANCE;
    Logger logger = Logger.getLogger(ShapeUtils.class.getName());
    ShapeUtils() {
        builders.put("BOX", new ShapeBuilder() { 
            public TriMesh buildShape() {
                return new Box("box", new Vector3f(), 0.5f, 0.5f, 0.5f);               
            }
        });
        
        builders.put("SPHERE", new ShapeBuilder() { 
            public TriMesh buildShape() {
                return new Sphere("sphere", 30, 30, 1);
            }
        });
        
        builders.put("CYLINDER", new ShapeBuilder() { 
            public TriMesh buildShape() {
                return new Cylinder("Cylinder", 10, 10, 1, 1);
            }
        });
        
        builders.put("DISK", new ShapeBuilder() {
            public TriMesh buildShape() {
                return new Disk("Disk", 10, 10, 1);
            }
        });
        
        builders.put("DOME", new ShapeBuilder() { 
            public TriMesh buildShape() {
                return new Dome("Dome", 3, 10, 1);
            }
        });
        
        builders.put("PYRAMID", new ShapeBuilder() { 
            public TriMesh buildShape() {
                return new Pyramid("Pyramid", 1, 1);
            }
        });
        
        builders.put("QUAD", new ShapeBuilder() { 
            public TriMesh buildShape() {
                return new Quad("Quad", 1, 1);
            }
        });
        
        builders.put("TEAPOT", new ShapeBuilder() { 
            public TriMesh buildShape() {
                return new Teapot("Teapot");
            }
        });
        
        builders.put("TORUS", new ShapeBuilder() { 
            public TriMesh buildShape() {
                return new Torus("Torus", 10, 10, 1, 2);
            }
        });
        
        builders.put("TUBE", new ShapeBuilder() { 
            public TriMesh buildShape() {
                return new Tube("Tube", 2, 1, 1);
            }
        });
        
        builders.put("MARKER", new ShapeBuilder() {
            public TriMesh buildShape() {
                //build a pyramid
                Pyramid shape = new Pyramid("Pyramid", 1, 1);
                
                //Move it up half its length so we can see all of it.
                shape.setLocalTranslation(0, 0.5f, 0);
                
                //rotate it upside down
                shape.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.DEG_TO_RAD*180, new Vector3f(0, 0, 1)));
                
                return shape;
                
            }
        });
        
    }
    
    private Map<String, ShapeBuilder> builders = new HashMap<String, ShapeBuilder>();
    
    public synchronized TriMesh buildShape(String shape) {
        
        if(builders.containsKey(shape.toUpperCase())) {
            return builders.get(shape).buildShape();
        }
        logger.warning("Shape: " + shape +" not supported!");
        return null;
    }
            
    public static interface ShapeBuilder {
        public TriMesh buildShape();
    }
}
