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

package org.jdesktop.wonderland.modules.marbleous.client.ui;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.jdesktop.wonderland.modules.marbleous.client.cell.TrackCell;
import org.jdesktop.wonderland.modules.marbleous.common.BumpTrackSegmentType;
import org.jdesktop.wonderland.modules.marbleous.common.TrackSegment;

/**
 * Frame for editing the properties of knots in a TrackSegment
 * @author Bernard Horan
 */
public class KnotFrame extends javax.swing.JFrame {
    private static Logger logger = Logger.getLogger(KnotFrame.class.getName());

    //The preferred width of the columns
    private static final int[] COLUMN_WIDTHS = {10, 50, 10, 10, 10, 10};
    //Use a formatter to display doubles to 2 decimal places
    private static DecimalFormat twoDForm = new DecimalFormat("#.##");
    //Table model to adapt a tracksegment's knots
    private KnotTableModel tableModel;
    //Flag to record when a segment's knots have been changed
    private boolean segmentModified;
    private TrackCell cell;
    


    /** Creates new form KnotFrame */
    public KnotFrame(KnotTableModel knotTableModel, TrackSegment segment, TrackCell cell) {
        tableModel = knotTableModel;
        this.cell = cell;
        initComponents();
        initTable();
        tableModel.addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent tme) {
                if (tme.getType() == TableModelEvent.UPDATE) {
                    logger.info("Updated table");
                    
                }
                //We don't care which row or column has been updated
                //Or whether a row has been added or deleted
                //Just that we need to inform the server and clients
                //That the tracksegment has been modified
                //However, we probably don't want to do that until the user
                //Tells us that they've finished editing
                //So, make a note that the segment has changed
                segmentModified = true;
            }
        });
        knotTable.setRowHeight(25);
        
        segmentEditorPanel.add(SegmentEditor.createSegmentEditor(cell, segment), BorderLayout.CENTER);
    }





    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        knotTable = new javax.swing.JTable();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        segmentEditorPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Segment Editor");
        setAlwaysOnTop(true);
        setName("Editing Track Segment"); // NOI18N

        knotTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        knotTable.setGridColor(java.awt.Color.black);
        knotTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(knotTable);

        okButton.setText("Save");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Restore");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        segmentEditorPanel.setMinimumSize(new java.awt.Dimension(388, 122));
        segmentEditorPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(segmentEditorPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(68, 68, 68)
                        .add(okButton)
                        .add(59, 59, 59)
                        .add(cancelButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(segmentEditorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton)
                    .add(cancelButton))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        logger.info("OK Button");
        if (segmentModified) {
            logger.info("Segment modified");
            cell.modifySegment(tableModel.getSegment());
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cell.restoreSegment(tableModel.getSegment());
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) throws UnsupportedLookAndFeelException {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                TrackSegment segment = new BumpTrackSegmentType().createSegment();
                new KnotFrame(new KnotTableModel(segment), segment, null).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton cancelButton;
    protected javax.swing.JScrollPane jScrollPane1;
    protected javax.swing.JTable knotTable;
    protected javax.swing.JButton okButton;
    protected javax.swing.JPanel segmentEditorPanel;
    // End of variables declaration//GEN-END:variables

    private void initTable() {
        knotTable.setModel(tableModel);

        //Use a specialised renderer and editor for the position column
        TableColumn positionColumn = knotTable.getColumnModel().getColumn(1);
        positionColumn.setCellRenderer(new PositionCellRenderer());
        positionColumn.setCellEditor(new PositionCellEditor());

        //Use a specialised renderer and editor for the rotation column
        TableColumn quaternionColumn = knotTable.getColumnModel().getColumn(2);
        quaternionColumn.setCellRenderer(new QuaternionCellRenderer());
        quaternionColumn.setCellEditor(new QuaternionTableCellEditor());

        // For the remaining columns use an editor that constrains user input to
        // +/- 1
        for (int i = 3; i < 5; i++) {
            TableColumn column = knotTable.getColumnModel().getColumn(i);
            column.setCellEditor(new PlusMinusOneCellEditor());
        }

        //Set the preferred width of the columns
        for (int i = 0; i < 5; i++) {
            TableColumn column = knotTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(COLUMN_WIDTHS[i]);            
        }
    }    

    //Renderer for rotation column
    public class QuaternionCellRenderer extends JLabel
                           implements TableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Quaternion quat = (Quaternion) value;
            float angleRadians = quat.toAngleAxis(new Vector3f(0,0,1));
            double angleDegrees = Math.toDegrees(angleRadians);
            
            setText(twoDForm.format(angleDegrees));
            //Workaround for
            //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6723524
            setOpaque(true);
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                if (row % 2 == 1)
                    setBackground(Color.WHITE);
                else
                    setBackground(table.getBackground());
            }
            return this;
        }
    }

    //Renderer for position column
    public class PositionCellRenderer extends JLabel
                           implements TableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Vector3f position = (Vector3f) value;
            StringBuffer buffer = new StringBuffer();
            buffer.append(position.x);
            buffer.append(",");
            buffer.append(position.y);
            buffer.append(",");
            buffer.append(position.z);
            setText(buffer.toString());
            //Workaround for
            //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6723524
            setOpaque(true);
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                if (row % 2 == 1)
                    setBackground(Color.WHITE);
                else
                    setBackground(table.getBackground());
            }
            return this;
        }
    }
    
    //Editor for rotation column
    public class QuaternionTableCellEditor extends AbstractCellEditor implements TableCellEditor {
        // This is the textfield that will handle the editing of the cell value
        JTextField textfield = new JTextField();

        // This method is called when a cell value is edited by the user.
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int rowIndex, int vColIndex) {
            //Workaround for
            //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6723524
            textfield.setOpaque(true);
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                if (rowIndex % 2 == 1)
                    setBackground(Color.WHITE);
                else
                    setBackground(table.getBackground());
            }

            // Configure the textfield with the specified value
            Quaternion quat = (Quaternion) value;
            float angleRadians = quat.toAngleAxis(new Vector3f(0, 0, 1));
            float angleDegrees = (float) Math.toDegrees(angleRadians);
            textfield.setText(twoDForm.format(angleDegrees));

            //Set the tooltip for the textfield
            textfield.setToolTipText("0 <= x <= 360");

            // Return the configured textfield
            return textfield;
        }

        // This method is called when editing is completed.
        // It must return the new value to be stored in the cell.
        public Object getCellEditorValue() {
            float angleDegrees = Float.parseFloat(textfield.getText());
            float angleRadians = (float) Math.toRadians(angleDegrees);
            Quaternion quat = new Quaternion().fromAngleAxis(angleRadians, new Vector3f(0,0,1));
            return quat;
        }

        // This method is called just before the cell value
        // is saved. If the value is not valid, false should be returned.
        @Override
        public boolean stopCellEditing() {
            float angleDegrees = Float.parseFloat(textfield.getText());
            if (angleDegrees > 360) {
                return false;
            }
            if (angleDegrees < 0) {
                return false;
            }            
            return super.stopCellEditing();
        }
    }

    //Editor to constrain user input to be between -1 and +1
    public class PlusMinusOneCellEditor extends AbstractCellEditor implements TableCellEditor {
        // This is the textfield that will handle the editing of the cell value
        JTextField textfield = new JTextField();

        // This method is called when a cell value is edited by the user.
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int rowIndex, int vColIndex) {
            if (isSelected) {
                textfield.setBackground(table.getSelectionBackground());
                textfield.setForeground(table.getSelectionForeground());
            } else {
                textfield.setBackground(table.getBackground());
                textfield.setForeground(table.getForeground());
            }
            textfield.setEnabled(table.isEnabled());
            textfield.setFont(table.getFont());

            // Configure the textfield with the specified value
            textfield.setText(value.toString());

            //Set the tooltip of the textfield
            textfield.setToolTipText("-1 <= x <= 1");

            // Return the configured textfield
            return textfield;
        }

        // This method is called when editing is completed.
        // It must return the new value to be stored in the cell.
        public Object getCellEditorValue() {
            return Float.parseFloat(textfield.getText());
        }

        // This method is called just before the cell value
        // is saved. If the value is not valid, false should be returned.
        @Override
        public boolean stopCellEditing() {
            float angleDegrees = Float.parseFloat(textfield.getText());
            if (angleDegrees > 1.0) {
                Toolkit.getDefaultToolkit().beep();
                return false;
            }
            if (angleDegrees < -1.0) {
                Toolkit.getDefaultToolkit().beep();
                return false;
            }
            return super.stopCellEditing();
        }

    }

    //This editor constrains user input so that the user can enter a 3 dimensional position
    public class PositionCellEditor extends AbstractCellEditor implements TableCellEditor {
        // This is the textfield that will handle the editing of the cell value
        JTextField textfield = new JTextField();

        // This method is called when a cell value is edited by the user.
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int rowIndex, int vColIndex) {
            // 'value' is value contained in the cell located at (rowIndex, vColIndex)

            //Workaround for
            //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6723524
            textfield.setOpaque(true);
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                if (rowIndex % 2 == 1)
                    setBackground(Color.WHITE);
                else
                    setBackground(table.getBackground());
            }

            Vector3f position = (Vector3f) value;
            StringBuffer buffer = new StringBuffer();
            buffer.append(position.x);
            buffer.append(",");
            buffer.append(position.y);
            buffer.append(",");
            buffer.append(position.z);
            textfield.setText(buffer.toString());

            //Set the tooltip for the position text field
            textfield.setToolTipText("x, y, z");

            // Return the configured textfield
            return textfield;
        }

        // This method is called when editing is completed.
        // It must return the new value to be stored in the cell.
        public Object getCellEditorValue() {
            return parse(textfield.getText());
        }

        // This method is called just before the cell value
        // is saved. If the value is not valid, false should be returned.
        public boolean stopCellEditing() {
            if (parse(textfield.getText()) == null) {
                Toolkit.getDefaultToolkit().beep();
                return false;
            }
            return super.stopCellEditing();
        }

        private Vector3f parse(String aString) {
            String delims = "[,]";
            String[] tokens = aString.split(delims);
            if (tokens.length != 3) {
                logger.warning("Invalid number of dimensions");
                return null;
            }
            float x,y,z;
            try {
                x = Float.parseFloat(tokens[0]);
            } catch (NumberFormatException e) {
                logger.warning("Invalid x variable");
                return null;
            }
            try {
                y = Float.parseFloat(tokens[1]);
            } catch (NumberFormatException e) {
                logger.warning("Invalid y variable");
                return null;
            }
            try {
                z = Float.parseFloat(tokens[2]);
            } catch (NumberFormatException e) {
                logger.warning("Invalid x variable");
                return null;
            }
            Vector3f vector = new Vector3f(x, y, z);
            if (!Vector3f.isValidVector(vector)) {
                logger.warning("Invalid vector");
                return null;
            }
            return vector;
        }

    }

}
