/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client.camera;

import com.jme.math.Vector3f;
import imi.camera.CameraModels;
import imi.camera.ChaseCamModel;
import imi.camera.ChaseCamState;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.FlexibleCameraAdapter;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.AvatarImiJME;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.annotation.ScriptMethod;

/**
 * Method to set the camera to a chase cam that always looks at the avatar from
 * the right.
 *
 * @author jagwire
 */
@ScriptMethod
public class SetRightCameraMethod implements ScriptMethodSPI {

    private ChaseCamState cameraState = null;
    private ChaseCamModel cameraModel = null;
    private static final Logger logger = Logger.getLogger(SetRightCameraMethod.class.getName());

    public String getFunctionName() {
        return "SetRightCamera";
    }

    public void setArguments(Object[] args) {
        //no arguments
    }

    public String getDescription() {
        return "Sets the camera to a chase camerea that always looks at the avatar" +
                "from the right.\n" +
                "-usage: SetRightCamera();";
    }

    public String getCategory() {
        return "Camera";
    }

    public void run() {
        setCamera(CameraUtils.RIGHT_OFFSET, CameraUtils.DEFAULT_LOOK_OFFSET);
    }


    public void setCamera(Vector3f positionOffset, Vector3f lookOffset) {
             //taken from AvatarClientPlugin.java:234-259
        ViewManager viewManager = ViewManager.getViewManager();
        ViewCell viewCell = viewManager.getPrimaryViewCell();
        CellTransform transform = viewCell.getWorldTransform();
        Vector3f translation = transform.getTranslation(null);
        CellRenderer renderer = viewCell.getCellRenderer(RendererType.RENDERER_JME);
        if(!(renderer instanceof AvatarImiJME)) {
            logger.warning("Unable to switch camera. Primary view cell renderer is not AvatarImiJME!");
            return;
        }
        AvatarImiJME avatarRenderer = (AvatarImiJME)renderer;

        if(cameraState == null) {
            cameraModel = (ChaseCamModel)CameraModels.getCameraModel(ChaseCamModel.class);
            cameraState = new ChaseCamState(positionOffset, lookOffset);
            cameraState.setDamping(1.7f);
            cameraState.setLookAtDamping(1.7f);
        }
        cameraState.setCameraPosition(translation.add(positionOffset));
        cameraState.setTargetCharacter(avatarRenderer.getAvatarCharacter());

        FlexibleCameraAdapter chaseCamera = new FlexibleCameraAdapter(cameraModel,
                                                                      cameraState);
        viewManager.setCameraController(chaseCamera);
    }

}
