/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.globals.builder;

import com.jme.math.Vector3f;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.PickDetails;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseMovedEvent3D;

/**
 *
 * @author Ryan
 */
public abstract class MinecraftMouseListener extends MouseFollowerListener {

    private static final Logger logger = Logger.getLogger(MouseFollowerListener.class.getName());

    public abstract void mouseClickedOnEntity(Vector3f normal, Vector3f position);
    
    public abstract void mouseMovedOnEntity(Vector3f normal, Vector3f position);
    
    public void mouseFollowerClicked(MouseButtonEvent3D event) {
        
        PickDetails details = event.getPickDetails();
        
        if(details == null) {
            return;
        }
        
        if(event.getEntity() == null) {
            return;
        }
        
        Vector3f triData[] = new Vector3f[3];
        
        details.getTriMesh().getTriangle(details.getTriIndex(), triData);
        triData[0] = details.getTriMesh().localToWorld(triData[0], null);
        triData[1] = details.getTriMesh().localToWorld(triData[1],null);
        triData[2] = details.getTriMesh().localToWorld(triData[2], null);
        
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f normal = new Vector3f();
        
        triData[0].subtract(triData[1], v1);
        triData[2].subtract(triData[1], v2);
        
        v1.normalizeLocal();
        v2.normalizeLocal();
        
        v2.cross(v1, normal);
        normal.normalizeLocal();
        
        
        mouseClickedOnEntity(normal, rootPositionOfEntity(event.getEntity()));
    }

    public void mouseFollowerMoved(MouseMovedEvent3D mme3d) {
        
        PickDetails details = mme3d.getPickDetails();
        
        if(details == null) {
            logger.warning("PICK DETAILS ARE NULL!");
            return;
        }
        
        if(mme3d.getEntity() == null) {
            logger.warning("ENTITY IS NULL!");
            return;
        }
        
        mouseMovedOnEntity(details.getNormal(), rootPositionOfEntity(mme3d.getEntity()));
    }
    
    private Vector3f rootPositionOfEntity(Entity entity) {
        Vector3f position = null;
        
        RenderComponent rc = entity.getComponent(RenderComponent.class);
        if(rc == null) {
            logger.warning("NO RENDER COMPONENT!");
            //we have problems!
            return null;
        }
        
        position = rc.getSceneRoot().getWorldTranslation();
        return position;
        
        
    }
}
