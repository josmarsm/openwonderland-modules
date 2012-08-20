/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.ezscript.client.globals.gui;

import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeUtils;
import org.jdesktop.wonderland.modules.ezscript.client.ShapeViewerEntity;

/**
 *
 * @author Ryan
 */
public class UIViewEntity extends ShapeViewerEntity {

    private Image image = null;
    private boolean textured;
    private List<Runnable> rs = new ArrayList<Runnable>();
    MouseEventListener listener = null;

    public UIViewEntity() {
        super("UI View");
        textured = false;
    }

    public UIViewEntity(Image i) {
        super("UI View");
        image = i;
        textured = true;
    }

    public UIViewEntity(URL textureURL) {
        super("UI View");


        ImageIcon icon = new ImageIcon(textureURL);
        image = icon.getImage();
        textured = true;
    }

    @Override
    protected void generateZBufferState(Node node) {
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();

        ZBufferState zState = (ZBufferState) rm.createRendererState(RenderState.StateType.ZBuffer);
        zState.setEnabled(false);
        zState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        node.setRenderState(zState);
    }

    private void generateTexture(Node node) {
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        TextureState ts = (TextureState) rm.createRendererState(RenderState.StateType.Texture);

        Texture t = null;

        t = TextureManager.loadTexture(image,
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear,
                1,
                false);
        
        t.setWrap(Texture.WrapMode.MirroredRepeat);
        t.setTranslation(new Vector3f());
        ts.setTexture(t);
        ts.setEnabled(true);

        node.setRenderState(ts);
    }
    
    private BlendState blendState() {
        RenderManager rm =  ClientContextJME.getWorldManager().getRenderManager();
        return (BlendState)rm.createRendererState(RenderState.StateType.Blend);
    }

    @Override
    protected void updateTransformInternal(Node node, Vector3f position, Quaternion orientation) {
        /*
         * We are working in x,y screen coordinates at this point. Setting the
         * z position is a must.
         */

        position.z = -0.8f;

        node.setLocalTranslation(position);
        node.setLocalRotation(orientation);

        WorldManager.getDefaultWorldManager().addToUpdateList(node);


    }

    @Override
    protected void generated() {
//        this.addComponent(MouseEventListener.class, new MouseEventListener());
        listener = new MouseEventListener();
        listener.addToEntity(this);
    }

    /**
     *
     * @param node
     */
    @Override
    protected void generateAppearance(Node node) {
        if (textured) {
            generateTexture(node);

            if (blended) {
                BlendState bs = (BlendState) blendState();
                bs.setEnabled(true);
                bs.setBlendEnabled(true);
                bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
                bs.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
                bs.setTestEnabled(true);
                bs.setTestFunction(BlendState.TestFunction.GreaterThan);
                node.setRenderState(bs);
            }


        } else {
            super.generateAppearance(node);
        }
    }

    @Override
    protected void generateMesh(Node rootNode) {
        TriMesh mesh = ShapeUtils.INSTANCE.buildShape("QUAD");
        mesh.setRenderQueueMode(Renderer.QUEUE_ORTHO);

        mesh.setZOrder(256);
        rootNode.setZOrder(256);
        rootNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        rootNode.attachChild(mesh);

    }

    public void onClick(Runnable runnable) {
        synchronized (rs) {
            rs.add(runnable);
        }
    }

    public void removeMouseListener() {
        listener.removeFromEntity(this);
        listener = null;
        synchronized (rs) {
            rs.clear();
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        
        synchronized(rs) {
            rs.clear();
        }
    }
    
//    @Override
//    protected void generateCollisionComponent(Node n) {
//        //do nothing
//    }

    private class MouseEventListener extends EventClassListener {

        @Override
        public Class[] eventClassesToConsume() {
            return new Class[]{MouseButtonEvent3D.class};
        }

        @Override
        public void commitEvent(Event event) {


//            if(this.getEntity() == null) {
//                return;
//            }

            MouseButtonEvent3D me3d = (MouseButtonEvent3D) event;
            if (me3d.isClicked()) {
                synchronized (rs) {
                    for (Runnable r : rs) {
                        r.run();
                    }
                }
            }
        }
    }
}
