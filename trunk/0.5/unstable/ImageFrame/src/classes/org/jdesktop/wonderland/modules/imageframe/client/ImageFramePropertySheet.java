/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.imageframe.client;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Enumeration;
import javax.swing.*;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameConstants;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameProperties;
import org.jdesktop.wonderland.modules.imageframe.common.ImageFrameServerState;

/**
 * properties panel for image frame
 */
@PropertiesFactory(ImageFrameServerState.class)
public class ImageFramePropertySheet extends javax.swing.JPanel implements PropertiesFactorySPI {

    private CellPropertiesEditor editor;
    private ImageFrameCell parentCell;
    private JFrame parentFrame;
    private Boolean dirty;
    private String oldFit;
    private String oldAspectRatio;
    private String oldOrientation;
    private String newFit;
    private String newAspectRatio;
    private String newOrientation;
    private ImageFrameProperties ifp;
    int fheight = 0;
    int fwidht = 0;

    /** Creates new form FairBoothConfiguration */
    public ImageFramePropertySheet() {
        initComponents();
        jPanel4.setVisible(false);
    }

    public ImageFramePropertySheet(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    private AbstractButton getSelectedButton(ButtonGroup buttonG) {

        Enumeration<AbstractButton> e = buttonG.getElements();
        while (e.hasMoreElements()) {
            AbstractButton ab = e.nextElement();
            if (ab.isSelected()) {
                return ab;
            }
        }
        return null;
    }

    private int getSelectedButtonIndex(ButtonGroup buttonG) {
        int i = 0;
        Enumeration<AbstractButton> e = buttonG.getElements();
        while (e.hasMoreElements()) {
            AbstractButton ab = e.nextElement();
            if (ab.isSelected()) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public void updateFit() {
        newFit = getSelectedButton(buttonGroup1).getText();
        if (checkDirty()) {
            editor.setPanelDirty(ImageFramePropertySheet.class, dirty);
        }
    }

    public void updateDemoImageAspectRatio() {
        AbstractButton but = getSelectedButton(buttonGroup2);
        newAspectRatio = getSelectedButton(buttonGroup2).getText();

        URL url = ImageFramePropertySheet.class.getResource("resources/ImageFrameIcon.jpg");
        ImageIcon imageIcon = new ImageIcon(url);
        int oldHeight = imageIcon.getIconHeight();
        int oldWidth = imageIcon.getIconWidth();
        double oldAspectRatio = (double) oldWidth / (double) oldHeight;
        double newAspectRation;

        if (but.getText().equals("1:1")) {
            newAspectRation = (double) 1 / (double) 1;
        } else if (but.getText().equals("5:4")) {
            newAspectRation = (double) 5 / (double) 4;
        } else if (but.getText().equals("4:3")) {
            newAspectRation = (double) 4 / (double) 3;
        } else if (but.getText().equals("16:9")) {
            newAspectRation = (double) 16 / (double) 9;
        } else {
            newAspectRation = (double) 2 / (double) 3;

        }
        int newHeight = 0;
        int newWidth = 0;
        int diff = 0;
        int newDiff = 0;

        if (((int) ((double) newAspectRation * (double) oldHeight)) < oldWidth) {
            newHeight = oldHeight;
            newWidth = (int) ((double) newAspectRation * (double) oldHeight);
            diff = oldWidth - newWidth;
            newDiff = diff / 2;
            BufferedImage tmpImage = new BufferedImage(oldWidth, oldHeight, BufferedImage.TYPE_INT_RGB);
            tmpImage.getGraphics().drawImage(imageIcon.getImage(), 0, 0, null);

            BufferedImage newImg = tmpImage.getSubimage(newDiff, 0, newWidth, newHeight);
            ImageIcon newIcon = new ImageIcon(newImg);
            //newImg.getGraphics().drawImage(tmpImage, 0, newDiff, null);
            jLabel6.setIcon(newIcon);

        } else {
            newWidth = oldWidth;
            newHeight = (int) (((double) 1 / (double) newAspectRation) * (double) oldHeight);

            diff = oldHeight - newHeight;
            newDiff = diff / 2;
            BufferedImage tmpImage = new BufferedImage(newWidth, oldHeight, BufferedImage.TYPE_INT_RGB);
            tmpImage.getGraphics().drawImage(imageIcon.getImage(), 0, 0, null);

            BufferedImage newImg = tmpImage.getSubimage(0, newDiff, newWidth, newHeight);
            ImageIcon newIcon = new ImageIcon(newImg);
            //newImg.getGraphics().drawImage(tmpImage, 0, newDiff, null);
            jLabel6.setIcon(newIcon);
        }

        // create image with new aspect ration
        //set orientation
        if (newOrientation.equals("Horizontal")) {
            ((RotateLabel) jLabel6).rotate(2);
        } else {
            ((RotateLabel) jLabel6).rotate(1);
        }

        if (checkDirty()) {
            editor.setPanelDirty(ImageFramePropertySheet.class, dirty);
        }
    }

    public void updateDemoImageOrientaion() {

        newOrientation = getSelectedButton(buttonGroup3).getText();
        if (newOrientation.equals("Horizontal")) {
            ((RotateLabel) jLabel6).rotate(2);
        } else {
            ((RotateLabel) jLabel6).rotate(1);
        }
        if (checkDirty()) {
            editor.setPanelDirty(ImageFramePropertySheet.class, dirty);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel4 = new javax.swing.JLabel();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new RotateLabel();
        jPanel1 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();
        jRadioButton7 = new javax.swing.JRadioButton();
        jRadioButton10 = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jRadioButton8 = new javax.swing.JRadioButton();
        jRadioButton9 = new javax.swing.JRadioButton();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("jLabel1");

        jLabel4.setText("jLabel4");

        setPreferredSize(new java.awt.Dimension(380, 330));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/imageframe/client/resources/ImageFrameIcon.jpg"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
        );

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("Fit Image");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        jLabel2.setText("Fit:");

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Constrain Height");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("Constrain Width");
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton1))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jRadioButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel3.setText("Aspect Ratio:");

        buttonGroup2.add(jRadioButton4);
        jRadioButton4.setText("1:1");
        jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton4ActionPerformed(evt);
            }
        });

        buttonGroup2.add(jRadioButton5);
        jRadioButton5.setText("5:4");
        jRadioButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton5ActionPerformed(evt);
            }
        });

        buttonGroup2.add(jRadioButton6);
        jRadioButton6.setText("4:3");
        jRadioButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton6ActionPerformed(evt);
            }
        });

        buttonGroup2.add(jRadioButton7);
        jRadioButton7.setText("16:9");
        jRadioButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton7ActionPerformed(evt);
            }
        });

        buttonGroup2.add(jRadioButton10);
        jRadioButton10.setText("2:3");
        jRadioButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton7)
                    .addComponent(jRadioButton5)
                    .addComponent(jRadioButton4)
                    .addComponent(jRadioButton6)
                    .addComponent(jRadioButton10))
                .addContainerGap(74, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jRadioButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton7)
                .addContainerGap(11, Short.MAX_VALUE))
        );

        jLabel5.setText("Orientation:");

        buttonGroup3.add(jRadioButton8);
        jRadioButton8.setText("Horizontal");
        jRadioButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton8ActionPerformed(evt);
            }
        });

        buttonGroup3.add(jRadioButton9);
        jRadioButton9.setText("Vertical");
        jRadioButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addGap(26, 26, 26)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton9)
                    .addComponent(jRadioButton8))
                .addContainerGap(47, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jRadioButton8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton9)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(28, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        // TODO add your handling code here:
        updateFit();
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed
        // TODO add your handling code here:
        updateDemoImageAspectRatio();
    }//GEN-LAST:event_jRadioButton4ActionPerformed

    private void jRadioButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton5ActionPerformed
        // TODO add your handling code here:
        updateDemoImageAspectRatio();
    }//GEN-LAST:event_jRadioButton5ActionPerformed

    private void jRadioButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton6ActionPerformed
        // TODO add your handling code here:
        updateDemoImageAspectRatio();
    }//GEN-LAST:event_jRadioButton6ActionPerformed

    private void jRadioButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton7ActionPerformed
        // TODO add your handling code here:

        updateDemoImageAspectRatio();
    }//GEN-LAST:event_jRadioButton7ActionPerformed

    private void jRadioButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton8ActionPerformed
        // TODO add your handling code here:
        //updateDemoImageOrientaion();
    }//GEN-LAST:event_jRadioButton8ActionPerformed

    private void jRadioButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton9ActionPerformed
        // TODO add your handling code here:
        //updateDemoImageOrientaion();
    }//GEN-LAST:event_jRadioButton9ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        // TODO add your handling code here:
        updateFit();
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        // TODO add your handling code here:
        updateFit();
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jRadioButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton10ActionPerformed
        // TODO add your handling code here:
        updateDemoImageAspectRatio();
    }//GEN-LAST:event_jRadioButton10ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton10;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JRadioButton jRadioButton8;
    private javax.swing.JRadioButton jRadioButton9;
    // End of variables declaration//GEN-END:variables

    public String getDisplayName() {
        return "ImageFrame Properties";
    }

    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    public JPanel getPropertiesJPanel() {
        return this;
    }

    public void setConfiguration() {
        if (editor != null) {
            parentCell = (ImageFrameCell) editor.getCell();
            ImageFrameProperties ifp = (ImageFrameProperties) parentCell.propertyMap.get(ImageFrameConstants.ImageFrameProperty);
            if (ifp != null) {
                int fit = ifp.getFit();
                switch (fit) {
                    case 0:
                        oldFit = jRadioButton1.getText();
                        newFit = oldFit;
                        jRadioButton1.setSelected(true);
                        jRadioButton2.setSelected(false);
                        jRadioButton3.setSelected(false);
                        break;
                    case 1:
                        oldFit = jRadioButton2.getText();
                        newFit = oldFit;
                        jRadioButton1.setSelected(false);
                        jRadioButton2.setSelected(true);
                        jRadioButton3.setSelected(false);
                        break;
                    case 2:
                        oldFit = jRadioButton3.getText();
                        newFit = oldFit;
                        jRadioButton1.setSelected(false);
                        jRadioButton2.setSelected(false);
                        jRadioButton3.setSelected(true);
                        break;
                }
                int ar = ifp.getAspectRatio();
                switch (ar) {
                    case 0:
                        oldAspectRatio = jRadioButton4.getText();
                        newAspectRatio = oldAspectRatio;
                        jRadioButton4.setSelected(true);
                        jRadioButton5.setSelected(false);
                        jRadioButton6.setSelected(false);
                        jRadioButton7.setSelected(false);

                        jRadioButton10.setSelected(false);
                        break;
                    case 1:
                        oldAspectRatio = jRadioButton5.getText();
                        newAspectRatio = oldAspectRatio;
                        jRadioButton4.setSelected(false);
                        jRadioButton5.setSelected(true);
                        jRadioButton6.setSelected(false);
                        jRadioButton7.setSelected(false);

                        jRadioButton10.setSelected(false);
                        break;
                    case 2:
                        oldAspectRatio = jRadioButton6.getText();
                        newAspectRatio = oldAspectRatio;
                        jRadioButton4.setSelected(false);
                        jRadioButton5.setSelected(false);
                        jRadioButton6.setSelected(true);
                        jRadioButton7.setSelected(false);

                        jRadioButton10.setSelected(false);
                        break;
                    case 3:
                        oldAspectRatio = jRadioButton7.getText();
                        newAspectRatio = oldAspectRatio;
                        jRadioButton4.setSelected(false);
                        jRadioButton5.setSelected(false);
                        jRadioButton6.setSelected(false);
                        jRadioButton7.setSelected(true);

                        jRadioButton10.setSelected(false);
                        break;
                    case 4:
                        oldAspectRatio = jRadioButton10.getText();
                        newAspectRatio = oldAspectRatio;
                        jRadioButton4.setSelected(false);
                        jRadioButton5.setSelected(false);
                        jRadioButton6.setSelected(false);
                        jRadioButton7.setSelected(false);
                        jRadioButton10.setSelected(true);
                        break;



                }
                int or = ifp.getOrientation();
                switch (or) {
                    case 0:
                        oldOrientation = jRadioButton8.getText();
                        newOrientation = oldOrientation;
                        jRadioButton8.setSelected(true);
                        jRadioButton9.setSelected(false);
                        break;
                    case 1:
                        oldOrientation = jRadioButton9.getText();
                        newOrientation = oldOrientation;
                        jRadioButton8.setSelected(false);
                        jRadioButton9.setSelected(true);
                        break;
                }

                //set aspect ratio
                updateDemoImageAspectRatio();

                //set orientation
                //updateDemoImageOrientaion();
            }
            editor.setPanelDirty(ImageFramePropertySheet.class, false);
        }
    }

    public void open() {
        restore();
    }

    public void close() {
    }

    public void restore() {
        setConfiguration();
    }

    public void apply() {

        ifp = new ImageFrameProperties();

        int fit = getSelectedButtonIndex(buttonGroup1);
        int ar = getSelectedButtonIndex(buttonGroup2);
        int or = getSelectedButtonIndex(buttonGroup3);
        ifp.setFit(fit);
        ifp.setAspectRatio(ar);
        ifp.setOrientation(or);

        parentCell.propertyMap.put(ImageFrameConstants.ImageFrameProperty, ifp);
    }

    public boolean checkDirty() {
        dirty = false;
        dirty |= !(oldFit.equals(newFit));
        dirty |= !(oldAspectRatio.equals(newAspectRatio));
        dirty |= !(oldOrientation.equals(newOrientation));
        return dirty;
    }

    public class RotateLabel extends JLabel {

        int flg = 0;

        public RotateLabel() {
            super();
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            int w2 = getWidth() / 2;
            int h2 = getHeight() / 2;
            if (flg == 1) {
                g2d.rotate(-Math.PI / 2, w2, h2);
                flg = 0;
            } else if (flg == 2) {
                g2d.rotate(0, w2, h2);
                flg = 0;
            }
            super.paintComponent(g);
        }

        public void rotate(int orientation) {
            flg = orientation;
            this.repaint();
        }
    }
}

