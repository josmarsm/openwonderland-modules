/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */

/**
 * WonderBuilders, Inc.
 *
 * Copyright (c) 2012, WonderBuilders, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * WonderBuilders, Inc. designates this particular file as subject to the
 * "Classpath" exception as provided WonderBuilders, Inc. in the License file
 * that accompanied this code.
 */
package org.jdesktop.wonderland.modules.standardsheet.client;

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardAnswer;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardQuestion;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardResult;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardSheet;

/**
 * Main class for displaying a standard sheet. Options are given to
 * collect and display results.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class StandardSheetPanel extends javax.swing.JPanel {
    private final List<StandardQuestionPanel> panels =
            new ArrayList<StandardQuestionPanel>();
    
    private StandardSheet sheet;
    private boolean editable = true;
    private TextQuestionPanel tp;
    private ISocialManager manager;
    private Sheet sh;
    private String userRole;
    
    /**
     * Creates new form StandardSheetPanel
     */
    public StandardSheetPanel() {
        initComponents();
        
    }

    public void renderSheet(StandardSheet sheet) {
        this.sheet = sheet;
        
        this.removeAll();
        panels.clear();
        int i=0;
        
        for (StandardQuestion question : sheet.getQuestions()) {
            StandardQuestionPanel panel = null;
            if(question.getType()!=null)
                panel = createPanelFor(question);
            if (panel != null) {
                panels.add(panel);
                panel.setEditable(isEditable());
                
                for (JComponent component : panel.getJComponents(getWidth())) {
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.gridx = 0;
                    gbc.gridy = GridBagConstraints.RELATIVE;
                    gbc.anchor = GridBagConstraints.PAGE_START;
                    gbc.fill = GridBagConstraints.BOTH;
                
                    this.add(component, gbc);
                }
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = GridBagConstraints.RELATIVE;
                gbc.ipady = 3;
                this.add(new JPanel(), gbc);
            }
        }
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.weighty = 1.0;
        this.add(new JPanel(), gbc);
        
        this.repaint();
    }
    
    protected StandardResult getResults() {
        List<StandardAnswer> out = new ArrayList<StandardAnswer>();
        
        for (StandardQuestionPanel panel : getPanels()) {
            StandardAnswer answer = panel.getAnswer();
            if (answer != null) {
                out.add(answer);
            }
        }
        
        StandardResult result = new StandardResult();
        result.setAnswers(out);
        return result;
    }
    
    protected void renderResults(StandardResult results) {
        clearResults();
        
        for (StandardAnswer answer : results.getAnswers()) {
            StandardQuestionPanel panel = findPanel(answer.getId());
            if (panel != null) {
                panel.renderAnswer(answer);
            }
        }
    }
    
    protected void setEditable(boolean editable) {
        this.editable = editable;
        
        for (StandardQuestionPanel panel : getPanels()) {
            panel.setEditable(editable);
        }
    }
    
    protected boolean isEditable() {
        return editable;
    }
    
    private StandardQuestionPanel findPanel(int id) {
        for (StandardQuestionPanel panel : getPanels()) {
            if (panel.getId() == id) {
                return panel;
            }
        }
        
        return null;
    }
    
    protected void clearResults() {
        for (StandardQuestionPanel panel : getPanels()) {
            panel.clearAnswer();
        }
    }
    
    public void setManager(ISocialManager manager) {
        this.manager = manager;
    }
    public ISocialManager getManager() {
        return manager;
    }
    
    protected StandardQuestionPanel createPanelFor(StandardQuestion question) {
        if (question.getType().equals("text")) {
            return new TextQuestionPanel(question.getText());
        } else if (question.getType().equals("field")) {
            return new FieldQuestionPanel(question);
        } else if (question.getType().equals("multiple")) {
            return new MultipleQuestionPanel(question);
        } else if (question.getType().equals("audio")) {
            return new AudioQuestionPanel(question);
        } else if (question.getType().equals("recording")) {
            RecordingQuestionPanel rqp = new RecordingQuestionPanel(question);
            rqp.setSheet(sh);
            rqp.setManager(manager);
            rqp.setUsername(manager.getUsername());
            rqp.setUserRole(userRole);
            return rqp;
        } else {
            return null;
        }
    }
    public void setSh(Sheet sheet) {
        this.sh = sheet;
    }
    public Sheet getSh() {
        return sh;
    }
    protected List<StandardQuestionPanel> getPanels() {
        return panels;
    }
    
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 5, 2, 5));
        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
