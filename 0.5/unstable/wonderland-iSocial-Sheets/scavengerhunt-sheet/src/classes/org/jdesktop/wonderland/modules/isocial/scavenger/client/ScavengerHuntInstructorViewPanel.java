/**
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 */

/*
 * ScavengerHuntInstructorViewPanel.java
 *
 * Created on Apr 1, 2012, 1:13:15 PM
 */
package org.jdesktop.wonderland.modules.isocial.scavenger.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.scavenger.client.components.ScavengerHuntComponent;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntConstants;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.ScavengerHuntItem;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.SharedDataItem;
import org.jdesktop.wonderland.modules.isocial.scavenger.common.SharedDataList;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapEventCli;
import org.jdesktop.wonderland.modules.sharedstate.client.SharedMapListenerCli;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/** Container for instructor view HUD component.
 *
 * @author Vladimir Djurovic
 */
public class ScavengerHuntInstructorViewPanel extends javax.swing.JPanel implements SharedMapListenerCli, TableModelListener {
    
    private static final Logger LOGGER = Logger.getLogger(ScavengerHuntInstructorViewPanel.class.getName());
    
    private InstructorViewItemTable table;
    private SharedMapCli sharedMap;
    private ISocialManager manager;
    private String sheetId;
    private Sheet sheet;
    private JScrollPane scroll;
    private Timer timer;

    /** Creates new form ScavengerHuntInstructorViewPanel */
    public ScavengerHuntInstructorViewPanel(ISocialManager manager, Sheet sheet) {
        initComponents();
        this.manager = manager;
        this.sheet = sheet;
        // get items, if any
        sheetId = sheet.getId();
         sharedMap = findSheetdMap(sheetId);
         List<ScavengerHuntItem> items = new ArrayList<ScavengerHuntItem>();
        if (sharedMap != null) {
            // check if reordering collection exists
            Collection<String> keys = null;
            if(sharedMap.containsKey(ScavengerHuntConstants.ORDER_LIST_NAME)){
                keys = ((SharedDataList)sharedMap.get(ScavengerHuntConstants.ORDER_LIST_NAME)).getList();
            } else {
                keys = sharedMap.keySet();
            }
            
            for (String key : keys) {
                // skip order list key
                if(key != null && !key.equals(ScavengerHuntConstants.ORDER_LIST_NAME)){
                    SharedData sd = sharedMap.get(key);
                    if(sd instanceof SharedDataItem){
                        items.add(((SharedDataItem) sd).getItem());
                    }
                    
                }
            }
             sharedMap.addSharedMapListener(this);
        } else {
            // start timer for polling shared map
            timer = new Timer(ScavengerHuntConstants.MAP_POLL_INTERVAL, new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    sharedMap = findSheetdMap(sheetId);
                    if(sharedMap != null){
                        sharedMap.addSharedMapListener(ScavengerHuntInstructorViewPanel.this);
                        timer.stop();
                    }
                }
            });
            timer.setRepeats(true);
            timer.start();
        }
        table = new InstructorViewItemTable(items);
        table.getModel().addTableModelListener(this);
        try {
            // get username for instructor
            final String username = manager.getUsername();
            getOwnSharedMap(username);
            ((DefaultComboBoxModel) viewCombo.getModel()).insertElementAt(username, 1);
            ((DefaultComboBoxModel) viewCombo.getModel()).insertElementAt("--------", 2);
            // find all students who have submitted results
            Collection<Result> results = manager.getResults(sheet.getId());
            for(Result r : results){
                if(!r.getCreator().equals(username)){
                    addStudent(r.getCreator());
                }
                
            }
            table.updateTableFromResults(manager.getResults(sheetId));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);
       
    }
    
     private SharedMapCli findSheetdMap(String id) {
        return ScavengerHuntComponent.getSharedMap(id, null);
    }
     
     /**
      * Try to get shared map for instructor\s own student view.
      * 
      * @param username  instructors username
      */
     private void getOwnSharedMap(final String username) {
        // try to get shared map for current user
        Thread th = new Thread(new Runnable() {

            public void run() {
                LOGGER.log(Level.WARNING, "Trying to get shared map for {0}", username);
                ScavengerHuntComponent.getSharedMap(sheetId, username);
                LOGGER.log(Level.WARNING, "Got shared map for {0}", username);
            }
        });
        th.start();
    }
     
     /**
      * Adds student to combo box list.
      * 
      * @param username  username of student
      */
     public final void addStudent(final String username){
         boolean exists = false;
         //check if this student is already present in model
         DefaultComboBoxModel model = (DefaultComboBoxModel)viewCombo.getModel();
         for(int i = 0;i < model.getSize();i++){
             if(model.getElementAt(i).equals(username)){
                 exists = true;
                 break;
             }
         }
         if(!exists){
             model.addElement(username);
         }
         // try to get shared map for student. This should client blocking when displaying student view
         Thread th = new Thread(new Runnable() {

            public void run() {
                LOGGER.log(Level.WARNING, "Trying to get shared map for {0}", username);
                ScavengerHuntComponent.getSharedMap(sheetId, username);
                LOGGER.log(Level.WARNING, "Got shared map for {0}", username);
            }
        });
        th.start();
     }
     
     /**
      * Updates item table with data from results.
      */
     public void updateItemTable(){
          try{
                Collection<Result>  results = manager.getResults(sheetId);
                if(table != null){
                    table.updateTableFromResults(results);
                }
                
            } catch(IOException ex){
                throw new RuntimeException(ex);
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

        jPanel1 = new javax.swing.JPanel();
        viewLabel = new javax.swing.JLabel();
        viewCombo = new javax.swing.JComboBox();

        setMaximumSize(new java.awt.Dimension(250, 2147483647));
        setMinimumSize(new java.awt.Dimension(250, 500));
        setPreferredSize(new java.awt.Dimension(250, 500));
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/isocial/scavenger/client/resources/strings"); // NOI18N
        viewLabel.setText(bundle.getString("ScavengerHuntInstructorViewPanel.viewLabel.text")); // NOI18N
        jPanel1.add(viewLabel);

        viewCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Instructor View" }));
        viewCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                viewComboItemStateChanged(evt);
            }
        });
        jPanel1.add(viewCombo);

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void viewComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_viewComboItemStateChanged
        final String selection = viewCombo.getSelectedItem().toString();
        // do not allow selecting "-------"
        if(selection.startsWith("---")){
            viewCombo.setSelectedIndex(0);
            return;
        }
        // change view on EDT thread
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                // if it's a student, create student view
                if (selection.equals("Instructor View")) {
                    scroll.setViewportView(table);
                    updateItemTable();
                } else {
                    ScavengerHuntStudentViewPanel panel = new ScavengerHuntStudentViewPanel(manager, sheet, selection);
                    JPanel aux = new JPanel(new BorderLayout());
                    aux.add(panel, BorderLayout.NORTH);
                    scroll.setViewportView(aux);
                    validate();
                }
            }
        });
        
    }//GEN-LAST:event_viewComboItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox viewCombo;
    private javax.swing.JLabel viewLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Listener for shared state data. This implementation will add new items to instructor view table, and 
     * reorder table rows if item order is changed.
     * 
     * @param smec  event
     */
    public void propertyChanged(SharedMapEventCli smec) {
        SharedData old = smec.getOldValue();
        SharedData newData = smec.getNewValue();
        if (newData instanceof SharedDataItem) {
            if (old == null && newData != null) {
                // if new item is added
                ((DefaultTableModel) table.getModel()).addRow(new Object[]{0, 0, null, ((SharedDataItem) newData).getItem().getName(), ((SharedDataItem) newData).getItem().getCellId()});
            } else if(old != null && newData != null){
                table.updateItem(((SharedDataItem)newData).getItem());
            }
        } else if(newData instanceof SharedDataList){
            // reorder items in table if list si changed
            // first, remove listener to prevent loop
            table.getModel().removeTableModelListener(this);
            List<String> list = ((SharedDataList)newData).getList();
            for(int i = 0;i < list.size();i++){
                String id = list.get(i);
                for(int row = 0;row < table.getModel().getRowCount();row++){
                    // check if IDs match
                    String val = (String)table.getModel().getValueAt(row, 4);
                    if(id.equals(val) && i != row){
                        ((InstructorViewTableModel)table.getModel()).moveRow(row, row, i);
                        break;
                    }
                }
            }
            // re-add modle listener
            table.getModel().addTableModelListener(this);
        } else if (old != null && newData == null) {
            // if item was removed
            table.removeItem(((SharedDataItem) old).getItem());
        }
    }

    /**
     * Handles row moving in table, in situations when guide moves items up/down. The list
     * contains cell IDs in order in which they should appear
     * 
     * @param e event
     */
    public void tableChanged(TableModelEvent e) {
        boolean doProcess = (e.getType() == TableModelEvent.DELETE || e.getType() == TableModelEvent.INSERT || e.getType() == TableModelEvent.UPDATE);
        if(doProcess && e.getColumn() == TableModelEvent.ALL_COLUMNS){
            List<String> itemIds = new ArrayList<String>();
            for(int i = 0;i < table.getModel().getRowCount();i++){
                itemIds.add((String)table.getModel().getValueAt(i, InstructorViewTableModel.ID_COLUMN_INDEX));
            }
            sharedMap.put(ScavengerHuntConstants.ORDER_LIST_NAME, new SharedDataList((itemIds)));
        }
    }
}
