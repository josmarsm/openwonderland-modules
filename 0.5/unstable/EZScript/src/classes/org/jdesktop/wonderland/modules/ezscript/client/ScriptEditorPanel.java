/*
 * ScriptEditorPanel.java
 *
 * Created on Jan 9, 2011, 11:21:04 AM
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ReturnableScriptMethodSPI;
import org.jdesktop.wonderland.modules.ezscript.client.SPI.ScriptMethodSPI;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedString;

/**
 *
 * @author JagWire
 */
public class ScriptEditorPanel extends javax.swing.JPanel {

    /** Creates new form ScriptEditorPanel */
    private EZScriptComponent scriptComponent;
    private JDialog dialog;
    private boolean isGlobal = false;
    private ScriptLibraryPanel library;
    private RegisteredCellsListPanel cellList;
    private CommandPanel commandPanel;
    private static final Logger logger = Logger.getLogger(ScriptEditorPanel.class.getName());
    
    private static final String MOUSE_EVENT_TEMPLATE = 
            "\n"
            + "Context.enableMouseEvents();\n"
            + "function clicky() {\n"
            + "    //TODO: respond to event here.\n"
            + "}\n"
            + "\n"
            + "Context.onClick(clicky, false);\n";
    
    private static final String PROXIMITY_EVENT_TEMPLATE = 
            "\n"
            + "Context.enableProximityEvents();\n"
            + ""
            + "function enter() {\n"
            + "    //TODO: respond to event here.\n"
            + "}\n\n"
            + "function exit() {\n"
            + "    //TODO: respond to event here.\n"
            + "}\n\n"
            + "Context.onApproach(enter, false);\n"
            + "Context.onLeave(exit, false);\n";
    private static final String KEY_EVENT_TEMPLATE = "";
    private static final String TRIGGER_EVENT_TEMPLATE = "";
    private TriggerQueryPanel triggerPanel = null;
    
    public ScriptEditorPanel(EZScriptComponent component, JDialog dialog) {
        initComponents();
        library = new ScriptLibraryPanel();
        isGlobal = false;
        jTabbedPane1.addTab("Library", library);
        this.scriptComponent = component;
        this.dialog = dialog;
        this.setMinimumSize(new Dimension(600, 400));
        this.setPreferredSize(new Dimension(600, 400));
        scriptArea.setTabSize(4);
        this.addAncestorListener(new AncestorListener() {
           
            public void ancestorAdded(AncestorEvent ae) {            
                addTemplates();
                logger.warning("Ancestor moved!");
            }

            public void ancestorRemoved(AncestorEvent ae) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void ancestorMoved(AncestorEvent ae) {
//                throw new UnsupportedOperationException("Not supported yet.");
               
            }
 });

    }

    public ScriptEditorPanel(JDialog dialog) {
        initComponents();
        isGlobal = true;
        library = new ScriptLibraryPanel();
        cellList = new RegisteredCellsListPanel();
        triggerPanel = new TriggerQueryPanel();
        jTabbedPane1.addTab("Library", library);
        jTabbedPane1.addTab("Registered Cells", cellList);
        jTabbedPane1.addTab("Trigger Utilities", triggerPanel);
//        commandPanel = new CommandPanel();
//        jTabbedPane1.addTab("Command Line", commandPanel);
        this.dialog = dialog;
        this.setMinimumSize(new Dimension(600, 400));
        this.setPreferredSize(new Dimension(600, 400));
        scriptArea.setTabSize(4);
        
        String script = new String();
        try {
            //grab startup script
            script = retrieveStartupScript();
        } catch (IOException ex) {
            Logger.getLogger(ScriptEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            scriptArea.setText(script);
        }
     
    }
    
    public void addTemplates() {
        
        if(this.scriptComponent == null 
                //if a script larger than 5 characters is there, don't add a
                //template, it could be legit.
                || scriptArea.getText().length() > 5)
            return;
        
        //clear any gibberish
        scriptArea.setText("");
        
        /**
         * We use append here instead of setText() in case multiple templates
         * should be added.
         */
        if(scriptComponent.areMouseEventsEnabled()) {
            logger.warning("ADDING MOUSE TEMPLATE!");
            scriptArea.append(MOUSE_EVENT_TEMPLATE);
        }
        
        if(scriptComponent.areProximityEventsEnabled()) {
            logger.warning("ADDING PROXIMITY TEMPLATE!");
            scriptArea.append(PROXIMITY_EVENT_TEMPLATE);
        }
        
        if(scriptComponent.areKeyEventsEnabled()) {
            //TODO
        }
        
        if(scriptComponent.areTriggersEnabled()) {
            //TODO
        }                
    }
    
    /**
     * This is a bit ugly, it could use some attention.
     * @return the full contents of the file.
     * @throws IOException if something goes wrong.
     */
    public String retrieveStartupScript() throws IOException {
        File dir = ClientContext.getUserDirectory("scripts");
        String script = new String();

        File startup = new File(dir, "startup.ez");
        if (!startup.exists()) {
            startup.createNewFile();
            return "";
        }

        //so the script definitely exists...
        FileInputStream in = new FileInputStream(startup);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String line;
        while ((line = br.readLine()) != null) {
            script += "\n" + line;

        }
        br.close();
        return script;
    }
    

    public void addLibraryEntry(ReturnableScriptMethodSPI method) {
        library.addEntry(method);
    }

    public void addLibraryEntry(ScriptMethodSPI method) {
        library.addEntry(method);
    }

    public void setScriptTextArea(String s) {
        scriptArea.setText(s);
    }
    private void threadedExecuteScript(final String script) {
        new Thread(new Runnable() {
            public void run() {
                if(!isGlobal) {
                    //cell execute
                    scriptComponent.getScriptMap().put("editor", SharedString.valueOf(script));
                    scriptComponent.getStateMap().put("script", SharedString.valueOf(script));
                    
                } else {
                    //client execute
                    new Thread(new Runnable() { 
                        public void run() {
                            ScriptManager.getInstance().evaluate(script);
                        }
                    }).start();
                    
                }
                        
            }
        }).start();
        
        if(isGlobal) {
           File dir = ClientContext.getUserDirectory("scripts");
            try {
                File startup = new File(dir, "startup.ez");
                if (!startup.exists()) {
                    startup.createNewFile();
                }

                FileWriter out = new FileWriter(startup);

                out.write(script);
                out.close();
            } catch (IOException iOException) {
                iOException.printStackTrace();
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        scriptArea = new javax.swing.JTextArea();
        clearButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        executeButton = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(620, 410));

        scriptArea.setColumns(20);
        scriptArea.setRows(5);
        jScrollPane1.setViewportView(scriptArea);

        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Close");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        executeButton.setText("Execute Script");
        executeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(executeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 177, Short.MAX_VALUE)
                .addComponent(clearButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addGap(110, 110, 110))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cancelButton)
                        .addGap(9, 9, 9))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(clearButton)
                        .addComponent(executeButton)))
                .addGap(19, 19, 19))
        );

        jTabbedPane1.addTab("Editor", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 602, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(61, 61, 61))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void executeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executeButtonActionPerformed

//        if (!isGlobal) {
//            System.out.println("executed button press!");
//            new Thread(new Runnable() {
//
//                public void run() {
//                    scriptComponent.getScriptMap().put("editor", SharedString.valueOf(scriptArea.getText()));
//                    scriptComponent.getStateMap().put("script", SharedString.valueOf(scriptArea.getText())); // for persistenceh
//                    //  scriptComponent.clearCallbacks();
//
//                    //scriptComponent.evaluateScript(scriptArea.getText());
//                }
//            }).start();
//        } else {
//            //TODO execute on client, not over network.
//            
//            ScriptManager.getInstance().evaluate(scriptArea.getText());
//            File dir = ClientContext.getUserDirectory("scripts");
//            try {
//                File startup = new File(dir, "startup.ez");
//                if (!startup.exists()) {
//                    startup.createNewFile();
//                }
//
//                FileWriter out = new FileWriter(startup);
//
//                out.write(scriptArea.getText());
//                out.close();
//            } catch (IOException iOException) {
//                iOException.printStackTrace();
//            }
//        }
        threadedExecuteScript(scriptArea.getText());
    }//GEN-LAST:event_executeButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // TODO add your handling code here:

        dialog.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        // TODO add your handling code here:
        scriptArea.setText("");
    }//GEN-LAST:event_clearButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton executeButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea scriptArea;
    // End of variables declaration//GEN-END:variables

}
