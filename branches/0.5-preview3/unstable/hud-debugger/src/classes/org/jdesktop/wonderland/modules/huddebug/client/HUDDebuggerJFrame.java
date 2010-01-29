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
package org.jdesktop.wonderland.modules.huddebug.client;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

/**
 * A JFrame that displays a list of HUD Components and various aspects of them.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class HUDDebuggerJFrame extends javax.swing.JFrame {

    // The table model that represents the HUD component
    private HUDTableModel hudTableModel = null;

    /** Creates new form HUDDebuggerJFrame */
    public HUDDebuggerJFrame() {
        initComponents();

        // The set table model for the list of HUD components
        hudTableModel = new HUDTableModel();
        hudTable.setModel(hudTableModel);

        // Set the default renderer and editor for all Boolean values to be a
        // checkbox
//        hudTable.setDefaultRenderer(Boolean.class,
//                new DefaultTableCellHeaderRenderer(
//        hudTable.setDefaultEditor(Boolean.class,
//                new DefaultCellEditor(new JCheckBox()));

        // Listen for when any changes are made to the HUD and update the
        // table display accordingly.
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        mainHUD.addEventListener(new HUDEventListener() {

            public void HUDObjectChanged(HUDEvent event) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        hudTableModel.refresh();
                    }
                });
            }
        });
    }

    /**
     * {
     */
    @Override
    public void setVisible(boolean isVisible) {
        // Refresh the table data if we are being made visible
        if (isVisible == true) {
            hudTableModel.refresh();
        }
        super.setVisible(isVisible);
    }

    /**
     * The table model that defines the columns and holds the data
     */
    private class HUDTableModel extends AbstractTableModel {

        // An ordered list of HUD components that are present
        private List<HUDComponent> componentList = null;
        // An array of column names for the table
        private String[] COLUMNS = {
            "Name", "Type", "Mode", "Position", "Size", "Enabled", "Visible",
            "Minimized", "Decorated"
        };
        // An array of classes that represent the column classes
        private Class[] COLUMN_CLASSES = {
            String.class, String.class, String.class, String.class, String.class,
            Boolean.class, Boolean.class, Boolean.class, Boolean.class
        };

        /** Default constructor */
        public HUDTableModel() {
            componentList = new LinkedList<HUDComponent>();
        }

        /**
         * Refresh the values in the table based upon the latest components in
         * the HUD.
         *
         * NOTE: This method assumes it is being called in the AWT Event Thread.
         */
        public void refresh() {
            componentList.clear();
            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            Iterator<HUDComponent> hudIt = mainHUD.getComponents();
            while (hudIt.hasNext() == true) {
                HUDComponent hudComponent = hudIt.next();
                componentList.add(hudComponent);
            }
            fireTableDataChanged();
        }

        /**
         * {@inheritDoc}
         */
        public int getRowCount() {
            return componentList.size();
        }

        /**
         * {@inheritDoc}
         */
        public int getColumnCount() {
            return COLUMNS.length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return COLUMN_CLASSES[columnIndex];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // Make the boolean values editable
            if (columnIndex >= 4 && columnIndex <= 7) {
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            // For the boolean values that we can edit, fetch the checkbox
            // and set the appropriate value
            if (columnIndex < 3 || columnIndex > 7) {
                return;
            }
            boolean isSelected = (Boolean) aValue;
            HUDComponent hudComponent = componentList.get(rowIndex);

            switch (columnIndex) {
                case 5:
                    hudComponent.setEnabled(isSelected);
                    break;
                case 6:
                    hudComponent.setVisible(isSelected);
                    break;
                case 7:
                    if (isSelected == true) {
                        hudComponent.setMinimized();
                    } else {
                        hudComponent.setMaximized();
                    }
                    break;
                case 8:
                    hudComponent.setDecoratable(isSelected);
                    break;
                default:
                    break;
            }
        }

        /**
         * {@inheritDoc}
         */
        public Object getValueAt(int rowIndex, int columnIndex) {
            HUDComponent hudComponent = componentList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return hudComponent.getName();
                case 1:
                    return hudComponent.getClass().getSimpleName();
                case 2:
                    return hudComponent.getDisplayMode();
                case 3:
                    Point location = hudComponent.getLocation();
                    return "(" + location.x + ", " + location.y + ")";
                case 4:
                    Dimension size = hudComponent.getSize();
                    return "(" + size.width + ", " + size.height + ")";
                case 5:
                    return hudComponent.isEnabled();
                case 6:
                    return hudComponent.isVisible();
                case 7:
                    return hudComponent.isMinimized();
                case 8:
                    return hudComponent.getDecoratable();
                default:
                    return null;
            }
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

        mainPanel = new javax.swing.JPanel();
        hudScrollPane = new javax.swing.JScrollPane();
        hudTable = new javax.swing.JTable();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/huddebug/client/resources/Bundle"); // NOI18N
        setTitle(bundle.getString("HUD_Debugger_Title")); // NOI18N
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        mainPanel.setPreferredSize(new java.awt.Dimension(600, 200));
        mainPanel.setLayout(new java.awt.GridLayout(1, 0));

        hudScrollPane.setPreferredSize(new java.awt.Dimension(600, 200));

        hudTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        hudTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        hudScrollPane.setViewportView(hudTable);

        mainPanel.add(hudScrollPane);

        getContentPane().add(mainPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane hudScrollPane;
    private javax.swing.JTable hudTable;
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables
}
