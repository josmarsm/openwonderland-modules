/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.fairbooth.client;

import com.jme.image.Texture;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.wonderbuilders.modules.colortheme.client.ColorThemeComponent;
import com.wonderbuilders.modules.colortheme.client.ColorThemeComponentConstants;
import com.wonderbuilders.modules.colortheme.common.ColorTheme;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.processor.WorkProcessor;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellChildrenChangeListener;
import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.modules.fairbooth.common.FairBoothConstants;
import org.jdesktop.wonderland.modules.fairbooth.common.FairBoothProperties;
import org.jdesktop.wonderland.modules.imageframe.client.ImageFrameCell;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;

/**
 *
 * @author Nilang
 */
 public class ChildChangeListener implements CellChildrenChangeListener {

        public FairBoothCell parentCell;
        public Cell boothCell;

        public ChildChangeListener(FairBoothCell parentCell) {
            this.parentCell = parentCell;
        }

        public void childAdded(Cell cell, Cell child) {
            
            if(child.getName().equals("BoothNameFrame")) {
                child.addStatusChangeListener(new ChildChangeListener.CellListener((FairBoothCell)cell));
            }
            
            if(child.getName().equals("BoothNameFramea")) {
                child.addStatusChangeListener(new ChildChangeListener.CellListener((FairBoothCell)cell));
            }
            
            if(child.getName().equals("InfoTextFrame")) {
                child.addStatusChangeListener(new ChildChangeListener.CellListener((FairBoothCell)cell.getParent()));
            }
            
            if(child.getName().equals("Booth Desk")) {
                child.addChildrenChangeListener(this);
                child.addStatusChangeListener(new ChildChangeListener.CellListener((FairBoothCell)cell));
            }
        }

        public void childRemoved(Cell cell, Cell child) {
            
        }
        
        public class CellListener implements CellStatusChangeListener {

            public CellListener() {}
            public FairBoothCell parentCell;

            public CellListener(FairBoothCell parentCell) {
                this.parentCell = parentCell;
            }

            public void cellStatusChanged(Cell cell, CellStatus status) {
                try {   
                    if (cell instanceof Cell) {

                        switch (status) {
                            case VISIBLE:
                                break;
                            case ACTIVE:
                                
                                if(cell.getName().equals("BoothNameFrame") || cell.getName().equals("BoothNameFramea")) {
                                    try {
                                        createBoothName((ImageFrameCell)cell);
                                    } catch (Exception ex) {
                                        Logger.getLogger(ChildChangeListener.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                if(cell.getName().equals("InfoTextFrame")) {
                                    try {
                                        createInfoText((ImageFrameCell)cell,boothCell);
                                    } catch (Exception ex) {
                                        Logger.getLogger(ChildChangeListener.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                
                                //System.out.println("status : "+cell.getName()+" "+cell.getCellID().toString()+" - "+cell.getParent().getCellID().toString());
                                if(cell.getName().equals("Booth Desk")) {
                                    
                                    //apply theme
                                    FairBoothProperties fbp = (FairBoothProperties) parentCell.configMap.get("FairBoothConfig");
                                    SharedStateComponent ssc = parentCell.getCellCache().getEnvironmentCell().getComponent(SharedStateComponent.class);
                                    SharedMapCli smc = ssc.get(ColorThemeComponentConstants.COLOR_THEME_SHARED_MAP);
                                    ColorTheme ct = (ColorTheme) smc.get(FairBoothConstants.colorThemes[fbp.getColorTheme()]);
                                    ColorThemeComponent ctc = cell.getComponent(ColorThemeComponent.class);
                                    //System.out.println("APPLYING COLOR TO TABLE..."+cell.getCellID().toString());
                                    //ctc.previewColor(FairBoothConstants.colorThemes[fbp.getColorTheme()], null,ct.getColorMap());

                                    //find info text frame cell
                                    ImageFrameCell infoFrame = null;
                                    Iterator itr = cell.getChildren().iterator();
                                    while(itr.hasNext()) {
                                        Cell c = (Cell) itr.next();
                                        if(c.getName().equals("InfoTextFrame")) {
                                            infoFrame = (ImageFrameCell) c;
                                            break;
                                        }
                                    }
                                    boothCell = cell;
                                    if(infoFrame!=null)
                                        createInfoText(infoFrame,cell);
                                }
                                
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
        
        public void createBoothName(ImageFrameCell cell) throws URISyntaxException, IOException {
            
            CellRendererJME renderer = (CellRendererJME) cell
                .getCellRenderer(Cell.RendererType.RENDERER_JME);
            RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
            final Node node = (Node) rc.getSceneRoot().getChild("Image Frame BackGround");
            if(node!=null) {
                final Quad oldQuad = (Quad) node.getChild("Image Frame quad");
                int width=630;
                int height=130;
                String bn = parentCell.boothName;
                if(parentCell.configMap!=null) {
                    FairBoothProperties fbp = (FairBoothProperties) parentCell.configMap.get("FairBoothConfig");
                    if(fbp!=null)
                        //bn = fbp.getBoothName();
                        bn = fbp.getBoothName();
                }

                // create font of booth name
                Font font = new Font("Arial", Font.BOLD,69);
                FontRenderContext frc1 = new FontRenderContext(null, true, true);
                Rectangle2D rec = font.getStringBounds(bn, frc1);
                int w = (int) (rec.getWidth()+10);
                int h = (int) (rec.getHeight()+10);
                BufferedImage untitleImage = new BufferedImage((int)w, (int)h, BufferedImage.TRANSLUCENT);

                FontRenderContext frc = ((Graphics2D)untitleImage.getGraphics()).getFontRenderContext();
                rec = font.getStringBounds(bn, frc);
                TextLayout layout = new TextLayout(bn, font, frc);
                double x = ((double)untitleImage.getWidth()/(double)2)-((double)rec.getWidth()/(double)2);
                double y= ((double)untitleImage.getHeight()/(double)2)+((double)rec.getHeight()/(double)4);
                layout.draw(((Graphics2D)untitleImage.getGraphics()), (float)x, (float)y);

                //create texture of booth name
                Texture texture =  TextureManager.loadTexture(Toolkit.getDefaultToolkit()
                                .createImage(untitleImage.getSource()),Texture.MinificationFilter.NearestNeighborNoMipMaps
                                ,Texture.MagnificationFilter.NearestNeighbor,true);

                texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
                texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
                texture.setWrap(Texture.WrapMode.BorderClamp);
                texture.setApply(Texture.ApplyMode.Replace);
                width = w;
                height = h;
                final int ww = width;
                final int hh = height;
                TextureState ts = (TextureState) oldQuad.getRenderState(RenderState.StateType.Texture);
                ts.setTexture(texture);
                oldQuad.setRenderState(ts);
                SceneWorker.addWorker(new WorkProcessor.WorkCommit() {
                    public void commit() {
                        oldQuad.resize(ww, hh);
                        oldQuad.updateModelBound();
                        ClientContextJME.getWorldManager().addToUpdateList(oldQuad);
                    }
                });
            }
    }
        
    public void createInfoText(ImageFrameCell cell,Cell deskCell) throws URISyntaxException, IOException {

        if(cell!=null) {
            CellRendererJME renderer = (CellRendererJME) cell
                .getCellRenderer(Cell.RendererType.RENDERER_JME);
            if(renderer!=null) {
                if(renderer.getEntity()!=null) {
                    RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
                    final Node node = (Node) rc.getSceneRoot().getChild("Image Frame BackGround");
                    if(node!=null) {
                        final Quad oldQuad = (Quad) node.getChild("Image Frame quad");
                        final int width=250;
                        final int height=80;
                        String name = parentCell.infoText;
                        if(parentCell.configMap!=null) {
                            FairBoothProperties fbp = (FairBoothProperties) parentCell.configMap.get("FairBoothConfig");
                            if(fbp!=null)
                                name = fbp.getInfoText();
                        }
                        String[] strings = name.split("\n");
                        BufferedImage untitleImage = new BufferedImage((int)width, (int)height, BufferedImage.TRANSLUCENT);
                        int j=0;
                        double yq=0f;
                        
                        //change info text
                        while(j<strings.length) {
                            strings[j] = strings[j].trim();
                            strings[j] = strings[j].replaceAll(",", ", ");
                            strings[j] = strings[j].replaceAll("\\.", ". ");
                            strings[j] = strings[j].replaceAll(":", ": ");
                            strings[j] = strings[j].replaceAll(";", "; ");
                            strings[j] = strings[j].replaceAll("\\)", ") ");
                            strings[j] = strings[j].replaceAll("]", "] ");
                            strings[j] = strings[j].replaceAll("-", "- ");
                            String ss[] = strings[j].split(" ");
                            String finalString="";
                            int i=0;
                            Font fontq=null;
                            FontRenderContext frcq=null;
                            Rectangle2D recq=null;
                            double xq=-1;
                            
                            // create font of info text
                            while(i<ss.length) {
                                fontq = new Font("Arial", Font.BOLD,17);
                                frcq = ((Graphics2D)untitleImage.getGraphics()).getFontRenderContext();
                                recq = fontq.getStringBounds(finalString+ss[i], frcq);
                                if((recq.getWidth()+30)>width) {
                                    recq = fontq.getStringBounds(finalString, frcq);
                                    TextLayout layoutq = new TextLayout(finalString, fontq, frcq);
                                    xq = ((double)untitleImage.getWidth()/(double)2)-((double)recq.getWidth()/(double)2);
                                    if(yq==-1)
                                        yq= ((double)untitleImage.getHeight()/(double)2)-((double)recq.getHeight()/(double)2);
                                    else
                                        yq = yq+17;

                                    layoutq.draw(((Graphics2D)untitleImage.getGraphics()), (float)xq, (float)yq);

                                    finalString="";
                                    continue;
                                }
                                finalString = finalString + ss[i]+" ";
                                i++;
                            }
                            if(finalString.length()!=0){
                                TextLayout layoutq = new TextLayout(finalString, fontq, frcq);
                                xq = ((double)untitleImage.getWidth()/(double)2)-((double)recq.getWidth()/(double)2);
                                yq = yq+17;
                                layoutq.draw(((Graphics2D)untitleImage.getGraphics()), (float)xq, (float)yq);
                            }
                            j++;
                        }

                        //create texture of info text
                        Texture texture =  TextureManager.loadTexture(Toolkit.getDefaultToolkit()
                                        .createImage(untitleImage.getSource()),Texture.MinificationFilter.NearestNeighborNoMipMaps
                                        ,Texture.MagnificationFilter.NearestNeighbor,true);
                        texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
                        texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
                        texture.setWrap(Texture.WrapMode.BorderClamp);
                        texture.setApply(Texture.ApplyMode.Replace);
                        TextureState ts = (TextureState) oldQuad.getRenderState(RenderState.StateType.Texture);
                        ts.setTexture(texture);
                        oldQuad.setRenderState(ts);
                    }
                }
            }
            
            //apply theme
            FairBoothProperties fbp = (FairBoothProperties) parentCell.configMap.get("FairBoothConfig");
            SharedStateComponent ssc = parentCell.getCellCache().getEnvironmentCell().getComponent(SharedStateComponent.class);
            SharedMapCli smc = ssc.get(ColorThemeComponentConstants.COLOR_THEME_SHARED_MAP);
            ColorTheme ct = (ColorTheme) smc.get(FairBoothConstants.colorThemes[fbp.getColorTheme()]);
            ColorThemeComponent ctc = boothCell.getComponent(ColorThemeComponent.class);
            //System.out.println("APPLYING COLOR TO TABLE1..."+boothCell.getCellID().toString());
            if(ctc!=null)
            ctc.previewColor(FairBoothConstants.colorThemes[fbp.getColorTheme()], null,ct.getColorMap());
        }
        
    }    
 }
