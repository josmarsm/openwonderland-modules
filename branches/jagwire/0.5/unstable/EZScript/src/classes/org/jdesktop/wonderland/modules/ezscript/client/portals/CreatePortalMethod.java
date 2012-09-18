/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.portals;

import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.Portal;
import org.jdesktop.mtgame.PortalBufferController;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.Sector;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 *
 * @author JagWire
 */
@ScriptMethod
public class CreatePortalMethod implements ScriptMethodSPI {

    private PortalBufferController bufferController = null;
    private Vector3f sourcePosition;
    private Vector3f destinationPosition;
    private Portal sourcePortal;
    private Portal destinationPortal;
    
    public String getFunctionName() {
        return "CreatePortal";
    }

    public void setArguments(Object[] args) {
        
    }

    public String getDescription() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return "Create prototype portal demo.";
    }

    public String getCategory() {
        return "unsupported";
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void run() {
        PortalUtils.createPortalSystem(ClientContextJME.getWorldManager());
     //   throw new UnsupportedOperationException("Not supported yet.");
    }

    Quad createEnterPortal(Portal p, Sector s, ZBufferState zbuf) {
        Quad portalQuad = new Quad("portal"+p.toString(), 12, 20);

        BoundingBox boundingBox = new BoundingBox(new Vector3f(), 6, 10.0f, 2.0f);
        portalQuad.setModelBound(boundingBox);
        portalQuad.setRenderState(zbuf);
        Orientation enterOrientation = new Orientation(new Vector3f(0, 12, 10), //posiiton
                                                       new Vector3f(0, 0, -1), //look
                                                       new Vector3f(0, 1, 0), //up
                                                       new Vector3f(-1, 0, 0));

        Orientation exitOrientation = new Orientation(new Vector3f(-10, 12, 0),
                                                       new Vector3f(-1, 0, 0),
                                                       new Vector3f(0, 1, 0),
                                                       new Vector3f(0, 0, 1));
        portalQuad.setLocalTranslation(enterOrientation.getPosition().x,
                                       enterOrientation.getPosition().y,
                                       enterOrientation.getPosition().z);
        p = new Portal(portalQuad, enterOrientation.getPosition(),
                                   enterOrientation.getLookDirection(),
                                   enterOrientation.getUpDirection(),
                                   enterOrientation.getLeftDirection(),
                                   exitOrientation.getPosition(),
                                   exitOrientation.getLookDirection(),
                                   exitOrientation.getUpDirection(),
                                   exitOrientation.getLeftDirection(), s, s);

        return portalQuad;
    }

    Quad createExitPortal(Portal p, Sector s, ZBufferState zbuf) {
                Quad portalQuad = new Quad("portal"+p.toString(), 12, 20);

        BoundingBox boundingBox = new BoundingBox(new Vector3f(), 6, 10.0f, 2.0f);
        portalQuad.setModelBound(boundingBox);
        portalQuad.setRenderState(zbuf);
        Orientation enterOrientation = new Orientation(new Vector3f(0, 12, 10), //posiiton
                                                       new Vector3f(0, 0, -1), //look
                                                       new Vector3f(0, 1, 0), //up
                                                       new Vector3f(-1, 0, 0));

        Orientation exitOrientation = new Orientation(new Vector3f(-10, 12, 0),
                                                       new Vector3f(-1, 0, 0),
                                                       new Vector3f(0, 1, 0),
                                                       new Vector3f(0, 0, 1));
        portalQuad.setLocalTranslation(enterOrientation.getPosition().x,
                                       enterOrientation.getPosition().y,
                                       enterOrientation.getPosition().z);
        p = new Portal(portalQuad, enterOrientation.getPosition(),
                                   enterOrientation.getLookDirection(),
                                   enterOrientation.getUpDirection(),
                                   enterOrientation.getLeftDirection(),
                                   exitOrientation.getPosition(),
                                   exitOrientation.getLookDirection(),
                                   exitOrientation.getUpDirection(),
                                   exitOrientation.getLeftDirection(), s, s);

        return portalQuad;
    }

    void createSectorAndPortals(WorldManager wm, PortalBufferController pbc) {
        BoundingBox bbox = new BoundingBox(new Vector3f(), 1000.0f, 1000.0f, 1000.0f);
        Sector mySector = new Sector(bbox);

        ZBufferState zbuf = (ZBufferState)wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
//<editor-fold defaultstate="collapsed" desc="blah">
        Quad p1Quad = new Quad("P1 Quad", 12, 20);
        BoundingBox pbbox = new BoundingBox(new Vector3f(), 6.0f, 10.0f, 2.0f);
        p1Quad.setModelBound(pbbox);
        p1Quad.setRenderState(zbuf);
        Vector3f p1EnterLoc = new Vector3f(0.0f, 12.0f, -214.0f);
        Vector3f p1EnterDir = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f p1EnterUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f p1EnterLeft = new Vector3f(-1.0f, 0.0f, 0.0f);
        Vector3f p1ExitLoc = new Vector3f(214.0f, 12.0f, 0.0f);
        Vector3f p1ExitDir = new Vector3f(-1.0f, 0.0f, 0.0f);
        Vector3f p1ExitUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f p1ExitLeft = new Vector3f(0.0f, 0.0f, 1.0f);
        p1Quad.setLocalTranslation(p1EnterLoc.x, p1EnterLoc.y, p1EnterLoc.z);

        sourcePortal = new Portal(p1Quad, p1EnterLoc, p1EnterDir, p1EnterUp,
                               p1EnterLeft, p1ExitLoc, p1ExitDir, p1ExitUp,
                               p1ExitLeft, mySector, mySector);
//</editor-fold>
        Quad p2Quad = new Quad("P2 Quad", 12, 20);
        BoundingBox p2bbox = new BoundingBox(new Vector3f(), 6.0f, 10.0f, 2.0f);
        p2Quad.setModelBound(p2bbox);
        p2Quad.setRenderState(zbuf);
        Vector3f p2EnterLoc = new Vector3f(214.0f, 12.0f, 0.0f);
        Vector3f p2EnterDir = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f p2EnterUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f p2EnterLeft = new Vector3f(0.0f, 0.0f,-1.0f);
        Vector3f p2ExitLoc = new Vector3f(0.0f, 12.0f, -214.0f);
        Vector3f p2ExitDir = new Vector3f(0.0f, 0.0f, 1.0f);
        Vector3f p2ExitUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f p2ExitLeft = new Vector3f(1.0f, 0.0f, 0.0f);
        p2Quad.setLocalTranslation(p2EnterLoc.x, p2EnterLoc.y, p2EnterLoc.z);
        Quaternion rot = new Quaternion();
        rot.fromAngleAxis(-(float)(Math.PI/2.0), p2EnterUp);
        p2Quad.setLocalRotation(rot.clone());

        destinationPortal = new Portal(p2Quad, p2EnterLoc, p2EnterDir, p2EnterUp,
                               p2EnterLeft, p2ExitLoc, p2ExitDir, p2ExitUp,
                               p2ExitLeft, mySector, mySector);
        mySector.addPortal(sourcePortal);
        mySector.addPortal(destinationPortal);
        pbc.addSector(mySector);

        wm.addToUpdateList(p1Quad);
        wm.addToUpdateList(p2Quad);

        Node n = new Node("Portals");
        n.attachChild(p1Quad);
        p1Quad.setGlowEnabled(true);
        p1Quad.setGlowColor(new ColorRGBA(0.0f, 0.0f, 1.0f, 0.5f));
        n.attachChild(p2Quad);
        p2Quad.setGlowEnabled(true);
        p2Quad.setGlowColor(new ColorRGBA(1.0f, 0.65f, 0.0f, 0.5f));
        RenderComponent rc = wm.getRenderManager().createRenderComponent(n);


        Entity qe = new Entity("");
        float[] times = new float[2];
        times[0] = 0.0f; times[1] = 0.75f;
        //animationProcessor = new PortalAnimationProcessor(wm, qe, null, null, times);
       // qe.addComponent(PortalAnimationProcessor.class, animationProcessor);
        qe.addComponent(RenderComponent.class, rc);
        wm.addEntity(qe);
    }

    class Orientation {
        private Vector3f position;
        private Vector3f lookDirection;
        private Vector3f upDirection;
        private Vector3f leftDirection;
        public Orientation(Vector3f p, Vector3f look, Vector3f up, Vector3f left) {
            position = p;
            lookDirection = look;
            upDirection = up;
            leftDirection = left;
        }

        public Vector3f getPosition() {
            return position;
        }

        public Vector3f getLookDirection() {
            return lookDirection;
        }

        public Vector3f getUpDirection() {
            return upDirection;
        }

        public Vector3f getLeftDirection() {
            return leftDirection;
        }
    }
}
