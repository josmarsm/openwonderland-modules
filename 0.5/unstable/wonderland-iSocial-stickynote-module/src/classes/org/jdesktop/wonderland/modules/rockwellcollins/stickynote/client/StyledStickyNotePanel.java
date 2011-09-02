/**
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
package org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.util.logging.Logger;
import javax.swing.JWindow;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.cell.StickyNoteCell;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteCellClientState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.messages.StickyNoteSyncMessage;
//import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.MutableAttributeSet;
import javax.swing.JTextPane;
import java.awt.Font;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.SelectedTextStyle;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.NoteAttributeSet;

import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.text.SimpleAttributeSet;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteTypes;

/**
 * The JPanel displayed in Wonderland for a generic sticky note.
 *
 */
//http://www.exampledepot.com/egs/javax.swing.text/style_HiliteWords2.html
public class StyledStickyNotePanel extends javax.swing.JPanel implements ActionListener, FocusListener, StickyNotePanel {

    private JFrame frame;
    private Timer keyTimer;
    private static Logger logger = Logger.getLogger(StyledStickyNotePanel.class.getName());
    private StickyNoteCellClientState lastSyncedState = new StickyNoteCellClientState();
    private StickyNoteCell cell;
    private StickyNoteParentPanel parentPanel = null;

    /** Creates new form GenericStickyNotePanel */
    public StyledStickyNotePanel() {
        initComponents();
        keyTimer = new Timer(1000, this);
        jTextPane1.addFocusListener(this);
        JButton italicButton = new JButton();
        //add Italic button
        italicButton.setName("Italic");
        italicButton.setFont(new Font("Arial", Font.ITALIC, 18));
        italicButton.setText("I");
        italicButton.addActionListener(listener);
        italicButton.setSize(20, 20);
        jPanel1.add(italicButton);

        //add Bold button
        JButton boldButton = new JButton();
        boldButton.setFont(new Font("Arial", Font.BOLD, 18));
        boldButton.setText("B");
        boldButton.setName("Bold");
        boldButton.addActionListener(listener);
        boldButton.setSize(20, 20);
        jPanel1.add(boldButton);

        //add underline button

        JButton underlineButton = new JButton();
        underlineButton.setName("Underline");
        underlineButton.setFont(new Font("Arial", Font.PLAIN, 18));
        underlineButton.setText("U");
        underlineButton.addActionListener(listener);
        underlineButton.setSize(20, 20);
        jPanel1.add(underlineButton);

        //Add Plus button
        JButton plusButton = new JButton();
        plusButton.setText("+");
        plusButton.setName("Plus");
        plusButton.addActionListener(listener);
        plusButton.setSize(20, 20);
        jPanel1.add(plusButton);

        //Add Minus Button
        JButton minusButton = new JButton();
        minusButton.setText("-");
        minusButton.setSize(20, 20);
        minusButton.setName("Minus");
        minusButton.addActionListener(listener);
        jPanel1.add(minusButton);


    }
    /*
     * This is the Action Listener when a style button is clicked
     * Need to get the clicked button name and the selected text beginning position and ending position
     */
    private final ActionListener listener = new ActionListener() {

        public void actionPerformed(ActionEvent ae) {
            Object source = ae.getSource();
            if (source instanceof JButton) {
                JButton srcButton = (JButton) source;
                String name = srcButton.getName();
                String buttonName;
                SelectedTextStyle temp = new SelectedTextStyle();
                if (srcButton.getName().equals("Bold")) {
                    buttonName = "Bold";

                } else if (srcButton.getName().equals("Italic")) {
                    buttonName = "Italic";

                } else if (srcButton.getName().equals("Underline")) {
                    buttonName = "Underline";
                } else if (srcButton.getName().equals("Plus")) {
                    buttonName = "Plus";
                } else if (srcButton.getName().equals("Minus")) {
                    buttonName = "Minus";
                } else {
                    buttonName = "";
                }
                temp.setSelectedStartPos(jTextPane1.getSelectionStart());
                temp.setSelectedEndPos(jTextPane1.getSelectionStart() + jTextPane1.getSelectedText().length());
                temp.setButtonName(buttonName);

                if (lastSyncedState != null) {

                    setJTextPaneFont(jTextPane1, buttonName, Color.BLACK, jTextPane1.getSelectionStart(), jTextPane1.getSelectedText().length());
                    update_all_attributes_to_server();
                    lastSyncedState.setSelectedTextStyle(temp);
                    cell.sendSyncMessage(lastSyncedState);

                    jTextPane1.setSelectionStart(0);
                    jTextPane1.setSelectionEnd(0);
                    //buttonName="";
                    repaint();


                }

            }
        }
    };

    /** Creates new form GenericStickyNotePanel */
    public void getAllAttributes() {

        boolean isBold, isItalic, isUnderline;
        int fontSize;
        StyledDocument doc = jTextPane1.getStyledDocument();
        List<NoteAttributeSet> temp;
        temp = lastSyncedState.getAllNoteAttributes();
        if (lastSyncedState.getAllNoteAttributes() != null) {
            for (int i = 0; i < temp.size(); i++) {
                NoteAttributeSet noteAtt = (NoteAttributeSet) temp.get(i);
                isItalic = noteAtt.getItalic();
                isBold = noteAtt.getBold();
                isUnderline = noteAtt.getUnderline();
                fontSize = noteAtt.getFontSize();
                MutableAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setItalic(attrs, isItalic);
                StyleConstants.setBold(attrs, isBold);
                StyleConstants.setUnderline(attrs, isUnderline);

                StyleConstants.setFontSize(attrs, fontSize);


                doc.setCharacterAttributes(i, 1, attrs, true);

            }
        }

    }

    public StyledStickyNotePanel(StickyNoteCell cell, StickyNoteCellClientState state, StickyNoteParentPanel parentP) {
        this();
        this.cell = cell;
        parentPanel = parentP;
        jTextPane1.setText(state.getNoteText());
        //add the attributes here;
        StyledDocument doc = jTextPane1.getStyledDocument();
        boolean isBold, isItalic, isUnderline;
        int fontSize;

        if (state.getAllNoteAttributes() != null) {
            for (int i = 0; i < state.getAllNoteAttributes().size(); i++) {
                NoteAttributeSet noteAtt = (NoteAttributeSet) state.getAllNoteAttributes().get(i);
                MutableAttributeSet attrs = new SimpleAttributeSet();
                isItalic = noteAtt.getItalic();
                isBold = noteAtt.getBold();
                isUnderline = noteAtt.getUnderline();
                fontSize = noteAtt.getFontSize();

                StyleConstants.setItalic(attrs, isItalic);
                StyleConstants.setBold(attrs, isBold);
                StyleConstants.setUnderline(attrs, isUnderline);

                StyleConstants.setFontSize(attrs, fontSize);
                doc.setCharacterAttributes(i, 1, attrs, false);

            }
        }
        setColor(state.getNoteColor());


    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        jToolBar1.setRollover(true);

        setBackground(new java.awt.Color(255, 255, 153));
        setMinimumSize(new java.awt.Dimension(23, 23));
        setPreferredSize(new java.awt.Dimension(250, 280));
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        setLayout(new java.awt.GridLayout(1, 0));

        jPanel2.setBackground(new java.awt.Color(255, 255, 153));
        jPanel2.setPreferredSize(new java.awt.Dimension(250, 280));

        jPanel1.setBackground(new java.awt.Color(255, 255, 153));
        jPanel1.setMinimumSize(new java.awt.Dimension(238, 20));
        jPanel1.setPreferredSize(new java.awt.Dimension(238, 20));
        jPanel1.setLayout(new java.awt.GridLayout(1, 5, 0, 1));

        jScrollPane2.setBackground(new java.awt.Color(255, 255, 153));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(242, 94));

        jTextPane1.setBackground(new java.awt.Color(255, 255, 153));
        jTextPane1.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        jTextPane1.setPreferredSize(new java.awt.Dimension(242, 90));
        jScrollPane2.setViewportView(jTextPane1);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
        );

        add(jPanel2);
    }// </editor-fold>//GEN-END:initComponents

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
// TODO add your handling code here:
        //System.out.println("Panel focus Gained");
    }//GEN-LAST:event_formFocusGained
    private boolean ortho = false;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

    public static void main(String args[]) {
        JWindow j = new JWindow();
        System.out.println(j.getLayout());
        j.add(new StyledStickyNotePanel());
        j.pack();
        j.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        resetSelectedTextStyle();

        if (e.getSource() == keyTimer) {
            // Check to see if we need to send a message w/ new text to the clients
            checkSendChanges();
        }
        //Only if lastSyncedState is not null, then send out the message
        if (lastSyncedState != null) {

            cell.sendSyncMessage(lastSyncedState);

        }
    }

    public void focusGained(FocusEvent e) {
        keyTimer.start();

    }

    public void focusLost(FocusEvent e) {
        keyTimer.stop();
        checkSendChanges();
    }

    /*
     * Reset the selected Text Style to empty
     */
    public void resetSelectedTextStyle() {
        SelectedTextStyle tem = new SelectedTextStyle();

        lastSyncedState.setSelectedTextStyle(tem);


    }

    private void update_all_attributes_to_server() {

        ArrayList temp_attributes;
        temp_attributes = new ArrayList();
        boolean isBold, isItalic, isUnderline;
        int fontSize, i;
        int currentCaretPos;
        currentCaretPos = jTextPane1.getCaretPosition();
        for (i = 0; i < jTextPane1.getText().length(); i++) {

            jTextPane1.setCaretPosition(i);

            SimpleAttributeSet attrs = new SimpleAttributeSet(jTextPane1.getCharacterAttributes());//us
            isItalic = StyleConstants.isItalic(attrs);
            isBold = StyleConstants.isBold(attrs);
            isUnderline = StyleConstants.isUnderline(attrs);
            fontSize = StyleConstants.getFontSize(attrs);


            NoteAttributeSet noteAttr = new NoteAttributeSet(isItalic, isBold, isUnderline, fontSize);
            temp_attributes.add(i, noteAttr);


        }
        jTextPane1.setCaretPosition(currentCaretPos);


        lastSyncedState.setAllNoteAttributes(temp_attributes);

    }

    private synchronized void checkSendChanges() {
        lastSyncedState.setNoteType(StickyNoteTypes.STYLED);
        if (lastSyncedState == null || !lastSyncedState.getNoteText().equals(jTextPane1.getText())) {

            lastSyncedState.setNoteText(jTextPane1.getText());
            update_all_attributes_to_server();

            cell.sendSyncMessage(lastSyncedState);
        }
    }
    /*
     * Process the message when getting a message from server
     */

    public void processMessage(final StickyNoteSyncMessage pcm) {
        lastSyncedState = pcm.getState();

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                //Update the jTextPanel only when the text is different
                if (!jTextPane1.getText().equals(pcm.getState().getNoteText()))//this was the bug for blank click updateing
                {
                    jTextPane1.setText(pcm.getState().getNoteText());
                    getAllAttributes();
                }

                setColor(pcm.getState().getNoteColor());

                SelectedTextStyle lastStyle = lastSyncedState.getSelectedTextStyle();
                String buttonName;
                buttonName = lastStyle.getButtonName();
                if (!lastStyle.getButtonName().equals("") && lastStyle.getSelectedEndPos() - lastStyle.getSelectedStartPos() > 0) {

                    setJTextPaneFont(jTextPane1, buttonName, Color.BLACK, lastStyle.getSelectedStartPos(), lastStyle.getSelectedEndPos() - lastStyle.getSelectedStartPos());

                  }


                repaint();

            }
        });
    }

    /*
     * Update the jTextPane1 text style based on the position of selected text and the button name
     */
    public void setJTextPaneFont(JTextPane jtp, String buttonName, Color c, int startPos, int length) {
        //need set the Caret position to the end of the start postion to get the current attribute of the
        //selected text


        int currentCaretPos = jtp.getCaretPosition();
        jtp.setCaretPosition(startPos + 1);



        //used to remember old attributes;

        SimpleAttributeSet attrs = new SimpleAttributeSet(jtp.getCharacterAttributes());

        //new attribute to be updated based on the old attributes
        MutableAttributeSet attrs_new = new SimpleAttributeSet();

        if (buttonName.equals("Bold")) {

            StyleConstants.setBold(attrs_new, !StyleConstants.isBold(attrs));


        } else if (buttonName.equals("Italic")) {
            StyleConstants.setItalic(attrs_new, !StyleConstants.isItalic(attrs));
        } else if (buttonName.equals("Underline")) {
            StyleConstants.setUnderline(attrs_new, !StyleConstants.isUnderline(attrs));


        } else if (buttonName.equals("Plus")) {
            int currentSize = StyleConstants.getFontSize(attrs);
            StyleConstants.setFontSize(attrs_new, currentSize + 2);



        } else if (buttonName.equals("Minus")) {
            int currentSize = StyleConstants.getFontSize(attrs);
            if (currentSize > 1) {
                StyleConstants.setFontSize(attrs_new, currentSize - 2);
            }



        }
        //Only if a button is clicked and the selected text is longer than 0
        if (buttonName != null && (!buttonName.equals(""))) {

            // Retrieve the pane's document object
            StyledDocument doc = jtp.getStyledDocument();
            //need to set the last parameter to false to keep the old style
            //not replace
            doc.setCharacterAttributes(startPos, length, attrs_new, false);
        }

        jtp.setCaretPosition(currentCaretPos);

    }

    public void setColor(String color) {
        Color newColor = StickyNoteCell.parseColorString(color);

        this.setBackground(newColor);

        //jScrollPane1.setBackground(newColor);

        // notePane.setBackground(newColor);
        jPanel2.setBackground(newColor);
        jTextPane1.setBackground(newColor);
        jScrollPane2.setBackground(newColor);

        parentPanel.setBackground(newColor);
        //lastSyncedState.setNoteColor(color);
    }
}
