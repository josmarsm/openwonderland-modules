/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.videocube.client;

import com.jme.bounding.BoundingBox;
import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.geom.BufferUtils;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
import org.jdesktop.wonderland.modules.videoplayer.client.FrameListener;
import org.jdesktop.wonderland.modules.videoplayer.client.VideoPlayer;
import org.jdesktop.wonderland.modules.videoplayer.client.VideoPlayer.VideoPlayerState;
import org.jdesktop.wonderland.modules.videoplayer.client.VideoPlayerImpl;
import org.jdesktop.wonderland.modules.videoplayer.client.VideoStateListener;

public class VideoCubeRenderer extends BasicRenderer {
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 512;

    public enum Side {
        TOP(   new Vector3f(0f, 0.5f, 0f),
               new Quaternion(new float[] { (float) Math.toRadians(90), 0, 0 })),
        BOTTOM(new Vector3f(0f, -0.5f, 0f),
               new Quaternion(new float[] { (float) Math.toRadians(-90), 0, 0 })),
        FRONT( new Vector3f(0f, 0f, 0.5f),
               new Quaternion(new float[] { 0, 0, 0 })),
        BACK(  new Vector3f(0f, 0f, -0.5f),
               new Quaternion(new float[] { 0, (float) Math.toRadians(180), 0 })),
        LEFT(  new Vector3f(-0.5f, 0f, 0f),
               new Quaternion(new float[] { 0, (float) Math.toRadians(-90), 0 })),
        RIGHT( new Vector3f(0.5f, 0f, 0f),
               new Quaternion(new float[] { 0, (float) Math.toRadians(90), 0 }));
            
        private final Vector3f position;
        private final Quaternion rotation;
        
        Side(Vector3f position, Quaternion rotation) {
            this.position = position;
            this.rotation = rotation;
        }

        public Vector3f position() {
            return position;
        }

        public Quaternion rotation() {
            return rotation;
        }
    }

    private final EnumMap<Side, Quad> quads;
    private final EnumMap<Side, Texture> textures;
    
    private ProcessorCollectionComponent processors;
    private Node node = null;
    
    public VideoCubeRenderer(Cell cell) {
        super(cell);

        quads = new EnumMap<Side, Quad>(Side.class);
        textures = new EnumMap<Side, Texture>(Side.class);
    }

    public void attach(Side side, VideoPlayerImpl player) {
        Quad quad = getQuad(side);
        Texture texture = getTexture(side);

        processors.addProcessor(new VideoProcessor(quad, texture, player));
    }

    protected Quad getQuad(Side side) {
        return quads.get(side);
    }

    protected Texture getTexture(Side side) {
        return textures.get(side);
    }

    protected Quad createQuad(Side side) {
        Quad quad = new Quad(side.name(), 1f, 1f);
        quad.setLocalTranslation(side.position());
        quad.setLocalRotation(side.rotation());

        return quad;
    }

    protected Texture createTexture(Side side, Quad quad) {
        int width = TEXTURE_WIDTH;
        int height = TEXTURE_HEIGHT;

        // Create the buffered image using dummy data to initialize it
        ByteBuffer data = BufferUtils.createByteBuffer(width * height * 3);
        Image image = new Image(Image.Format.RGB8, width, height, data);

        // Create the texture which wraps the image
        Texture texture = new Texture2D();
        logger.fine("Created new texture " + texture);
        texture.setImage(image);
        texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
        texture.setApply(Texture.ApplyMode.Replace);

        // apply the texture to the quad
        RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
        TextureState ts = (TextureState) rm.createRendererState(StateType.Texture);
        ts.setTexture(texture);
        ts.setEnabled(true);
        quad.setRenderState(ts);

        return texture;
    }

    protected Node createSceneGraph(Entity entity) {
        node = new Node();

        for (Side side : Side.values()) {
            Quad quad = createQuad(side);
            Texture texture = createTexture(side, quad);

            quads.put(side, quad);
            textures.put(side, texture);

            node.attachChild(quad);
        }

        node.setModelBound(new BoundingBox());
        node.updateModelBound();
        node.setName("Cell_"+cell.getCellID()+":"+cell.getName());

        processors = entity.getComponent(ProcessorCollectionComponent.class);
        if (processors == null) {
            processors = new ProcessorCollectionComponent();
            entity.addComponent(ProcessorCollectionComponent.class, processors);
        }

        processors.addProcessor(new SpinProcessor(node));

        return node;
    }

    class SpinProcessor extends ProcessorComponent {
        private final Node node;

        private long lastTime = System.currentTimeMillis();
        private float x = 0;
        private float y = 0;
        private float z = (float) (Math.PI / 4f);

        public SpinProcessor(Node node) {
            this.node = node;
        }

        @Override
        public void compute(ProcessorArmingCollection pac) {
            long time = System.currentTimeMillis();
            long dTime = time - lastTime;

            float rot = (float) (Math.PI * ((float) dTime / 5000f));
            x += rot;
            y += rot;
            //z += rot;

            lastTime = time;
        }

        @Override
        public void commit(ProcessorArmingCollection pac) {
            node.setLocalRotation(new Quaternion(new float[] { x, y, z }));
            WorldManager.getDefaultWorldManager().addToUpdateList(node);
        }

        @Override
        public void initialize() {
            setArmingCondition(new NewFrameCondition(this));
        }
    }

    class VideoProcessor extends ProcessorComponent
        implements FrameListener, VideoStateListener
    {
        private final Quad quad;
        private final Texture texture;
        private final VideoPlayerImpl player;

        private IVideoResampler resampler;
        private IVideoPicture textureFrame;
        private FrameQueue frameQueue;
        private Image image;

        public VideoProcessor(Quad quad, Texture texture, VideoPlayerImpl player) {
            this.quad = quad;
            this.texture = texture;
            this.player = player;

            player.addFrameListener(this);
            player.addStateListener(this);
        }

        @Override
        public void initialize() {
        }

        @Override
        public void compute(ProcessorArmingCollection pac) {
            if (frameQueue == null) {
                return;
            }

            IVideoPicture frame = frameQueue.nextFrame();
            if (frame == null) {
                return;
            }

            // resample into the texture frame
            resampler.resample(textureFrame, frame);

            // create an image with the data from the resampler
            image = new Image(Image.Format.RGB8, textureFrame.getWidth(),
                              textureFrame.getHeight(), textureFrame.getByteBuffer());
        }

        @Override
        public void commit(ProcessorArmingCollection pac) {
            if (image == null) {
                return;
            }

            // now draw the updated texture
            DisplaySystem.getDisplaySystem().getRenderer().updateTextureSubImage(
                    texture, 0, 0, image, 0, 0, image.getWidth(), image.getHeight());
            image = null;
        }

        public void openVideo(int videoWidth, int videoHeight,
                              IPixelFormat.Type videoType)
        {
            resampler = IVideoResampler.make(TEXTURE_WIDTH, TEXTURE_HEIGHT,
                                             IPixelFormat.Type.RGB24,
                                             videoWidth, videoHeight, videoType);

            textureFrame = IVideoPicture.make(IPixelFormat.Type.RGB24,
                                              TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        public void previewFrame(IVideoPicture ivp) {
        }

        public void playVideo(FrameQueue frameQueue) {
            this.frameQueue = frameQueue;
            setRunning(true);
        }

        public void stopVideo() {
            setRunning(false);
        }

        public void closeVideo() {
            this.frameQueue = null;
            setRunning(false);
        }

        public void mediaStateChanged(VideoPlayerState oldState,
                                      VideoPlayerState newState)
        {
            if (newState == VideoPlayerState.MEDIA_READY) {
                player.play();
            }
        }

        private void setRunning(boolean running) {
            if (running) {
                setArmingCondition(new NewFrameCondition(this));
            } else {
                setArmingCondition(null);
            }
        }
    }
}
