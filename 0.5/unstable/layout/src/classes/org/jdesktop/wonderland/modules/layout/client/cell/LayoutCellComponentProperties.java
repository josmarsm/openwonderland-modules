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
package org.jdesktop.wonderland.modules.layout.client.cell;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.layout.api.common.LayoutConfig;
import org.jdesktop.wonderland.modules.layout.api.common.spi.LayoutFactorySPI;
import org.jdesktop.wonderland.modules.layout.client.LayoutRegistry;
import org.jdesktop.wonderland.modules.layout.common.LayoutCellComponentServerState;

/**
 * A property sheet for the layout component.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@PropertiesFactory(LayoutCellComponentServerState.class)
public class LayoutCellComponentProperties extends JPanel
        implements PropertiesFactorySPI {

    // The error logger
    private static final Logger LOGGER =
            Logger.getLogger(LayoutCellComponentProperties.class.getName());
    
    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/layout/client/cell/resources/" +
            "Bundle");

    // The cell properties editor
    private CellPropertiesEditor editor = null;

    // A map of display names visible in the combo box to their layout
    // factories
    private Map<String, LayoutFactorySPI> layoutFactoryMap = null;

    // A map of layout config classes and their display names
    private Map<Class<? extends LayoutConfig>, String> layoutConfigMap = null;

    // The originally selected layout, to be reset upon Restore, represented
    // by its display name
    private String originalLayout = null;

    /** Creates new form SampleComponentProperties */
    public LayoutCellComponentProperties() {
        // Initialize the GUI
        initComponents();

        // Populate the combo box with the names of the layouts registered
        layoutFactoryMap = new HashMap<String, LayoutFactorySPI>();
        layoutConfigMap = new HashMap<Class<? extends LayoutConfig>, String>();
        LayoutRegistry registry = LayoutRegistry.getInstance();
        Set<LayoutFactorySPI> factorySet = registry.getLayoutFactories();
        for (LayoutFactorySPI factory : factorySet) {

            // Populate the map of display names to layout factories and add
            // to the combo box
            String displayName = factory.getDisplayName();
            layoutFactoryMap.put(displayName, factory);
            layoutComboBox.addItem(displayName);

            // Populate the reverse map from LayoutConfig class to display
            // names. This will help translate the LayoutConfig we receive into
            // the selected item in the combo box.
            LayoutConfig layoutConfig = factory.getDefaultLayoutConfig(null);
            layoutConfigMap.put(layoutConfig.getClass(), displayName);
        }

        // Initialize the combo box to the first selected
        layoutComboBox.setSelectedIndex(0);
        originalLayout = (String) layoutComboBox.getItemAt(0);

        // Listen to when the combo box has a new selection and update the
        // dirty/clean state of the properties panel
        layoutComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String)layoutComboBox.getSelectedItem();
                boolean isD = originalLayout.equals(selectedItem) == false;
                editor.setPanelDirty(LayoutCellComponentProperties.class, isD);
            }
        });
    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return BUNDLE.getString("Layout_Cell_Component");
    }

    /**
     * @inheritDoc()
     */
    public JPanel getPropertiesJPanel() {
        return this;
    }

    /**
     * @inheritDoc()
     */
    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    /**
     * @inheritDoc()
     */
    public void open() {
        CellServerState state = editor.getCellServerState();
        CellComponentServerState compState = state.getComponentServerState(
                LayoutCellComponentServerState.class);

        // Set the original layout information and update the combo box
        // selection. Using the LayoutConfig object, we look up the layout
        // display name to select in the combo box.
        if (state != null) {
            LayoutConfig layoutConfig =
                    ((LayoutCellComponentServerState)compState).getLayoutConfig();

            LOGGER.warning("Layout Config: " + layoutConfig);
            originalLayout = layoutConfigMap.get(layoutConfig.getClass());
            layoutComboBox.setSelectedItem(originalLayout);
        }
    }

    /**
     * @inheritDoc()
     */
    public void close() {
        // Do nothing for now.
    }

    /**
     * @inheritDoc()
     */
    public void apply() {
        // Fetch the selected layout from the combo box, and using the Map,
        // fetch the layout factory to create a layout config to send to the
        // server
        CellServerState state = editor.getCellServerState();
        CellComponentServerState compState = state.getComponentServerState(
                LayoutCellComponentServerState.class);

        String selectedLayout = (String)layoutComboBox.getSelectedItem();
        LayoutFactorySPI factory = layoutFactoryMap.get(selectedLayout);
        LayoutConfig layoutConfig = factory.getDefaultLayoutConfig(null);
        ((LayoutCellComponentServerState) compState).setLayoutConfig(layoutConfig);
        editor.addToUpdateList(compState);
    }

    /**
     * @inheritDoc()
     */
    public void restore() {
        // Restore from the original state stored.
        layoutComboBox.setSelectedItem(originalLayout);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        layoutComboBox = new javax.swing.JComboBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/layout/client/cell/resources/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("Layout")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layoutComboBox, 0, 304, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(layoutComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(253, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JComboBox layoutComboBox;
    // End of variables declaration//GEN-END:variables
}
