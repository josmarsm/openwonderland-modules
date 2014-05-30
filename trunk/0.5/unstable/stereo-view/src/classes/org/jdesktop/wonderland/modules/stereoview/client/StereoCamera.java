/*
 */
package org.jdesktop.wonderland.modules.stereoview.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.ThirdPersonCameraProcessor;

/**
 * A camera class that updates the position of two cameras based on
 * an interaxial distance and an angle of rotation
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class StereoCamera extends ThirdPersonCameraProcessor { 
    // second camera
    private final CameraNode secondCam;
    
    // first camera
    private CameraNode firstCam;
    
    // interaxial distance
    private float dist = 0.0f;
    
    // focus rotation
    private float angle = 0.0f;
    
    public StereoCamera(CameraNode secondCam) {
        super();
        
        this.secondCam = secondCam;
    }
    
    public synchronized float getAxialDistance() {
        return dist;
    }
    
    public synchronized void setAxialDistance(float dist) {
        this.dist = dist;
        
        setCommitRequired();
    }
    
    public synchronized float getAngle() {
        return angle;
    }
    
    public synchronized void setAngle(float angle) {
        this.angle = angle;
        
        setCommitRequired();
    }

    @Override
    protected void setCameraPosition(Quaternion rotation, Vector3f translation) {
        float fDist = getAxialDistance() / 2.0f;
        float fAngle = getAngle() / 2.0f;
        
        // calculate the distance at the current location, rotated to the
        // correct angle
        Vector3f vDist = new Vector3f(fDist, 0f, 0f);
        rotation.multLocal(vDist);
        
        // update translations based on offset
        Vector3f firstCamTrans = new Vector3f(translation);
        firstCamTrans.addLocal(vDist);
        
        Vector3f secondCamTrans = new Vector3f(translation);
        secondCamTrans.subtractLocal(vDist);
        
        // update rotations
        Quaternion firstCamRot = new Quaternion();
        firstCamRot.fromAngleAxis(-fAngle, Vector3f.UNIT_Y);
        firstCamRot = rotation.mult(firstCamRot);
        
        Quaternion secondCamRot = new Quaternion();
        secondCamRot.fromAngleAxis(fAngle, Vector3f.UNIT_Y);
        secondCamRot = rotation.mult(secondCamRot);
        
        firstCam.setLocalRotation(firstCamRot);
        firstCam.setLocalTranslation(firstCamTrans);
        
        secondCam.setLocalRotation(secondCamRot);
        secondCam.setLocalTranslation(secondCamTrans);
        
        // make sure to update the second camera position
        ClientContextJME.getWorldManager().addToUpdateList(secondCam);
    }

    @Override
    public void setEnabled(boolean enabled, CameraNode cameraNode) {
        super.setEnabled(enabled, cameraNode);
        
        if (enabled) {
            this.firstCam = cameraNode;
        }
    }
}
