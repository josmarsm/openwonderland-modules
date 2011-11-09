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
package org.jdesktop.wonderland.modules.shader.client;

import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.shader.common.ShaderComponentServerState;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedBoolean;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedFloat;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

/**
 *
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 * @author JagWire
 * 
 */
@PropertiesFactory(ShaderComponentServerState.class)
public class ShaderProperties
        extends JPanel implements PropertiesFactorySPI {

    private CellPropertiesEditor editor = null;
    private String originalVShader = "";
    private String originalFShader = "";
    
    
    private static final String template =
            "void main() \n{"
            + "\n\n\n\n"
            + "}";
    
    
    /** Creates new form SampleComponentProperties */
    public ShaderProperties() {
        // Initialize the GUI
        initComponents();
        vShaderArea.getDocument().addDocumentListener(new PropertiesDocumentListener());
        fShaderArea.getDocument().addDocumentListener(new PropertiesDocumentListener());
        
        vShaderArea.setText(template);
        fShaderArea.setText(template);
                

    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return "Shaders";
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
        ShaderCapability sc = getCapability();
        if (sc != null && sc.getShaderMap() != null && !sc.getShaderMap().isEmpty()) {
            SharedString vShader = (SharedString) sc.getShaderMap().get("vertex");
            SharedString fShader = (SharedString) sc.getShaderMap().get("fragment");
//        DefaultStyledDocument s = new DefaultStyledDocument();

            //populate fields
            originalVShader = vShader.getValue();
            originalFShader = fShader.getValue();
            //populate UI components        
            getvShaderArea().setText(vShader.getValue());
            getfShaderArea().setText(fShader.getValue());
            populateUniformsTable(sc.getUniformsMap());
        } else {
            vShaderArea.setText(template);
            fShaderArea.setText(template);
            //TODO: handle uniforms table
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
//        editor.addToUpdateList(compState);
        
            getCapability().getShaderMap().put("vertex", SharedString.valueOf(vShaderArea.getText()));
            getCapability().getShaderMap().put("fragment", SharedString.valueOf(fShaderArea.getText()));
            
            //TODO: handle uniforms
            
            originalVShader = vShaderArea.getText();
            originalFShader = fShaderArea.getText();
    }

    /**
     * @inheritDoc()
     */
    public void restore() {
        // Restore from the original state stored.
        vShaderArea.setText(originalVShader);
        fShaderArea.setText(originalFShader);
        
        //handle uniforms
        
    }



    public JTable getUniformsTable() {
        return uniformsTable;
    }


    
    public ShaderCapability getCapability() {
        if(editor.getCell().getComponent(ShaderCapability.class) != null) {
            return editor.getCell().getComponent(ShaderCapability.class);
        }
        return null;
    }

    public JTextArea getfShaderArea() {
        return fShaderArea;
    }

    public JTextArea getvShaderArea() {
        return vShaderArea;
    }

    private void populateUniformsTable(SharedMapCli Us) {
        for(Map.Entry<String, SharedData> e: Us.entrySet()) {
           if(e.getValue() instanceof SharedBoolean) {
               
           } else if(e.getValue() instanceof SharedFloat) {
               //etc..
           }
        }
    }
    
    
    private boolean isDirty() {
        return !originalVShader.equals(vShaderArea.getText())
                || !originalFShader.equals(fShaderArea.getText());
    }
    
    
    class PropertiesDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent de) {
            
            if(editor != null) {
                editor.setPanelDirty(ShaderProperties.class, isDirty());
            }
               
        }

        public void removeUpdate(DocumentEvent de) {
            if(editor != null) {
                editor.setPanelDirty(ShaderProperties.class, isDirty());
            }
        }

        public void changedUpdate(DocumentEvent de) {
            if(editor != null) {
                editor.setPanelDirty(ShaderProperties.class, isDirty());
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

        tabbedPane = new javax.swing.JTabbedPane();
        VertextPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        vShaderArea = new javax.swing.JTextArea();
        fragmentPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        fShaderArea = new javax.swing.JTextArea();
        uniformsPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        uniformsTable = new javax.swing.JTable();

        vShaderArea.setColumns(20);
        vShaderArea.setRows(5);
        jScrollPane1.setViewportView(vShaderArea);

        org.jdesktop.layout.GroupLayout VertextPanelLayout = new org.jdesktop.layout.GroupLayout(VertextPanel);
        VertextPanel.setLayout(VertextPanelLayout);
        VertextPanelLayout.setHorizontalGroup(
            VertextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
        );
        VertextPanelLayout.setVerticalGroup(
            VertextPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
        );

        tabbedPane.addTab("Vertex", VertextPanel);

        fShaderArea.setColumns(20);
        fShaderArea.setRows(5);
        jScrollPane2.setViewportView(fShaderArea);

        org.jdesktop.layout.GroupLayout fragmentPanelLayout = new org.jdesktop.layout.GroupLayout(fragmentPanel);
        fragmentPanel.setLayout(fragmentPanelLayout);
        fragmentPanelLayout.setHorizontalGroup(
            fragmentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
        );
        fragmentPanelLayout.setVerticalGroup(
            fragmentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
        );

        tabbedPane.addTab("Fragment", fragmentPanel);

        uniformsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Name", "Type", "Value"
            }
        ));
        jScrollPane3.setViewportView(uniformsTable);

        org.jdesktop.layout.GroupLayout uniformsPanelLayout = new org.jdesktop.layout.GroupLayout(uniformsPanel);
        uniformsPanel.setLayout(uniformsPanelLayout);
        uniformsPanelLayout.setHorizontalGroup(
            uniformsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
        );
        uniformsPanelLayout.setVerticalGroup(
            uniformsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
        );

        tabbedPane.addTab("Uniforms", uniformsPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(tabbedPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(tabbedPane)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel VertextPanel;
    private javax.swing.JTextArea fShaderArea;
    private javax.swing.JPanel fragmentPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel uniformsPanel;
    private javax.swing.JTable uniformsTable;
    private javax.swing.JTextArea vShaderArea;
    // End of variables declaration//GEN-END:variables
}
