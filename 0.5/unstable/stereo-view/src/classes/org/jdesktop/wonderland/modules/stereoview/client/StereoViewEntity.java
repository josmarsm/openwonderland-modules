/**
 */
package org.jdesktop.wonderland.modules.stereoview.client;

import com.jme.image.Texture;
import com.jme.image.Texture.Type;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.state.jogl.JOGLTextureState;
import com.jme.util.geom.BufferUtils;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.TextureRenderBuffer;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.ViewProperties;

/**
 */
public class StereoViewEntity extends Entity 
    implements RenderUpdater, ImageProducer 
{
    private static final Logger LOGGER =
            Logger.getLogger(StereoViewEntity.class.getName());

    // The scene graph node for the position of the camera
    private final Node cameraPosition;
    
    // the actual camera node
    private final CameraNode cameraNode;

    // The camera component
    private final CameraComponent cameraComponent;
     
    // The byte array into which the camera data is copied
    private final int[] pixels;

    // The buffer into which the camera is renderer, so we can copy it into
    // a BufferedImage
    private final TextureRenderBuffer textureBuffer;
    
    // image source
    private final MemoryImageSource imageSource;
    
    // the stereo camera
    private final StereoCamera stereoCam;
    
    // whether this entity is enabled
    private boolean enabled = false;
    
    public StereoViewEntity(int width, int height) 
    {
        super("Stereo View Entity");

        this.cameraPosition = new Node("Stereo View Camera");
        this.cameraNode = new CameraNode();
        cameraPosition.attachChild(cameraNode);
        
        ViewProperties viewProps = ViewManager.getViewManager().getViewProperties();
        this.cameraComponent = createCameraComponent(cameraPosition, cameraNode, 
                                                     width, height, viewProps);
        addComponent(CameraComponent.class, cameraComponent);
        
        this.textureBuffer = createTextureBuffer(width, height, cameraComponent);
        this.stereoCam = new StereoCamera(cameraNode);
        
        this.pixels = new int[width * height];
        this.imageSource = new MemoryImageSource(width, height, pixels, 0, width);
        this.imageSource.setAnimated(true);
    }
    
    public synchronized void setAngle(float angle) {
        stereoCam.setAngle(angle);
    }
    
    public synchronized float getAngle() {
        return stereoCam.getAngle();
    }
    
    public synchronized void setDistance(float distance) {
        stereoCam.setAxialDistance(distance);
    }
    
    public synchronized float getDistance() {
        return stereoCam.getAxialDistance();
    }
    
    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
        
        if (enabled) {
            // register listeners
            getTextureBuffer().setRenderUpdater(this);
            getTextureBuffer().setEnable(true);
            ViewManager.getViewManager().setCameraController(stereoCam);
        } else {
            // unregiester listeners
            ViewManager.getViewManager().setCameraController(ViewManager.getDefaultCamera());
            getTextureBuffer().setEnable(false);
            getTextureBuffer().setRenderUpdater(null);
            
        }
    }
    
    public synchronized boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Get the texture buffer
     * @return the texture buffer
     */
    protected TextureRenderBuffer getTextureBuffer() {
        return textureBuffer;
    }
        
    /**
     * Get the current camera position
     * @return the camera position node
     */
    protected Node getCameraPosition() {
        return cameraPosition;
    }
    
    /**
     * Get the actual camera node
     * @return the camera node
     */
    protected CameraNode getCameraNode() {
        return cameraNode;
    }
    
    /**
     * Get the current camera component
     * @return the camera component
     */
    protected CameraComponent getCameraComponent() {
        return cameraComponent;
    }
    
    /**
     * Create a camera component
     * @param cameraPosition the camera node to attach this component to
     * @param cameraNode the actual camera node
     * @param width the width of the image
     * @param height the height of the image
     * @param viewProps the view properties for the camera
     * @return a camera component
     */
    private static CameraComponent createCameraComponent(Node cameraPosition,
            CameraNode cameraNode, int width, int height, ViewProperties viewProps)
    {
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();        
        float aspectRatio = (float) width / height;
                
        // Create a camera component and associated with the texture buffer we
        // have created.
        return rm.createCameraComponent(
                cameraPosition,             // The Node of the camera scene graph
                cameraNode,                 // The Camera
                width,                      // Viewport width
                height,                     // Viewport height
                viewProps.getFieldOfView(), // Field of view
                aspectRatio,                // Aspect ratio
                viewProps.getFrontClip(),   // Front clip
                viewProps.getBackClip(),    // Rear clip
                false                       // Primary?
                );
    }
    
    /**
     * Create a texture buffer for the given image
     * @param width the width of the image
     * @param height the height of the image
     * @return a texture buffer for that image
     */
    private static TextureRenderBuffer createTextureBuffer(int width, int height,
                                                           CameraComponent cc)
    {
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        TextureRenderBuffer out = new PackedTextureRenderBuffer(width, height);
        out.setIncludeOrtho(true);
    
        // Associated the texture buffer with the render manager, but keep it
        // off initially.
        out.setEnable(false);
        out.setCameraComponent(cc);
        rm.addRenderBuffer(out);
        
        return out;
    }

    /**
     * Dispose of any resources associated with this entity. Once called, the
     * Entity can no longer be used.
     */
    public void dispose() {
        // Remove the listener from the view cell, if there is one
        setEnabled(false);
        
        // Remove the render buffer to clean it up
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        rm.removeRenderBuffer(getTextureBuffer());
    }
    
    /**
     * {@inheritDoc}
     */
    public void update(Object arg0) {
        // Take the latest from the texture buffer into which we rendered
        // the camera and draw it into the image
        IntBuffer ib = getTextureBuffer().getTextureData().asIntBuffer();
        ib.rewind();
        ib.get(pixels);
        imageSource.newPixels();
    }

    /**
     * Camera update method. Must be called on MT-Game render thread
     * @param camera the camera position
     */
//    public void cameraMoved(CellTransform camera) {
//        // start with the camera position
//        Vector3f translation = camera.getTranslation(null);
//        Quaternion rotation = camera.getRotation(null);
//        Vector3f scale = camera.getScaling(null);
//        
//        // apply the offset
//        CellTransform off = getOffset();
//        translation.addLocal(off.getTranslation(null));
//        rotation.multLocal(off.getRotation(null));
//        scale.mult(off.getScaling());
//        
//        Node pos = getCameraPosition();
//        pos.setLocalTranslation(translation);
//        pos.setLocalRotation(rotation);
//        pos.setLocalScale(scale);
//        ClientContextJME.getWorldManager().addToUpdateList(pos);
//    }

    public void addConsumer(ImageConsumer ic) {
        imageSource.addConsumer(ic);
    }

    public boolean isConsumer(ImageConsumer ic) {
        return imageSource.isConsumer(ic);
    }

    public void removeConsumer(ImageConsumer ic) {
        imageSource.removeConsumer(ic);
    }

    public void startProduction(ImageConsumer ic) {
        imageSource.startProduction(ic);
    }

    public void requestTopDownLeftRightResend(ImageConsumer ic) {
        imageSource.requestTopDownLeftRightResend(ic);
    }
    
    private static class PackedTextureRenderBuffer extends TextureRenderBuffer {
        private ByteBuffer readBuffer;
        
        public PackedTextureRenderBuffer(int width, int height) {
            super (Target.TEXTURE_2D, width, height, 0);
        }

        @Override
        protected ByteBuffer allocateTextureData(GL gl, Texture t, Type type) {
            int components = GL.GL_RGBA8;
            int format = GL.GL_RGBA;
            int dataType = GL.GL_UNSIGNED_BYTE;
        
            Texture.RenderToTextureType rttType = Texture.RenderToTextureType.RGBA;
            t.setRenderToTextureType(rttType);

            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, components, getWidth(), getHeight(), 0,
                            format, dataType, null);
            
            if (t.getMinificationFilter().usesMipMapLevels()) {
                gl.glGenerateMipmap(GL.GL_TEXTURE_2D);
            }

            readBuffer = BufferUtils.createByteBuffer(getWidth()*getHeight()*4);
            return readBuffer;
        }

        @Override
        public ByteBuffer getTextureData() {
            GL2 gl = GLU.getCurrentGL().getGL2();

            JOGLTextureState.doTextureBind(getTexture().getTextureId(), 0, Texture.Type.TwoDimensional);
            gl.glGetTexImage(GL.GL_TEXTURE_2D, 0, GL.GL_BGRA, GL2.GL_UNSIGNED_INT_8_8_8_8_REV, readBuffer.asIntBuffer());
        
            //System.out.println("GetErroe: " + gl.glGetError());
            return (readBuffer);
        }
    }
}
