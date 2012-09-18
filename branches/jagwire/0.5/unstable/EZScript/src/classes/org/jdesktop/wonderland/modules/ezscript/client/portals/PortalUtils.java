/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.portals;

import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.math.Ray;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import java.util.logging.Logger;
import org.jdesktop.mtgame.DefaultBufferController;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.JMECollisionSystem;
import org.jdesktop.mtgame.PickDetails;
import org.jdesktop.mtgame.PickInfo;
import org.jdesktop.mtgame.Portal;
import org.jdesktop.mtgame.PortalBufferController;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.Sector;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author JagWire
 */
public class PortalUtils {

    private final static Logger logger = Logger.getLogger(PortalUtils.class.getName());

    public static PortalBufferController createPortalSystem(WorldManager manager) {
        PortalBufferController pbc =
                (PortalBufferController)manager.getRenderManager().getBufferController();
        //pbc = (EZPortalBufferController)manager.getRenderManager().getBufferController();

        logger.warning("Grabbed previous buffer controllers");
        //manager.getRenderManager().setBufferController(pbc);
        logger.warning("Set new buffer controllers");

        
        Portal enter = null;
        Portal exit = null;
        logger.warning("creating sector and portals");
        createSectorAndPortals(manager, pbc, enter, exit);
        logger.warning("sector and portals created");
        //pbc.getCurrentOnscreenBuffer().makeCurrent(null, null);
        
        return pbc;
    }
    private void calculateNewPortalCoordinate(Portal enterPortal,
                                            Vector3f[] collidedGeometry,
                                            Vector3f nLoc,
                                            Portal exitPortal,
                                            JMECollisionSystem collisionSystem,
                                            boolean animating) {
        float heightClearance = 12.0f;
        Vector3f n = new Vector3f();
        Vector3f nUp = new Vector3f();
        Vector3f nLeft = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f pDir = new Vector3f();
        Vector3f pUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f pLeft = new Vector3f();
        Vector3f w = new Vector3f();
        Vector3f cLoc = new Vector3f();
        Vector3f cDir = new Vector3f();
        Vector3f cUp = new Vector3f();
        Vector3f cLeft = new Vector3f();

        enterPortal.getEnterCoordinate(null, pDir, null, pLeft);
        enterPortal.getEnterCoordinate(cLoc, cDir, cUp, cLeft);

//            System.out.println("Current Coordinate System: ");
//            System.out.println("Dir: " + pDir);
//            System.out.println("Up: " + pUp);
//            System.out.println("Left: " + pLeft);

        // Create the new normal vector
        collidedGeometry[0].subtract(collidedGeometry[1], v1);
        collidedGeometry[2].subtract(collidedGeometry[1], v2);
        v1.normalizeLocal();
        v2.normalizeLocal();
        v2.cross(v1, n);
        n.normalizeLocal();

        // Now compute the new Up vector
        Vector3f Po = new Vector3f();
        Vector3f Pp = new Vector3f();
        collidedGeometry[0].add(pUp, Po);
        collidedGeometry[0].subtract(Po, w);
        //w.negateLocal();
        float d = n.dot(w) / n.length();
        Pp.scaleAdd(d, n, Po);
        Pp.subtract(collidedGeometry[0], nUp);
        nUp.normalizeLocal();

        // Finally, get left by doing a cross
        Vector3f enterDir = n.negate();
        nUp.cross(n, nLeft);
        Vector3f enterLeft = nLeft.negate();

//            System.out.println("New Coordinate System: ");
//            System.out.println("Loc: " + nLoc);
//            System.out.println("Dir: " + n);
//            System.out.println("Up: " + nUp);
//            System.out.println("Left: " + nLeft);

        // Nudge the new location in the direction of the normal
        nLoc.scaleAdd(0.1f, n, nLoc);

        // Check if we are a certain height above a floor
        Ray heightRay = new Ray();
        heightRay.origin = nLoc;
        heightRay.direction = nUp.negate();
        PickInfo pi2 = collisionSystem.pickAllWorldRay(heightRay, true, false);
        if (pi2.size() != 0) {
            PickDetails pd2 = pi2.get(0);
            if (pd2.getDistance() < heightClearance) {
                float nudge = heightClearance - pd2.getDistance();
                nLoc.scaleAdd(nudge, nUp, nLoc);
            }
        }

        Quaternion endRot = new Quaternion();
        endRot.fromAxes(nLeft, nUp, n);

        animating = true;

        //let's not depend on animationProcessors here.
        //        animationProcessor.reset(enterPortal, nLoc, endRot);
        //        animationProcessor.start();

        enterPortal.setEnterCoordinate(nLoc, enterDir, nUp, enterLeft);
        exitPortal.setExitCoordinate(nLoc, n, nUp, nLeft);
    }

    private static void createSectorAndPortals(WorldManager wm,
                                          PortalBufferController pbc,
                                          Portal enterPortal,
                                          Portal exitPortal) {
        BoundingBox bbox = new BoundingBox(new Vector3f(), 1000.0f, 1000.0f, 1000.0f);
        Sector mySector = new Sector(bbox);

        ZBufferState zbuf = (ZBufferState)wm.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);

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

        enterPortal = new Portal(p1Quad, p1EnterLoc, p1EnterDir, p1EnterUp,
                               p1EnterLeft, p1ExitLoc, p1ExitDir, p1ExitUp,
                               p1ExitLeft, mySector, mySector);

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

        exitPortal = new Portal(p2Quad, p2EnterLoc, p2EnterDir, p2EnterUp,
                               p2EnterLeft, p2ExitLoc, p2ExitDir, p2ExitUp,
                               p2ExitLeft, mySector, mySector);
        mySector.addPortal(enterPortal);
        mySector.addPortal(exitPortal);
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
       // animationProcessor = new PortalAnimationProcessor(wm, qe, null, null, times);
        ///qe.addComponent(PortalAnimationProcessor.class, animationProcessor);
        qe.addComponent(RenderComponent.class, rc);
        wm.addEntity(qe);
    }

     public static class EZPortalBufferController extends PortalBufferController {


         public EZPortalBufferController(DefaultBufferController controller) {
            super();
        }


    }
}