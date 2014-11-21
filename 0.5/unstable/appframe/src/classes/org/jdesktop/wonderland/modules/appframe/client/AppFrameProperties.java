/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */
package org.jdesktop.wonderland.modules.appframe.client;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameConstants;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameProp;
import org.jdesktop.wonderland.modules.bestview.client.BestViewUtils;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.modules.appframe.common.AppFramePinToMenu;
import org.jdesktop.wonderland.modules.appframe.common.AppFrameServerState;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 *
 * @author jkaplan
 */
//this file is to handle properties of appframe
//@PropertiesFactory
@PropertiesFactory(AppFrameServerState.class)
public class AppFrameProperties extends javax.swing.JPanel implements PropertiesFactorySPI {

    public boolean dirty = false;
    private CellPropertiesEditor editor;
    private static final Logger LOGGER =
            Logger.getLogger(AppFrameProperties.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org.jdesktop.wonderland.modules.appframe.client.resources.Bundle");
    // original value of "test" property
    String origTest;
    String origAspectRatio;
    String origOrientation;
    String origMaxHistory;
    String origBoarderColor;
    HashMap<String, String> origPinToMenu;
    String newTest;
    String newAspectRatio;
    String newOrientation;
    String newMaxHistory;
    String newBoarderColor;
    HashMap<String, String> newPinToMenu;
    public AppFrame parentCell;
    public JFrame AddApp;
    public JFrame parent;

    /** Creates new form GameKioskProperties */
    public AppFrameProperties() {
        initComponents();

        origPinToMenu = new HashMap<String, String>();
        newPinToMenu = new HashMap<String, String>();
 
        jTextField1.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                checkDirty();
            }

            public void removeUpdate(DocumentEvent e) {
                checkDirty();
            }

            public void changedUpdate(DocumentEvent e) {
                checkDirty();
            }
        });


    }

    public void populateProp() {
        if (newOrientation.equals("Horizontal")) {
            jRadioButton3.setSelected(true);
        } else {
            jRadioButton4.setSelected(true);
        }
        if (newAspectRatio.equals("3*4")) {
            jRadioButton1.setSelected(true);
        } else {
            jRadioButton2.setSelected(true);
        }
    }

    public void updateBorderColor() {
        jPanel6.setBackground(parseColorString(newBoarderColor));
    }

    public AppFrameProperties(AppFrame parentCell, JFrame parent) {
        initComponents();
        this.parentCell = parentCell;

        this.parent = parent;
        origPinToMenu = new HashMap<String, String>();
        newPinToMenu = new HashMap<String, String>();
        setProperties();
        populatePinTOMenu();

        jTextField1.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                checkDirty();
            }

            public void removeUpdate(DocumentEvent e) {
                checkDirty();
            }

            public void changedUpdate(DocumentEvent e) {
                checkDirty();
            }
        });


    }
//this method is used to set the current properties of app frame 

    public String getDisplayName() {
        return BUNDLE.getString("AppFrame");
    }

    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    public JPanel getPropertiesJPanel() {
        return this;
    }

    public void open() {
        // load default values from shared state
        // restore will update the UI based on the values loaded in
        // this method
      
        jPanel8.setVisible(false);
        setProperties();
        populatePinTOMenu();
        restore();
    }

    public void close() {
    }

    public void restore() {
        // update the UI based on the default values
        dirty = false;
        checkDirty();
        if (dirty) {
            cancel();
        }
        // update the buttons

    }

    public void apply() {
        // set the values based on shared state
        ok();

        // update buttons.

    }

    public void setProperties() {
        try {
            if (editor != null) {
                parentCell = (AppFrame) editor.getCell();
                parentCell.appFrameProperties = this;
            }
            AppFrameProp afp = (AppFrameProp) parentCell.propertyMap.get("afp");

            origAspectRatio = afp.getAspectRatio();
            origBoarderColor = afp.getBorderColor();


            origOrientation = afp.getOrientation();
            origMaxHistory = afp.getMaxHistory();

            Set<Entry<String, SharedData>> pinnedItems =
                    parentCell.pinToMenuMap.entrySet();
            if (!pinnedItems.isEmpty()) {
                for (Entry<String, SharedData> pinnedItem : pinnedItems) {
                    AppFramePinToMenu afPin = (AppFramePinToMenu) pinnedItem.getValue();
                    origPinToMenu.put(afPin.getFileName(), afPin.getFileURL());
                }
                newPinToMenu.putAll(origPinToMenu);
            }
            newAspectRatio = origAspectRatio;
            newBoarderColor = origBoarderColor;
            newOrientation = origOrientation;
            newMaxHistory = origMaxHistory;

            if (origAspectRatio.equals("3*4")) {
                jRadioButton1.setSelected(true);
            } else {
                jRadioButton2.setSelected(true);
            }
            Color bc = parseColorString(origBoarderColor);
            jPanel6.setBackground(bc);
            if (origOrientation.equalsIgnoreCase("Horizontal")) {
                jRadioButton3.setSelected(true);
            } else {
                jRadioButton4.setSelected(true);
            }
            jTextField1.setText(origMaxHistory);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //this method is used to store changes made in the properties

    public void ok() {
        try {
            AppFrameProp afp = (AppFrameProp) parentCell.propertyMap.get("afp");
            if (!newAspectRatio.equals(origAspectRatio)) {
                afp.setAspectRatio(newAspectRatio);
                origAspectRatio = newAspectRatio;
            }
            if (!newBoarderColor.equals(origBoarderColor)) {
                afp.setBorderColor(newBoarderColor);
                origBoarderColor = newBoarderColor;
            }
            if (!newMaxHistory.equals(origMaxHistory)) {
                afp.setMaxHistory(jTextField1.getText());
                origMaxHistory = newMaxHistory;
            }
            if (!newOrientation.equals(origOrientation)) {
                afp.setOrientation(newOrientation);
                origOrientation = newOrientation;
            }
            if (newPinToMenu.size() == origPinToMenu.size()) {
                for (Iterator<Entry<String, String>> i = newPinToMenu.entrySet().iterator(); i.hasNext();) {
                    Entry<String, String> e = i.next();
                    if (origPinToMenu.containsKey(e.getKey()) && origPinToMenu.containsValue(e.getValue())) {
                    } else {
                        setPinToMenu(newPinToMenu);
                        origPinToMenu.clear();
                        origPinToMenu.putAll(newPinToMenu);
                        break;
                    }
                }
            } else {
                setPinToMenu(newPinToMenu);
                origPinToMenu.clear();
                origPinToMenu.putAll(newPinToMenu);
            }
            dirty = false;
            parentCell.propertyMap.put("afp", afp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//this methood is responsible for changing the orientation of app frame(parentCell) and it will call method to changes orientation of child

    public void setPinToMenu(HashMap<String, String> pinToMenu) {
        try {
            Set<Entry<String, SharedData>> pinned = parentCell.pinToMenuMap.entrySet();
            if (!pinned.isEmpty()) {
                for (Entry<String, SharedData> pinnedItem : pinned) {
                    parentCell.pinToMenuMap.remove(pinnedItem.getKey());
                }
            }
            Set<Entry<String, String>> pinnedItems = pinToMenu.entrySet();

            if (!pinnedItems.isEmpty()) {
                for (Entry<String, String> pinnedItem : pinnedItems) {
                    AppFramePinToMenu afPin = new AppFramePinToMenu(pinnedItem.getKey(), pinnedItem.getValue());
                    parentCell.pinToMenuMap.put(afPin.getFileName(), afPin);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateOrientationParent(final AppFrame cell, String orientation, String role) {
        try {


            CellTransform update = cell.getLocalTransform();
            Quaternion rot = new Quaternion();
            update.getRotation(rot);
            float angles[] = new float[3];
            angles[0] = 0;
            angles[1] = 0;

            if (role.equals("parent")) {
                if (orientation.equalsIgnoreCase("Horizontal")) {
                    angles[2] = (float) (0);
                } else if (orientation.equalsIgnoreCase("Vertical")) {
                    angles[2] = (float) (3.14 / 2);
                }
                update.setRotation(new Quaternion(angles));
                final CellTransform ft = update;
                SceneWorker.addWorker(new WorkCommit() {

                    public void commit() {
                        CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
                        RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
                        rc.getSceneRoot().getChild("FrameNode").setLocalRotation(ft.getRotation(null));
                        ClientContextJME.getWorldManager().addToUpdateList(rc.getSceneRoot());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//this method is used to updating the orientation of child cell

    public static void updateOrientation(final Cell cell, String orientation
            , String aspectRatio, float origWidth, float origHeight) {
        try {
            CellTransform update = cell.getLocalTransform();
            Quaternion rot = new Quaternion();
            update.getRotation(rot);
            float angles[] = new float[3];
            if (cell instanceof App2DCell) {
                App2DCell child = (App2DCell) cell;
                child.getApp().updateSlaveWindows();
                rot.toAngles(angles);
                float scale;
                float w = child.getApp().getPrimaryWindow().getWidth();
                float h = child.getApp().getPrimaryWindow().getHeight();
                
                if (orientation.equals("Horizontal")) {
                    
                } else if (orientation.equals("Vertical")) {
                    update.setRotation(new Quaternion(angles));
                }
                
                /**
                 * Fit the dropped content into AppFrame but preserve its aspect ration
                 */
                if (orientation.equals("Horizontal")) {
                    if (aspectRatio.equalsIgnoreCase("3*4")) {
                        if((352-w)<(264-h)) {
                            scale = (Math.abs(352-w))/w;
                            if(w<352) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        } else {
                            scale = (Math.abs(264-h))/h;
                            if(h<264) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        }
                    } else {
                        if((470-w)<(264-h)) {
                            scale = (Math.abs(470-w))/w;
                            if(w<470) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        } else {
                            scale = (Math.abs(264-h))/h;
                            if(h<264) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        }
                    }
                } else {
                    if (aspectRatio.equalsIgnoreCase("3*4")) {
                        if((264-w)<(352-h)) {
                            scale = (Math.abs(264-w))/w;
                            if(w<264) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        } else {
                            scale = (Math.abs(352-h))/h;
                            if(h<352) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        }
                    } else {
                        if((264-w)<(470-h)) {
                            scale = (Math.abs(264-w))/w;
                            if(w<264) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        } else {
                            scale = (Math.abs(470-h))/h;
                            if(h<470) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        }
                    }
                }
                float div = (float)100*child.getApp().getPrimaryWindow().getPixelScale().getX();
                update.setScaling((float)update.getScaling()/(float)div);
            } else {
                BoundingVolume bv = BestViewUtils.getModelBounds(cell);
                float scale1;
                float scale2;
                CellTransform parentTrans = cell.getLocalTransform();
                if (bv instanceof BoundingBox) {
                    if (orientation.equalsIgnoreCase("Horizontal")) {
                        if (aspectRatio.equalsIgnoreCase("3*4")) {
                            scale1 = (float) (((1.75) ) / origWidth);
                            scale2 = (float) (((1.31) ) / origHeight);
                        } else {
                            scale1 = (float) (((2.33) ) / origWidth);
                            scale2 = (float) (((1.31) ) / origHeight);
                        }
                        angles[2] = (float) (0);
                        update.setRotation(new Quaternion(angles));
                    } else {
                        // angles[2] = (float) (-3.14 / 2);
                        update.setRotation(new Quaternion(angles));
                        if (aspectRatio.equalsIgnoreCase("3*4")) {
                            scale1 = (float) (((1.31) ) / origWidth);
                            scale2 = (float) (((1.75) ) / origHeight);
                        } else {
                            scale1 = (float) (((1.31) ) / origWidth);
                            scale2 = (float) (((2.33) ) / origHeight);
                        }
                    }
                    if (scale1 < scale2) {
                        update.setScaling(scale1);
                    } else {
                        update.setScaling(scale2);
                    }// update.transform(bb);
                }
            }
            final CellTransform ft = update;
            SceneWorker.addWorker(new WorkCommit() {

                public void commit() {
                    CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
                    RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
                    rc.getSceneRoot().setLocalTranslation(ft.getTranslation(null));
                    rc.getSceneRoot().setLocalRotation(ft.getRotation(null));
                    rc.getSceneRoot().setLocalScale(ft.getScaling(null));
                    ClientContextJME.getWorldManager().addToUpdateList(rc.getSceneRoot());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//this method is used to change the aspect ratio of child cell

    public static void updateAspect(final Cell cell, String orientation
            , String aspectRatio, float origWidth, float origHeight) {
        try {
            CellTransform update = cell.getLocalTransform();
            if (cell instanceof App2DCell) {
                App2DCell child = (App2DCell) cell;
                float scale;
                float w = child.getApp().getPrimaryWindow().getWidth();
                float h = child.getApp().getPrimaryWindow().getHeight();
                
                /**
                 * Fit the dropped content into AppFrame but preserve its aspect ration
                 */
                if (aspectRatio.equals("3*4")) {
                    if (orientation.equalsIgnoreCase("Horizontal")) {
                        if((352-w)<(264-h)) {
                            scale = (Math.abs(352-w))/w;
                            if(w<352) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        } else {
                            scale = (Math.abs(264-h))/h;
                            if(h<264) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        }
                    } else {
                        if((264-w)<(352-h)) {
                            scale = (Math.abs(264-w))/w;
                            if(w<264) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        } else {
                            scale = (Math.abs(352-h))/h;
                            if(h<352) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        }
                    }
                } else {
                    if (orientation.equalsIgnoreCase("Horizontal")) {
                        if((470-w)<(264-h)) {
                            scale = (Math.abs(470-w))/w;
                            if(w<470) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        } else {
                            scale = (Math.abs(264-h))/h;
                            if(h<264) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        }
                    } else {
                        if((264-w)<(470-h)) {
                            scale = (Math.abs(264-w))/w;
                            if(w<264) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        } else {
                            scale = (Math.abs(470-h))/h;
                            if(h<470) {
                                update.setScaling(1+scale);
                            } else {
                                update.setScaling(1-scale);
                            }
                        }
                    }
                    
                }
                float div = (float)100*child.getApp().getPrimaryWindow().getPixelScale().getX();
                update.setScaling((float)update.getScaling()/(float)div);
            } else {
                BoundingVolume bv = BestViewUtils.getModelBounds(cell);
                float scale1;
                float scale2;
                CellTransform parentTrans = cell.getLocalTransform();
                if (aspectRatio.equalsIgnoreCase("3*4")) {
                    if (orientation.equalsIgnoreCase("Horizontal")) {
                        scale1 = (float) ((1.75 ) / origWidth);
                        scale2 = (float) ((1.31 ) / origHeight);
                    } else {
                        scale1 = (float) (((1.31) ) / origWidth);
                        scale2 = (float) (((1.75) ) / origHeight);
                    }
                } else {
                    if (orientation.equalsIgnoreCase("Horizontal")) {
                        scale1 = (float) (((2.33) ) / origWidth);
                        scale2 = (float) (((1.31) ) / origHeight);
                    } else {
                        scale1 = (float) (((1.31) ) / origWidth);
                        scale2 = (float) (((2.33) ) / origHeight);
                    }
                }
                if (scale1 < scale2) {
                    update.setScaling(scale1);
                } else {
                    update.setScaling(scale2);
                }
            }
            final CellTransform ft=update;
            SceneWorker.addWorker(new WorkCommit() {

                public void commit() {
                    CellRendererJME renderer = (CellRendererJME) cell.getCellRenderer(RendererType.RENDERER_JME);
                    RenderComponent rc = renderer.getEntity().getComponent(RenderComponent.class);
                    rc.getSceneRoot().setLocalTranslation(ft.getTranslation(null));
                    rc.getSceneRoot().setLocalScale(ft.getScaling(null));
                    ClientContextJME.getWorldManager().addToUpdateList(rc.getSceneRoot());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
//this method is use to retrive the movablecomponent of child cell

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        qualityBG = new javax.swing.ButtonGroup();
        playTypeBG = new javax.swing.ButtonGroup();
        displayBG = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jPanel7 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        canvas1 = new java.awt.Canvas();
        jPanel1 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jPanel8 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        canvas2 = new java.awt.Canvas();

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setPreferredSize(new java.awt.Dimension(440, 530));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/appframe/client/resources/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("AppFrameProperties.jLabel1.text")); // NOI18N

        jLabel3.setText(bundle.getString("AppFrameProperties.jLabel3.text")); // NOI18N

        jLabel4.setText(bundle.getString("AppFrameProperties.jLabel4.text")); // NOI18N

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText(bundle.getString("AppFrameProperties.jRadioButton1.text")); // NOI18N
        jRadioButton1.setActionCommand(bundle.getString("AppFrameProperties.jRadioButton1.actionCommand")); // NOI18N
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText(bundle.getString("AppFrameProperties.jRadioButton2.text")); // NOI18N
        jRadioButton2.setActionCommand(bundle.getString("AppFrameProperties.jRadioButton2.actionCommand")); // NOI18N
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("AppFrameProperties.jPanel1.border.title"))); // NOI18N
        jPanel1.setToolTipText(bundle.getString("AppFrameProperties.Menu:.toolTipText")); // NOI18N
        jPanel1.setName("Menu:"); // NOI18N

        jLabel6.setText(bundle.getString("AppFrameProperties.jLabel6.text")); // NOI18N

        jTextField1.setText(bundle.getString("AppFrameProperties.jTextField1.text")); // NOI18N

        jLabel7.setText(bundle.getString("AppFrameProperties.jLabel7.text")); // NOI18N

        jLabel8.setText(bundle.getString("AppFrameProperties.jLabel8.text")); // NOI18N

        jButton2.setText(bundle.getString("AppFrameProperties.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText(bundle.getString("AppFrameProperties.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText(bundle.getString("AppFrameProperties.jButton4.text")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jList1.setVisibleRowCount(2);
        jScrollPane1.setViewportView(jList1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel7))
                    .addComponent(jLabel8)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(94, 94, 94))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                        .addComponent(jButton4))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("AppFrameProperties.jPanel2.border.title"))); // NOI18N

        jLabel2.setText(bundle.getString("AppFrameProperties.jLabel2.text")); // NOI18N

        jPanel5.setBackground(new java.awt.Color(153, 153, 153));

        jPanel6.setBackground(new java.awt.Color(255, 51, 102));
        jPanel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel6MouseClicked(evt);
            }
        });

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 8, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 36, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel5.setText(bundle.getString("AppFrameProperties.jLabel5.text")); // NOI18N

        jLabel9.setText(bundle.getString("AppFrameProperties.jLabel9.text")); // NOI18N

        jLabel10.setText(bundle.getString("AppFrameProperties.jLabel10.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5))
                    .addComponent(jLabel10)
                    .addComponent(jLabel9))
                .addContainerGap(88, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel5)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10))
        );

        buttonGroup2.add(jRadioButton3);
        jRadioButton3.setText(bundle.getString("AppFrameProperties.jRadioButton3.text")); // NOI18N
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        buttonGroup2.add(jRadioButton4);
        jRadioButton4.setText(bundle.getString("AppFrameProperties.jRadioButton4.text")); // NOI18N
        jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton4ActionPerformed(evt);
            }
        });

        jPanel8.setPreferredSize(new java.awt.Dimension(300, 62));

        jButton6.setText(bundle.getString("AppFrameProperties.jButton6.text")); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton5.setText(bundle.getString("AppFrameProperties.jButton5.text")); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5)
                    .addComponent(jButton6))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        canvas2.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4))
                                .addGap(25, 25, 25)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jRadioButton3)
                                    .addComponent(jRadioButton1))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jRadioButton2)
                                    .addComponent(jRadioButton4))
                                .addGap(148, 148, 148))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(canvas2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(738, 738, 738)
                        .addComponent(canvas1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(167, 167, 167)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(canvas1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jRadioButton1)
                            .addComponent(jRadioButton2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jRadioButton3)
                            .addComponent(jRadioButton4)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(61, 61, 61))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(133, 133, 133)
                .addComponent(canvas2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(397, 397, 397))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            final JFrame AddDocument = new JFrame();
            AddDocument.setTitle("Add Document");
            parentCell.addDocument = "pin";
            JPanel contextPanel2 = new AddServerDocument(AddDocument, parentCell);
            AddDocument.add(contextPanel2);
            // TODO add your handling code here:
            AddDocument.pack();
            AddDocument.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    public void cancel() {
        try {
            AppFrameProp afp = (AppFrameProp) parentCell.propertyMap.get("afp");
            newPinToMenu.clear();
            newPinToMenu.putAll(origPinToMenu);
            populatePinTOMenu();
            afp.setAspectRatio(origAspectRatio);
            afp.setOrientation(origOrientation);
            afp.setBorderColor(origBoarderColor);
            afp.setMaxHistory(origMaxHistory);
            setPinToMenu(origPinToMenu);
            jPanel6.setBackground(parseColorString(origBoarderColor));
            parentCell.propertyMap.put("afp", afp);
            if (origOrientation.equalsIgnoreCase("Horizontal") && !origOrientation.equals(newOrientation)) {
                jRadioButton3.setSelected(true);
                updateOrientationParent(parentCell, origOrientation, "parent");
                if (parentCell.getNumChildren() == 1) {
                    float origWidth = 0, origHeight = 0, origImageWidth = 0, origImageHeight = 0;
                    Cell child = parentCell.getChildren().iterator().next();
                    if (child instanceof App2DCell) {
                        App2DCell childCell = (App2DCell) child;
                        origWidth = parentCell.origWidth * childCell.getApp().getPixelScale().getX();
                        origHeight = parentCell.origHeight * childCell.getApp().getPixelScale().getY();
                        updateOrientation(child, origOrientation, origAspectRatio
                                , origWidth, origHeight);
                    } else {
                        origImageWidth = parentCell.origImageWidth;
                        origImageHeight = parentCell.origImageHeight;
                        updateOrientation(child, origOrientation, origAspectRatio
                                , origImageWidth, origImageHeight);
                    }
                }
            } else if (origOrientation.equalsIgnoreCase("Vertical") && !origOrientation.equals(newOrientation)) {
                jRadioButton4.setSelected(true);
                updateOrientationParent(parentCell, origOrientation, "parent");
                if (parentCell.getNumChildren() == 1) {
                    float origWidth = 0, origHeight = 0, origImageWidth = 0, origImageHeight = 0;
                    Cell child = parentCell.getChildren().iterator().next();
                    if (child instanceof App2DCell) {
                        App2DCell childCell = (App2DCell) child;
                        origWidth = parentCell.origWidth * childCell.getApp().getPixelScale().getX();
                        origHeight = parentCell.origHeight * childCell.getApp().getPixelScale().getY();
                        updateOrientation(child, origOrientation, origAspectRatio
                                , origWidth, origHeight);
                    } else {
                        origImageWidth = parentCell.origImageWidth;
                        origImageHeight = parentCell.origImageHeight;
                        updateOrientation(child, origOrientation, origAspectRatio
                                , origImageWidth, origImageHeight);
                    }
                }
            }
            if (origAspectRatio.equals("3*4") && !origAspectRatio.equals(newAspectRatio)) {
                jRadioButton1.setSelected(true);
                aspectRatio34(origAspectRatio, parentCell);
                if (parentCell.getNumChildren() == 1) {
                    float origWidth = 0, origHeight = 0;
                    Cell child = parentCell.getChildren().iterator().next();
                    if (child instanceof App2DCell) {
                        App2DCell childCell = (App2DCell) child;
                        origWidth = parentCell.origWidth * childCell.getApp().getPixelScale().getX();
                        origHeight = parentCell.origHeight * childCell.getApp().getPixelScale().getY();
                    } else {
                        origWidth = parentCell.origImageWidth;
                        origHeight = parentCell.origImageHeight;
                    }
                    updateAspect(child, newOrientation, newAspectRatio, origWidth
                            , origHeight);
                }
            }
            if (origAspectRatio.equals("16*9") && !origAspectRatio.equals(newAspectRatio)) {
                jRadioButton2.setSelected(true);
                aspectRatio169(origAspectRatio, parentCell);
                if (parentCell.getNumChildren() == 1) {
                    float origWidth = 0, origHeight = 0;
                    Cell child = parentCell.getChildren().iterator().next();
                    if (child instanceof App2DCell) {
                        App2DCell childCell = (App2DCell) child;
                        origWidth = parentCell.origWidth * childCell.getApp().getPixelScale().getX();
                        origHeight = parentCell.origHeight * childCell.getApp().getPixelScale().getY();
                    } else {
                        origWidth = parentCell.origImageWidth;
                        origHeight = parentCell.origImageHeight;
                    }
                    updateAspect(child, newOrientation, newAspectRatio, origWidth
                            , origHeight);
                }
            }
            jTextField1.setText(origMaxHistory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jPanel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel6MouseClicked
        // TODO add your handling code here:
        try {
            Color newColor = getUserSelectedColor();
            if (newColor == null) {
                return;
            }
            jPanel6.setBackground(newColor);
            MouseClickListener.HIGHLIGHT_COLOR = new ColorRGBA(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), newColor.getAlpha());
            newBoarderColor = newColor.getRed() + ":" + newColor.getGreen() + ":" + newColor.getBlue() + ":" + newColor.getAlpha();
            checkDirty();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jPanel6MouseClicked
    private class AddAppMenuListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                AddApp.setVisible(false);
                newPinToMenu.put(e.getActionCommand(), "pinnedApp");
                populatePinTOMenu();
            } catch (Exception ei) {
                ei.printStackTrace();
            }
        }
    }
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        cancel();
        if (editor == null) {
            parent.setVisible(false);
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        try {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    AddApp = new JFrame();
                    Set<CellFactorySPI> cellFactoryList = CellRegistry.getCellRegistry().getAllCellFactories();
                    AddApp.setTitle("Add App");
                    AddApp.setLocation(MouseInfo.getPointerInfo().getLocation());
                    JPanel contextPanel = new JPanel();
                    contextPanel.setLayout(new BoxLayout(contextPanel,
                            BoxLayout.Y_AXIS));
                    AddApp.add(contextPanel);
                    JMenuItem[] menus = new JMenuItem[20];
                    int i = 0;
                    AddAppMenuListener aaml = new AddAppMenuListener();
                    
                    for (CellFactorySPI cfs : cellFactoryList) {
                        if (cfs.getDisplayName() != null) {
                            if (cfs.getDisplayName().equals("Webcam Viewer")) {
                                menus[i] = new JMenuItem(cfs.getDisplayName());
                                menus[i].addActionListener(aaml);
                                contextPanel.add(menus[i++]);
                            }
                        }
                    }
                    for (String ext : AppFrameConstants.extension) {
                        Iterator<CellFactorySPI> iterator = CellRegistry.getCellRegistry().getCellFactoriesByExtension(ext).iterator();
                        while (iterator.hasNext()) {
                            CellFactorySPI factorySPI = iterator.next();
                            menus[i] = new JMenuItem(factorySPI.getDisplayName());
                            menus[i].addActionListener(aaml);
                            contextPanel.add(menus[i++]);
                        }
                    }
                    for (CellFactorySPI cfs : cellFactoryList) {
                        if (cfs.getDisplayName() != null) {
                            if (cfs.getDisplayName().equals("Text Editor")) {
                                menus[i] = new JMenuItem(cfs.getDisplayName());
                                menus[i].addActionListener(aaml);
                                contextPanel.add(menus[i++]);
                            }
                        }
                    }
                    for (CellFactorySPI cfs : cellFactoryList) {
                        if (cfs.getDisplayName() != null) {
                            if (cfs.getDisplayName().equals("Screen Sharer")) {
                                menus[i] = new JMenuItem(cfs.getDisplayName());
                                menus[i].addActionListener(aaml);
                                contextPanel.add(menus[i++]);
                            }
                        }
                    }
                    AddApp.setResizable(false);
                    AddApp.pack();
                    AddApp.setVisible(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    public static void aspectRatio169(final String aspectRatio, AppFrame parentCell) {
        try {
            CellRendererJME renderer = (CellRendererJME) parentCell.getCellRenderer(RendererType.RENDERER_JME);
            final Entity parentEntity = ((CellRendererJME) renderer).getEntity();
            RenderComponent rc = parentEntity.getComponent(RenderComponent.class);
            final Node sceneRoot = (Node) rc.getSceneRoot().getChild("FrameNode");
            Spatial s1 = sceneRoot.getChild("App Frame Button1");
            Vector3f v3f = s1.getLocalTranslation();
            if (v3f.getX() > -2.28f) {
                Spatial s2 = sceneRoot.getChild("App Frame Button2");
                Spatial s3 = sceneRoot.getChild("App Frame Button3");
                Spatial s4 = sceneRoot.getChild("App Frame Button4");
                s1.setLocalTranslation(-2.28f, 1.4f, .08f);
                s2.setLocalTranslation(2.28f, 1.4f, .08f);
                s3.setLocalTranslation(-2.28f, -1.4f, .08f);
                s4.setLocalTranslation(2.28f, -1.4f, .08f);
            }
            Spatial s = sceneRoot.getChild(0);
            sceneRoot.detachChild(s);
            DeployedModel m;
            try {
                URL url = AssetUtils.getAssetURL("wla://app-frame/Frame_16-9_BW_frame_07.DAE/Frame_16-9_BW_frame_07.DAE.gz.dep", parentCell);
                m = LoaderManager.getLoaderManager().getLoaderFromDeployment(url);
                Node mine = m.getModelLoader().loadDeployedModel(m, null);
                sceneRoot.attachChildAt(mine, 0);
                Spatial apn = sceneRoot.getChild("App Frame BackGround");
                sceneRoot.detachChild(apn.getParent());
                Quaternion rot = new Quaternion();
                rot.fromAngles(0f, 0f, 0f);
                float scale = 0.003f;
                Entity e5 = parentCell.createPanel(new Vector3f(0f, 0f, 0f), rot, scale, sceneRoot, aspectRatio);
                parentEntity.addEntity(e5);
            } catch (IOException ex) {
                Logger.getLogger(AppFrameProperties.class.getName()).log(Level.SEVERE, null, ex);
            }
            SceneWorker.addWorker(new WorkCommit() {

                public void commit() {
                    ClientContextJME.getWorldManager().addToUpdateList(sceneRoot);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void aspectRatio34(final String aspectRatio, AppFrame parentCell) {
        try {
            CellRendererJME renderer = (CellRendererJME) parentCell.getCellRenderer(RendererType.RENDERER_JME);
            final Entity parentEntity = ((CellRendererJME) renderer).getEntity();
            RenderComponent rc = parentEntity.getComponent(RenderComponent.class);
            final Node sceneRoot = (Node) rc.getSceneRoot().getChild("FrameNode");
            Spatial s1 = sceneRoot.getChild("App Frame Button1");
            if(s1!=null) {
            Vector3f v3f = s1.getLocalTranslation();
                if (v3f.getX() < -1.68f) {
                    Spatial s2 = sceneRoot.getChild("App Frame Button2");
                    Spatial s3 = sceneRoot.getChild("App Frame Button3");
                    Spatial s4 = sceneRoot.getChild("App Frame Button4");
                    s1.setLocalTranslation(-1.68f, 1.4f, .08f);
                    s2.setLocalTranslation(1.68f, 1.4f, .08f);
                    s3.setLocalTranslation(-1.68f, -1.4f, .08f);
                    s4.setLocalTranslation(1.68f, -1.4f, .08f);
                }
            }
            Spatial s = sceneRoot.getChild(0);
            sceneRoot.detachChild(s);
            float scale = 0.003f;
            DeployedModel m;
            try {
                URL url = AssetUtils.getAssetURL("wla://app-frame/Frame_4-3_BW_frame_07.DAE/Frame_4-3_BW_frame_07.DAE.gz.dep", parentCell);
                m = LoaderManager.getLoaderManager().getLoaderFromDeployment(url);
                Node mine = m.getModelLoader().loadDeployedModel(m, null);
                sceneRoot.attachChildAt(mine, 0);
                Spatial apn = sceneRoot.getChild("App Frame BackGround");
                sceneRoot.detachChild(apn.getParent());
                Quaternion rot = new Quaternion();
                rot.fromAngles(0f, 0f, 0f);
                Entity backPanel = parentCell.createPanel(new Vector3f(0f, 0f, 0f), rot, scale, sceneRoot, aspectRatio);
                parentEntity.addEntity(backPanel);
            } catch (IOException ex) {
                Logger.getLogger(AppFrameProperties.class.getName()).log(Level.SEVERE, null, ex);
            }
            SceneWorker.addWorker(new WorkCommit() {

                public void commit() {
                    ClientContextJME.getWorldManager().addToUpdateList(sceneRoot);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        try {
            newAspectRatio = evt.getActionCommand();
            aspectRatio169(
                    newAspectRatio, parentCell);
            if (parentCell.getNumChildren() == 1) {
                float origWidth = 0, origHeight = 0;
                Cell child = parentCell.getChildren().iterator().next();
                if (child instanceof App2DCell) {
                    App2DCell childCell = (App2DCell) child;
//                    origWidth = parentCell.origWidth;
//                    origHeight = parentCell.origHeight;
                    origWidth = parentCell.origWidth * childCell.getApp().getPixelScale().getX();
                    origHeight = parentCell.origHeight * childCell.getApp().getPixelScale().getY();
                } else {
                    origWidth = parentCell.origImageWidth;
                    origHeight = parentCell.origImageHeight;
                }
                updateAspect(child, newOrientation, newAspectRatio, origWidth, origHeight);
            }
            checkDirty();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        try {
            newAspectRatio = evt.getActionCommand();
            aspectRatio34(newAspectRatio, parentCell);
            if (parentCell.getNumChildren() == 1) {
                float origWidth = 0, origHeight = 0;
                Cell child = parentCell.getChildren().iterator().next();
                if (child instanceof App2DCell) {
                    App2DCell childCell = (App2DCell) child;
//                    origWidth = parentCell.origWidth;
//                    origHeight = parentCell.origHeight;
                    origWidth = parentCell.origWidth * childCell.getApp().getPixelScale().getX();
                    origHeight = parentCell.origHeight * childCell.getApp().getPixelScale().getY();
                } else {
                    origWidth = parentCell.origImageWidth;
                    origHeight = parentCell.origImageHeight;
                }
                updateAspect(child, newOrientation, newAspectRatio, origWidth, origHeight);
            }
            checkDirty();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        try {
            newOrientation = evt.getActionCommand();
            updateOrientationParent(parentCell, evt.getActionCommand(), "parent");
            if (parentCell.getNumChildren() == 1) {
                float origWidth = 0, origHeight = 0;
                Cell child = parentCell.getChildren().iterator().next();
                if (child instanceof App2DCell) {
                    App2DCell childCell = (App2DCell) child;
                    origWidth = parentCell.origWidth * childCell.getApp().getPixelScale().getX();
                    origHeight = parentCell.origHeight * childCell.getApp().getPixelScale().getY();
                } else {
                    origWidth = parentCell.origImageWidth;
                    origHeight = parentCell.origImageHeight;
                }
                updateOrientation(child, newOrientation, newAspectRatio, origWidth
                        , origHeight);
            }
            checkDirty();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed
        try {
            newOrientation = evt.getActionCommand();
            updateOrientationParent(parentCell, newOrientation, "parent");
            if (parentCell.getNumChildren() == 1) {
                float origWidth = 0, origHeight = 0;
                Cell child = parentCell.getChildren().iterator().next();
                if (child instanceof App2DCell) {
                    App2DCell childCell = (App2DCell) child;
                    origWidth = parentCell.origWidth * childCell.getApp().getPixelScale().getX();
                    origHeight = parentCell.origHeight * childCell.getApp().getPixelScale().getY();
                } else {
                    origWidth = parentCell.origImageWidth;
                    origHeight = parentCell.origImageHeight;
                }
                updateOrientation(child, newOrientation, newAspectRatio, origWidth, origHeight);
                updateAspect(
                        child, newOrientation, newAspectRatio, origWidth, origHeight);
            }
   checkDirty();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_jRadioButton4ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (jList1.getSelectedValue() != null) {
            newPinToMenu.remove(jList1.getSelectedValue());
            parentCell.pinToMenuMap.remove(jList1.getSelectedValue());
            populatePinTOMenu();
}
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        if (dirty) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ok();
                }
            });
        }
        if (editor == null) {
            parent.setVisible(false);
        }

    }//GEN-LAST:event_jButton6ActionPerformed

    public Color getUserSelectedColor() {
        Color oldColor = parseColorString(AppFrameConstants.BorderColor);
        Color newColor = JColorChooser.showDialog(
                JmeClientMain.getFrame().getFrame(),
                "Border Color", oldColor);
        return newColor;
    }

    public static Color parseColorString(String colorString) {
        String[] c = colorString.split(":");
        if (c.length < 3) {
            LOGGER.severe("Improperly formatted color string passed: " + colorString);
            return null;
        }
        Integer r = Integer.parseInt(c[0]);
        Integer g = Integer.parseInt(c[1]);
        Integer b = Integer.parseInt(c[2]);
        Color newColor = new Color(r, g, b);
        return newColor;
    }

    public void populateMaxHistory() {
        jTextField1.setText(newMaxHistory);


    }

    public void populatePinTOMenu() {
        jList1.setListData(newPinToMenu.keySet().toArray());
        checkDirty();


    }

    /**
     * Find the appropriate map for storing our data in shared state. In this
     * case, the map is on the current cell, and named for the AppFrame class.
     */
    
    /**
     * Determine if the panel "Apply" and "OK" buttons should be enabled.
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private java.awt.Canvas canvas1;
    private java.awt.Canvas canvas2;
    private javax.swing.ButtonGroup displayBG;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.ButtonGroup playTypeBG;
    private javax.swing.ButtonGroup qualityBG;
    // End of variables declaration//GEN-END:variables

    public void checkDirty() {


        if (jTextField1.getText() != null) {
            dirty |= !origMaxHistory.equals(jTextField1.getText());
            newMaxHistory = jTextField1.getText();


        } else {
            dirty |= !origMaxHistory.equals(newMaxHistory);


        }
        if (!origAspectRatio.equalsIgnoreCase(newAspectRatio) || !origOrientation.equalsIgnoreCase(newOrientation) || !origBoarderColor.equalsIgnoreCase(newBoarderColor)) {
            dirty = true;


        }

        if (!dirty) {
            if (newPinToMenu.size() == origPinToMenu.size()) {

                for (Iterator<Entry<String, String>> i = newPinToMenu.entrySet().iterator(); i.hasNext();) {
                    Entry<String, String> e = i.next();


                    if (origPinToMenu.containsKey(e.getKey()) && origPinToMenu.containsValue(e.getValue())) {
                    } else {
                        dirty = true;


                        break;


                    }

                }

            } else {
                dirty = true;



            }

        }
        if (editor != null) {
            editor.setPanelDirty(AppFrameProperties.class, dirty);
        }


    }
}

