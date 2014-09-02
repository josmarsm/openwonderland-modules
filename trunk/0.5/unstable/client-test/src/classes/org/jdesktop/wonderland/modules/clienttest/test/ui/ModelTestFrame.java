/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

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
 * ModelTestFrame.java
 *
 * Created on Oct 25, 2011, 1:16:40 PM
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JDialog;

/**
 *
 * @author jkaplan
 */
public class ModelTestFrame extends javax.swing.JFrame {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/clienttest/test/ui/resources/Bundle");
    
    private boolean answered = false;
    private TestResult answer;
    private boolean loading = true;
    private JDialog testDialog = null;
    
    /** Creates new form ModelTestFrame */
    public ModelTestFrame() {
        initComponents();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setAnswer(TestResult.NOT_RUN);
            }
        });
        setResizable(false);
    }
    
    public void setReferenceImage(ImageIcon image) {
        referenceLabel.setIcon(image);
        repaint();
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        testDialog.setVisible(visible);
    }
    
    public void setTestPane(Component component) {
        //testPanel.add(component, BorderLayout.CENTER);
        
        /*
        * open dialog for rendering test. Also add component listener.
        */
        testDialog = new JDialog(this, "Child", false);
        testDialog.setUndecorated(true);
        testDialog.add(component);
        testDialog.pack();
        
        addComponentListener(new ComponentAdapter() {

            public void componentMoved(ComponentEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        if(testDialog.isVisible()) {
                        testDialog.setLocation((int)testPanel.getLocationOnScreen().getX()+13
                        ,(int)testPanel.getLocationOnScreen().getY()+13);
                        }
                    }
                });
            }

            public void componentShown(ComponentEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        if(testDialog.isVisible()) {
                        testDialog.setLocation((int)testPanel.getLocationOnScreen().getX()+13
                        ,(int)testPanel.getLocationOnScreen().getY()+13);
                        }
                    }
                });
            }

        });
    }
    
    public TestResult waitForAnswer() throws InterruptedException {
        synchronized (this) {
            while (!isAnswered()) {
                wait();
            }
            
            return getAnswer();
        }
    }
    
    public synchronized void reset() {
        answered = false;
        answer = TestResult.NOT_RUN;
        
        loading = true;
        yesButton.setEnabled(false);
        noButton.setEnabled(false);
        setTestLabelText(BUNDLE.getString("Loading"));
    }

    public synchronized void setLoaded() {
        loading = false;
        yesButton.setEnabled(true);
        noButton.setEnabled(true);
    }
    
    public synchronized void setFPS(float fps) {
        if (!isLoading()) {
            String fpsStr = String.valueOf(Math.round(fps));
            String text = MessageFormat.format(
                    BUNDLE.getString("FPS"), fpsStr);
            setTestLabelText(text);
        }
    }
    
    protected synchronized boolean isLoading() {
        return loading;
    }
    
    protected void setTestLabelText(String text) {
        StringBuilder set = new StringBuilder(
                BUNDLE.getString("ModelTestFrame.testLabel.text"));
        if (text != null && text.length() > 0) {
            set.append(" ").append(text);
        }
        
        testLabel.setText(set.toString());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        questionLabel = new javax.swing.JLabel();
        noButton = new javax.swing.JButton();
        yesButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        referenceHolder = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        referenceLabel = new javax.swing.JLabel();
        testHolder = new javax.swing.JPanel();
        testLabel = new javax.swing.JLabel();
        testPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        questionLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/clienttest/test/ui/resources/Bundle"); // NOI18N
        questionLabel.setText(bundle.getString("ModelTestFrame.questionLabel.text")); // NOI18N

        noButton.setText(bundle.getString("ModelTestFrame.noButton.text")); // NOI18N
        noButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noButtonActionPerformed(evt);
            }
        });

        yesButton.setText(bundle.getString("ModelTestFrame.yesButton.text")); // NOI18N
        yesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yesButtonActionPerformed(evt);
            }
        });

        mainPanel.setLayout(new java.awt.GridLayout(1, 0));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(bundle.getString("ModelTestFrame.jLabel1.text")); // NOI18N

        referenceLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 0, 10), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0))));

        javax.swing.GroupLayout referenceHolderLayout = new javax.swing.GroupLayout(referenceHolder);
        referenceHolder.setLayout(referenceHolderLayout);
        referenceHolderLayout.setHorizontalGroup(
            referenceHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
            .addComponent(referenceLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
        );
        referenceHolderLayout.setVerticalGroup(
            referenceHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, referenceHolderLayout.createSequentialGroup()
                .addComponent(referenceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1))
        );

        mainPanel.add(referenceHolder);

        testLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        testLabel.setText(bundle.getString("ModelTestFrame.testLabel.text")); // NOI18N

        testPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 0, 10), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0))));
        testPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout testHolderLayout = new javax.swing.GroupLayout(testHolder);
        testHolder.setLayout(testHolderLayout);
        testHolderLayout.setHorizontalGroup(
            testHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(testLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
            .addComponent(testPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
        );
        testHolderLayout.setVerticalGroup(
            testHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, testHolderLayout.createSequentialGroup()
                .addComponent(testPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testLabel))
        );

        mainPanel.add(testHolder);

        cancelButton.setText(bundle.getString("ModelTestFrame.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(questionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(yesButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cancelButton)
                .addContainerGap())
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 693, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(questionLabel)
                    .addComponent(noButton)
                    .addComponent(yesButton)
                    .addComponent(cancelButton))
                .addGap(26, 26, 26))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void noButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noButtonActionPerformed
        setAnswer(TestResult.FAIL);
    }//GEN-LAST:event_noButtonActionPerformed

    private void yesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yesButtonActionPerformed
        setAnswer(TestResult.PASS);
    }//GEN-LAST:event_yesButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setAnswer(TestResult.NOT_RUN);
    }//GEN-LAST:event_cancelButtonActionPerformed

    protected synchronized void setAnswer(TestResult answer) {
        if (this.answered) {
            return;
        }
        
        this.answer = answer;
        this.answered = true;
        notifyAll();
    }
    
    protected synchronized boolean isAnswered() {
        return answered;
    }
    
    protected synchronized TestResult getAnswer() {
        return answer;
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
            java.util.logging.Logger.getLogger(ModelTestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ModelTestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ModelTestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ModelTestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new ModelTestFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton noButton;
    private javax.swing.JLabel questionLabel;
    private javax.swing.JPanel referenceHolder;
    private javax.swing.JLabel referenceLabel;
    private javax.swing.JPanel testHolder;
    private javax.swing.JLabel testLabel;
    private javax.swing.JPanel testPanel;
    private javax.swing.JButton yesButton;
    // End of variables declaration//GEN-END:variables
}
