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
 * ClientTestUI.java
 *
 * Created on Oct 17, 2011, 5:57:18 PM
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui;

import java.awt.CardLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.jme.Webstart.JnlpSecurityManager;
import org.jdesktop.wonderland.client.jme.WonderlandURLStreamHandlerFactory;
import org.jdesktop.wonderland.modules.clienttest.test.ui.TestManager.TestListener;

    
/**
 *
 * @author jkaplan
 */
public class ClientTestUI extends javax.swing.JFrame
    implements TestListener
{
    private static final Logger LOGGER =
            Logger.getLogger(ClientTestUI.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/clienttest/test/ui/resources/Bundle");

    private final List<SectionRadioButton> buttons =
            new ArrayList<SectionRadioButton>();
    
    private final ActionListener buttonActionListener;
    
    private Image background;
    
    /** Creates new form ClientTestUI */
    public ClientTestUI() {
        TestManager.INSTANCE.initialize();
      
        URL bgImage = ClientTestUI.class.getResource("resources/Watermark-clipped.png");
        background = Toolkit.getDefaultToolkit().createImage(bgImage);
        
        initComponents();
        
        buttonActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                SectionRadioButton src = (SectionRadioButton) ae.getSource();
                jumpTo(src.getSection());
            }
        };
        
        for (TestSection section : TestManager.INSTANCE.getSections()) {            
            SectionRadioButton toAdd = createButton(section);
            sectionsPanel.add(toAdd);
            contentPane.add(section.getPanel(), section.getName(), -1);
            buttons.add(toAdd);
        }
        
        TestManager.INSTANCE.addTestListener(this);
        
        // update UI
        sectionChanged(TestManager.INSTANCE.getCurrentSection(), true);
    }
    
    protected SectionRadioButton getButtonFor(TestSection section) {
        for (SectionRadioButton button : buttons) {
            if (button.getSection().equals(section)) {
                return button;
            }
        }
        
        return null;
    }
    
    protected SectionRadioButton createButton(TestSection section) {
        SectionRadioButton button = new SectionRadioButton(section);
        
        Font f = button.getFont();
        f = f.deriveFont(Font.BOLD, 18);
        button.setFont(f);
        
        button.addActionListener(buttonActionListener);
        
        sectionsBG.add(button);
        return button;
    }

    protected void jumpTo(TestSection section) {
        TestManager.INSTANCE.setCurrentSection(section);
    }

    public void sectionChanged(TestSection current, boolean autoStart) {
        SectionRadioButton button = getButtonFor(current);
        button.setSelected(true);
        
        current.sectionVisible();
        ((CardLayout) contentPane.getLayout()).show(contentPane, current.getName());
        
        boolean runnable = (current instanceof RunnableTestSection);
        
        backButton.setEnabled(TestManager.INSTANCE.hasPreviousSection());
        skipButton.setEnabled(runnable && TestManager.INSTANCE.hasNextSection());
        
        if (runnable) {
            if (autoStart) {
                nextButton.setText(BUNDLE.getString("Next"));
                nextButton.setEnabled(false);
                ((RunnableTestSection) current).sectionStarted();
            } else {
                nextButton.setText(BUNDLE.getString("Start"));
                nextButton.setEnabled(true);
            }
        } else {
            nextButton.setText(BUNDLE.getString("Next"));
            nextButton.setEnabled(TestManager.INSTANCE.hasNextSection());
        }
    }

    public void sectionComplete(TestSection current) {
        nextButton.setText(BUNDLE.getString("Next"));
        nextButton.setEnabled(true);
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sectionsBG = new javax.swing.ButtonGroup();
        bgPanel = new BackgroundPanel(background);
        owlLogo = new javax.swing.JLabel();
        topPane = new javax.swing.JPanel();
        contentPane = new javax.swing.JPanel();
        sectionsPanel = new javax.swing.JPanel();
        buttonPane = new javax.swing.JPanel();
        backButton = new javax.swing.JButton();
        skipButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/clienttest/test/ui/resources/Bundle"); // NOI18N
        setTitle(bundle.getString("ClientTestUI.title")); // NOI18N

        bgPanel.setBackground(new java.awt.Color(255, 255, 255));
        bgPanel.setOpaque(false);

        javax.swing.GroupLayout bgPanelLayout = new javax.swing.GroupLayout(bgPanel);
        bgPanel.setLayout(bgPanelLayout);
        bgPanelLayout.setHorizontalGroup(
            bgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 749, Short.MAX_VALUE)
        );
        bgPanelLayout.setVerticalGroup(
            bgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 574, Short.MAX_VALUE)
        );

        owlLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/clienttest/test/ui/resources/OpenWonderlandLogo.png"))); // NOI18N

        topPane.setBackground(new java.awt.Color(255, 255, 51));
        topPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 10, 0, 10));
        topPane.setOpaque(false);

        contentPane.setBackground(new java.awt.Color(0, 204, 204));
        contentPane.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3)));
        contentPane.setOpaque(false);
        contentPane.setLayout(new java.awt.CardLayout());

        sectionsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(45, 0, 0, 0));
        sectionsPanel.setOpaque(false);
        sectionsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 10));

        javax.swing.GroupLayout topPaneLayout = new javax.swing.GroupLayout(topPane);
        topPane.setLayout(topPaneLayout);
        topPaneLayout.setHorizontalGroup(
            topPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPaneLayout.createSequentialGroup()
                .addComponent(sectionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentPane, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE))
        );
        topPaneLayout.setVerticalGroup(
            topPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentPane, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
            .addComponent(sectionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
        );

        buttonPane.setBackground(new java.awt.Color(255, 51, 51));
        buttonPane.setOpaque(false);

        backButton.setText(bundle.getString("ClientTestUI.backButton.text")); // NOI18N
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        skipButton.setText(bundle.getString("ClientTestUI.skipButton.text")); // NOI18N
        skipButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skipButtonActionPerformed(evt);
            }
        });

        nextButton.setText(bundle.getString("ClientTestUI.nextButton.text")); // NOI18N
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(bundle.getString("ClientTestUI.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPaneLayout = new javax.swing.GroupLayout(buttonPane);
        buttonPane.setLayout(buttonPaneLayout);
        buttonPaneLayout.setHorizontalGroup(
            buttonPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonPaneLayout.createSequentialGroup()
                .addContainerGap(420, Short.MAX_VALUE)
                .addComponent(cancelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(backButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(skipButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nextButton)
                .addContainerGap())
        );
        buttonPaneLayout.setVerticalGroup(
            buttonPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buttonPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(skipButton)
                    .addComponent(backButton)
                    .addComponent(cancelButton)
                    .addComponent(nextButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(buttonPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(topPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(owlLogo)
                    .addContainerGap(470, Short.MAX_VALUE)))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(bgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(topPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(486, Short.MAX_VALUE)
                    .addComponent(owlLogo)
                    .addContainerGap()))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(bgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        TestManager.INSTANCE.previousSection();
    }//GEN-LAST:event_backButtonActionPerformed

    private void skipButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skipButtonActionPerformed
        ((RunnableTestSection) TestManager.INSTANCE.getCurrentSection()).sectionSkipped();
        TestManager.INSTANCE.nextSection();
    }//GEN-LAST:event_skipButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        if (nextButton.getText().equals(BUNDLE.getString("Next"))) {
            TestManager.INSTANCE.nextSection();
        } else {
            nextButton.setText(BUNDLE.getString("Next"));
            nextButton.setEnabled(false);
            ((RunnableTestSection) TestManager.INSTANCE.getCurrentSection()).sectionStarted();
        }
    }//GEN-LAST:event_nextButtonActionPerformed

    protected static class SectionRadioButton extends JRadioButton {
        private final TestSection section;
        
        public SectionRadioButton(TestSection section) {
            super (section.getName());
            
            this.section = section;
        }
        
        public TestSection getSection() {
            return section;
        }
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
            java.util.logging.Logger.getLogger(ClientTestUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientTestUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientTestUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientTestUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        // set up resolving Wonderland URLs
        URL.setURLStreamHandlerFactory(new WonderlandURLStreamHandlerFactory());
        
        // set security manager
        System.setSecurityManager(new JnlpSecurityManager());
        
        // set up our custom log handler to pipe messages to the LogViewerFrame.
        // We need to do this to work around the fact that Web Start won't
        // load loggers not on the system classpath
        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(new LogHandler());

        final UncaughtExceptionHandler ueh = new UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.log(Level.WARNING, null, e);
            }
        };
        
        // make sure Swing exceptions are captured in the log
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Thread.currentThread().setUncaughtExceptionHandler(ueh);
            }
        });
        
        /* Create and display the form */
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ClientTestUI().setVisible(true);
            }
        });
        
        // try our best to log any uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler(ueh);
    }
    
    static class BackgroundPanel extends JPanel {
        private final Image image;
        
        public BackgroundPanel(Image image) {
            this.image = image;
        }

        @Override
        public void paintComponent(Graphics g) {
            int y = getHeight() - image.getHeight(null);            
            g.drawImage(image, 0, y, this);
        }
    }
    
    static class LogHandler extends ConsoleHandler {
        @Override
        public void publish(LogRecord record) {
            super.publish(record);
         
            TestManager.INSTANCE.appendToLog(getFormatter().format(record));
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JPanel bgPanel;
    private javax.swing.JPanel buttonPane;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel contentPane;
    private javax.swing.JButton nextButton;
    private javax.swing.JLabel owlLogo;
    private javax.swing.ButtonGroup sectionsBG;
    private javax.swing.JPanel sectionsPanel;
    private javax.swing.JButton skipButton;
    private javax.swing.JPanel topPane;
    // End of variables declaration//GEN-END:variables
}
