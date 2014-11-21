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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
import org.jdesktop.wonderland.modules.isocial.common.model.annotation.ISocialModel;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardQuestion;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardResult;
import org.jdesktop.wonderland.modules.standardsheet.common.StandardSheet;

/**
 * Test class that renders a set of questions and answers.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class SheetRenderFrame extends javax.swing.JFrame {
    private static final Logger LOGGER =
            Logger.getLogger(SheetRenderFrame.class.getName());
    
    private StandardSheetPanel panel;
    private JAXBContext context;
    
    /**
     * Creates new form SheetRenderFrame
     */
    public SheetRenderFrame() {
        initComponents();
        
        panel = new StandardSheetPanel();
        scrollPane.setViewportView(panel);
        
        StandardSheet sheet = new StandardSheet();
        List<StandardQuestion> questions = new ArrayList<StandardQuestion>();
        
        StandardQuestion q = new StandardQuestion();
        q.setId(1);
        q.setType("text");
        q.setText("Some text in a standard question");
        questions.add(q);
        
        q = new StandardQuestion();
        q.setId(2);
        q.setType("text");
        q.setText("A very long text string that should hopefully wrap onto a"
                + " few lines. If not here is some extra to help it.");
        questions.add(q);
        
        q = new StandardQuestion();
        q.setId(3);
        q.setType("text");
        q.setText("And another");
        questions.add(q);
        
        q = new StandardQuestion();
        q.setId(4);
        q.setType("field");
        q.setText("Some long instructions for a simple text field with 4 lines");
        q.getProperties().put("lines", "4");
        q.getProperties().put("instructions", "Fill in the text field");
        questions.add(q);
        
        q = new StandardQuestion();
        q.setId(5);
        q.setType("text");
        q.setText("After the field");
        questions.add(q);
        
        q = new StandardQuestion();
        q.setId(6);
        q.setType("field");
        q.setText("A one line question");
        q.getProperties().put("lines", "1");
        q.getProperties().put("instructions", "Fill me in");
        questions.add(q);
        
        q = new StandardQuestion();
        q.setId(7);
        q.setType("multiple");
        q.setText("Mutiple choice question");
        q.getProperties().put("choices", "A is for apple\nB if for bear\nC is for cookie");
        q.getProperties().put("other", "false");
        q.getProperties().put("multiple", "true");
        questions.add(q);
        
        q = new StandardQuestion();
        q.setId(8);
        q.setType("multiple");
        q.setText("And some checkboxes!");
        q.getProperties().put("choices", "Nick!\nJared!\nPeter!\nThere's a fire in the barn!");
        q.getProperties().put("other", "true");
        q.getProperties().put("otherText", "None of the above");
        q.getProperties().put("multiple", "false");
        questions.add(q);
        
        sheet.setQuestions(questions);
        
        panel.setSize(getWidth() - 30, 1);
        panel.renderSheet(sheet);    
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        answerFrame = new javax.swing.JFrame();
        jScrollPane1 = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        renderButton = new javax.swing.JButton();
        getButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();

        jScrollPane1.setViewportView(editorPane);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout answerFrameLayout = new javax.swing.GroupLayout(answerFrame.getContentPane());
        answerFrame.getContentPane().setLayout(answerFrameLayout);
        answerFrameLayout.setHorizontalGroup(
            answerFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, answerFrameLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(cancelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(okButton))
            .addComponent(jScrollPane1)
        );
        answerFrameLayout.setVerticalGroup(
            answerFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(answerFrameLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(answerFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        renderButton.setText("Render Answers");
        renderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renderButtonActionPerformed(evt);
            }
        });

        getButton.setText("Get Answers");
        getButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getButtonActionPerformed(evt);
            }
        });

        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPane)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 105, Short.MAX_VALUE)
                .addComponent(clearButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(getButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(renderButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(renderButton)
                    .addComponent(getButton)
                    .addComponent(clearButton)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        panel.clearResults();
        panel.setEditable(true);
    }//GEN-LAST:event_clearButtonActionPerformed

    private void getButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getButtonActionPerformed
        StandardResult result = panel.getResults();
        String serialized = serialize(result);
        
        editorPane.setText(serialized);
        editorPane.setEditable(false);
        cancelButton.setVisible(false);
        
        answerFrame.pack();
        answerFrame.setVisible(true);
        answerFrame.toFront();
    }//GEN-LAST:event_getButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (cancelButton.isVisible()) {
            String result = editorPane.getText();
            StandardResult deserialized = deserialize(result);
            panel.renderResults(deserialized);
            panel.setEditable(false);
        }
        
        answerFrame.setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        answerFrame.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void renderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renderButtonActionPerformed
        editorPane.setText("");
        editorPane.setEditable(true);
        cancelButton.setVisible(true);
        
        answerFrame.pack();
        answerFrame.setVisible(true);
        answerFrame.toFront();
    }//GEN-LAST:event_renderButtonActionPerformed

    private String serialize(StandardResult result) {
        StringWriter sw = new StringWriter();
         
        try {
            Marshaller m = getJAXBContext().createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(result, sw);
            return sw.toString();
        } catch (JAXBException je) {
            LOGGER.log(Level.WARNING, "Error serializing " + result, je);
            return "";
        }
    }
    
    private StandardResult deserialize(String result) {
        StringReader sr = new StringReader(result);
        
        try {
            Unmarshaller u = getJAXBContext().createUnmarshaller();
            return (StandardResult) u.unmarshal(sr);
        } catch (JAXBException je) {
            LOGGER.log(Level.WARNING, "Error serializing " + result, je);
            return new StandardResult();
        }
    }
    
    private JAXBContext getJAXBContext() throws JAXBException {
        if (context == null) {
            ScannedClassLoader scl = ScannedClassLoader.getSystemScannedClassLoader();
            Set<String> classNames = scl.getClasses(ISocialModel.class);
            
            List<Class> classes = new ArrayList<Class>();
            for (String className : classNames) {
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException cnfe) {
                    LOGGER.log(Level.WARNING, "Class not found: " + className, cnfe);
                }
            }
            
            context = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));
        }
        
        return context;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SheetRenderFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SheetRenderFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SheetRenderFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SheetRenderFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new SheetRenderFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFrame answerFrame;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JButton getButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okButton;
    private javax.swing.JButton renderButton;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
}
