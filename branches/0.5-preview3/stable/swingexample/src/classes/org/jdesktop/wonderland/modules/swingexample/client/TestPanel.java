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
package org.jdesktop.wonderland.modules.swingexample.client;

import javax.swing.JWindow;
import javax.swing.JFrame;

/**
 * The JPanel for the Swing example
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
public class TestPanel extends javax.swing.JPanel {

    private JFrame frame;

    /** Creates new form TestPanel */
    public TestPanel() {
        initComponents();
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jButton2 = new javax.swing.JButton();

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/swingexample/client/Bundle"); // NOI18N
        jButton2.setText(bundle.getString("TestPanel.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        add(jButton2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        // TODO add your handling code here:
        //System.out.println("Panel focus Gained");
    }//GEN-LAST:event_formFocusGained

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    System.err.println("Button pressed");
}//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton2;
    // End of variables declaration//GEN-END:variables

    public static void main(String args[]) {
        JWindow j = new JWindow();
        //JFrame j = new JFrame();
//        j.addMouseMotionListener(new MouseMotionAdapter() {
//           public void mouseMoved(java.awt.event.MouseEvent evt) {
//                System.out.println(evt);
//            }
//        });
        System.out.println(j.getLayout());
        j.add(new TestPanel());
        j.pack();
        j.setVisible(true);
    }
}