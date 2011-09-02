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
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.cell.SectionStyledStickyNoteCell;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.SectionStyledStickyNoteCellClientState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.messages.SectionStyledStickyNoteSyncMessage;
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

/**
 * The JPanel displayed in Wonderland for a styled sectioned generic sticky note.
 *
 */
/**
 *
 * @author xiuzhen
 */
public class SectionStyledStickyNotePanel extends javax.swing.JPanel implements ActionListener, FocusListener {

    private JFrame frame;
    private Timer keyTimer;
    private static Logger logger = Logger.getLogger(SectionStyledStickyNotePanel.class.getName());
    private SectionStyledStickyNoteCellClientState lastSyncedState = new SectionStyledStickyNoteCellClientState();
    private SectionStyledStickyNoteCell cell;
    private SectionStyledStickyNoteParentPanel parentPanel = null;

    /** Creates new form SectionStyledStickyNotePanel */
    public SectionStyledStickyNotePanel() {
        initComponents();
        jTextPane1.setBackground(Color.yellow);

        keyTimer = new Timer(1000, this);
        //notePane.addFocusListener(this);
        jTextPane1.addFocusListener(this);
        jTextPane2.addFocusListener(this);

        JButton italicButton = new JButton();
        italicButton.setName("Italic");
        italicButton.setFont(new Font("Arial", Font.ITALIC, 18));

        italicButton.setText("I");
        italicButton.addActionListener(listener);
        italicButton.setSize(20, 20);
        jPanel1.add(italicButton);

        JButton boldButton = new JButton();
        boldButton.setFont(new Font("Arial", Font.BOLD, 18));
        boldButton.setText("B");
        boldButton.setName("Bold");
        boldButton.addActionListener(listener);
        boldButton.setSize(20, 20);
        jPanel1.add(boldButton);



        JButton underlineButton = new JButton();

        underlineButton.setName("Underline");
        underlineButton.setFont(new Font("Arial", Font.PLAIN, 18));
        underlineButton.setText("U");
        underlineButton.addActionListener(listener);
        underlineButton.setSize(20, 20);

        jPanel1.add(underlineButton);

        JButton plusButton = new JButton();
        plusButton.setText("+");

        plusButton.setName("Plus");
        plusButton.addActionListener(listener);
        plusButton.setSize(20, 20);
        jPanel1.add(plusButton);

        JButton minusButton = new JButton();
        minusButton.setText("-");
        minusButton.setSize(20, 20);

        minusButton.setName("Minus");
        minusButton.addActionListener(listener);
        jPanel1.add(minusButton);


    }

    private void update_all_attributes_to_server() {

        ArrayList temp_attributes;
        temp_attributes = new ArrayList();
        boolean isBold, isItalic, isUnderline;
        int fontSize;
        int currentCaretPos;
        currentCaretPos = jTextPane1.getCaretPosition();


        for (int i = 0; i < jTextPane1.getText().length(); i++) {

            jTextPane1.setCaretPosition(i);

            SimpleAttributeSet attrs = new SimpleAttributeSet(jTextPane1.getCharacterAttributes());//us
            isItalic = StyleConstants.isItalic(attrs);
            isBold = StyleConstants.isBold(attrs);
            isUnderline = StyleConstants.isUnderline(attrs);
            fontSize = StyleConstants.getFontSize(attrs);


            NoteAttributeSet noteAttr = new NoteAttributeSet(isItalic, isBold, isUnderline, fontSize);
            //temp_attributes.add(i, attrs);
            temp_attributes.add(i, noteAttr);

        }
        jTextPane1.setCaretPosition(currentCaretPos);

        //lastSyncedState.setPartNoteAttributes(temp_attributes, jTextPane1.getSelectionStart(),jTextPane1.getSelectedText().length() +jTextPane1.getSelectionStart());
        lastSyncedState.setAllNoteAttributes(temp_attributes);

    }
    private final ActionListener listener = new ActionListener() {

        public void actionPerformed(ActionEvent ae) {
            Object source = ae.getSource();
            if (source instanceof JButton) {
                JButton srcButton = (JButton) source;
                String name = srcButton.getName();
                boolean isUnderline;
                String buttonName;
                SelectedTextStyle temp = new SelectedTextStyle();
                Font font;// = new Font("Lucida Grande", Font.ITALIC, 20);
                if (srcButton.getName().equals("Bold")) {
                    font = new Font("Lucida Grande", Font.BOLD, 16);
                    buttonName = "Bold";

                } else if (srcButton.getName().equals("Italic")) {
                    font = new Font("Lucida Grande", Font.ITALIC, 16);
                    buttonName = "Italic";

                } else if (srcButton.getName().equals("Underline")) {
                    isUnderline = true;
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
                    //also need to update the attributes
                    update_all_attributes_to_server();
                    lastSyncedState.setSelectedTextStyle(temp);
                    lastSyncedState.setsectionNum(1);

                    cell.sendSyncMessage(lastSyncedState);
                    jTextPane1.setSelectionStart(0);
                    jTextPane1.setSelectionEnd(0);
                    buttonName = "";
                    repaint();


                }

            }
        }
    };

    public SectionStyledStickyNotePanel(SectionStyledStickyNoteCell cell, SectionStyledStickyNoteCellClientState state, SectionStyledStickyNoteParentPanel parentP) {
        this();
        this.cell = cell;
        parentPanel = parentP;
        boolean isBold, isItalic, isUnderline;
        int fontSize;
        jTextPane1.setText(state.getNoteText());

        StyledDocument doc = jTextPane1.getStyledDocument();
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

        jTextPane2.setText(state.getSecondNoteText());
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();

        setBackground(new java.awt.Color(255, 255, 153));
        setPreferredSize(new java.awt.Dimension(250, 400));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jPanel2.setBackground(new java.awt.Color(255, 255, 153));
        jPanel2.setPreferredSize(new java.awt.Dimension(250, 300));

        jPanel1.setBackground(new java.awt.Color(255, 255, 153));
        jPanel1.setPreferredSize(new java.awt.Dimension(242, 20));
        jPanel1.setLayout(new java.awt.GridLayout(1, 5, 0, 1));

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 153));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(242, 125));

        jTextPane1.setBackground(new java.awt.Color(255, 255, 153));
        jTextPane1.setPreferredSize(new java.awt.Dimension(242, 125));
        jScrollPane1.setViewportView(jTextPane1);

        jScrollPane2.setBackground(new java.awt.Color(255, 255, 153));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(242, 125));

        jTextPane2.setMinimumSize(new java.awt.Dimension(250, 125));
        jTextPane2.setPreferredSize(new java.awt.Dimension(250, 125));
        jScrollPane2.setViewportView(jTextPane2);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))
        );

        add(jPanel2);
    }// </editor-fold>//GEN-END:initComponents

    private void formFocusGained(java.awt.event.FocusEvent evt) {
// TODO add your handling code here:
        //System.out.println("Panel focus Gained");
    }

    public static void main(String args[]) {
        JWindow j = new JWindow();
        System.out.println(j.getLayout());
        j.add(new SectionStyledStickyNotePanel());
        j.pack();
        j.setVisible(true);
    }

    /**
     * This Method is called when user is typing in the text box
     */
    public void actionPerformed(ActionEvent e) {
        resetSelectedTextStyle();
        if (e.getSource() == keyTimer) {
            // Check to see if we need to send a message w/ new text to the clients
            checkSendChanges();
        } else if (lastSyncedState != null) {
            cell.sendSyncMessage(lastSyncedState);

        }
    }

    /**
     * This need to take care which text area user is working on
     * by setting up the setsectionNum
     * @param e
     */
    public void focusGained(FocusEvent e) {
        keyTimer.start();
        if (e.getSource() == jTextPane1) {
            lastSyncedState.setsectionNum(1);
        } else if (e.getSource() == jTextPane2) {
            lastSyncedState.setsectionNum(2);


        }


    }

    public void focusLost(FocusEvent e) {
        keyTimer.stop();
        checkSendChanges();
    }

    public void resetSelectedTextStyle() {
        SelectedTextStyle tem = new SelectedTextStyle();

        lastSyncedState.setSelectedTextStyle(tem);


    }

    private synchronized void checkSendChanges() {

        // if (lastSyncedState == null || !lastSyncedState.getNoteText().equals(notePane.getText())) {
        if (lastSyncedState.getSectionNum() == 1) {
            if (lastSyncedState == null || !lastSyncedState.getNoteText().equals(jTextPane1.getText())) {
                // We need to send our changes
                //lastSyncedState.setNoteText(notePane.getText());
                lastSyncedState.setNoteText(jTextPane1.getText());
                update_all_attributes_to_server();

                cell.sendSyncMessage(lastSyncedState);
            }
        } else //the second textsession
        {
            if (lastSyncedState == null || !lastSyncedState.getSecondNoteText().equals(jTextPane2.getText())) {
                // We need to send our changes
                //lastSyncedState.setNoteText(notePane.getText());
                lastSyncedState.setSecondNoteText(jTextPane2.getText());

                cell.sendSyncMessage(lastSyncedState);
            }
        }


    }

    public void getAllAttributes() {

        boolean isBold, isItalic, isUnderline;
        int fontSize;
        //add the attributes here;
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

    public void processMessage(final SectionStyledStickyNoteSyncMessage pcm) {
        lastSyncedState = pcm.getState();

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                //notePane.setText(pcm.getState().getNoteText());
                //if(j)
                int secNum = lastSyncedState.getSectionNum();

                if (secNum == 2) {

                    if (!jTextPane2.getText().equals(pcm.getState().getSecondNoteText()))//this was the bug for blank click updateing
                    {
                        jTextPane2.setText(pcm.getState().getSecondNoteText());
                    }
                    setColor(pcm.getState().getNoteColor());

                    repaint();



                } else //section=1 
                {
                    if (!jTextPane1.getText().equals(pcm.getState().getNoteText()))//this was the bug for blank click updateing
                    {
                        jTextPane1.setText(pcm.getState().getNoteText());
                        getAllAttributes();
                    }

                    setColor(pcm.getState().getNoteColor());

                    SelectedTextStyle lastStyle = lastSyncedState.getSelectedTextStyle();
                    String buttonName;
                    buttonName = lastStyle.getButtonName();
                    // if (lastStyle.getSelectedEndPos() - lastStyle.getSelectedStartPos() > 0) {
                    if (!lastStyle.getButtonName().equals("") && lastStyle.getSelectedEndPos() - lastStyle.getSelectedStartPos() > 0) {

                        setJTextPaneFont(jTextPane1, buttonName, Color.BLACK, lastStyle.getSelectedStartPos(), lastStyle.getSelectedEndPos() - lastStyle.getSelectedStartPos());

                    }

                    repaint();

                }
            }
        });
    }

    public void setJTextPaneFont(JTextPane jtp, String buttonName, Color c, int startPos, int length) {

        int currentCaretPos = jtp.getCaretPosition();
        jtp.setCaretPosition(startPos + 1);

        SimpleAttributeSet attrs = new SimpleAttributeSet(jtp.getCharacterAttributes());//used to remember old attributes;
        MutableAttributeSet attrs_new = new SimpleAttributeSet();//new attribute for this action


        if (buttonName.equals("Bold")) {

            if (!attrs.isDefined("Bold")) {
                attrs.addAttribute("Bold", true);
            }
            StyleConstants.setBold(attrs_new, !StyleConstants.isBold(attrs));


        } else if (buttonName.equals("Italic")) {

            if (!attrs.isDefined("Italic")) {
                attrs.addAttribute("Italic", true);
            }


            StyleConstants.setItalic(attrs_new, !StyleConstants.isItalic(attrs));
        } else if (buttonName.equals("Underline")) {
            if (!attrs.isDefined("Underlin")) {
                attrs.addAttribute("Underline", true);
            }

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

        if (buttonName != null && (!buttonName.equals(""))) {
            StyledDocument doc = jtp.getStyledDocument();


            doc.setCharacterAttributes(startPos, length, attrs_new, false);
        }

        jtp.setCaretPosition(currentCaretPos);

    }

    public void setColor(String color) {
        Color newColor = SectionStyledStickyNoteCell.parseColorString(color);

        this.setBackground(newColor);
        jScrollPane1.setBackground(newColor);
        jTextPane1.setBackground(newColor);
        jTextPane2.setBackground(newColor);

        parentPanel.setBackground(newColor);
        lastSyncedState.setNoteColor(color);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    // End of variables declaration//GEN-END:variables
}
