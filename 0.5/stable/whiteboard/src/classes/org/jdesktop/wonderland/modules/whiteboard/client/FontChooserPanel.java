/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.whiteboard.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.geom.Rectangle2D;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboPopup;

/**
 * font chooser with font family,style,size and color
 *
 * @author Abhishek Upadhyay
 */
public class FontChooserPanel extends javax.swing.JPanel {

    static final String[] fontStyleString = new String[]{"Font.PLAIN", "Font.ITALIC", "Font.BOLD", "Font.ITALIC+Font.BOLD"};
    static final int[] fontStyleInt = new int[]{Font.PLAIN, Font.ITALIC, Font.BOLD, Font.ITALIC + Font.BOLD};

    private int currentSize = 24;
    private int currentStyle = Font.PLAIN;
    private String currentStyleString = "Font.PLAIN";
    private String currentFontName = "Monospaced";
    private Color currentColor = Color.black;

    private int initialSize = 24;
    private String initialStyleString = "Font.PLAIN";
    private String initialFontName = "Monospaced";
    private Color initialColor = Color.black;
    private GraphicsPanel gp;

    private boolean forTextChange = false;

    /**
     * Creates new form FontChooserPanel
     */
    public FontChooserPanel() {
        initComponents();
        initials();
    }

    public FontChooserPanel(String fontName, String fontSize, String color, String style, String weight) {
        this.initialFontName = fontName;
        this.initialSize = Integer.parseInt(fontSize);
        this.initialColor = Color.decode(color);
        if (style.equals("normal") && weight.equals("normal")) {
            this.initialStyleString = "Font.PLAIN";
        } else if (style.equals("normal") && weight.equals("bold")) {
            this.initialStyleString = "Font.BOLD";
        } else if (style.equals("italic") && weight.equals("normal")) {
            this.initialStyleString = "Font.ITALIC";
        } else if (style.equals("italic") && weight.equals("bold")) {
            this.initialStyleString = "Font.ITALIC+Font.BOLD";
        }
        initComponents();
        initials();
        
        if (!System.getProperty("os.name").contains("Windows")) {
            final BasicComboPopup fontFamilyPopup = (BasicComboPopup) fontFamilyComboBox.getUI()
                    .getAccessibleChild(fontFamilyComboBox, 0);
            final BasicComboPopup stylePopup = (BasicComboPopup) styleComboBox.getUI()
                    .getAccessibleChild(styleComboBox, 0);
            fontFamilyPopup.addPopupMenuListener(new MyPopupListener(fontFamilyPopup));
            stylePopup.addPopupMenuListener(new MyPopupListener(stylePopup));
        }
    }

    private class MyPopupListener implements PopupMenuListener {

        private BasicComboPopup popup;

        public MyPopupListener(BasicComboPopup popup) {
            this.popup = popup;
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            popup.setLocation(MouseInfo.getPointerInfo().getLocation());
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }

    private void initials() {
        attachPreviewPanel();
        populateFontFamily();
        populateStyles();
        populateSlider();

        sizeText.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                changeSlider();
            }

            public void removeUpdate(DocumentEvent e) {
                changeSlider();
            }

            public void changedUpdate(DocumentEvent e) {
                changeSlider();
            }

            private void changeSlider() {
                try {
                    forTextChange = true;
                    if (!sizeText.getText().equals("")) {
                        currentSize = Integer.parseInt(sizeText.getText());
                        sizeSlider.setValue(currentSize); // set the slider
                    }
                } catch (NumberFormatException ne) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            sizeText.setText("" + currentSize);
                        }
                    });
                }
            }

        });

    }

    private void attachPreviewPanel() {
        gp = new GraphicsPanel();
        previewPanel.add(gp);
        previewPanel.validate();
        validate();
    }

    private void populateSlider() {
        sizeSlider.setMinimum(5);
        sizeSlider.setMaximum(240);
        sizeSlider.setMajorTickSpacing(20); // sets numbers for big tick marks
        sizeSlider.setMinorTickSpacing(1); // smaller tick marks
        sizeSlider.setPaintTicks(true); // display the ticks
        sizeSlider.setPaintLabels(true); // show the numbers
        sizeSlider.setValue(initialSize);
        sizeText.setText("" + initialSize);
    }

    private void populateStyles() {
        styleComboBox.setModel(new DefaultComboBoxModel(fontStyleString));
        styleComboBox.setSelectedItem(initialStyleString);
    }

    private void populateFontFamily() {
        String[] fontNames = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        Vector visFonts = new Vector(fontNames.length);

        for (int i = 0; i < fontNames.length; i++) {
            Font f = new Font(fontNames[i], Font.PLAIN, 12);
            if (f.canDisplay('a')) {
                // Display only fonts that have the alphabetic characters.
                // On my machine there are almost 20 fonts (eg, Wingdings)
                // that don't display text.
                visFonts.add(fontNames[i]);
            } else {
                
            }
        }
        fontFamilyComboBox.setModel(new DefaultComboBoxModel(visFonts));
        fontFamilyComboBox.setSelectedItem(initialFontName);
    }

    // A component to draw sample string with given font family, style, and size.
    class GraphicsPanel extends JPanel {

        public GraphicsPanel() {
            this.setPreferredSize(new Dimension(600, 100));
            this.setBackground(Color.white);
            this.setForeground(Color.black);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g); // paint background
            String text = "Font(\""
                    + currentFontName + "\", "
                    + currentStyleString + ", "
                    + currentSize + ");";
            Font f = new Font(currentFontName, currentStyle, currentSize);
            g.setColor(currentColor);
            g.setFont(f);

            // Find the size of this text so we can center it
            FontMetrics fm = g.getFontMetrics(f); // metrics for this object
            Rectangle2D rect = fm.getStringBounds(text, g); // size of string
            int textHeight = (int) (rect.getHeight());
            int textWidth = (int) (rect.getWidth());
            int panelHeight = this.getHeight();
            int panelWidth = this.getWidth();
            // Center text horizontally and vertically
            int x = (panelWidth - textWidth) / 2;
            int y = (panelHeight - textHeight) / 2 + fm.getAscent();
            g.drawString(text, x, y);
        }
    }

    public String getFontName() {
        return currentFontName;
    }

    public String getFontSize() {
        return "" + currentSize;
    }

    public JButton getOkButton() {
        return okButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public String getFontWeight() {
        if (currentStyle == Font.PLAIN) {
            return "normal";
        } else if (currentStyle == Font.BOLD) {
            return "bold";
        } else if (currentStyle == Font.ITALIC) {
            return "normal";
        } else if (currentStyle == (Font.BOLD + Font.ITALIC)) {
            return "bold";
        }
        return "normal";
    }

    public String getFontStyle() {
        if (currentStyle == Font.PLAIN) {
            return "normal";
        } else if (currentStyle == Font.BOLD) {
            return "normal";
        } else if (currentStyle == Font.ITALIC) {
            return "italic";
        } else if (currentStyle == (Font.BOLD + Font.ITALIC)) {
            return "italic";
        }
        return "normal";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        previewPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        fontFamilyComboBox = new javax.swing.JComboBox();
        styleComboBox = new javax.swing.JComboBox();
        sizeText = new javax.swing.JTextField();
        sizeSlider = new javax.swing.JSlider();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        previewPanel.setLayout(new java.awt.BorderLayout());

        jLabel1.setText("Font family : ");

        jLabel2.setText("Style : ");

        jLabel3.setText("Size : ");

        fontFamilyComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        fontFamilyComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontFamilyComboBoxActionPerformed(evt);
            }
        });

        styleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        styleComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                styleComboBoxActionPerformed(evt);
            }
        });

        sizeText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sizeTextActionPerformed(evt);
            }
        });

        sizeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sizeSliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(styleComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(sizeText, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(fontFamilyComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sizeSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE))
                .addGap(10, 10, 10))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(fontFamilyComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(styleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(sizeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(previewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        okButton.setText("Ok");

        cancelButton.setText("Cancel");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(okButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addContainerGap())
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fontFamilyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontFamilyComboBoxActionPerformed
        // TODO add your handling code here:
        currentFontName = fontFamilyComboBox.getSelectedItem().toString();
        gp.repaint();

    }//GEN-LAST:event_fontFamilyComboBoxActionPerformed

    private void styleComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_styleComboBoxActionPerformed
        // TODO add your handling code here:
        currentStyle = fontStyleInt[styleComboBox.getSelectedIndex()];
        gp.repaint();
    }//GEN-LAST:event_styleComboBoxActionPerformed

    private void sizeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sizeSliderStateChanged
        // TODO add your handling code here:
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (!forTextChange) {
                    currentSize = sizeSlider.getValue();
                    sizeText.setText("" + currentSize);
                    gp.repaint();
                } else {
                    forTextChange = false;
                }
            }
        });
    }//GEN-LAST:event_sizeSliderStateChanged

    private void sizeTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sizeTextActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_sizeTextActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox fontFamilyComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JSlider sizeSlider;
    private javax.swing.JTextField sizeText;
    private javax.swing.JComboBox styleComboBox;
    // End of variables declaration//GEN-END:variables
}
