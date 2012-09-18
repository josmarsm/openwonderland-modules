/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JWindow;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.client.cell.StickyNoteCell;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteCellClientState;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.cell.StickyNoteTypes;
import org.jdesktop.wonderland.modules.rockwellcollins.stickynote.common.messages.StickyNoteSyncMessage;
import org.jdesktop.wonderland.client.hud.*;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

/**
 *  The Task sticky note panel viewed in Wonderland.
 * @author Ryan (mymegabyte)
 */
public class TaskStickyNotePanel
        extends javax.swing.JPanel
        implements ActionListener, StickyNotePanel {

    private JFrame frame;
    private static final Logger LOGGER = Logger.getLogger(
            TaskStickyNotePanel.class.getName());
    private StickyNoteCell cell;
    private StickyNoteCellClientState lastSyncState =
            new StickyNoteCellClientState();
    private Timer keyTimer;
    private HUD mainHUD;
    private HUDComponent hudComponent;
    private JPanel parentPanel;

    /** Creates new form FreeStickynotePanel */
    public TaskStickyNotePanel() {
        initComponents();
        keyTimer = new Timer(1000, this);
        mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
    }

    /** Creates new form FreeStickynotePanel
     * @param cell the sticky note cell
     * @param state the client state
     * @param parent the parent JPanel
     */
    public TaskStickyNotePanel(StickyNoteCell cell,
            StickyNoteCellClientState state, JPanel parent) {
        this();
        this.cell = cell;
        parentPanel = parent;
        lastSyncState = state;
        setFields();
        setExpandedState(false);
    }

    public static void main(String args[]) {
        JWindow j = new JWindow();
        //JFrame j = new JFrame();
//        j.addMouseMotionListener(new MouseMotionAdapter() {
//           public void mouseMoved(java.awt.event.MouseEvent evt) {
//                System.out.println(evt);
//            }
//        });
        System.out.println(j.getLayout());
        j.add(new TaskStickyNotePanel());
        j.pack();
        j.setVisible(true);
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == keyTimer) {
            // Check to see if we need to send a message w/ new text to the clients
            checkSendChanges();
        }
    }

    public void processMessage(final StickyNoteSyncMessage pcm) {
        lastSyncState = pcm.getState();
        setFields();
    }

    public void setFields() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (lastSyncState == null) {
                    return;
                }
                assigneeField.setText(lastSyncState.getNoteAssignee());
                dueField.setText(lastSyncState.getNoteDue());
                taskNameField.setText(lastSyncState.getNoteName());
                statusField.setText(lastSyncState.getNoteStatus());
                notesPane.setText(lastSyncState.getNoteText());
                setColor(lastSyncState.getNoteColor());
            }
        });
    }

    /**
     * sets the color of the task sticky note panel
     * @param color the color to set
     */
    public void setColor(String color) {
        Color newColor = StickyNoteCell.parseColorString(color);

        this.setBackground(newColor);
        taskNameField.setBackground(newColor);
        expandButton.setBackground(newColor);
        assigneeField.setBackground(newColor);
        dueField.setBackground(newColor);
        statusField.setBackground(newColor);
        notesScrollPane.setBackground(newColor);
        notesPane.setBackground(newColor);
        parentPanel.setBackground(newColor);

        lastSyncState.setNoteColor(color);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        taskLabel = new javax.swing.JLabel();
        taskNameField = new javax.swing.JTextField();
        expandButton = new javax.swing.JToggleButton();
        assigneeLabel = new javax.swing.JLabel();
        assigneeField = new javax.swing.JTextField();
        dueLabel = new javax.swing.JLabel();
        dueField = new javax.swing.JTextField();
        statusLabel = new javax.swing.JLabel();
        statusField = new javax.swing.JTextField();
        notesLabel = new javax.swing.JLabel();
        notesScrollPane = new javax.swing.JScrollPane();
        notesPane = new javax.swing.JTextArea();

        setBackground(new java.awt.Color(255, 255, 153));
        setRequestFocusEnabled(false);

        taskLabel.setFont(new java.awt.Font("Verdana", 0, 14));
        taskLabel.setLabelFor(taskNameField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/rockwellcollins/stickynote/client/resources/Bundle"); // NOI18N
        taskLabel.setText(bundle.getString("TaskStickyNotePanel.taskLabel.text")); // NOI18N

        taskNameField.setBackground(new java.awt.Color(255, 255, 153));
        taskNameField.setFont(new java.awt.Font("Verdana", 0, 14));
        taskNameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFieldFocusLost(evt);
            }
        });

        expandButton.setBackground(new java.awt.Color(255, 255, 153));
        expandButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/rockwellcollins/stickynote/client/resources/downArrow23x10.png"))); // NOI18N
        expandButton.setPreferredSize(new java.awt.Dimension(20, 19));
        expandButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expandButtonActionPerformed(evt);
            }
        });

        assigneeLabel.setFont(new java.awt.Font("Verdana", 0, 14));
        assigneeLabel.setLabelFor(assigneeField);
        assigneeLabel.setText(bundle.getString("TaskStickyNotePanel.assigneeLabel.text")); // NOI18N

        assigneeField.setBackground(new java.awt.Color(255, 255, 153));
        assigneeField.setFont(new java.awt.Font("Verdana", 0, 14));
        assigneeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFieldFocusLost(evt);
            }
        });

        dueLabel.setBackground(new java.awt.Color(255, 255, 153));
        dueLabel.setFont(new java.awt.Font("Verdana", 0, 14));
        dueLabel.setLabelFor(dueField);
        dueLabel.setText(bundle.getString("TaskStickyNotePanel.dueLabel.text")); // NOI18N

        dueField.setBackground(new java.awt.Color(255, 255, 153));
        dueField.setFont(new java.awt.Font("Verdana", 0, 14));
        dueField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFieldFocusLost(evt);
            }
        });

        statusLabel.setBackground(new java.awt.Color(255, 255, 204));
        statusLabel.setFont(new java.awt.Font("Verdana", 0, 14));
        statusLabel.setLabelFor(statusField);
        statusLabel.setText(bundle.getString("TaskStickyNotePanel.statusLabel.text")); // NOI18N

        statusField.setBackground(new java.awt.Color(255, 255, 153));
        statusField.setFont(new java.awt.Font("Verdana", 0, 14));
        statusField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFieldFocusLost(evt);
            }
        });

        notesLabel.setBackground(new java.awt.Color(255, 255, 153));
        notesLabel.setFont(new java.awt.Font("Verdana", 0, 14));
        notesLabel.setLabelFor(notesPane);
        notesLabel.setText(bundle.getString("TaskStickyNotePanel.notesLabel.text")); // NOI18N

        notesScrollPane.setBackground(new java.awt.Color(255, 255, 153));

        notesPane.setBackground(new java.awt.Color(255, 255, 153));
        notesPane.setColumns(20);
        notesPane.setFont(new java.awt.Font("Verdana", 0, 14));
        notesPane.setLineWrap(true);
        notesPane.setRows(5);
        notesPane.setWrapStyleWord(true);
        notesPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFieldFocusLost(evt);
            }
        });
        notesScrollPane.setViewportView(notesPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(dueLabel)
                    .add(statusLabel)
                    .add(notesLabel)
                    .add(layout.createSequentialGroup()
                        .add(taskLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(taskNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(expandButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, notesScrollPane, 0, 0, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(assigneeLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(assigneeField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                                .add(dueField)
                                .add(statusField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {assigneeLabel, dueLabel, statusLabel, taskLabel}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {assigneeField, dueField, statusField, taskNameField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(taskLabel)
                            .add(taskNameField))
                        .add(6, 6, 6))
                    .add(layout.createSequentialGroup()
                        .add(expandButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(assigneeLabel)
                    .add(assigneeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dueLabel)
                    .add(dueField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusLabel)
                    .add(statusField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(notesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(notesScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 119, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFieldFocusLost
        LOGGER.fine("formFieldFocusLost");
        keyTimer.stop();
        checkSendChanges();
    }//GEN-LAST:event_formFieldFocusLost

    private void formFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFieldFocusGained
        LOGGER.fine("formFieldFocusGained");
        keyTimer.start();
    }//GEN-LAST:event_formFieldFocusGained

    private void expandButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandButtonActionPerformed
        setExpandedState(expandButton.isSelected());
    }//GEN-LAST:event_expandButtonActionPerformed

    private void setDetailsVisible(boolean visible) {
        assigneeLabel.setVisible(visible);
        assigneeField.setVisible(visible);
        dueLabel.setVisible(visible);
        dueField.setVisible(visible);
        statusLabel.setVisible(visible);
        statusField.setVisible(visible);
        notesLabel.setVisible(visible);
        notesScrollPane.setVisible(visible);
        revalidate();
    }

    private void setExpandedState(boolean expanded) {
        setDetailsVisible(expanded);
        if (expanded) {
            expandButton.setIcon(new ImageIcon(getClass().getResource(
                    "/org/jdesktop/wonderland/modules/rockwellcollins/"
                    + "stickynote/client/resources/upArrow23x10.png")));
            if (hudComponent == null) {
                parentPanel.remove(this);
                hudComponent = mainHUD.createComponent(this);
                mainHUD.addComponent(hudComponent);
                hudComponent.setVisible(true);
                hudComponent.addEventListener(new HUDEventListener() {

                    public void HUDObjectChanged(HUDEvent event) {
                        if (event.getEventType().equals(
                                HUDEvent.HUDEventType.CLOSED)) {
                            setExpandedState(false);
                        }
                    }
                });
            }

        } else {
            expandButton.setIcon(new ImageIcon(getClass().getResource(
                    "/org/jdesktop/wonderland/modules/rockwellcollins/"
                    + "stickynote/client/resources/downArrow23x10.png")));
            if (hudComponent != null) {
                //hudComponent.setVisible(false);
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        mainHUD.removeComponent(hudComponent);
                        hudComponent = null;
                    }
                });
                
                parentPanel.add(this);
                parentPanel.revalidate();
                cell.getApp().getControlArb().releaseControl();
            }
        }
    }

    private synchronized void checkSendChanges() {
        LOGGER.fine("Checking to see if we need to send changes.");
        StickyNoteCellClientState testState = new StickyNoteCellClientState();
        testState.setNoteAssignee(assigneeField.getText());
        testState.setNoteDue(dueField.getText());
        testState.setNoteName(taskNameField.getText());
        testState.setNoteStatus(statusField.getText());
        testState.setNoteText(notesPane.getText());
        testState.setNoteType(StickyNoteTypes.TASK);
        testState.setNoteColor(lastSyncState.getNoteColor());

        if (testState.hasChanges(lastSyncState)) {
            cell.sendSyncMessage(testState);
            lastSyncState.copyLocal(testState);
            LOGGER.fine("Sent sync message to server.");
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField assigneeField;
    private javax.swing.JLabel assigneeLabel;
    private javax.swing.JTextField dueField;
    private javax.swing.JLabel dueLabel;
    private javax.swing.JToggleButton expandButton;
    private javax.swing.JLabel notesLabel;
    private javax.swing.JTextArea notesPane;
    private javax.swing.JScrollPane notesScrollPane;
    private javax.swing.JTextField statusField;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel taskLabel;
    private javax.swing.JTextField taskNameField;
    // End of variables declaration//GEN-END:variables
}
