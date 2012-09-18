/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

/*
 * TestResultView.java
 *
 * Created on Dec 1, 2011, 11:48:46 AM
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui;

import org.jdesktop.wonderland.modules.clienttest.test.ui.tests.FakeTest;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import org.json.simple.JSONObject;

/**
 *
 * @author jkaplan
 */
public class TestResultView extends javax.swing.JFrame {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/clienttest/test/ui/resources/Bundle");
    
    private final Test test;
    
    private boolean messagesHidden = false;
    
    /** Creates new form TestResultView */
    public TestResultView(Test test) {
        this.test = test;
        
        initComponents();
        
        TestResult res = test.getResult();
        
        nameLabel.setText(MessageFormat.format(BUNDLE.getString("Result_Name"),
                          test.getName()));
        
        headlineLabel.setIcon(new ImageIcon(
                getClass().getResource("resources/" + res.getIcon())));
        String headline = test.getHeadline(res);
        if (headline != null) {
            headlineLabel.setText(headline);
        }
        
        String fix = test.getFixes(res);
        if (fix != null) {
            fixesTF.setText(fix);
        } else {
            causesLabel.setVisible(false);
            fixesSP.setVisible(false);
        }
        
        String messages = test.getMessages();
        if (messages != null) {
            messagesTF.setText(messages);
        } else {
            messagesLabel.setVisible(false);
            messagesSP.setVisible(false);
            showButton.setVisible(false);
        }
        
        // TODO: make the UI work
        showButton.setVisible(false);
        setMessagesHidden(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        showButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        headlineLabel = new javax.swing.JLabel();
        fixesSP = new javax.swing.JScrollPane();
        fixesTF = new javax.swing.JEditorPane();
        messagesLabel = new javax.swing.JLabel();
        messagesSP = new javax.swing.JScrollPane();
        messagesTF = new javax.swing.JTextArea();
        causesLabel = new javax.swing.JLabel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/clienttest/test/ui/resources/Bundle"); // NOI18N
        setTitle(bundle.getString("TestResultView.title")); // NOI18N
        setBackground(new java.awt.Color(255, 255, 255));

        bgPanel.setBackground(new java.awt.Color(255, 255, 255));

        nameLabel.setFont(new java.awt.Font("Lucida Grande", 1, 18));
        nameLabel.setText(bundle.getString("TestResultView.nameLabel.text")); // NOI18N

        showButton.setText(bundle.getString("TestResultView.showButton.text")); // NOI18N
        showButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showButtonActionPerformed(evt);
            }
        });

        okButton.setText(bundle.getString("TestResultView.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        headlineLabel.setFont(new java.awt.Font("Lucida Grande", 0, 24));
        headlineLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/clienttest/test/ui/resources/bullet_red.png"))); // NOI18N
        headlineLabel.setText(bundle.getString("TestResultView.headlineLabel.text")); // NOI18N

        fixesTF.setContentType(bundle.getString("TestResultView.fixesTF.contentType")); // NOI18N
        fixesTF.setEditable(false);
        fixesTF.setText("");
        fixesSP.setViewportView(fixesTF);

        messagesLabel.setFont(new java.awt.Font("Lucida Grande", 1, 18));
        messagesLabel.setText(bundle.getString("TestResultView.messagesLabel.text")); // NOI18N

        messagesTF.setColumns(20);
        messagesTF.setEditable(false);
        messagesTF.setRows(5);
        messagesSP.setViewportView(messagesTF);

        causesLabel.setFont(new java.awt.Font("Lucida Grande", 1, 18));
        causesLabel.setText(bundle.getString("TestResultView.causesLabel.text")); // NOI18N

        javax.swing.GroupLayout bgPanelLayout = new javax.swing.GroupLayout(bgPanel);
        bgPanel.setLayout(bgPanelLayout);
        bgPanelLayout.setHorizontalGroup(
            bgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bgPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fixesSP, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
                    .addComponent(nameLabel)
                    .addComponent(headlineLabel)
                    .addComponent(causesLabel)
                    .addGroup(bgPanelLayout.createSequentialGroup()
                        .addComponent(messagesLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 361, Short.MAX_VALUE)
                        .addComponent(showButton))
                    .addComponent(messagesSP, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
                    .addComponent(okButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        bgPanelLayout.setVerticalGroup(
            bgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bgPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(headlineLabel)
                .addGap(18, 18, 18)
                .addComponent(causesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fixesSP, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(bgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messagesLabel)
                    .addComponent(showButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messagesSP, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(okButton))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bgPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void showButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showButtonActionPerformed
        setMessagesHidden(!isMessagesHidden());
    }//GEN-LAST:event_showButtonActionPerformed

    private void setMessagesHidden(boolean messagesHidden) {
        this.messagesHidden = messagesHidden;
        
        if (messagesTF.getText().isEmpty()) {
            // don't show an empty dialog
            return;
        }
        
        if (messagesHidden) {
            showButton.setText(BUNDLE.getString("Show_Button"));
            messagesSP.setVisible(false);
        } else {
            showButton.setText(BUNDLE.getString("Hide_Button"));
            messagesSP.setVisible(true);
        }
        
        if (isVisible()) {
            setVisible(false);
            pack();
            setVisible(true);
        }
    }
    
    private boolean isMessagesHidden() {
        return messagesHidden;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TestResultView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TestResultView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TestResultView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TestResultView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                Test fake = new FakeTest();
                JSONObject config = new JSONObject();
                config.put("name", "Fake test");
                fake.initialize(config);
                
                new TestResultView(fake).setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bgPanel;
    private javax.swing.JLabel causesLabel;
    private javax.swing.JScrollPane fixesSP;
    private javax.swing.JEditorPane fixesTF;
    private javax.swing.JLabel headlineLabel;
    private javax.swing.JLabel messagesLabel;
    private javax.swing.JScrollPane messagesSP;
    private javax.swing.JTextArea messagesTF;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton showButton;
    // End of variables declaration//GEN-END:variables
}
