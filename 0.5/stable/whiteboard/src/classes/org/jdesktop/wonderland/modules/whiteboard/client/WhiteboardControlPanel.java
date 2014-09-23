/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/*
 * Project Wonderland
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 * 
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 * 
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 * 
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */

/*
 * WhiteboardControlPanel.java
 *
 * Created on Jan 29, 2009, 4:55:50 PM
 */
package org.jdesktop.wonderland.modules.whiteboard.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.modules.whiteboard.client.WhiteboardToolManager.WhiteboardColor;
import org.jdesktop.wonderland.modules.whiteboard.client.WhiteboardToolManager.WhiteboardTool;

/**
 *
 * @author nsimpson
 * @author Abhishek Upadhyay
 */
public class WhiteboardControlPanel extends javax.swing.JPanel implements CellMenu {

    private static final Logger logger = Logger.getLogger(WhiteboardControlPanel.class.getName());
    protected boolean fillMode = false;
    protected ArrayList<WhiteboardCellMenuListener> cellMenuListeners = new ArrayList();
    protected Map toolMappings;
    protected Map colorMappings;
    protected WhiteboardDragGestureListener gestureListener;
    protected WhiteboardWindow window;
    protected ButtonHoverListener hoverListener;
    protected Map<JButton,String> toolTipTextMap;

    public WhiteboardControlPanel(WhiteboardWindow window) {
        this.window = window;
        initComponents();
        initButtonMaps();
        initListeners();
    }

    public void addCellMenuListener(WhiteboardCellMenuListener listener) {
        cellMenuListeners.add(listener);
    }

    public void removeCellMenuListener(WhiteboardCellMenuListener listener) {
        cellMenuListeners.remove(listener);
    }

    private void initButtonMaps() {
        toolMappings = Collections.synchronizedMap(new HashMap());
        toolMappings.put(WhiteboardTool.SELECTOR, selectButton);
        toolMappings.put(WhiteboardTool.LINE, lineButton);
        toolMappings.put(WhiteboardTool.RECT, rectangleButton);
        toolMappings.put(WhiteboardTool.ELLIPSE, ellipseButton);
        toolMappings.put(WhiteboardTool.RECT_FILL, rectangleFillButton);
        toolMappings.put(WhiteboardTool.ELLIPSE_FILL, ellipseFillButton);
        toolMappings.put(WhiteboardTool.NEW_TEXT, textButton);
        toolMappings.put(WhiteboardTool.BACKGROUND_IMAGE, backgroundImageButton);
        toolMappings.put(WhiteboardTool.IMAGE, moveableImage);

        colorMappings = Collections.synchronizedMap(new HashMap());
        colorMappings.put(WhiteboardColor.RED, new Object());
        colorMappings.put(WhiteboardColor.GREEN, new Object());
        colorMappings.put(WhiteboardColor.BLUE, new Object());
        colorMappings.put(WhiteboardColor.BLACK, new Object());
        colorMappings.put(WhiteboardColor.WHITE, new Object());
        
        //add tooltips
        toolTipTextMap = new HashMap<JButton, String>();
        toolTipTextMap.put(selectButton, " selector ");
        toolTipTextMap.put(textButton, " add text ");
        toolTipTextMap.put(fontButton, " change font ");
        toolTipTextMap.put(lineButton, " line ");
        toolTipTextMap.put(rectangleButton, " rectangle ");
        toolTipTextMap.put(rectangleFillButton, " filled rectangle ");
        toolTipTextMap.put(ellipseButton, " ellipse ");
        toolTipTextMap.put(ellipseFillButton, " filled ellipse ");
        toolTipTextMap.put(colorChooserButton, " color chooser ");
        toolTipTextMap.put(bringToFrontButton, " bring to front ");
        toolTipTextMap.put(sendToBackButton, " send to back ");
        toolTipTextMap.put(moveableImage, " add moveable image ");
        toolTipTextMap.put(backgroundImageButton, " add backgroung image ");
        toolTipTextMap.put(newButton, " clear ");
        toolTipTextMap.put(resizeButton, " resize element ");
        toolTipTextMap.put(dragButton, " drag ");
        if (!System.getProperty("os.name").contains("Windows")) {
            if(hoverListener==null) {
                hoverListener = new ButtonHoverListener();
                selectButton.addMouseListener(hoverListener);
                textButton.addMouseListener(hoverListener);
                fontButton.addMouseListener(hoverListener);
                lineButton.addMouseListener(hoverListener);
                rectangleButton.addMouseListener(hoverListener);
                rectangleFillButton.addMouseListener(hoverListener);
                ellipseButton.addMouseListener(hoverListener);
                ellipseFillButton.addMouseListener(hoverListener);
                colorChooserButton.addMouseListener(hoverListener);
                bringToFrontButton.addMouseListener(hoverListener);
                sendToBackButton.addMouseListener(hoverListener);
                moveableImage.addMouseListener(hoverListener);
                backgroundImageButton.addMouseListener(hoverListener);
                newButton.addMouseListener(hoverListener);
                resizeButton.addMouseListener(hoverListener);
                dragButton.addMouseListener(hoverListener);
            }
        } else {
            selectButton.setToolTipText(toolTipTextMap.get(selectButton));
            textButton.setToolTipText(toolTipTextMap.get(textButton));
            fontButton.setToolTipText(toolTipTextMap.get(fontButton));
            lineButton.setToolTipText(toolTipTextMap.get(lineButton));
            rectangleButton.setToolTipText(toolTipTextMap.get(rectangleButton));
            rectangleFillButton.setToolTipText(toolTipTextMap.get(rectangleFillButton));
            ellipseButton.setToolTipText(toolTipTextMap.get(ellipseButton));
            ellipseFillButton.setToolTipText(toolTipTextMap.get(ellipseFillButton));
            colorChooserButton.setToolTipText(toolTipTextMap.get(colorChooserButton));
            bringToFrontButton.setToolTipText(toolTipTextMap.get(bringToFrontButton));
            sendToBackButton.setToolTipText(toolTipTextMap.get(sendToBackButton));
            moveableImage.setToolTipText(toolTipTextMap.get(moveableImage));
            backgroundImageButton.setToolTipText(toolTipTextMap.get(backgroundImageButton));
            newButton.setToolTipText(toolTipTextMap.get(newButton));
            resizeButton.setToolTipText(toolTipTextMap.get(resizeButton));
            dragButton.setToolTipText(toolTipTextMap.get(dragButton));
        }
    }
    
    private class ButtonHoverListener extends MouseAdapter {

        Popup popup = null;

        @Override
        public void mouseEntered(final MouseEvent e) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    JButton but = (JButton) e.getSource();
                    JPanel panel = new JPanel();
                    panel.setLayout(new BorderLayout(2,2));
                    panel.setBackground(new Color(240,220,130));
                    panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
                    JLabel l = new JLabel(toolTipTextMap.get(but));
                    panel.add(l);
                    Point location = MouseInfo.getPointerInfo().getLocation();
                    popup = PopupFactory.getSharedInstance().getPopup(selectButton, panel
                            , location.x + 30, location.y);
                    popup.show();
                }
            });
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (popup != null) {
                popup.hide();
            }
        }
    }

    private void initListeners() {
        DragSource ds = DragSource.getDefaultDragSource();
        gestureListener = new WhiteboardDragGestureListener(window);
        ds.createDefaultDragGestureRecognizer(dragButton,
                DnDConstants.ACTION_COPY_OR_MOVE, gestureListener);
    }

    public void setSelectedColor(WhiteboardColor color) {
        JButton colorButton = (JButton) colorMappings.get(color);
        depressButton(colorButton, true);
    }

    public WhiteboardColor getSelectedColor() {
        return WhiteboardColor.BLACK;
    }

    /**
     * Gets whether a button is depressed
     * @param button the button to check
     * @return true if the button is depressed, false otherwise
     */
    public boolean isButtonDepressed(JButton button) {
        return button.isBorderPainted();
    }

    /**
     * Depress/undepress a button
     * @param button the button to depress
     * @param depress true to depress a button, false to undepress
     */
    public void depressButton(JButton button, boolean depress) {
        button.setBorderPainted(depress);
    }

    public void selectTool(WhiteboardTool tool) {
        JButton button = (JButton) toolMappings.get(tool);
        if (button != null) {
            depressButton(button, true);
        }
    }

    public void deselectTool(WhiteboardTool tool) {
        JButton button = (JButton) toolMappings.get(tool);
        if (button != null) {
            depressButton(button, false);
        }
    }

    public void selectColor(WhiteboardColor color) {

    }

    public void deselectColor(WhiteboardColor color) {
        JButton button = (JButton) colorMappings.get(color);
        if (button != null) {
            depressButton(button, false);
        }
    }

    public JSpinner getResizeSpinner() {
        return resizeSpinner;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        selectButton = new javax.swing.JButton();
        lineButton = new javax.swing.JButton();
        rectangleButton = new javax.swing.JButton();
        ellipseButton = new javax.swing.JButton();
        dragButton = new javax.swing.JButton();
        backgroundImageButton = new javax.swing.JButton();
        fontButton = new javax.swing.JButton();
        moveableImage = new javax.swing.JButton();
        textButton = new javax.swing.JButton();
        colorChooserButton = new javax.swing.JButton();
        sendToBackButton = new javax.swing.JButton();
        bringToFrontButton = new javax.swing.JButton();
        rectangleFillButton = new javax.swing.JButton();
        ellipseFillButton = new javax.swing.JButton();
        resizeButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        resizeSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(231, 230, 230));

        newButton.setBackground(new java.awt.Color(231, 230, 230));
        newButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardNewDocument32x32.png"))); // NOI18N
        newButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        newButton.setBorderPainted(false);
        newButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        selectButton.setBackground(new java.awt.Color(231, 230, 230));
        selectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardSelect32x32.png"))); // NOI18N
        selectButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        selectButton.setBorderPainted(false);
        selectButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectButtonActionPerformed(evt);
            }
        });

        lineButton.setBackground(new java.awt.Color(231, 230, 230));
        lineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardDrawLine32x32.png"))); // NOI18N
        lineButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        lineButton.setBorderPainted(false);
        lineButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        lineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineButtonActionPerformed(evt);
            }
        });

        rectangleButton.setBackground(new java.awt.Color(231, 230, 230));
        rectangleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardDrawRectangle32x32.png"))); // NOI18N
        rectangleButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        rectangleButton.setBorderPainted(false);
        rectangleButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        rectangleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rectangleButtonActionPerformed(evt);
            }
        });

        ellipseButton.setBackground(new java.awt.Color(231, 230, 230));
        ellipseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardDrawEllipse32x32.png"))); // NOI18N
        ellipseButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        ellipseButton.setBorderPainted(false);
        ellipseButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        ellipseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ellipseButtonActionPerformed(evt);
            }
        });

        dragButton.setBackground(new java.awt.Color(231, 230, 230));
        dragButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardDrag32x32.png"))); // NOI18N
        dragButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        dragButton.setBorderPainted(false);
        dragButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        dragButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dragButtonActionPerformed(evt);
            }
        });

        backgroundImageButton.setBackground(new java.awt.Color(231, 230, 230));
        backgroundImageButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardBackgroundImage32x32.png"))); // NOI18N
        backgroundImageButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        backgroundImageButton.setBorderPainted(false);
        backgroundImageButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        backgroundImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundImageButtonActionPerformed(evt);
            }
        });

        fontButton.setBackground(new java.awt.Color(231, 230, 230));
        fontButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardFont32x32.png"))); // NOI18N
        fontButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        fontButton.setBorderPainted(false);
        fontButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        fontButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontButtonActionPerformed(evt);
            }
        });

        moveableImage.setBackground(new java.awt.Color(231, 230, 230));
        moveableImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardMovableImage32x32.png"))); // NOI18N
        moveableImage.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        moveableImage.setBorderPainted(false);
        moveableImage.setMargin(new java.awt.Insets(0, -4, 0, -4));
        moveableImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveableImageActionPerformed(evt);
            }
        });

        textButton.setBackground(new java.awt.Color(231, 230, 230));
        textButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardAddText32x32.png"))); // NOI18N
        textButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        textButton.setBorderPainted(false);
        textButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        textButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textButtonActionPerformed(evt);
            }
        });

        colorChooserButton.setBackground(new java.awt.Color(231, 230, 230));
        colorChooserButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardFill32x32.png"))); // NOI18N
        colorChooserButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        colorChooserButton.setBorderPainted(false);
        colorChooserButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        colorChooserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooserButtonActionPerformed(evt);
            }
        });

        sendToBackButton.setBackground(new java.awt.Color(231, 230, 230));
        sendToBackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardSendToBack32x32.png"))); // NOI18N
        sendToBackButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        sendToBackButton.setBorderPainted(false);
        sendToBackButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        sendToBackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendToBackButtonActionPerformed(evt);
            }
        });

        bringToFrontButton.setBackground(new java.awt.Color(231, 230, 230));
        bringToFrontButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardBringToFront32x32.PNG"))); // NOI18N
        bringToFrontButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        bringToFrontButton.setBorderPainted(false);
        bringToFrontButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        bringToFrontButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bringToFrontButtonActionPerformed(evt);
            }
        });

        rectangleFillButton.setBackground(new java.awt.Color(231, 230, 230));
        rectangleFillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardDrawFillRectangle32x32.png"))); // NOI18N
        rectangleFillButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        rectangleFillButton.setBorderPainted(false);
        rectangleFillButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        rectangleFillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rectangleFillButtonActionPerformed(evt);
            }
        });

        ellipseFillButton.setBackground(new java.awt.Color(231, 230, 230));
        ellipseFillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardDrawEllipseFilled32x32.png"))); // NOI18N
        ellipseFillButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        ellipseFillButton.setBorderPainted(false);
        ellipseFillButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        ellipseFillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ellipseFillButtonActionPerformed(evt);
            }
        });

        resizeButton.setBackground(new java.awt.Color(231, 230, 230));
        resizeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/whiteboard/client/resources/WhiteboardResize32x32.PNG"))); // NOI18N
        resizeButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        resizeButton.setBorderPainted(false);
        resizeButton.setMargin(new java.awt.Insets(0, -4, 0, -4));
        resizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resizeButtonActionPerformed(evt);
            }
        });

        resizeSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("%");

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(resizeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 64, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resizeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(selectButton)
                .add(0, 0, 0)
                .add(textButton)
                .add(0, 0, 0)
                .add(fontButton)
                .add(0, 0, 0)
                .add(lineButton)
                .add(0, 0, 0)
                .add(rectangleButton)
                .add(0, 0, 0)
                .add(rectangleFillButton)
                .add(0, 0, 0)
                .add(ellipseButton)
                .add(0, 0, 0)
                .add(ellipseFillButton)
                .add(0, 0, 0)
                .add(colorChooserButton)
                .add(0, 0, 0)
                .add(bringToFrontButton)
                .add(0, 0, 0)
                .add(sendToBackButton)
                .add(0, 0, 0)
                .add(moveableImage)
                .add(0, 0, 0)
                .add(backgroundImageButton)
                .add(0, 0, 0)
                .add(newButton)
                .add(0, 0, 0)
                .add(dragButton)
                .add(0, 0, 0)
                .add(resizeButton)
                .add(0, 0, 0)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(fontButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(moveableImage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(newButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(selectButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lineButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rectangleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ellipseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(dragButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(textButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(backgroundImageButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(colorChooserButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sendToBackButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bringToFrontButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rectangleFillButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ellipseFillButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(resizeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .add(0, 0, 0))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.remove();
        }
}//GEN-LAST:event_newButtonActionPerformed

    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectButtonActionPerformed
        if (!isButtonDepressed(selectButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while (iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.selector();
            }
        }
}//GEN-LAST:event_selectButtonActionPerformed

    private void lineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineButtonActionPerformed
        if (!isButtonDepressed(lineButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while (iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.line();
            }
        }
}//GEN-LAST:event_lineButtonActionPerformed

    private void rectangleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rectangleButtonActionPerformed
        if (!isButtonDepressed(rectangleButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while (iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.rect(false);
            }
        }
}//GEN-LAST:event_rectangleButtonActionPerformed

    private void ellipseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ellipseButtonActionPerformed
        if (!isButtonDepressed(ellipseButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while (iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.ellipse(false);
            }
        }
}//GEN-LAST:event_ellipseButtonActionPerformed

    private void dragButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dragButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_dragButtonActionPerformed

    private void backgroundImageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundImageButtonActionPerformed
        // TODO add your handling code here:
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.backgroundImage();
        }
    }//GEN-LAST:event_backgroundImageButtonActionPerformed

    private void fontButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontButtonActionPerformed
        // TODO add your handling code here:
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.changeFont();
        }
    }//GEN-LAST:event_fontButtonActionPerformed

    private void moveableImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveableImageActionPerformed
        // TODO add your handling code here:
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.image();
        }
    }//GEN-LAST:event_moveableImageActionPerformed

    private void textButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textButtonActionPerformed
        // TODO add your handling code here:
        if (!isButtonDepressed(textButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while (iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.newText();
            }
        }
    }//GEN-LAST:event_textButtonActionPerformed

    private void colorChooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooserButtonActionPerformed
        // TODO add your handling code here:
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.colorChooser();
        }
    }//GEN-LAST:event_colorChooserButtonActionPerformed

    private void sendToBackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendToBackButtonActionPerformed
        // TODO add your handling code here:
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.sendToBack();
        }
    }//GEN-LAST:event_sendToBackButtonActionPerformed

    private void bringToFrontButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bringToFrontButtonActionPerformed
        // TODO add your handling code here:
        Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
        while (iter.hasNext()) {
            WhiteboardCellMenuListener listener = iter.next();
            listener.bringToFront();
        }
    }//GEN-LAST:event_bringToFrontButtonActionPerformed

    private void rectangleFillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rectangleFillButtonActionPerformed
        // TODO add your handling code here:
        if (!isButtonDepressed(rectangleFillButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while (iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.rect(true);
            }
        }
    }//GEN-LAST:event_rectangleFillButtonActionPerformed

    private void ellipseFillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ellipseFillButtonActionPerformed
        // TODO add your handling code here:
        if (!isButtonDepressed(ellipseFillButton)) {
            Iterator<WhiteboardCellMenuListener> iter = cellMenuListeners.iterator();
            while (iter.hasNext()) {
                WhiteboardCellMenuListener listener = iter.next();
                listener.ellipse(true);
            }
        }
    }//GEN-LAST:event_ellipseFillButtonActionPerformed

    private void resizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resizeButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_resizeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backgroundImageButton;
    private javax.swing.JButton bringToFrontButton;
    private javax.swing.JButton colorChooserButton;
    private javax.swing.JButton dragButton;
    private javax.swing.JButton ellipseButton;
    private javax.swing.JButton ellipseFillButton;
    private javax.swing.JButton fontButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton lineButton;
    private javax.swing.JButton moveableImage;
    private javax.swing.JButton newButton;
    private javax.swing.JButton rectangleButton;
    private javax.swing.JButton rectangleFillButton;
    private javax.swing.JButton resizeButton;
    private javax.swing.JSpinner resizeSpinner;
    private javax.swing.JButton selectButton;
    private javax.swing.JButton sendToBackButton;
    private javax.swing.JButton textButton;
    // End of variables declaration//GEN-END:variables
}
