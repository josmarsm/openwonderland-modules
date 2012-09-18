/**
 * Open Wonderland
 *
 * Copyright (c) 2011-12, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.webcaster.client;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import com.jme.math.Quaternion;
import com.jme.scene.CameraNode;
import com.jme.scene.Spatial;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.TextureRenderBuffer;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.client.jme.utils.ScenegraphUtils;

/**
 * @author Christian O'Connell
 * @author Bernard Horan
 */
public class WebcasterCellRenderer extends BasicRenderer implements RenderUpdater
{
    private static final int IMAGE_HEIGHT = 360;
    private static final int IMAGE_WIDTH = 640;

    private Node node = null;

    private TextureRenderBuffer textureBuffer;
    private BufferedImage captureImage;
    private CaptureComponent captureComponent = null;

    private Spatial videoSpatial;

    public WebcasterCellRenderer(Cell cell) {
        super(cell);
    }

    public JComponent getCaptureComponent(){
        return captureComponent;
    }

    protected Node createSceneGraph(Entity entity)
    {
        node = new Node();

        try
        {
            LoaderManager manager = LoaderManager.getLoaderManager();
            URL url = AssetUtils.getAssetURL("wla://webcaster/Precision_HD_Camera.dae/Precision_HD_Camera.dae.gz.dep", this.getCell());
            DeployedModel dm = manager.getLoaderFromDeployment(url);
            Node cameraModel = dm.getModelLoader().loadDeployedModel(dm, entity);

            cameraModel.setLocalScale(0.1f);
            cameraModel.setLocalTranslation(-0.4f, 0f, 0f);

            float angleDegrees = 180;
            float angleRadians = (float) Math.toRadians(angleDegrees);
            Quaternion quat = new Quaternion().fromAngleAxis(angleRadians, new Vector3f(0,1,0));

            videoSpatial = ScenegraphUtils.findNamedNode(cameraModel, "ID626");

            node.setLocalRotation(quat);
            node.attachChild(cameraModel);
        }
        catch(IOException e){
            logger.log(Level.SEVERE, "Failed to load camera model", e);
        }

        node.setModelBound(new BoundingBox());
        node.updateModelBound();
        node.setName("Cell_" + cell.getCellID() + ":" + cell.getName());

        createViewfinder(node);

        CameraListener listener = new CameraListener();
        listener.addToEntity(entity);

        return node;
    }

    private void createViewfinder(Node node)
    {
        WorldManager wm = ClientContextJME.getWorldManager();

        textureBuffer = (TextureRenderBuffer) wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.TEXTURE_2D, IMAGE_WIDTH, IMAGE_HEIGHT);
        textureBuffer.setIncludeOrtho((false));

        CameraNode cn = new CameraNode("MyCamera", null);
        Node cameraSG = new Node("cameraSG");
        cameraSG.attachChild(cn);
        //Move the camera so that it's in the right position
        cameraSG.setLocalTranslation(0, 0.45f, -0.85f);

        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cn, IMAGE_WIDTH, IMAGE_HEIGHT, 45.0f, (float) IMAGE_WIDTH/ (float) IMAGE_HEIGHT, 1f, 2000f, false);

        textureBuffer.setCameraComponent(cc);

        wm.getRenderManager().addRenderBuffer(textureBuffer);
        textureBuffer.setRenderUpdater(this);

        node.attachChild(cameraSG);

        captureComponent = new CaptureComponent();
        captureComponent.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
    }

    public void update(Object o)
    {
        captureImage = createBufferedImage(textureBuffer.getTextureData());

        try
        {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    captureComponent.repaint();
                }
            });
        }
        catch (Exception x)
        {
            throw new RuntimeException("Cannot capture texture buffer image");
        }

        if (((WebcasterCell) cell).getWebcasting()){
            ((WebcasterCell) cell).write(captureImage);
        }
    }

    private BufferedImage createBufferedImage(ByteBuffer bb)
    {
        int width = IMAGE_WIDTH;
        int height = IMAGE_HEIGHT;

        bb.rewind();
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                int index = (y*width + x)*3;
                int b = bb.get(index);
                int g = bb.get(index+1);
                int r = bb.get(index+2);

                int pixel = ((r&255)<<16) | ((g&255)<< 8) | ((b&255)) | 0xff000000;

                bi.setRGB(x, (height-y)-1, pixel);
            }
        }

        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        image.getGraphics().drawImage(bi, 0, 0, null);

        return image;
    }

    public void setButtonWebcastingState(final boolean state)
    {
        WorldManager wm = ClientContextJME.getWorldManager();
        SceneWorker.addWorker(new WorkCommit() {

            public void commit() {
                float z = 0;
                if (state) {
                    z = -0.15f;
                }
                logger.warning("z: " + z);
                //videoSpatial.setLocalTranslation(0f, 0f, z);
            }
        });
        
        wm.addToUpdateList(videoSpatial);
    }

    public class CaptureComponent extends JComponent {
        public CaptureComponent() {
            setBorder(BorderFactory.createLineBorder(Color.black));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            if (captureImage != null) {
                g.drawImage(captureImage, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
            }
        }
    }

    class CameraListener extends EventClassListener
    {
        CameraListener() {
            super();
        }

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }
        
        @Override
        public void commitEvent(Event event)
        {
            MouseButtonEvent3D mbe = (MouseButtonEvent3D) event;
            if (mbe.getButton() != ButtonId.BUTTON1) {
                return;
            }

            TriMesh mesh = mbe.getPickDetails().getTriMesh();
            
            switch (mbe.getID()) {
                case MouseEvent.MOUSE_CLICKED:
                    mouseClicked(mesh);
                    break;
            }
        }

        private void mouseClicked(TriMesh mesh) {
            if (mesh.toString().equals("ID627-Material3 (com.jme.scene.SharedMesh)")) {
                if (!((WebcasterCell) cell).isRemoteWebcasting()) {
                    ((WebcasterCell) cell).setWebcasting(!((WebcasterCell) cell).getWebcasting());
                    ((WebcasterCell) cell).updateControlPanel();

                }
            }
        }
    }
}
