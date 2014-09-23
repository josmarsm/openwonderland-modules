/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.imageframe.client;

import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Iterator;
import javax.imageio.ImageIO;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.processor.WorkProcessor;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameConstants;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameProperties;
import org.jdesktop.wonderland.modules.imageviewer.client.cell.ImageViewerCell;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;

/**
 *
 */
public class PropertyMapChangeListener implements SharedMapListenerCli {

    private ImageFrameCell parentCell;
    private int oldFit;
    private int oldAspectRation;
    private int oldOrientation;
    private int newFit;
    private int newAspectRation;
    private int newOrientation;
    
    public PropertyMapChangeListener(ImageFrameCell parentCell) {
        this.parentCell = parentCell;
    }
    
    public void propertyChanged(SharedMapEventCli smec) {
        
        ImageFrameProperties newIfp = (ImageFrameProperties) smec.getNewValue();
        ImageFrameProperties oldIfp = (ImageFrameProperties) smec.getOldValue();
        
        if(newIfp!=null) {
            newFit = newIfp.getFit();
            newAspectRation = newIfp.getAspectRatio();
            newOrientation = newIfp.getOrientation();
        }
        
        if(oldIfp!=null) {
            oldFit = oldIfp.getFit();
            oldAspectRation = oldIfp.getAspectRatio();
            oldOrientation = oldIfp.getOrientation();
        }
        
        if(oldIfp!=null) {
            if(newAspectRation!=oldAspectRation) {
                changeAspectRatio(ImageFrameConstants.aspectRatioArray[newAspectRation],newOrientation);
            }
            ImageViewerCell ivCell = null;
            Iterator it = parentCell.getChildren().iterator();
            while(it.hasNext()) {
                Cell cell = (Cell) it.next();
                if(cell instanceof ImageViewerCell) {
                    ivCell = (ImageViewerCell) cell;
                }
            }
            if(ivCell!=null)
                updateSize(ivCell);
        } else {
            if(newAspectRation!=0) {
                changeAspectRatio(ImageFrameConstants.aspectRatioArray[newAspectRation],newOrientation);
            } 
        }
        
        if(newIfp.getIsRemoveImage()) {
            if(!smec.getSenderID().toString().equals(parentCell.getCellID().toString())) {
                changeAspectRatio(ImageFrameConstants.aspectRatioArray[newIfp.getAspectRatio()],0);
                ImageFrameProperties ifp = (ImageFrameProperties) parentCell.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
                ifp.setIsRemoveImage(false);
            }
        }
        
                
    }
    
    public void changeAspectRatio(final String aspectRatio,int newOrientation) {
        CellRendererJME renderer = (CellRendererJME) parentCell
                .getCellRenderer(Cell.RendererType.RENDERER_JME);
        RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
        final Node node = (Node) rc.getSceneRoot().getChild("Image Frame BackGround");
        if(node!=null) {
        Quad quad = (Quad) node.getChild("Image Frame quad");
        
        float width=880f;
        float height=880f;
        if(aspectRatio.equals(ImageFrameConstants.aspectRatioArray[0])) {
            width = 880f;
            height = 880f;
        } else if(aspectRatio.equals(ImageFrameConstants.aspectRatioArray[1])) {
            double ar = (double)5/(double)4;
            width = 880f;
            height = ((int)((double)width/(double)ar));
        } else if(aspectRatio.equals(ImageFrameConstants.aspectRatioArray[2])) {
            double ar = (double)4/(double)3;
            width = 880f;
            height = ((int)((double)width/(double)ar));
        } else if(aspectRatio.equals(ImageFrameConstants.aspectRatioArray[3])){
            double ar = (double)16/(double)9;
            width = 880f;
            height = ((int)((double)width/(double)ar));
        }
        else
        {    double ar = (double)2/(double)3;
            width = 880f;
            height = ((int)((double)width/(double)ar));

        }
        if(newOrientation==1) {
            float k = height;
            height = width;
            width = k;
        }
        
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
    }
    
    private void updateSize(final Cell cell) {
        try {
            int frameWidth=0;
            int frameHeight=0;
            double frameAspectRatio = 0;
            double newAspectRation = (double)1/(double)1;

            //get image height & width from texture
            ImageFrameProperties ifp = (ImageFrameProperties) parentCell.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
            ImageViewerCell ivCell = (ImageViewerCell) cell;
            
            CellRendererJME cr = (CellRendererJME) ivCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            RenderComponent rc = cr.getEntity().getComponent(RenderComponent.class);
            final Node node = (Node) rc.getSceneRoot().getChild("Image Viewer Node");
            final Box box = (Box) rc.getSceneRoot().getChild("Box");
            TextureState ts = (TextureState) box.getRenderState(RenderState.StateType.Texture);

            //create buffered image
            URL url = AssetUtils.getAssetURL(ivCell.getImageURI());
            BufferedImage bimage = ImageIO.read(url.openStream());
            int originalWidth = bimage.getWidth();
            int originalHeight = bimage.getHeight();
            double originalAspectRatio = (double)originalWidth/(double)originalHeight;
            if(ifp.getFit()==0) {
                //--for Fit Image--//
                //change image aspect ration according to frame aspect ratio
                newAspectRation = ImageFrameConstants.daspectRatioArray[ifp.getAspectRatio()];
                frameWidth = 880;
                frameHeight = ((int)((double)frameWidth/(double)newAspectRation));

                //change aspect ration when orientation is vertical
                if(ifp.getOrientation()==1) {
                    newAspectRation = (double)1/newAspectRation;
                    int k;
                    k=frameHeight;
                    frameHeight=frameWidth;
                    frameWidth=k;
                }

                int newHeight = 0;
                int newWidth = 0;
                BufferedImage finalImage=null;

                //calculate new height & width
                if(originalAspectRatio==newAspectRation) {
                    newHeight=originalHeight;
                    newWidth=originalWidth;
                    //crop image
                    finalImage = bimage.getSubimage(0, 0, newWidth, newHeight);
                } else {
                    if(((int)((double)newAspectRation * (double)originalHeight))<originalWidth) {
                        newHeight = originalHeight;
                        newWidth = (int)((double)newAspectRation * (double)newHeight);
                        //crop image
                        finalImage = bimage.getSubimage(Math.abs(originalWidth-newWidth)/2, 0, newWidth, newHeight);
                    } else {
                        newWidth = originalWidth;
                        newHeight = (int)(((double)1/(double)newAspectRation) * (double)newWidth);
                        //crop image
                        finalImage = bimage.getSubimage(0, Math.abs(originalHeight-newHeight)/2, newWidth, newHeight);
                    }
                }

                //create new texture
                ts.removeTexture(ts.getTexture());
                Texture texture = TextureManager.loadTexture(Toolkit.getDefaultToolkit()
                        .createImage(finalImage.getSource()),Texture.MinificationFilter.NearestNeighborNoMipMaps
                        ,Texture.MagnificationFilter.NearestNeighbor,true);
                texture.setWrap(Texture.WrapMode.BorderClamp);
                texture.setTranslation(new Vector3f());
                ts.setTexture(texture);

                //change frame size according to image size
                changeFrameSize(frameWidth, frameHeight);
                
                //create new Box with new texture
                updateBoxSize(ifp, box, newAspectRation, node, ts);
                
            } else {
                //--For constrain height/width--//
                newAspectRation = originalAspectRatio;
                frameWidth = 880;
                frameAspectRatio=ImageFrameConstants.daspectRatioArray[ifp.getAspectRatio()];
                frameHeight = ((int)((double)frameWidth/(double)frameAspectRatio));

                if(ifp.getOrientation()==1) {
                    //newAspectRation = (double)1/newAspectRation;
                    int k;
                    k=frameHeight;
                    frameHeight=frameWidth;
                    frameWidth=k;
                }
                
                int newHeight = 0;
                int newWidth = 0;

                if(ifp.getFit()==1) {
                    newHeight=frameHeight;
                    newWidth=(int)((double)newHeight*(double)newAspectRation);
                } else {
                    newWidth=frameWidth;
                    newHeight=(int)((double)newWidth/(double)newAspectRation);
                }
                //create new texture
                ts.removeTexture(ts.getTexture());
                Texture texture = TextureManager.loadTexture(Toolkit.getDefaultToolkit()
                        .createImage(bimage.getSource()),Texture.MinificationFilter.NearestNeighborNoMipMaps
                        ,Texture.MagnificationFilter.NearestNeighbor,true);
                texture.setWrap(Texture.WrapMode.BorderClamp);
                texture.setTranslation(new Vector3f());
                ts.setTexture(texture);
                
                //change frame size according to image size
                if(ifp.getFit()==1) {
                    changeFrameSize(newWidth, frameHeight);
                } else {
                    changeFrameSize(frameWidth, newHeight);
                }

                //update box size
                updateBoxSize(ifp, box, newAspectRation, node, ts,newWidth,newHeight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void changeFrameSize(int newWidth,int newHeight) {
            
        CellRendererJME renderer = (CellRendererJME) parentCell
                .getCellRenderer(Cell.RendererType.RENDERER_JME);
        RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
        final Node node = (Node) rc.getSceneRoot().getChild("Image Frame BackGround");
        Quad quad = (Quad) node.getChild("Image Frame quad");

        float width=newWidth;
        float height=newHeight;

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
    
    private void updateBoxSize(ImageFrameProperties ifp,final Box box
                ,double newAspectRatio,final Node node,TextureState ts,int newWidth,int newHeight) {
        float x=0f;
        float y=0f;
         
        x=1.32f*(float)newWidth/(float)880;
        y=1.32f*(float)newHeight/(float)880;
        
        final Box newBox = new Box("Box", new Vector3f(0, 0, 0)
                    , x
                    , y, box.zExtent);
        box.setRenderState(ts);

        final float xx =x;
        final float yy=y;
       
        SceneWorker.addWorker(new WorkProcessor.WorkCommit() {
            public void commit() {
                box.updateGeometry(Vector3f.ZERO, xx, yy, box.zExtent);
                box.updateWorldBound();
                box.updateModelBound();
                ClientContextJME.getWorldManager().addToUpdateList(box);
            }
        });
    }
    private void updateBoxSize(ImageFrameProperties ifp,final Box box
                ,double newAspectRatio,final Node node,TextureState ts) {
        float x=0f;
        float y=0f;
        if(ifp.getOrientation()==0) {
            x=1.32f;
            y=1.32f/(float)newAspectRatio;
        } else {
            y=1.32f;
            x=1.32f*(float)newAspectRatio;
        }
        final Box newBox = new Box("Box", new Vector3f(0, 0, 0)
                    , x
                    , y, box.zExtent);
        box.setRenderState(ts);

        final float xx =x;
        final float yy=y;
        SceneWorker.addWorker(new WorkProcessor.WorkCommit() {
            public void commit() {
                box.updateGeometry(Vector3f.ZERO, xx, yy, box.zExtent);
                box.updateWorldBound();
                box.updateModelBound();
                ClientContextJME.getWorldManager().addToUpdateList(box);
            }
        });
    }
    
}
