/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.fairbooth.client;

import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.processor.WorkProcessor;
import org.jdesktop.wonderland.client.cell.*;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.fairbooth.client.jme.cellrenderer.FairBoothCellRenderer;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.*;
import org.jdesktop.wonderland.common.cell.messages.*;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.modules.fairbooth.common.FairBoothClientState;
import org.jdesktop.wonderland.modules.fairbooth.common.FairBoothConstants;
import org.jdesktop.wonderland.modules.fairbooth.common.FairBoothProperties;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedStateComponent;
import org.jdesktop.wonderland.modules.imageframe.client.*;
import org.jdesktop.wonderland.modules.imageframe.common.*;


/**
 *
 * @author Nilang
 */
public class FairBoothCell extends Cell {

    @UsesCellComponent
    public SharedStateComponent sharedState;
    public SharedMapCli configMap;
    private FairBoothCellRenderer renderer = null;
    String boothName = "Untitled Booth - "+this.getCellID().toString();
    private int colorTheme = 0;
    String infoText = "Untitled";
    private int leftPanelFrames = 1;
    private int rightPanelFrames = 1;
    @UsesCellComponent
    ColorThemeComponent ctc;

    public FairBoothCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        this.addChildrenChangeListener(new ChildChangeListener(this));
    }
    
    @Override
    public void setClientState(CellClientState clientState) {
        super.setClientState(clientState);
        boothName = ((FairBoothClientState) clientState).getBoothName();
        colorTheme = ((FairBoothClientState) clientState).getColorTheme();
        infoText = ((FairBoothClientState) clientState).getInfoText();
        leftPanelFrames = ((FairBoothClientState) clientState).getLeftPanelFrames();
        rightPanelFrames = ((FairBoothClientState) clientState).getRightPanelFrames();
    }

    @Override
    public void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        if (status == CellStatus.INACTIVE && increasing == false) {
            
        } else if (status == CellStatus.ACTIVE && increasing == true) {
            
        } else if (status == CellStatus.RENDERING && increasing == true) {

            //Set Initial Properties
            configMap = sharedState.get(FairBoothConstants.configMap);
            configMap.addSharedMapListener(new PropertyChangeListener(this));
            configMap = sharedState.get(FairBoothConstants.configMap);
            if (configMap.size() == 0) {
                FairBoothProperties fbp = new FairBoothProperties();
                fbp.setBoothName(boothName);
                fbp.setColorTheme(colorTheme);
                fbp.setLeftPanelFrames(leftPanelFrames);
                fbp.setRightPanelFrames(rightPanelFrames);
                fbp.setInfoText(infoText);
                fbp.setBoothNameFrame(false);
                configMap.put("FairBoothConfig", fbp);
                int i = 0;
                Map<String, ColorTheme> themes = new HashMap<String, ColorTheme>();
                SharedStateComponent ssc = getCellCache().getEnvironmentCell().getComponent(SharedStateComponent.class);
                if (ssc != null) {
                    SharedMapCli smc = ssc.get(ColorThemeComponentConstants.COLOR_THEME_SHARED_MAP);
                    if (smc != null) {
                        for (String theme : FairBoothConstants.colorThemes) {
                            ColorTheme ct = new ColorTheme(theme);
                            Map<String, String> themeMap = new HashMap<String, String>();
                            themeMap = getThemeMap(i);
                            ct.setColorMap(themeMap);
                            if ((ColorTheme) smc.get(theme) == null) {
                                themes.put(theme, ct);
                            }
                            i++;
                        }
                        smc.putAll(themes);
                        ColorTheme currTheme = (ColorTheme) smc.get(FairBoothConstants.colorThemes[colorTheme]);
                        if (ctc != null) {
                            ctc.previewColor(FairBoothConstants.colorThemes[colorTheme], null, currTheme.getColorMap());
                        }
                    }
                }
            } else {
                if (configMap != null) {
                    FairBoothProperties fbp = (FairBoothProperties) configMap.get("FairBoothConfig");
                    SharedStateComponent ssc = getCellCache().getEnvironmentCell().getComponent(SharedStateComponent.class);
                    if (ssc != null) {
                        SharedMapCli smc = ssc.get(ColorThemeComponentConstants.COLOR_THEME_SHARED_MAP);
                        if (smc != null) {
                            ColorTheme ct = (ColorTheme) smc.get(FairBoothConstants.colorThemes[colorTheme]);
                            if (ct != null) {
                                ctc.previewColor(FairBoothConstants.colorThemes[fbp.getColorTheme()], null, ct.getColorMap());
                            }
                        }
                    }
                }
            }

        }
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            this.renderer = new FairBoothCellRenderer(this);
            return this.renderer;
        } else {
            return super.createCellRenderer(rendererType);
        }
    }

    private void changeBoothNameForCell(String name, ImageFrameCell cell) throws IOException {
        CellRendererJME renderer = (CellRendererJME) cell
                .getCellRenderer(Cell.RendererType.RENDERER_JME);
        if (renderer != null) {
            if (renderer.getEntity() != null) {
                RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
                final Node node = (Node) rc.getSceneRoot().getChild("Image Frame BackGround");
                if (node != null) {
                    final Quad oldQuad = (Quad) node.getChild("Image Frame quad");
                    int width = 630;
                    int height = 130;
                    String bn = name;

                    //create font of booth name
                    Font font = new Font("Arial", Font.BOLD, 69);
                    FontRenderContext frc1 = new FontRenderContext(null, true, true);
                    Rectangle2D rec = font.getStringBounds(bn, frc1);
                    int w = (int) (rec.getWidth() + 10);
                    int h = (int) (rec.getHeight() + 10);
                    BufferedImage untitleImage = new BufferedImage((int) w, (int) h, BufferedImage.TRANSLUCENT);

                    FontRenderContext frc = ((Graphics2D) untitleImage.getGraphics()).getFontRenderContext();
                    rec = font.getStringBounds(bn, frc);
                    TextLayout layout = new TextLayout(bn, font, frc);
                    double x = ((double) untitleImage.getWidth() / (double) 2) - ((double) rec.getWidth() / (double) 2);
                    double y = ((double) untitleImage.getHeight() / (double) 2) + ((double) rec.getHeight() / (double) 4);
                    layout.draw(((Graphics2D) untitleImage.getGraphics()), (float) x, (float) y);

                    //create new texture with new booth name
                    Texture texture = TextureManager.loadTexture(Toolkit.getDefaultToolkit()
                            .createImage(untitleImage.getSource()), Texture.MinificationFilter.NearestNeighborNoMipMaps, Texture.MagnificationFilter.NearestNeighbor, true);

                    texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
                    texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
                    texture.setWrap(Texture.WrapMode.BorderClamp);
                    texture.setApply(Texture.ApplyMode.Replace);
                    width = (int) (w);
                    height = (int) (h);
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
        }
    }

    public void changeBoothName(String name) throws URISyntaxException, IOException {
        ImageFrameCell cell = null;
        ImageFrameCell cell1 = null;
        Iterator itr = getChildren().iterator();
        while (itr.hasNext()) {
            Cell c = (Cell) itr.next();
            if (c.getName().equals("BoothNameFrame")) {
                cell = (ImageFrameCell) c;
            }
            if (c.getName().equals("BoothNameFramea")) {
                cell1 = (ImageFrameCell) c;
            }
        }
        if (cell != null) {
            changeBoothNameForCell(name, cell);
        }
        if (cell1 != null) {
            changeBoothNameForCell(name, cell1);
        }

    }

    public void changeInfoText(String name) throws URISyntaxException, IOException {

        ImageFrameCell cell = null;
        Cell deskCell = null;
        
        //find booth desk cell
        Iterator itr = getChildren().iterator();
        while (itr.hasNext()) {
            Cell c = (Cell) itr.next();
            if (c.getName().equals("Booth Desk")) {
                deskCell = c;
                break;
            }
        }
        
        //find info text image frame cell
        if (deskCell != null) {
            itr = deskCell.getChildren().iterator();
            while (itr.hasNext()) {
                Cell c = (Cell) itr.next();
                if (c.getName().equals("InfoTextFrame")) {
                    cell = (ImageFrameCell) c;
                    break;
                }
            }
        }


        if (cell != null) {
            CellRendererJME renderer = (CellRendererJME) cell
                    .getCellRenderer(Cell.RendererType.RENDERER_JME);
            if (renderer != null) {
                if (renderer.getEntity() != null) {
                    RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
                    final Node node = (Node) rc.getSceneRoot().getChild("Image Frame BackGround");
                    if (node != null) {
                        final Quad oldQuad = (Quad) node.getChild("Image Frame quad");
                        final int width = 250;
                        final int height = 80;
                        String[] strings = name.split("\n");

                        BufferedImage untitleImage = new BufferedImage((int) width, (int) height, BufferedImage.TRANSLUCENT);
                        int j = 0;
                        double yq = 0f;
                        
                        //change the info text
                        while (j < strings.length) {
                            strings[j] = strings[j].trim();
                            strings[j] = strings[j].replaceAll(",", ", ");
                            strings[j] = strings[j].replaceAll("\\.", ". ");
                            strings[j] = strings[j].replaceAll(":", ": ");
                            strings[j] = strings[j].replaceAll(";", "; ");
                            strings[j] = strings[j].replaceAll("\\)", ") ");
                            strings[j] = strings[j].replaceAll("]", "] ");
                            strings[j] = strings[j].replaceAll("-", "- ");
                            String ss[] = strings[j].split(" ");
                            String finalString = "";
                            int i = 0;
                            Font fontq = null;
                            FontRenderContext frcq = null;
                            Rectangle2D recq = null;
                            double xq = -1;
                            
                            // create font of info text
                            while (i < ss.length) {
                                fontq = new Font("Arial", Font.BOLD, 17);
                                frcq = ((Graphics2D) untitleImage.getGraphics()).getFontRenderContext();
                                recq = fontq.getStringBounds(finalString + ss[i], frcq);
                                if ((recq.getWidth() + 30) > width) {
                                    recq = fontq.getStringBounds(finalString, frcq);
                                    TextLayout layoutq = new TextLayout(finalString, fontq, frcq);
                                    xq = ((double) untitleImage.getWidth() / (double) 2) - ((double) recq.getWidth() / (double) 2);
                                    if (yq == -1) {
                                        yq = ((double) untitleImage.getHeight() / (double) 2) - ((double) recq.getHeight() / (double) 2);
                                    } else {
                                        yq = yq + 17;
                                    }

                                    layoutq.draw(((Graphics2D) untitleImage.getGraphics()), (float) xq, (float) yq);

                                    finalString = "";
                                    continue;
                                }
                                finalString = finalString + ss[i] + " ";
                                i++;
                            }
                            if (finalString.length() != 0) {
                                TextLayout layoutq = new TextLayout(finalString, fontq, frcq);
                                xq = ((double) untitleImage.getWidth() / (double) 2) - ((double) recq.getWidth() / (double) 2);
                                yq = yq + 17;
                                layoutq.draw(((Graphics2D) untitleImage.getGraphics()), (float) xq, (float) yq);
                            }
                            j++;
                        }

                        //create texture with new info text
                        Texture texture = TextureManager.loadTexture(Toolkit.getDefaultToolkit()
                                .createImage(untitleImage.getSource()), Texture.MinificationFilter.NearestNeighborNoMipMaps, Texture.MagnificationFilter.NearestNeighbor, true);

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
        }
    }

    public CellServerState getImageFrameServerState(Properties prop) {
        Iterator<CellFactorySPI> iterator = CellRegistry.getCellRegistry().getAllCellFactories().iterator();
        while (iterator.hasNext()) {
            CellFactorySPI factorySPI = iterator.next();
            if (factorySPI.getDisplayName() != null && factorySPI.getDisplayName().equals("Image Frame")) {
                CellServerState serverState = factorySPI.getDefaultCellServerState(prop);
                return serverState;
            }
        }
        return null;
    }

    private Cell getChildFromName(String childName, Cell parent) {
        Iterator itr = parent.getChildren().iterator();
        while (itr.hasNext()) {
            Cell c = (Cell) itr.next();
            if (c.getName().equals(childName)) {
                return c;
            }
        }
        return null;
    }

    public void removeChildFromName(String cellId) {
        try {
            WonderlandSession session = this.getCellCache().getSession();
            CellEditChannelConnection connection = (CellEditChannelConnection) session.getConnection(CellEditConnectionType.CLIENT_TYPE);
            CellDeleteMessage msg = new CellDeleteMessage(new CellID(Long.parseLong(cellId)));
            connection.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void AddImageFramesInRightPanel(final int nof) {
        int i;
        final float s = 1.13f;
        float f = 0.77f;
        
        //get Right Panel app frame List
        List<String> frameList = new ArrayList<String>();
        Iterator it = this.getChildren().iterator();
        while (it.hasNext()) {
            Cell cell = (Cell) it.next();
            if (cell.getName().contains("rightFrame")) {
                frameList.add(cell.getCellID().toString());
            }
        }
        
        //attach app frames to right panel
        if (nof == 1) {
            CellServerState serverState = null;

            if (frameList.size() != 0) {
                final int old_nof = frameList.size();
                if (old_nof == 2) {
                    Cell c1 = getChildFromName("rightFrame0", this);
                    Cell c2 = getChildFromName("rightFrame1", this);
                    removeChildFromName(c2.getCellID().toString());
                    final ImageFrameCell cell1 = (ImageFrameCell) c1;
                    cell1.setName("rightFrame0");
                    float angles[] = new float[3];
                    angles[0] = 0f;
                    angles[1] = (float) (-3.14 / 2);
                    angles[2] = (float) 0;
                    Quaternion rot = new Quaternion(angles);
                    MovableComponent movableComp = cell1.getComponent(MovableComponent.class);
                    CellTransform cellTransform = cell1.getLocalTransform();
                    cellTransform.setTranslation(new Vector3f(2.71f, -0.52f, -0.46f));
                    cellTransform.setScaling(s);
                    cellTransform.setRotation(rot);
                    movableComp.localMoveRequest(cellTransform);
                    ImageFrameProperties ifp = (ImageFrameProperties) cell1.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
                    ImageFrameProperties newIfp = new ImageFrameProperties();
                    newIfp.setAspectRatio(2);
                    newIfp.setFit(ifp.getFit());
                    newIfp.setOrientation(0);
                    cell1.propertyMap.put(ImageFrameConstants.ImageFrameProperty, newIfp);
                } else {
                    Cell cell1 = getChildFromName("rightFrame0", this);
                    Cell cell2 = getChildFromName("rightFrame1", this);
                    Cell cell3 = getChildFromName("rightFrame2", this);
                    Cell cell4 = getChildFromName("rightFrame3", this);
                    removeChildFromName(cell2.getCellID().toString());
                    removeChildFromName(cell3.getCellID().toString());
                    removeChildFromName(cell4.getCellID().toString());
                    cell1.setName("rightFrame0");
                    MovableComponent movableComp = cell1.getComponent(MovableComponent.class);
                    CellTransform cellTransform = cell1.getLocalTransform();
                    cellTransform.setTranslation(new Vector3f(2.71f, -0.52f, -0.46f));
                    cellTransform.setScaling(s);
                    movableComp.localMoveRequest(cellTransform);
                }
            } else {
                float[] angles = new float[3];
                angles[0] = 0f;
                angles[1] = (float) (-3.14 / 2);
                angles[2] = 0f;
                Quaternion quat = new Quaternion(angles);
                Properties prop = new Properties();
                prop.setProperty("aspectRatio", "2");
                serverState = getImageFrameServerState(prop);
                if (serverState != null) {
                    PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                    if (pcss == null) {
                        pcss = new PositionComponentServerState();
                        serverState.addComponentServerState(pcss);
                    }
                    pcss.setTranslation(new Vector3f(2.71f, -0.52f, -0.46f));
                    pcss.setScaling(new Vector3f(s / nof, 0, 0));
                    pcss.setRotation(quat);
                    CellEditChannelConnection connection = (CellEditChannelConnection) this.getCellCache().getSession().getConnection(CellEditConnectionType.CLIENT_TYPE);
                    serverState.setName("rightFrame0");
                    CellCreateMessage msg = new CellCreateMessage(this.getCellID(), serverState);
                    try {
                        connection.send(msg);
                    } catch (Exception ex) {
                        Logger.getLogger(FairBoothCell.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else if (nof == 2) {
            if (frameList.size() != 0) {
                int old_nof = frameList.size();
                if (old_nof == 1) {
                    Cell cell1 = getChildFromName("rightFrame0", this);
                    float angles[] = new float[3];
                    angles[0] = 0f;
                    angles[1] = (float) (-3.14 / 2);
                    angles[2] = 0f;
                    final Quaternion rot = new Quaternion(angles);
                    final ImageFrameCell cell = (ImageFrameCell) cell1;
                    MovableComponent movableComp = cell.getComponent(MovableComponent.class);
                    CellTransform cellTransform = cell.getLocalTransform();
                    cellTransform.setTranslation(new Vector3f(2.71f, -0.52f, 0.31f));
                    cellTransform.setScaling(0.555f);
                    movableComp.localMoveRequest(cellTransform);
                    ImageFrameProperties ifp = (ImageFrameProperties) cell.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
                    ImageFrameProperties newIfp = new ImageFrameProperties();
                    newIfp.setAspectRatio(4);
                    newIfp.setFit(ifp.getFit());
                    newIfp.setOrientation(0);
                    cell.setName("rightFrame0");
                    cell.propertyMap.put(ImageFrameConstants.ImageFrameProperty, newIfp);
                    f = -f + 0.04f;
                    Properties prop = new Properties();
                    prop.setProperty("aspectRatio", "4");
                    prop.setProperty("orientation", "0");
                    CellServerState serverState = getImageFrameServerState(prop);
                    if (serverState != null) {
                        PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                        if (pcss == null) {
                            pcss = new PositionComponentServerState();
                            serverState.addComponentServerState(pcss);
                        }
                        pcss.setTranslation(new Vector3f(2.71f, -0.52f, -1.22f));
                        pcss.setRotation(rot);
                        pcss.setScaling(new Vector3f(0.555f, 0, 0));
                        CellEditChannelConnection connection = (CellEditChannelConnection) this.getCellCache().getSession().getConnection(CellEditConnectionType.CLIENT_TYPE);
                        serverState.setName("rightFrame1");
                        CellCreateMessage msg = new CellCreateMessage(this.getCellID(), serverState);
                        try {
                            connection.send(msg);
                        } catch (Exception ex) {
                            Logger.getLogger(FairBoothCell.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else if (old_nof == 4) {
                    Cell c1 = getChildFromName("rightFrame0", this);
                    Cell c2 = getChildFromName("rightFrame1", this);
                    Cell c3 = getChildFromName("rightFrame2", this);
                    Cell c4 = getChildFromName("rightFrame3", this);
                    removeChildFromName(c3.getCellID().toString());
                    removeChildFromName(c4.getCellID().toString());
                    final ImageFrameCell cell1 = (ImageFrameCell) c1;
                    final ImageFrameCell cell2 = (ImageFrameCell) c2;
                    MovableComponent movableComp = cell1.getComponent(MovableComponent.class);
                    CellTransform cellTransform = cell1.getLocalTransform();
                    cellTransform.setTranslation(new Vector3f(2.71f, -0.52f, 0.31f));
                    cellTransform.setScaling(0.555f);
                    movableComp.localMoveRequest(cellTransform);
                    ImageFrameProperties ifp = (ImageFrameProperties) cell1.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
                    ImageFrameProperties newIfp = new ImageFrameProperties();
                    newIfp.setAspectRatio(4);
                    newIfp.setFit(ifp.getFit());
                    newIfp.setOrientation(0);
                    cell1.setName("rightFrame0");
                    cell1.propertyMap.put(ImageFrameConstants.ImageFrameProperty, newIfp);
                    f = -f + 0.04f;
                    MovableComponent movableComp1 = cell2.getComponent(MovableComponent.class);
                    CellTransform cellTransform1 = cell2.getLocalTransform();
                    cellTransform1.setTranslation(new Vector3f(2.71f, -0.52f, -1.22f));
                    cellTransform1.setScaling(0.555f);
                    movableComp1.localMoveRequest(cellTransform1);
                    ifp = (ImageFrameProperties) cell2.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
                    ImageFrameProperties newIfp1 = new ImageFrameProperties();
                    newIfp1.setAspectRatio(4);
                    newIfp1.setFit(ifp.getFit());
                    newIfp1.setOrientation(0);
                    cell2.setName("rightFrame1");
                    cell2.propertyMap.put(ImageFrameConstants.ImageFrameProperty, newIfp1);
                }
            } else {
                float offset = 0f;
                for (i = 0; i < nof; i++) {
                    Properties prop = new Properties();
                    prop.setProperty("aspectRatio", "4");
                    prop.setProperty("orientation", "0");
                    CellServerState serverState = getImageFrameServerState(prop);
                    if (serverState != null) {
                        PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                        if (pcss == null) {
                            pcss = new PositionComponentServerState();
                            serverState.addComponentServerState(pcss);
                        }
                        pcss.setTranslation(new Vector3f(2.71f, -0.46f, 0.33f + offset));
                        offset = 1.56f;
                        float angles[] = new float[3];
                        angles[0] = 0f;
                        angles[1] = (float) (-3.14 / 2);
                        angles[2] = 0f;
                        Quaternion rot = new Quaternion(angles);
                        pcss.setRotation(rot);
                        pcss.setScaling(new Vector3f(0.555f, 0, 0));
                        CellEditChannelConnection connection = (CellEditChannelConnection) this.getCellCache().getSession().getConnection(CellEditConnectionType.CLIENT_TYPE);
                        serverState.setName("rightFrame" + i);
                        CellCreateMessage msg = new CellCreateMessage(this.getCellID(), serverState);
                        try {
                            connection.send(msg);
                        } catch (Exception ex) {
                            Logger.getLogger(FairBoothCell.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        f = -f + 0.04f;
                    }
                }
            }
        } else {
            if (frameList.size() != 0) {
                final int old_nof = frameList.size();
                if (old_nof == 1) {
                    Cell c1 = getChildFromName("rightFrame0", this);
                    final ImageFrameCell cell1 = (ImageFrameCell) c1;
                    cell1.setName("rightFrame0");
                    MovableComponent movableComp = cell1.getComponent(MovableComponent.class);
                    CellTransform cellTransform = cell1.getLocalTransform();
                    cellTransform.setTranslation(new Vector3f(2.71f, 0.1f, 0.3f));
                    cellTransform.setScaling(0.525f);
                    movableComp.localMoveRequest(cellTransform);
                    f = -f + 0.04f;
                    Properties prop = new Properties();
                    prop.setProperty("aspectRatio", "2");
                    CellServerState serverState = getImageFrameServerState(prop);
                    if (serverState != null) {
                        PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                        if (pcss == null) {
                            pcss = new PositionComponentServerState();
                            serverState.addComponentServerState(pcss);
                        }
                        pcss.setTranslation(new Vector3f(2.71f, 0.1f, -1.2f));
                        pcss.setScaling(new Vector3f(0.525f, 0, 0));
                        float[] angles = new float[3];
                        angles[0] = 0f;
                        angles[1] = (float) (-3.14 / 2);
                        angles[2] = 0f;
                        Quaternion quat = new Quaternion(angles);
                        pcss.setRotation(quat);
                        CellEditChannelConnection connection = (CellEditChannelConnection) this.getCellCache().getSession().getConnection(CellEditConnectionType.CLIENT_TYPE);
                        serverState.setName("rightFrame1");
                        CellCreateMessage msg = new CellCreateMessage(this.getCellID(), serverState);
                        try {
                            connection.send(msg);
                        } catch (Exception ex) {
                            Logger.getLogger(FairBoothCell.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else if (old_nof == 2) {
                    Cell c1 = getChildFromName("rightFrame0", this);
                    Cell c2 = getChildFromName("rightFrame1", this);
                    final ImageFrameCell cell1 = (ImageFrameCell) c1;
                    final ImageFrameCell cell2 = (ImageFrameCell) c2;
                    ImageFrameProperties ifp = (ImageFrameProperties) cell1.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
                    ImageFrameProperties newIfp = new ImageFrameProperties();
                    newIfp.setAspectRatio(2);
                    newIfp.setFit(ifp.getFit());
                    newIfp.setOrientation(0);
                    cell1.setName("rightFrame0");
                    cell1.propertyMap.put(ImageFrameConstants.ImageFrameProperty, newIfp);
                    ifp = (ImageFrameProperties) cell2.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
                    ImageFrameProperties newIfp1 = new ImageFrameProperties();
                    newIfp1.setAspectRatio(2);
                    newIfp1.setFit(ifp.getFit());
                    newIfp1.setOrientation(0);
                    cell2.setName("rightFrame1");
                    cell2.propertyMap.put(ImageFrameConstants.ImageFrameProperty, newIfp1);
                    MovableComponent movableComp = cell1.getComponent(MovableComponent.class);
                    CellTransform cellTransform = cell1.getLocalTransform();
                    cellTransform.setTranslation(new Vector3f(2.71f, 0.1f, 0.3f));
                    cellTransform.setScaling(0.525f);
                    movableComp.localMoveRequest(cellTransform);
                    f = -f + 0.04f;
                    MovableComponent movableComp1 = cell2.getComponent(MovableComponent.class);
                    CellTransform cellTransform1 = cell2.getLocalTransform();
                    cellTransform1.setTranslation(new Vector3f(2.71f, 0.1f, -1.2f));
                    cellTransform1.setScaling(0.525f);
                    float[] angles1 = new float[3];
                    angles1[0] = 0f;
                    angles1[1] = (float) (-3.14 / 2);
                    angles1[2] = 0f;
                    Quaternion quat = new Quaternion(angles1);
                    cellTransform1.setRotation(quat);
                    movableComp1.localMoveRequest(cellTransform1);
                }
            } else {
                float offset = 0f;
                for (i = 0; i < nof / 2; i++) {
                    float[] angles = new float[3];
                    angles[0] = 0f;
                    angles[1] = (float) (-3.14 / 2);
                    angles[2] = 0f;
                    Quaternion quat = new Quaternion(angles);
                    Properties prop = new Properties();
                    prop.setProperty("aspectRatio", "2");
                    CellServerState serverState = getImageFrameServerState(prop);
                    if (serverState != null) {
                        PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                        if (pcss == null) {
                            pcss = new PositionComponentServerState();
                            serverState.addComponentServerState(pcss);
                        }
                        pcss.setTranslation(new Vector3f(2.71f, 0.1f, 0.3f - offset));
                        offset = 1.5f;
                        pcss.setScaling(new Vector3f(0.525f, 0, 0));
                        pcss.setRotation(quat);
                        CellEditChannelConnection connection = (CellEditChannelConnection) this.getCellCache().getSession().getConnection(CellEditConnectionType.CLIENT_TYPE);
                        serverState.setName("rightFrame" + i);
                        CellCreateMessage msg = new CellCreateMessage(this.getCellID(), serverState);
                        try {
                            connection.send(msg);
                        } catch (Exception ex) {
                            Logger.getLogger(FairBoothCell.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        f = -f + 0.04f;
                    }
                }
            }
            f = 0.8f;
            float offset = 0f;
            for (i = 0; i < nof / 2; i++) {
                float[] angles = new float[3];
                angles[0] = 0f;
                angles[1] = (float) (-3.14 / 2);
                angles[2] = 0f;
                Quaternion quat = new Quaternion(angles);
                Properties prop = new Properties();
                prop.setProperty("aspectRatio", "2");
                CellServerState serverState = getImageFrameServerState(prop);
                if (serverState != null) {
                    PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                    if (pcss == null) {
                        pcss = new PositionComponentServerState();
                        serverState.addComponentServerState(pcss);
                    }
                    pcss.setTranslation(new Vector3f(2.71f, -1.01f, 0.3f - offset));
                    offset = 1.5f;
                    pcss.setScaling(new Vector3f(0.525f, 0, 0));
                    pcss.setRotation(quat);
                    CellEditChannelConnection connection = (CellEditChannelConnection) this.getCellCache().getSession().getConnection(CellEditConnectionType.CLIENT_TYPE);
                    serverState.setName("rightFrame" + (i + 2));
                    CellCreateMessage msg = new CellCreateMessage(this.getCellID(), serverState);
                    try {
                        connection.send(msg);
                    } catch (Exception ex) {
                        Logger.getLogger(FairBoothCell.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    f = -f + 0.04f;
                }
            }
        }
    }

    public void AddImageFramesInLeftPanel(final int nof) {
        int i;
        final float x = 0.61f;
        final float y = -0.46f;
        final float z = -2.46f;
        final float s = 1.1f;
        float f = 0.75f;
        
        //get Left Panel app frame List
        List<String> frameList = new ArrayList<String>();
        Iterator it = this.getChildren().iterator();
        while (it.hasNext()) {
            Cell cell = (Cell) it.next();

            if (cell.getName().contains("leftFrame")) {
                frameList.add(cell.getCellID().toString());
            }
        }
        
        //attach app frames to left panel
        if (nof == 1) {
            CellServerState serverState = null;
            if (frameList.size() != 0) {
                final int old_nof = frameList.size();
                if (old_nof == 2) {
                    Cell c1 = getChildFromName("leftFrame0", this);
                    Cell c2 = getChildFromName("leftFrame1", this);
                    removeChildFromName(c2.getCellID().toString());
                    final ImageFrameCell cell1 = (ImageFrameCell) c1;
                    cell1.setName("leftFrame0");
                    MovableComponent movableComp = cell1.getComponent(MovableComponent.class);
                    CellTransform cellTransform = cell1.getLocalTransform();
                    cellTransform.setTranslation(new Vector3f(0.56f, -0.51f, -2.46f));
                    cellTransform.setScaling(s);
                    movableComp.localMoveRequest(cellTransform);
                    ImageFrameProperties ifp = (ImageFrameProperties) cell1.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
                    ImageFrameProperties newIfp = new ImageFrameProperties();
                    newIfp.setAspectRatio(2);
                    newIfp.setFit(ifp.getFit());
                    newIfp.setOrientation(0);
                    cell1.propertyMap.put(ImageFrameConstants.ImageFrameProperty, newIfp);
                } else {
                    Cell c1 = getChildFromName("leftFrame0", this);
                    Cell c2 = getChildFromName("leftFrame1", this);
                    Cell c3 = getChildFromName("leftFrame2", this);
                    Cell c4 = getChildFromName("leftFrame3", this);
                    removeChildFromName(c2.getCellID().toString());
                    removeChildFromName(c3.getCellID().toString());
                    removeChildFromName(c4.getCellID().toString());
                    ImageFrameCell cell1 = (ImageFrameCell) c1;
                    cell1.setName("leftFrame0");
                    MovableComponent movableComp = cell1.getComponent(MovableComponent.class);
                    CellTransform cellTransform = cell1.getLocalTransform();
                    cellTransform.setTranslation(new Vector3f(0.56f, -0.51f, -2.46f));
                    cellTransform.setScaling(s);
                    movableComp.localMoveRequest(cellTransform);
                }
            } else {
                Properties prop = new Properties();
                prop.setProperty("aspectRatio", "2");
                serverState = getImageFrameServerState(prop);
                if (serverState != null) {
                    PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                    if (pcss == null) {
                        pcss = new PositionComponentServerState();
                        serverState.addComponentServerState(pcss);
                    }
                    pcss.setTranslation(new Vector3f(0.56f, -0.51f, -2.46f));
                    pcss.setScaling(new Vector3f(s, 0, 0));
                    CellEditChannelConnection connection = (CellEditChannelConnection) this.getCellCache().getSession().getConnection(CellEditConnectionType.CLIENT_TYPE);
                    serverState.setName("leftFrame0");
                    CellCreateMessage msg = new CellCreateMessage(this.getCellID(), serverState);
                    try {
                        connection.send(msg);
                    } catch (Exception ex) {
                        Logger.getLogger(FairBoothCell.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else if (nof == 2) {
            if (frameList.size() != 0) {
                int old_nof = frameList.size();
                if (old_nof == 1) {
                    Cell c1 = getChildFromName("leftFrame0", this);
                    final ImageFrameCell cell1 = (ImageFrameCell) c1;
                    ImageFrameProperties ifp = (ImageFrameProperties) cell1.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
                    ImageFrameProperties newIfp = new ImageFrameProperties();
                    newIfp.setAspectRatio(4);
                    newIfp.setFit(ifp.getFit());
                    newIfp.setOrientation(0);
                    cell1.setName("leftFrame0");
                    cell1.propertyMap.put(ImageFrameConstants.ImageFrameProperty, newIfp);
                    MovableComponent movableComp = cell1.getComponent(MovableComponent.class);
                    CellTransform cellTransform = cell1.getLocalTransform();
                    cellTransform.setTranslation(new Vector3f(1.33f, -0.52f, -2.46f));
                    cellTransform.setScaling(0.555f);
                    movableComp.localMoveRequest(cellTransform);
                    f = -f - 0.05f;
                    Properties prop = new Properties();
                    prop.setProperty("aspectRatio", "4");
                    prop.setProperty("orientation", "0");
                    CellServerState serverState = getImageFrameServerState(prop);
                    if (serverState != null) {
                        PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                        if (pcss == null) {
                            pcss = new PositionComponentServerState();
                            serverState.addComponentServerState(pcss);
                        }
                        pcss.setTranslation(new Vector3f(-0.19f, -0.52f, -2.46f));
                        pcss.setScaling(new Vector3f(0.555f, 0, 0));
                        CellEditChannelConnection connection = (CellEditChannelConnection) this.getCellCache().getSession().getConnection(CellEditConnectionType.CLIENT_TYPE);
                        serverState.setName("leftFrame1");
                        CellCreateMessage msg = new CellCreateMessage(this.getCellID(), serverState);
                        try {
                            connection.send(msg);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } else if (old_nof == 4) {
                    Cell c1 = getChildFromName("leftFrame0", this);
                    Cell c2 = getChildFromName("leftFrame1", this);
                    Cell c3 = getChildFromName("leftFrame2", this);
                    Cell c4 = getChildFromName("leftFrame3", this);
                    removeChildFromName(c3.getCellID().toString());
                    removeChildFromName(c4.getCellID().toString());
                    final ImageFrameCell cell1 = (ImageFrameCell) c1;
                    final ImageFrameCell cell2 = (ImageFrameCell) c2;
                    ImageFrameProperties ifp = (ImageFrameProperties) cell1.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
                    ImageFrameProperties newIfp = new ImageFrameProperties();
                    newIfp.setAspectRatio(4);
                    newIfp.setFit(ifp.getFit());
                    newIfp.setOrientation(0);
                    cell1.setName("leftFrame0");
                    cell1.propertyMap.put(ImageFrameConstants.ImageFrameProperty, newIfp);
                    ifp = (ImageFrameProperties) cell2.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
                    ImageFrameProperties newIfp1 = new ImageFrameProperties();
                    newIfp1.setAspectRatio(4);
                    newIfp1.setFit(ifp.getFit());
                    newIfp1.setOrientation(0);
                    cell2.setName("leftFrame1");
                    cell2.propertyMap.put(ImageFrameConstants.ImageFrameProperty, newIfp1);
                    MovableComponent movableComp = cell1.getComponent(MovableComponent.class);
                    CellTransform cellTransform = cell1.getLocalTransform();
                    cellTransform.setTranslation(new Vector3f(1.33f, -0.52f, -2.46f));
                    cellTransform.setScaling(0.555f);
                    movableComp.localMoveRequest(cellTransform);

                    f = -f - 0.05f;
                    MovableComponent movableComp1 = cell2.getComponent(MovableComponent.class);
                    CellTransform cellTransform1 = cell2.getLocalTransform();
                    cellTransform1.setTranslation(new Vector3f(-0.19f, -0.52f, -2.46f));
                    cellTransform1.setScaling(0.555f);
                    movableComp1.localMoveRequest(cellTransform1);
                }
            } else {
                for (i = 0; i < nof; i++) {
                    Properties prop = new Properties();
                    prop.setProperty("aspectRatio", "4");
                    prop.setProperty("orientation", "0");
                    CellServerState serverState = getImageFrameServerState(prop);
                    if (serverState != null) {
                        PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                        if (pcss == null) {
                            pcss = new PositionComponentServerState();
                            serverState.addComponentServerState(pcss);
                        }
                        pcss.setTranslation(new Vector3f(-0.19f, -0.52f, -2.46f));
                        pcss.setScaling(new Vector3f(0.555f, 0, 0));
                        CellEditChannelConnection connection = (CellEditChannelConnection) this.getCellCache().getSession().getConnection(CellEditConnectionType.CLIENT_TYPE);
                        serverState.setName("leftFrame" + i);
                        CellCreateMessage msg = new CellCreateMessage(this.getCellID(), serverState);
                        try {
                            connection.send(msg);
                        } catch (Exception ex) {
                            Logger.getLogger(FairBoothCell.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        f = -f - 0.05f;
                    }
                }
            }
        } else {
            if (frameList.size() != 0) {
                final int old_nof = frameList.size();
                if (old_nof == 1) {
                    Cell c1 = getChildFromName("leftFrame0", this);
                    final ImageFrameCell cell1 = (ImageFrameCell) c1;
                    cell1.setName("leftFrame0");
                    MovableComponent movableComp = cell1.getComponent(MovableComponent.class);
                    CellTransform cellTransform = cell1.getLocalTransform();
                    cellTransform.setTranslation(new Vector3f(1.32f, 0.1f, -2.46f));
                    cellTransform.setScaling(0.525f);
                    movableComp.localMoveRequest(cellTransform);

                    f = -f - 0.05f;
                    Properties prop = new Properties();
                    prop.setProperty("aspectRatio", "2");
                    CellServerState serverState = getImageFrameServerState(prop);
                    if (serverState != null) {
                        PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                        if (pcss == null) {
                            pcss = new PositionComponentServerState();
                            serverState.addComponentServerState(pcss);
                        }
                        pcss.setTranslation(new Vector3f(-0.19f, 0.1f, -2.46f));
                        pcss.setScaling(new Vector3f(0.525f, 0, 0));
                        CellEditChannelConnection connection = (CellEditChannelConnection) this.getCellCache().getSession().getConnection(CellEditConnectionType.CLIENT_TYPE);
                        serverState.setName("leftFrame1");
                        CellCreateMessage msg = new CellCreateMessage(this.getCellID(), serverState);
                        try {
                            connection.send(msg);
                        } catch (Exception ex) {
                            Logger.getLogger(FairBoothCell.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else if (old_nof == 2) {
                    Cell c1 = getChildFromName("leftFrame0", this);
                    Cell c2 = getChildFromName("leftFrame1", this);

                    final ImageFrameCell cell1 = (ImageFrameCell) c1;
                    final ImageFrameCell cell2 = (ImageFrameCell) c2;
                    ImageFrameProperties ifp = (ImageFrameProperties) cell1.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
                    ImageFrameProperties newIfp = new ImageFrameProperties();
                    newIfp.setAspectRatio(2);
                    newIfp.setFit(ifp.getFit());
                    newIfp.setOrientation(0);
                    cell1.setName("leftFrame0");
                    cell1.propertyMap.put(ImageFrameConstants.ImageFrameProperty, newIfp);
                    ifp = (ImageFrameProperties) cell2.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
                    ImageFrameProperties newIfp1 = new ImageFrameProperties();
                    newIfp1.setAspectRatio(2);
                    newIfp1.setFit(ifp.getFit());
                    newIfp1.setOrientation(0);
                    cell2.setName("leftFrame1");
                    cell2.propertyMap.put(ImageFrameConstants.ImageFrameProperty, newIfp1);
                    MovableComponent movableComp = cell1.getComponent(MovableComponent.class);
                    CellTransform cellTransform = cell1.getLocalTransform();
                    cellTransform.setTranslation(new Vector3f(1.32f, 0.1f, -2.46f));
                    cellTransform.setScaling(0.525f);
                    movableComp.localMoveRequest(cellTransform);

                    f = -f - 0.05f;
                    MovableComponent movableComp1 = cell2.getComponent(MovableComponent.class);
                    CellTransform cellTransform1 = cell2.getLocalTransform();
                    cellTransform1.setTranslation(new Vector3f(-0.19f, 0.1f, -2.46f));
                    cellTransform1.setScaling(0.525f);
                    movableComp1.localMoveRequest(cellTransform1);
                }
            } else {
                float offset = 0f;
                for (i = 0; i < nof / 2; i++) {
                    Properties prop = new Properties();
                    prop.setProperty("aspectRatio", "2");
                    CellServerState serverState = getImageFrameServerState(prop);
                    if (serverState != null) {
                        PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                        if (pcss == null) {
                            pcss = new PositionComponentServerState();
                            serverState.addComponentServerState(pcss);
                        }
                        pcss.setTranslation(new Vector3f(1.32f - offset, 0.1f, z));
                        offset = 1.51f;
                        pcss.setScaling(new Vector3f(0.525f, 0, 0));
                        CellEditChannelConnection connection = (CellEditChannelConnection) this.getCellCache().getSession().getConnection(CellEditConnectionType.CLIENT_TYPE);
                        serverState.setName("leftFrame" + i);
                        CellCreateMessage msg = new CellCreateMessage(this.getCellID(), serverState);
                        try {
                            connection.send(msg);
                        } catch (Exception ex) {
                            Logger.getLogger(FairBoothCell.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            f = 0.8f;
            float offset = 0f;
            for (i = 0; i < nof / 2; i++) {
                Properties prop = new Properties();
                prop.setProperty("aspectRatio", "2");
                CellServerState serverState = getImageFrameServerState(prop);
                if (serverState != null) {
                    PositionComponentServerState pcss = (PositionComponentServerState) serverState.getComponentServerState(PositionComponentServerState.class);
                    if (pcss == null) {
                        pcss = new PositionComponentServerState();
                        serverState.addComponentServerState(pcss);
                    }
                    pcss.setTranslation(new Vector3f(1.32f - offset, -1.01f, z));
                    offset = 1.51f;
                    pcss.setScaling(new Vector3f(0.525f, 0, 0));
                    CellEditChannelConnection connection = (CellEditChannelConnection) this.getCellCache().getSession().getConnection(CellEditConnectionType.CLIENT_TYPE);
                    serverState.setName("leftFrame" + (i + 2));
                    CellCreateMessage msg = new CellCreateMessage(this.getCellID(), serverState);
                    try {
                        connection.send(msg);
                    } catch (Exception ex) {
                        Logger.getLogger(FairBoothCell.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    f = -f - 0.05f;
                }
            }
        }
    }

    public Map<String, String> getThemeMap(int colorTheme) {
        Map<String, String> themeMap = new HashMap<String, String>();
        if (colorTheme == 0) {
            //Red
            themeMap.put("FFFFFF", "F1B3B3");
            themeMap.put("CCCCCC", "E26E60");
            themeMap.put("999999", "DB4E4E");
            themeMap.put("666666", "DB1F2A");
            themeMap.put("333333", "CA002A");
            themeMap.put("000000", "A12830");
        } else if (colorTheme == 1) {
            //Orange
            themeMap.put("FFFFFF", "F4D09D");
            themeMap.put("CCCCCC", "E8A36E");
            themeMap.put("999999", "B9293B");
            themeMap.put("666666", "DC7E00");
            themeMap.put("333333", "BF550D");
            themeMap.put("000000", "BF550D");
        } else if (colorTheme == 2) {
            //Yellow
            themeMap.put("FFFFFF", "C6D6E2");
            themeMap.put("CCCCCC", "FDEC00");
            themeMap.put("999999", "F6D300");
            themeMap.put("666666", "E9A700");
            themeMap.put("333333", "DC7E00");
            themeMap.put("000000", "5E4E00");
        } else if (colorTheme == 3) {
            //Green
            themeMap.put("FFFFFF", "D1E2A9");
            themeMap.put("CCCCCC", "A5C47D");
            themeMap.put("999999", "FCE220");
            themeMap.put("666666", "4EA32A");
            themeMap.put("333333", "007251");
            themeMap.put("000000", "216462");
        } else if (colorTheme == 4) {
            //Blue
            themeMap.put("FFFFFF", "A6CBF0");
            themeMap.put("CCCCCC", "60BAE3");
            themeMap.put("999999", "4978A9");
            themeMap.put("666666", "2C4E86");
            themeMap.put("333333", "004B67");
            themeMap.put("000000", "2D3C4D");
        } else if (colorTheme == 5) {
            //Purple
            themeMap.put("FFFFFF", "C8AFD1");
            themeMap.put("CCCCCC", "BA99C3");
            themeMap.put("999999", "9979AD");
            themeMap.put("666666", "7E5899");
            themeMap.put("333333", "6A3787");
            themeMap.put("000000", "462556");
        } else if (colorTheme == 6) {
            //Cool Dark
            themeMap.put("FFFFFF", "45A989");
            themeMap.put("CCCCCC", "00ADCE");
            themeMap.put("999999", "0052a5");
            themeMap.put("666666", "8c65d3");
            themeMap.put("333333", "004159");
            themeMap.put("000000", "000000");
        } else if (colorTheme == 7) {
            //Cool Medium
            themeMap.put("FFFFFF", "89C1A2");
            themeMap.put("CCCCCC", "7BC1D8");
            themeMap.put("999999", "67AADF");
            themeMap.put("666666", "8081B8");
            themeMap.put("333333", "65a8c4");
            themeMap.put("000000", "000000");
        } else if (colorTheme == 8) {
            //Cool Pastel
            themeMap.put("FFFFFF", "FFFFFF");
            themeMap.put("CCCCCC", "9BCCB6");
            themeMap.put("999999", "aacee2");
            themeMap.put("666666", "60BAE3");
            themeMap.put("333333", "cab9f1");
            themeMap.put("000000", "AACEE2");
        } else if (colorTheme == 9) {
            //Warm Dark
            themeMap.put("FFFFFF", "67545B");
            themeMap.put("CCCCCC", "575D40");
            themeMap.put("999999", "A59A45");
            themeMap.put("666666", "E29063");
            themeMap.put("333333", "5B3900");
            themeMap.put("000000", "000000");
        } else if (colorTheme == 10) {
            //Warm Light
            themeMap.put("FFFFFF", "C6B9B3");
            themeMap.put("CCCCCC", "BCBA7F");
            themeMap.put("999999", "F3EBA3");
            themeMap.put("666666", "EEB794");
            themeMap.put("333333", "AF7D5C");
            themeMap.put("000000", "5B3900");
        } else if (colorTheme == 11) {
            //Pastel Mix
            themeMap.put("FFFFFF", "FCEAAE");
            themeMap.put("CCCCCC", "F6D3D7");
            themeMap.put("999999", "A7AED7");
            themeMap.put("666666", "67B6B4");
            themeMap.put("333333", "C5B9A1");
            themeMap.put("000000", "94A7AE");
        } else if (colorTheme == 12) {
            //Art Deco Dark
            themeMap.put("FFFFFF", "00A2AB");
            themeMap.put("CCCCCC", "606C72");
            themeMap.put("999999", "3E596F");
            themeMap.put("666666", "D2434E");
            themeMap.put("333333", "411D63");
            themeMap.put("000000", "000000");
        } else if (colorTheme == 13) {
            //Art Deco Medium
            themeMap.put("FFFFFF", "A39994");
            themeMap.put("CCCCCC", "7CBFC0");
            themeMap.put("999999", "798EA2");
            themeMap.put("666666", "DD788A");
            themeMap.put("333333", "665A88");
            themeMap.put("000000", "000000");
        } else if (colorTheme == 14) {
            //Art Deco Pastel
            themeMap.put("FFFFFF", "C6CACF");
            themeMap.put("CCCCCC", "95C590");
            themeMap.put("999999", "8EB3CB");
            themeMap.put("666666", "E59CA4");
            themeMap.put("333333", "A6A4D0");
            themeMap.put("000000", "000000");
        } else if (colorTheme == 15) {
            //Elegant
            themeMap.put("FFFFFF", "606C77");
            themeMap.put("CCCCCC", "00372E");
            themeMap.put("999999", "172C51");
            themeMap.put("666666", "5F194D");
            themeMap.put("333333", "4C2B00");
            themeMap.put("000000", "391C00");
        } else if (colorTheme == 16) {
            //Fun
            themeMap.put("FFFFFF", "E18876");
            themeMap.put("CCCCCC", "E49600");
            themeMap.put("999999", "00828E");
            themeMap.put("666666", "7D2880");
            themeMap.put("333333", "4C4C4C");
            themeMap.put("000000", "000000");
        } else if (colorTheme == 17) {
            //Fun Pastel
            themeMap.put("FFFFFF", "C8C8C8");
            themeMap.put("CCCCCC", "C6A5CA");
            themeMap.put("999999", "E8A6B1");
            themeMap.put("666666", "F1C175");
            themeMap.put("333333", "86C5DA");
            themeMap.put("000000", "4C4C4C");
        } else if (colorTheme == 18) {
            //Neutral
            themeMap.put("FFFFFF", "B2BBC3");
            themeMap.put("CCCCCC", "BBBC91");
            themeMap.put("999999", "D5C28A");
            themeMap.put("666666", "C6B9AB");
            themeMap.put("333333", "ADA3A4");
            themeMap.put("000000", "998693");
        } else if (colorTheme == 19) {
            //Earth Tones
            themeMap.put("FFFFFF", "B69A71");
            themeMap.put("CCCCCC", "988F76");
            themeMap.put("999999", "333C1A");
            themeMap.put("666666", "6C3108");
            themeMap.put("333333", "482714");
            themeMap.put("000000", "35291F");
        } else if (colorTheme == 20) {
            //Bold
            themeMap.put("FFFFFF", "FAE868");
            themeMap.put("CCCCCC", "FF9627");
            themeMap.put("999999", "FF6600");
            themeMap.put("666666", "C4CD20");
            themeMap.put("333333", "703F8A");
            themeMap.put("000000", "FF2761");
        } else if (colorTheme == 21) {
            //Muted
            themeMap.put("FFFFFF", "D9DADE");
            themeMap.put("CCCCCC", "999891");
            themeMap.put("999999", "AD2F18");
            themeMap.put("666666", "CDC04B");
            themeMap.put("333333", "1E3072");
            themeMap.put("000000", "1D2220");
        } else if (colorTheme == 22) {
            //Primary Colors
            themeMap.put("FFFFFF", "FFFFFF");
            themeMap.put("CCCCCC", "FAFA00");
            themeMap.put("999999", "00FA00");
            themeMap.put("666666", "FF0000");
            themeMap.put("333333", "0000FF");
            themeMap.put("000000", "000000");
        } else {
            //Print Colors
            themeMap.put("FFFFFF", "FFF200");
            themeMap.put("CCCCCC", "FAFA00");
            themeMap.put("999999", "00FA00");
            themeMap.put("666666", "FF0000");
            themeMap.put("333333", "0000FF");
            themeMap.put("000000", "000000");
        }
        return themeMap;
    }
}
