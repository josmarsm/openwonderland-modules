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
            "Name", "Display", "Position", "Size", "Enabled", "Visible",
            "Minimized", "Decorated"
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
        public Object getValueAt(int rowIndex, int columnIndex) {
            HUDComponent hudComponent = componentList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return hudComponent.getName();
                case 1:
                    return hudComponent.getDisplayMode();
                case 2:
                    Point location = hudComponent.getLocation();
                    return "(" + location.x + ", " + location.y + ")";
                case 3:
                    Dimension size = hudComponent.getSize();
                    return "(" + size.width + ", " + size.height + ")";
                case 4:
                    return hudComponent.isEnabled();
                case 5:
                    return hudComponent.isVisible();
                case 6:
                    return hudComponent.isMinimized();
                case 7:
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
        getContentPane().setLayout(new java.awt.GridLayout());

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        mainPanel.setLayout(new java.awt.GridLayout());

        hudTable.setModel(new javax.swing.table.DefaultTableModel(
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
