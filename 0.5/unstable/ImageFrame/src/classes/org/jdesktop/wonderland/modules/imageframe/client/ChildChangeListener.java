/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.imageframe.client;

import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import org.jdesktop.mtgame.CollisionComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.processor.WorkProcessor;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellChildrenChangeListener;
import org.jdesktop.wonderland.client.cell.CellEditChannelConnection;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.CellStatus;
import static org.jdesktop.wonderland.common.cell.CellStatus.ACTIVE;
import static org.jdesktop.wonderland.common.cell.CellStatus.DISK;
import static org.jdesktop.wonderland.common.cell.CellStatus.VISIBLE;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellDeleteMessage;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameConstants;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameProperties;
import org.jdesktop.wonderland.modules.imageviewer.client.cell.ImageViewerCell;

/**
 *
 */
public class ChildChangeListener implements CellChildrenChangeListener {

    public ImageFrameCell parentCell;
    public Cell removedCell;

    public ChildChangeListener(ImageFrameCell parentCell) {
        this.parentCell = parentCell;
    }

    public void remove() {

        try {
            WonderlandSession session = parentCell.getSession();
            CellEditChannelConnection connection = (CellEditChannelConnection) session.getConnection(CellEditConnectionType.CLIENT_TYPE);
            CellDeleteMessage msg = new CellDeleteMessage(parentCell.getChildren().iterator().next().getCellID());
            connection.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void show(Cell cell) {
        ImageFrameCell ifCell = (ImageFrameCell) cell;
        CellRendererJME cr = (CellRendererJME) ifCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        if(cr!=null && cr.getEntity()!=null) {
            RenderComponent rc = cr.getEntity().getComponent(RenderComponent.class);
            final Node node = (Node) rc.getSceneRoot().getChild("Image Frame BackGround");

            SceneWorker.addWorker(new WorkProcessor.WorkCommit() {

                public void commit() {
                    node.setCullHint(CullHint.Never);
                    ClientContextJME.getWorldManager().addToUpdateList(node);
                }
            });
        }
    }

    public void hide(final Cell cell) {
        SceneWorker.addWorker(new WorkProcessor.WorkCommit() {

            public void commit() {
                ImageFrameCell ifCell = (ImageFrameCell) cell;
                CellRendererJME cr = (CellRendererJME) ifCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            if(cr!=null)
                         {

                RenderComponent rc = cr.getEntity().getComponent(RenderComponent.class);

                Node node = (Node) rc.getSceneRoot().getChild("Image Frame BackGround");

                node.setCullHint(CullHint.Always);
                ClientContextJME.getWorldManager().addToUpdateList(node);
            }}
        });
    }

    public void childAdded(Cell cell, Cell child) {
        try {
            if (cell.getNumChildren() > 1) {
                remove();

            }
            hide(cell);
            final ImageViewerCell ivCell = (ImageViewerCell) child;

            SceneWorker.addWorker(new WorkProcessor.WorkCommit() {

                public void commit() {
                    CellRendererJME cr = (CellRendererJME) ivCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                 if(cr!=null)
                 {
                    RenderComponent rc = cr.getEntity().getComponent(RenderComponent.class);

                    Node node = (Node) rc.getSceneRoot();

                    node.setCullHint(CullHint.Always);
                    ClientContextJME.getWorldManager().addToUpdateList(node);
                }}
            });
            ChildChangeListener.CellListener listener = new ChildChangeListener.CellListener(parentCell);
            child.addStatusChangeListener(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBoxSize(ImageFrameProperties ifp, final Box box, double newAspectRatio, final Node node, TextureState ts, int newWidth, int newHeight) {

        CellTransform parentTrasf = parentCell.getLocalTransform();

        float x = 0f;
        float y = 0f;

        x = (1.32f * (float) newWidth) / (float) 880;
        y = (1.32f * (float) newHeight) / (float) 880;

        final Box newBox = new Box("Box", new Vector3f(0, 0, 0), x, y, box.zExtent);
        box.setRenderState(ts);

        final float xx = x;
        final float yy = y;

        SceneWorker.addWorker(new WorkProcessor.WorkCommit() {

            public void commit() {
                box.updateGeometry(Vector3f.ZERO, xx, yy, box.zExtent);
                box.updateWorldBound();
                box.updateModelBound();
                ClientContextJME.getWorldManager().addToUpdateList(box);
            }
        });
    }

    private void updateBoxSize(ImageFrameProperties ifp, final Box box, double newAspectRatio, final Node node, TextureState ts) {

        CellTransform parentTrasf = parentCell.getLocalTransform();

        float x = 0f;
        float y = 0f;
        if (ifp.getOrientation() == 0) {
            x = 1.32f;
            y = (1.32f) / (float) newAspectRatio;
        } else {
            y = 1.32f;
            x = 1.32f * (float) newAspectRatio;
        }

        final Box newBox = new Box("Box", new Vector3f(0, 0, 0), x, y, box.zExtent);
        box.setRenderState(ts);

        final float xx = x;
        final float yy = y;

        SceneWorker.addWorker(new WorkProcessor.WorkCommit() {

            public void commit() {
                box.updateGeometry(Vector3f.ZERO, xx, yy, box.zExtent);
                box.updateWorldBound();
                box.updateModelBound();
                ClientContextJME.getWorldManager().addToUpdateList(box);
            }
        });
    }

    private void updateSize(final Cell cell) {

        try {
            if (removedCell != null) {
                removedCell = null;
                return;
            }
            int frameWidth = 0;
            int frameHeight = 0;
            double frameAspectRatio = 0;
            double newAspectRation = (double) 1 / (double) 1;

            //get image height & width from texture
            ImageFrameProperties ifp = (ImageFrameProperties) parentCell.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
            ImageViewerCell ivCell = (ImageViewerCell) cell;
            CellRendererJME cr = (CellRendererJME) ivCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            RenderComponent rc = cr.getEntity().getComponent(RenderComponent.class);
            final Node node = (Node) rc.getSceneRoot().getChild("Image Viewer Node");
            final Box box = (Box) rc.getSceneRoot().getChild("Box");
            TextureState ts = (TextureState) box.getRenderState(RenderState.StateType.Texture);
            Image img = ts.getTexture().getImage();
            int originalWidth = img.getWidth();
            int originalHeight = img.getHeight();
            double originalAspectRatio = (double) originalWidth / (double) originalHeight;

            //create buffered image
            URL url = AssetUtils.getAssetURL(ivCell.getImageURI());
            BufferedImage bimage = new BufferedImage(originalWidth, originalHeight, BufferedImage.TRANSLUCENT);
            bimage.getGraphics().drawImage(ImageIO.read(url.openStream()), 0, 0, null);

            if (ifp.getFit() == 0) {
                //change image aspect ration according to frame aspect ratio
                newAspectRation = ImageFrameConstants.daspectRatioArray[ifp.getAspectRatio()];
                frameWidth = 880;
                frameHeight = ((int) ((double) frameWidth / (double) newAspectRation));

                //change aspect ration when orientation is vertical
                if (ifp.getOrientation() == 1) {
                    newAspectRation = (double) 1 / newAspectRation;
                    int k;
                    k = frameHeight;
                    frameHeight = frameWidth;
                    frameWidth = k;
                }

                int newHeight = 0;
                int newWidth = 0;
                BufferedImage finalImage = null;

                //create image of new height & width
                if (originalAspectRatio == newAspectRation) {
                    newHeight = originalHeight;
                    newWidth = originalWidth;
                    //crop image
                    finalImage = bimage.getSubimage(0, 0, newWidth, newHeight);
                } else {
                    if (((int) ((double) newAspectRation * (double) originalHeight)) < originalWidth) {
                        newHeight = originalHeight;
                        newWidth = (int) ((double) newAspectRation * (double) newHeight);
                        //crop image
                        finalImage = bimage.getSubimage(Math.abs(originalWidth - newWidth) / 2, 0, newWidth, newHeight);
                    } else {
                        newWidth = originalWidth;
                        newHeight = (int) (((double) 1 / (double) newAspectRation) * (double) newWidth);
                        //crop image
                        finalImage = bimage.getSubimage(0, Math.abs(originalHeight - newHeight) / 2, newWidth, newHeight);
                    }
                }

                //create new texture
                ts.removeTexture(ts.getTexture());
                Texture texture = TextureManager.loadTexture(Toolkit.getDefaultToolkit().createImage(finalImage.getSource()), Texture.MinificationFilter.NearestNeighborNoMipMaps, Texture.MagnificationFilter.NearestNeighbor, true);
                texture.setWrap(Texture.WrapMode.BorderClamp);
                texture.setTranslation(new Vector3f());
                ts.setTexture(texture);

                //create new Box with new texture
                updateBoxSize(ifp, box, newAspectRation, node, ts);

                //set pickable false for image
                CollisionComponent cc = cr.getEntity().getComponent(CollisionComponent.class);
                cc.setPickable(false);

            } else {
                //preserve aspect ration
                newAspectRation = originalAspectRatio;
                frameWidth = 880;
                frameAspectRatio = ImageFrameConstants.daspectRatioArray[ifp.getAspectRatio()];
                frameHeight = ((int) ((double) frameWidth / (double) frameAspectRatio));

                if (ifp.getOrientation() == 1) {
                    //newAspectRation = (double)1/newAspectRation;
                    int k;
                    k = frameHeight;
                    frameHeight = frameWidth;
                    frameWidth = k;
                }

                int newHeight = 0;
                int newWidth = 0;

                if (ifp.getFit() == 1) {
                    newHeight = frameHeight;
                    newWidth = (int) ((double) newHeight * (double) newAspectRation);
                } else {
                    newWidth = frameWidth;
                    newHeight = (int) ((double) newWidth / (double) newAspectRation);
                }

                //change frame size according to image size
                if (ifp.getFit() == 1) {
                    changeFrameSize(newWidth, frameHeight);
                } else {
                    changeFrameSize(frameWidth, newHeight);
                }

                //update box size
                updateBoxSize(ifp, box, newAspectRation, node, ts, newWidth, newHeight);

                //set pickable false for image
                CollisionComponent cc = cr.getEntity().getComponent(CollisionComponent.class);
                cc.setPickable(false);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeFrameSize(int newWidth, int newHeight) {

        CellRendererJME renderer = (CellRendererJME) parentCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
        final Node node = (Node) rc.getSceneRoot().getChild("Image Frame BackGround");
        Quad quad = (Quad) node.getChild("Image Frame quad");

        float width = newWidth;
        float height = newHeight;

        final Quad newQuad = new Quad("Image Frame quad", width, height);
        newQuad.setModelBound(quad.getModelBound());
        newQuad.setRenderState(quad.getRenderState(RenderState.StateType.Texture));
        newQuad.setRenderState(quad.getRenderState(RenderState.StateType.ZBuffer));
        newQuad.setRenderState(quad.getRenderState(RenderState.StateType.Blend));
        SceneWorker.addWorker(new WorkProcessor.WorkCommit() {

            public void commit() {
                node.detachChildNamed("Image Frame quad");
                node.attachChild(newQuad);
                node.updateModelBound();
                ClientContextJME.getWorldManager().addToUpdateList(node);
            }
        });
    }

    public void childRemoved(Cell cell, Cell child) {
        removedCell = cell;
        if (cell.getNumChildren() == 0) {
            show(cell);
        }

    }

    public class CellListener implements CellStatusChangeListener {

        public CellListener() {
        }
        public ImageFrameCell parentCell;

        public CellListener(ImageFrameCell parentCell) {
            this.parentCell = parentCell;
        }

        public void cellStatusChanged(final Cell cell, CellStatus status) {
            try {

                if (cell instanceof Cell) {

                    switch (status) {
                        case ACTIVE:
                            updateSize(cell);
                            final ImageViewerCell ivCell = (ImageViewerCell) cell;
                         
                            SceneWorker.addWorker(new WorkProcessor.WorkCommit() {

                                public void commit() {
                                    CellRendererJME cr = (CellRendererJME) ivCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                                   if(cr!=null){
                                    RenderComponent rc = cr.getEntity().getComponent(RenderComponent.class);

                                    Node node = (Node) rc.getSceneRoot();

                                    node.setCullHint(CullHint.Never);
                                    ClientContextJME.getWorldManager().addToUpdateList(node);
                                    }
                                   }
                            });
                            break;
                        case VISIBLE:
                            break;
                        case DISK:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
