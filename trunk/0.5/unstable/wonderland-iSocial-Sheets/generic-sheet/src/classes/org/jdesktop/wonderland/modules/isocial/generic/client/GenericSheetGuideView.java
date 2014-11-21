/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.generic.client;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.client.view.DockableSheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.ResultListener;
import org.jdesktop.wonderland.modules.isocial.client.view.SheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.annotation.View;
import org.jdesktop.wonderland.modules.isocial.common.model.Instance;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.generic.common.GenericAnswer;
import org.jdesktop.wonderland.modules.isocial.generic.common.GenericResult;
import org.jdesktop.wonderland.modules.isocial.generic.common.GenericSheet;

/**
 * This view will consist of a JTable whose rows and columns remain stationary.
 * 
 */
/**
 * Generic Sheet guide view.
 * @author Jonathan Kaplan <Jonathankap@wonderbuilders.com>
 * @author Kaustubh
 */
@View(value = GenericSheet.class, roles = {Role.GUIDE, Role.ADMIN})
public class GenericSheetGuideView extends JPanel
        implements SheetView, ResultListener, DockableSheetView {

    private static final Logger LOGGER =
            Logger.getLogger(GenericSheetGuideView.class.getName());
    private final DefaultTableModel resultsModel, studentModel;
    private ISocialManager manager;
    private Sheet sheet;
    private Role role;
    private HashMap<String, Boolean> resutlsReceived;

    /** Creates new form GenericSheetGuideView1 */
    public GenericSheetGuideView() {
        initComponents();
        resultsModel = (DefaultTableModel) resultTable.getModel();
        studentModel = (DefaultTableModel) studentTable.getModel();
    }

    public void initialize(ISocialManager manager, Sheet sheet, Role role) {
        this.manager = manager;
        this.sheet = sheet;
        this.role = role;
        resutlsReceived = new HashMap<String, Boolean>();
        manager.addResultListener(this.sheet.getId(), this);

        Sheet prevSheet = null;
        Collection<Result> prevResults = null;

        /**
         * Check if the sheet is singleton (Unit goal sheet). 
         * If yes, find the previously submitted goal results and display them.
         */
        boolean singleton = ((GenericSheet) sheet.getDetails()).getSingleton();
        if (singleton) {
            try {
                prevResults = findPreviousValue(((GenericSheet) sheet.getDetails()).getName());
                prevSheet = findPrevSheet(((GenericSheet) sheet.getDetails()).getName());
            } catch (IOException ex) {
                Logger.getLogger(GenericSheetGuideView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (prevSheet != null) {
            this.sheet = prevSheet;
        }
        addColumnContents();
        resultTable.setCellSelectionEnabled(false);
        resultTable.setDefaultRenderer(Object.class, new CustomTableRenderer());
        studentTable.setDefaultRenderer(Object.class, new CustomTableRenderer());
        setTableRenderers();

        try {
            if (prevResults != null) {
                for (Result result : prevResults) {
                    Vector vector = new Vector();
                    vector.add(result.getCreator());
                    studentModel.addRow(vector);
                    vector = new Vector();
                    vector.addAll(result.getDetails().getResultValues(null, null));
                    resultsModel.addRow(vector);
                    resutlsReceived.put(result.getCreator(), true);
                }
            } else {
                for (Result r : manager.getResults(this.sheet.getId())) {
                    GenericResult details = (GenericResult) r.getDetails();
                    List<String> resultValues = details.getResultValues(null, null);
                    Vector vector = new Vector();
                    vector.add(r.getCreator());
                    studentModel.addRow(vector);
                    vector = new Vector();
                    vector.addAll(resultValues);
                    resultsModel.addRow(vector);
                    resutlsReceived.put(r.getCreator(), true);
                }
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Errror getting results", ioe);
        }
    }

    public String getMenuName() {
        return ((GenericSheet) sheet.getDetails()).getName();
    }

    public boolean isAutoOpen() {
        return ((GenericSheet) sheet.getDetails()).isAutoOpen();
    }

    public HUDComponent open(HUD hud) {
        return hud.createComponent(this);
    }

    public void close() {
        manager.removeResultListener(sheet.getId(), this);
    }

    public void resultAdded(final Result result) {
        GenericResult details = (GenericResult) result.getDetails();
        List<String> resultValues = details.getResultValues(null, null);
        Vector vector = new Vector();
        resutlsReceived.put(result.getCreator(), true);
        vector.add(result.getCreator());
        studentModel.addRow(vector);
        vector = new Vector();
        vector.addAll(resultValues);
        resultsModel.addRow(vector);
        resultTable.repaint();
        doLayout();
        validate();
        repaint();
    }

    public void resultUpdated(final Result result) {
        String creator = result.getCreator();
        GenericResult details = (GenericResult) result.getDetails();
        List<GenericAnswer> answers = details.getAnswers();

        for (Iterator<GenericAnswer> it = answers.iterator(); it.hasNext();) {
            GenericAnswer genericAnswer = it.next();
            String columnName = genericAnswer.getQuestionTitle();
            String studentAnswer = genericAnswer.getValue();
            int column = findColumnNumber(columnName);
            int row = findRowNumber(creator);
            if (column != 0 && row != 1000) {
                resultsModel.setValueAt(studentAnswer, row, column);
            }
        }
        resultTable.repaint();
        doLayout();
        validate();
        repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        studentTable = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultTable = new javax.swing.JTable();
        refreshButton = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(400, 610));

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        studentTable.setFont(new java.awt.Font("Lucida Grande", 0, 13)); // NOI18N
        studentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        studentTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        studentTable.setAutoscrolls(false);
        studentTable.setEnabled(false);
        studentTable.setRowHeight(80);
        studentTable.setShowGrid(true);
        jScrollPane2.setViewportView(studentTable);

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(300, 400));

        resultTable.setFont(new java.awt.Font("Lucida Grande", 0, 13)); // NOI18N
        resultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        resultTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        resultTable.setAutoscrolls(false);
        resultTable.setEnabled(false);
        resultTable.setRowHeight(80);
        resultTable.setShowGrid(true);
        resultTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(resultTable);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/isocial/generic/client/resources/refresh_button.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/isocial/generic/client/Bundle"); // NOI18N
        refreshButton.setText(bundle.getString("GenericSheetGuideView.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(163, 163, 163)
                .addComponent(refreshButton)
                .addContainerGap(164, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 560, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(refreshButton))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        try {
            Collection<Result> results = manager.getResults(sheet.getId());
            Iterator<Result> iterator = results.iterator();
            while (iterator.hasNext()) {
                Result result = iterator.next();
                refreshResult(result);
            }
        } catch (IOException ex) {
            Logger.getLogger(GenericSheetGuideView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_refreshButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton refreshButton;
    private javax.swing.JTable resultTable;
    private javax.swing.JTable studentTable;
    // End of variables declaration//GEN-END:variables

    /**
     * This method add the initial column headings. The headings includes the question
     * titles. The answer rows for each student are added later on.
     */
    private void addColumnContents() {
        studentModel.addColumn("Student");
        studentTable.getColumn("Student").setHeaderRenderer(new CustomTableRenderer());
        GenericSheet details = (GenericSheet) sheet.getDetails();
        List<String> resultHeadings = details.getResultHeadings();
        Vector v = new Vector();
        v.addAll(resultHeadings);
        resultsModel.setColumnIdentifiers(v);
        Enumeration<TableColumn> columns = resultTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = columns.nextElement();
            int stringWidth = getFontMetrics(resultTable.getFont()).stringWidth((String) column.getIdentifier());
            resultTable.setSize(getPreferredSize().width + stringWidth, getPreferredSize().height);
            resultTable.repaint();
            doLayout();
            validate();
            repaint();
        }
    }

    /**
     * The table renderer of JTextArea is added to table cells to display the cell
     * text wrapped.
     */
    private void setTableRenderers() {
        Enumeration<TableColumn> columns = resultTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = columns.nextElement();
            String header = (String) column.getIdentifier();
            CustomTableRenderer renderer = new CustomTableRenderer();
            int headerWidth = 0;
            if (renderer instanceof JTextArea) {
                JTextArea area = renderer;
                headerWidth = area.getFontMetrics(area.getFont()).stringWidth(header);
                if (headerWidth > 100) {
                    headerWidth = headerWidth / 2;
                }
                area.setPreferredSize(new Dimension(headerWidth, 80));
            }
            column.setHeaderRenderer(renderer);
            column.setPreferredWidth(headerWidth);
        }
    }

    /**
     * This method finds the column number from the given column name.
     * @param columnName
     * @return
     */
    private int findColumnNumber(String columnName) {
        int count = resultsModel.getColumnCount();
        for (int i = 0; i < count; i++) {
            String name = resultsModel.getColumnName(i);
            if (name.equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return 0;
    }

    private int findRowNumber(String creator) {
        int count = resultsModel.getRowCount();
        for (int i = 0; i < count; i++) {
            String name = (String) resultsModel.getValueAt(i, 0);
            if (name.equalsIgnoreCase(creator)) {
                return i;
            }
        }
        return 1000;
    }

    /**
     * This method returns the dockable attribute of the sheet.
     * @return
     */
    public boolean isDockable() {
        return ((GenericSheet) sheet.getDetails()).isDockable();
    }

    /**
     * Refresh the OG view on refresh button.
     * @param result
     */
    private void refreshResult(Result result) {
        //verify if the student result is already added to OG view.
        if (resutlsReceived.containsKey(result.getCreator())) {
            if (resutlsReceived.get(result.getCreator()) == true) {
                return;
            }
        }
        //if not added, add new result to the table.
        resultAdded(result);
    }

    class NamedResult {

        private final Result result;

        public NamedResult(Result result) {
            this.result = result;
        }

        public Result getResult() {
            return result;
        }

        @Override
        public String toString() {
            return result.getCreator();
        }
    }

    private Collection<Result> findPreviousValue(String name) throws IOException {
        Collection<Result> results = null;
        List instances = new ArrayList(manager.getInstances());
        Collections.reverse(instances);
        for (Object object : instances) {
            Instance instance = (Instance) object;
            //if (!(instance.getId().equals(manager.getCurrentInstance().getId()))) {
                if (instance.getUnit().getId().equals(manager.getCurrentInstance().getUnit().getId())) {
                for (Sheet s : instance.getSheets()) {
                    // check if this sheet is a setter matching this getter
                    if (s.getName().equalsIgnoreCase(name)) {
                        if (results == null) {
                            results = new ArrayList<Result>();
                        }
                        results.addAll(getSheetValue(instance, s));
                    }
                }
            }
            //}
        }
        // not found
        return results;
    }

    /**
     * Find a result for this user and extract its value
     */
    private Collection<Result> getSheetValue(Instance i, Sheet s) throws IOException {
        // get all results
        Collection<Result> results = manager.getResultsForInstance(i.getId(), s.getId());

        // find result for all users
        return results;
    }

    /**
     * Find the matching sheet from previous instances.
     */
    private Sheet findPrevSheet(String name) throws IOException {
        List instances = new ArrayList(manager.getInstances());
        Collections.reverse(instances);
        for (Object object : instances) {
            Instance instance = (Instance) object;
            if (!(instance.getId().equals(manager.getCurrentInstance().getId()))) {
                if (instance.getUnit().getId().equals(manager.getCurrentInstance().getUnit().getId())) {
                    for (Sheet s : instance.getSheets()) {
                        // check if this sheet is a setter matching this getter
                        if (s.getName().equalsIgnoreCase(name)) {
                            return s;
                        }
                    }
                }
            }
        }
        // not found
        return null;
    }
}
